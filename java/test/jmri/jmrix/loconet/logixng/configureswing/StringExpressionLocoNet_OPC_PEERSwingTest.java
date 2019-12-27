package jmri.jmrix.loconet.logixng.configureswing;

import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test Bundle
 * 
 * @author Daniel Bergqvist 2018
 */
public class StringExpressionLocoNet_OPC_PEERSwingTest {

    @Test
    public void testCtor() {
        SwingConfiguratorInterface sci = new StringExpressionLocoNet_OPC_PEERSwing();
        Assert.assertNotNull("object exists", sci);
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
