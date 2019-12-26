package jmri.jmrit.logixng.analog.actions;

import java.util.Locale;
import jmri.AnalogIO;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.AbstractBaseTestBase;
import jmri.jmrit.logixng.AnalogActionBean;
import jmri.jmrit.logixng.AnalogExpressionBean;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test AbstractAnalogAction
 * 
 * @author Daniel Bergqvist 2018
 */
public abstract class AbstractAnalogActionTestBase extends AbstractBaseTestBase {

    public abstract NamedBean createNewBean(String systemName) throws Exception;
    
    @Test
    public void testBadSystemName() throws Exception {
        boolean hasThrown = false;
        try {
            // Create a bean with bad system name. This must throw an exception
            NamedBean bean = createNewBean("IQ111");
            // We should never get here.
            Assert.assertNotNull("Bean is not null", bean);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Exception is correct", "system name is not valid", e.getMessage());
            hasThrown = true;
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testBundle() {
        Assert.assertEquals("strings are equal", "Set memory", Bundle.getMessage("AnalogActionMemory0"));
        Assert.assertEquals("strings are equal", "Set memory IM1", Bundle.getMessage("AnalogActionMemory1", "IM1"));
        Assert.assertEquals("strings are equal", "Set memory", Bundle.getMessage(Locale.CANADA, "AnalogActionMemory0"));
        Assert.assertEquals("strings are equal", "Set memory IM1", Bundle.getMessage(Locale.CANADA, "AnalogActionMemory1", "IM1"));
    }
    
    @Test
    public void testGetBeanType() {
        Assert.assertTrue("String matches", "Analog action".equals(((AnalogActionBean)_base).getBeanType()));
    }
    
    @Test
    public void testState() throws JmriException {
        AnalogActionBean _action = (AnalogActionBean)_base;
        _action.setState(AnalogIO.INCONSISTENT);
        Assert.assertTrue("State matches", AnalogIO.INCONSISTENT == _action.getState());
        jmri.util.JUnitAppender.assertWarnMessage("Unexpected call to getState in AbstractAnalogAction.");
        _action.setState(AnalogIO.UNKNOWN);
        Assert.assertTrue("State matches", AnalogIO.UNKNOWN == _action.getState());
        jmri.util.JUnitAppender.assertWarnMessage("Unexpected call to getState in AbstractAnalogAction.");
        _action.setState(AnalogIO.INCONSISTENT);
        Assert.assertTrue("State matches", AnalogIO.INCONSISTENT == _action.getState());
        jmri.util.JUnitAppender.assertWarnMessage("Unexpected call to getState in AbstractAnalogAction.");
    }
    
}
