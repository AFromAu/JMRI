package jmri.jmrit.logixng.digital.boolean_actions.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.jdom2.Element;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ActionTurnoutXml
 * 
 * @author Daniel Bergqvist 2019
 */
public class OnChangeActionXmlTest {

    @Test
    public void testLoad() throws JmriConfigureXmlException {
        AbstractNamedBeanManagerConfigXML b;
        
        b = new OnChangeXml();
        Assert.assertNotNull("exists", b);
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
