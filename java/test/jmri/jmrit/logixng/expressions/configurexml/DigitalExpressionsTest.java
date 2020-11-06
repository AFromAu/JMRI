package jmri.jmrit.logixng.expressions.configurexml;

import jmri.jmrit.logixng.expressions.configurexml.FalseXml;
import jmri.jmrit.logixng.expressions.configurexml.ExpressionTimerXml;
import jmri.jmrit.logixng.expressions.configurexml.TrueXml;
import jmri.jmrit.logixng.expressions.configurexml.ExpressionTurnoutXml;
import jmri.jmrit.logixng.expressions.configurexml.OrXml;
import jmri.jmrit.logixng.expressions.configurexml.AntecedentXml;
import jmri.jmrit.logixng.expressions.configurexml.AndXml;
import jmri.jmrit.logixng.expressions.configurexml.ResetOnTrueXml;
import jmri.jmrit.logixng.expressions.configurexml.ExpressionLightXml;
import jmri.jmrit.logixng.expressions.configurexml.HoldXml;
import jmri.jmrit.logixng.expressions.configurexml.TriggerOnceXml;
import jmri.jmrit.logixng.expressions.configurexml.ExpressionSensorXml;
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
public class DigitalExpressionsTest {

    @Test
    public void testLoad() throws JmriConfigureXmlException {
        AbstractNamedBeanManagerConfigXML b;
        
        b = new AndXml();
        Assert.assertNotNull("exists", b);
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");
        
        b = new AntecedentXml();
        Assert.assertNotNull("exists", b);
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");
        
        b = new ExpressionLightXml();
        Assert.assertNotNull("exists", b);
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");
        
        b = new ExpressionSensorXml();
        Assert.assertNotNull("exists", b);
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");
        
        b = new ExpressionTurnoutXml();
        Assert.assertNotNull("exists", b);
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");
        
        b = new FalseXml();
        Assert.assertNotNull("exists", b);
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");
        
        b = new HoldXml();
        Assert.assertNotNull("exists", b);
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");
        
        b = new OrXml();
        Assert.assertNotNull("exists", b);
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");
        
        b = new ResetOnTrueXml();
        Assert.assertNotNull("exists", b);
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");
        
        b = new ExpressionTimerXml();
        Assert.assertNotNull("exists", b);
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");
        
        b = new TriggerOnceXml();
        Assert.assertNotNull("exists", b);
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");
        
        b = new TrueXml();
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
