package jmri.jmrit.logixng.digital.actions;

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Locale;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.util.ThreadingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This action sets the state of a turnout.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class ActionTurnout extends AbstractDigitalAction implements VetoableChangeListener {

    private NamedBeanHandle<Turnout> _turnoutHandle;
    private TurnoutState _turnoutState = TurnoutState.THROWN;
    
    public ActionTurnout(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    public void setTurnout(String turnoutName) {
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).getTurnout(turnoutName);
        setTurnout(turnout);
        if (turnout == null) {
            log.error("turnout \"{}\" is not found", turnoutName);
        }
    }
    
    public void setTurnout(@Nonnull NamedBeanHandle<Turnout> handle) {
        _turnoutHandle = handle;
        InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
    }
    
    public void setTurnout(@CheckForNull Turnout turnout) {
        if (turnout != null) {
            if (_turnoutHandle != null) {
                InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
            }
            _turnoutHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                    .getNamedBeanHandle(turnout.getDisplayName(), turnout);
        } else {
            _turnoutHandle = null;
            InstanceManager.turnoutManagerInstance().removeVetoableChangeListener(this);
        }
    }
    
    public NamedBeanHandle<Turnout> getTurnout() {
        return _turnoutHandle;
    }
    
    public void setTurnoutState(TurnoutState state) {
        _turnoutState = state;
    }
    
    public TurnoutState getTurnoutState() {
        return _turnoutState;
    }
    
    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Turnout) {
                if (evt.getOldValue().equals(getTurnout().getBean())) {
                    throw new PropertyVetoException(getDisplayName(), evt);
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Turnout) {
                if (evt.getOldValue().equals(getTurnout().getBean())) {
                    ActionTurnout.this.setTurnout((Turnout)null);
                }
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute() {
        final Turnout t = _turnoutHandle.getBean();
//        final int newState = _turnoutState.getID();
        ThreadingUtil.runOnLayout(() -> {
            if (_turnoutState == TurnoutState.TOGGLE) {
//                t.setCommandedState(newState);
                if (t.getCommandedState() == Turnout.CLOSED) {
                    t.setCommandedState(Turnout.THROWN);
                } else {
                    t.setCommandedState(Turnout.CLOSED);
                }
            } else {
                t.setCommandedState(_turnoutState.getID());
            }
        });
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "Turnout_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String turnoutName;
        if (_turnoutHandle != null) {
            turnoutName = _turnoutHandle.getBean().getDisplayName();
        } else {
            turnoutName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        return Bundle.getMessage(locale, "Turnout_Long", turnoutName, _turnoutState._text);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }

    
    // This constant is only used internally in TurnoutState but must be outside
    // the enum.
    private static final int TOGGLE_ID = -1;
    
    
    public enum TurnoutState {
        CLOSED(Turnout.CLOSED, InstanceManager.getDefault(TurnoutManager.class).getClosedText()),
        THROWN(Turnout.THROWN, InstanceManager.getDefault(TurnoutManager.class).getThrownText()),
        TOGGLE(TOGGLE_ID, Bundle.getMessage("TurnoutToggleStatus"));
        
        private final int _id;
        private final String _text;
        
        private TurnoutState(int id, String text) {
            this._id = id;
            this._text = text;
        }
        
        static public TurnoutState get(int id) {
            switch (id) {
                case Turnout.CLOSED:
                    return CLOSED;
                    
                case Turnout.THROWN:
                    return THROWN;
                    
                case TOGGLE_ID:
                    return TOGGLE;
                    
                default:
                    throw new IllegalArgumentException("invalid turnout state");
            }
        }
        
        public int getID() {
            return _id;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    private final static Logger log = LoggerFactory.getLogger(ActionTurnout.class);
    
}
