package jmri.jmrix.loconet.logixng;

import jmri.jmrit.logixng.string.expressions.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Locale;
import javax.annotation.CheckForNull;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Receives an OPC_PEER message on the LocoNet
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class StringExpressionLocoNetOpcPeer extends AbstractStringExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private NamedBeanHandle<Memory> _memoryHandle;
    private boolean _listenersAreRegistered = false;
    
    public StringExpressionLocoNetOpcPeer(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        
        super(sys, user);
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory().getBean())) {
                    throw new PropertyVetoException(getDisplayName(), evt);
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory().getBean())) {
                    setMemory((Memory)null);
                }
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return true;
    }
    
    public void setMemory(String memoryName) {
        if (_listenersAreRegistered) {
            RuntimeException e = new RuntimeException("setMemory must not be called when listeners are registered");
            log.error("setMemory must not be called when listeners are registered", e);
            throw e;
        }
        if (memoryName != null) {
            Memory memory = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName);
            if (memory != null) {
                _memoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(memoryName, memory);
            } else {
                log.warn("memory '{}' does not exists", memoryName);
            }
        } else {
            _memoryHandle = null;
        }
    }
    
    public void setMemory(NamedBeanHandle<Memory> handle) {
        if (_listenersAreRegistered) {
            RuntimeException e = new RuntimeException("setMemory must not be called when listeners are registered");
            log.error("setMemory must not be called when listeners are registered", e);
            throw e;
        }
        _memoryHandle = handle;
    }
    
    public void setMemory(@CheckForNull Memory memory) {
        if (_listenersAreRegistered) {
            RuntimeException e = new RuntimeException("setMemory must not be called when listeners are registered");
            log.error("setMemory must not be called when listeners are registered", e);
            throw e;
        }
        if (memory != null) {
            _memoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                    .getNamedBeanHandle(memory.getDisplayName(), memory);
        } else {
            _memoryHandle = null;
        }
    }
    
    public NamedBeanHandle<Memory> getMemory() {
        return _memoryHandle;
    }
    
    /** {@inheritDoc} */
    @Override
    public String evaluate() {
        if (_memoryHandle != null) {
            return jmri.util.TypeConversionUtil.convertToString(_memoryHandle.getBean().getValue(), false);
        } else {
            return "";
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void reset() {
        // Do nothing
    }
    
    @Override
    public FemaleSocket getChild(int index)
            throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public String getShortDescription(Locale locale) {
        if (_memoryHandle != null) {
            return Bundle.getMessage(locale, "StringExpressionLocoNet_OPC_PEER_Short", _memoryHandle.getBean().getDisplayName());
        } else {
            return Bundle.getMessage(locale, "StringExpressionLocoNet_OPC_PEER_Short", "none");
        }
    }

    @Override
    public String getLongDescription(Locale locale) {
        return getShortDescription(locale);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if ((! _listenersAreRegistered) && (_memoryHandle != null)) {
            _memoryHandle.getBean().addPropertyChangeListener("value", this);
            _listenersAreRegistered = true;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _memoryHandle.getBean().removePropertyChangeListener("value", this);
            _listenersAreRegistered = false;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }
    
    
    private final static Logger log = LoggerFactory.getLogger(StringExpressionLocoNetOpcPeer.class);
    
}
