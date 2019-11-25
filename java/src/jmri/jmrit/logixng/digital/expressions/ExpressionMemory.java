package jmri.jmrit.logixng.digital.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.FemaleSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluates the state of a Memory.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class ExpressionMemory extends AbstractDigitalExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private ExpressionMemory _template;
    private NamedBeanHandle<Memory> _memoryHandle;
    private MemoryOperation _memoryOperation = MemoryOperation.EQUAL;
    private CompareTo _compareTo = CompareTo.VALUE;
    private boolean _caseInsensitive = false;
    private String _constantValue = "";
    private NamedBeanHandle<Memory> _otherMemoryHandle;
    private boolean _listenersAreRegistered = false;
    
    public ExpressionMemory(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    private ExpressionMemory(ExpressionMemory template) {
        super(InstanceManager.getDefault(DigitalExpressionManager.class).getAutoSystemName(), null);
        _template = template;
        if (_template == null) throw new NullPointerException();    // Temporary solution to make variable used.
    }
    
    /** {@inheritDoc} */
    @Override
    public Base getNewObjectBasedOnTemplate() {
        return new ExpressionMemory(this);
    }
    
    public void setMemory(String memoryName) {
        if (_listenersAreRegistered) {
            RuntimeException e = new RuntimeException("setMemory must not be called when listeners are registered");
            log.error("setMemory must not be called when listeners are registered", e);
            throw e;
        }
        Memory memory = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName);
        if (memory != null) {
            _memoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(memoryName, memory);
        } else {
            log.error("memory {} is not found", memoryName);
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
    
    public void setConstantValue(String constantValue) {
        _constantValue = constantValue;
    }
    
    public String getConstantValue() {
        return _constantValue;
    }
    
    public void setMemoryOperation(MemoryOperation memoryOperation) {
        _memoryOperation = memoryOperation;
    }
    
    public MemoryOperation getMemoryOperation() {
        return _memoryOperation;
    }

    public void setCompareTo(CompareTo compareTo) {
        _compareTo = compareTo;
    }
    
    public CompareTo getCompareTo() {
        return _compareTo;
    }

    public void setCaseInsensitive(boolean caseInsensitive) {
        _caseInsensitive = caseInsensitive;
    }
    
    public boolean getCaseInsensitive() {
        return _caseInsensitive;
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
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
        return Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return true;
    }
    
    private String getString(Object o) {
        if (o != null) {
            return o.toString();
        }
        return null;
    }
    
    /**
     * Compare two values using the comparator set using the comparison
     * instructions in {@link #setNum1(int)}.
     *
     * <strong>Note:</strong> {@link #getNum1()} must be one of {@link #LESS_THAN},
     * {@link #LESS_THAN_OR_EQUAL}, {@link #EQUAL},
     * {@link #GREATER_THAN_OR_EQUAL}, or {@link #GREATER_THAN}.
     *
     * @param value1          left side of the comparison
     * @param value2          right side of the comparison
     * @param caseInsensitive true if comparison should be case insensitive;
     *                        false otherwise
     * @return true if values compare per getNum1(); false otherwise
     */
    private boolean compare(String value1, String value2, boolean caseInsensitive) {
        if (value1 == null) {
            return value2 == null;
        } else {
            if (value2 == null) {
                return false;
            }
            value1 = value1.trim();
            value2 = value2.trim();
        }
        try {
            int n1 = Integer.parseInt(value1);
            try {
                int n2 = Integer.parseInt(value2);
                log.debug("Compare numbers: n1= {} to n2= {}", n1, n2);
                switch (_memoryOperation) // both are numbers
                {
                    case LESS_THAN:
                        return (n1 < n2);
                    case LESS_THAN_OR_EQUAL:
                        return (n1 <= n2);
                    case EQUAL:
                        return (n1 == n2);
                    case NOT_EQUAL:
                        return (n1 == n2);
                    case GREATER_THAN_OR_EQUAL:
                        return (n1 >= n2);
                    case GREATER_THAN:
                        return (n1 > n2);
                    default:
                        throw new IllegalArgumentException("_memoryOperation has unknown value: "+_memoryOperation.name());
                }
            } catch (NumberFormatException nfe) {
                return false;   // n1 is a number, n2 is not
            }
        } catch (NumberFormatException nfe) {
            try {
                Integer.parseInt(value2);
                return false;     // n1 is not a number, n2 is
            } catch (NumberFormatException ex) { // OK neither a number
            }
        }
        log.debug("Compare Strings: value1= {} to value2= {}", value1, value2);
        int compare;
        if (caseInsensitive) {
            compare = value1.compareToIgnoreCase(value2);
        } else {
            compare = value1.compareTo(value2);
        }
        switch (_memoryOperation) {
            case LESS_THAN:
                if (compare < 0) {
                    return true;
                }
                break;
            case LESS_THAN_OR_EQUAL:
                if (compare <= 0) {
                    return true;
                }
                break;
            case EQUAL:
                if (compare == 0) {
                    return true;
                }
                break;
            case NOT_EQUAL:
                if (compare != 0) {
                    return true;
                }
                break;
            case GREATER_THAN_OR_EQUAL:
                if (compare >= 0) {
                    return true;
                }
                break;
            case GREATER_THAN:
                if (compare > 0) {
                    return true;
                }
                break;
            default:
                throw new IllegalArgumentException("_memoryOperation has unknown value: "+_memoryOperation.name());
        }
        return false;
    }
    
    private boolean matchRegex(String memoryValue, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(memoryValue);
        return m.matches();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean evaluate() {
        // ConditionalVariable, line 661:  boolean compare(String value1, String value2, boolean caseInsensitive) {
        String memoryValue = getString(_memoryHandle.getBean().getValue());
        String otherValue = null;
        
        switch (_compareTo) {
            case VALUE:
                otherValue = _constantValue;
                break;
            case MEMORY:
                otherValue = getString(_otherMemoryHandle.getBean().getValue());
                break;
            default:
                throw new IllegalArgumentException("_compareTo has unknown value: "+_compareTo.name());
        }
        
        switch (_memoryOperation) {
            case LESS_THAN:
                // fall through
            case LESS_THAN_OR_EQUAL:
                // fall through
            case EQUAL:
                // fall through
            case NOT_EQUAL:
                // fall through
            case GREATER_THAN_OR_EQUAL:
                // fall through
            case GREATER_THAN:
                return compare(memoryValue, otherValue, _caseInsensitive);
                
            case IS_NULL:
                return memoryValue == null;
            case IS_NOT_NULL:
                return memoryValue != null;
                
            case MATCH_REGEX:
                return matchRegex(memoryValue, otherValue);
                
            case NOT_MATCH_REGEX:
                return !matchRegex(memoryValue, otherValue);
                
            default:
                throw new IllegalArgumentException("_memoryOperation has unknown value: "+_memoryOperation.name());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void reset() {
        // Do nothing.
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
        return Bundle.getMessage(locale, "Memory_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String memoryName;
        if (_memoryHandle != null) {
            memoryName = _memoryHandle.getBean().getDisplayName();
        } else {
            memoryName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        return Bundle.getMessage(locale, "Memory_Long", memoryName, _memoryOperation._text);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered && (_memoryHandle != null)) {
            _memoryHandle.getBean().addPropertyChangeListener("KnownState", this);
            _listenersAreRegistered = true;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _memoryHandle.getBean().removePropertyChangeListener("KnownState", this);
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
    
    
    
    public enum MemoryOperation {
        LESS_THAN(Bundle.getMessage("MemoryOperation_LessThan"), true),
        LESS_THAN_OR_EQUAL(Bundle.getMessage("MemoryOperation_LessThanOrEqual"), true),
        EQUAL(Bundle.getMessage("MemoryOperation_Equal"), true),
        GREATER_THAN_OR_EQUAL(Bundle.getMessage("MemoryOperation_GreaterThanOrEqual"), true),
        GREATER_THAN(Bundle.getMessage("MemoryOperation_GreaterThan"), true),
        NOT_EQUAL(Bundle.getMessage("MemoryOperation_NotEqual"), true),
        IS_NULL(Bundle.getMessage("MemoryOperation_IsNull"), false),
        IS_NOT_NULL(Bundle.getMessage("MemoryOperation_IsNotNull"), false),
        MATCH_REGEX(Bundle.getMessage("MemoryOperation_MatchRegEx"), true),
        NOT_MATCH_REGEX(Bundle.getMessage("MemoryOperation_NotMatchRegEx"), true);
        
        private final String _text;
        private final boolean _extraValue;
        
        private MemoryOperation(String text, boolean extraValue) {
            this._text = text;
            this._extraValue = extraValue;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
        public boolean hasExtraValue() {
            return _extraValue;
        }
        
    }
    
    
    public enum CompareTo {
        VALUE(Bundle.getMessage("CompareTo_Value")),
        MEMORY(Bundle.getMessage("CompareTo_Memory"));
        
        private final String _text;
        
        private CompareTo(String text) {
            this._text = text;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    
    // Should the memory be used as is, or should it be converted using toString()
    // or toReportString() methods? The toReportString() will return null if the
    // memory does not contains a report.
    public enum TypeOfValue {
        AS_IS(Bundle.getMessage("TypeOfValue_AsIs")),
        TO_STRING(Bundle.getMessage("TypeOfValue_ToString")),
        TO_REPORT(Bundle.getMessage("TypeOfValue_ToReport"));
        
        private final String _text;
        
        private TypeOfValue(String text) {
            this._text = text;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    
    private final static Logger log = LoggerFactory.getLogger(ExpressionMemory.class);
    
}
