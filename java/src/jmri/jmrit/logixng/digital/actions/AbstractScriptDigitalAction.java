package jmri.jmrit.logixng.digital.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.DigitalAction;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.FemaleSocket;

/**
 * Abstract class for scripts that implements a digital expression.
 * <P>
 * A script that extends this class must implement the method evaluate().
 * It may also implement the methods setup(), registerScriptListeners(),
 * unregisterScriptListeners(), reset() and disposeMe().
 * <P>
 * If the script needs to run the ConditionalNG that this expression belongs to,
 * it does that by calling the method propertyChange().
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public abstract class AbstractScriptDigitalAction extends AbstractDigitalAction
        implements PropertyChangeListener, VetoableChangeListener {

    private boolean _listenersAreRegistered = false;
    private final DigitalAction _parentDigitalExpression;
    
    
    public AbstractScriptDigitalAction(DigitalAction de)
            throws BadUserNameException, BadSystemNameException {
        // This bean is never stored in a manager and
        // its system name nor user name is never used.
        super("IQDA0", null);
        _parentDigitalExpression = de;
    }
    
    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
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
        throw new UnsupportedOperationException("Not supported");
    }
    
    @Override
    public String getLongDescription(Locale locale) {
        throw new UnsupportedOperationException("Not supported");
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
    }
    
    /**
     * Register listeners if this object needs that.
     * The script can override this method to register any listener it needs.
     * <P>
     * This method is only called if listeners are not registered.
     */
    protected void registerScriptListeners() {
    }
    
    /**
     * Unregister listeners if this object needs that.
     * The script can override this method to unregister any listener it needs.
     * <P>
     * This method is only called if listeners are registered.
     */
    protected void unregisterScriptListeners() {
    }
    
    /** {@inheritDoc} */
    @Override
    public final void registerListenersForThisClass() {
        if (!_listenersAreRegistered) {
            registerScriptListeners();
            _listenersAreRegistered = true;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public final void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            unregisterScriptListeners();
            _listenersAreRegistered = false;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        _parentDigitalExpression.getConditionalNG().execute();
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }
    
}
