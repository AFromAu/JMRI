package jmri.jmrit.logixng.digital.expressions;

import java.util.concurrent.atomic.AtomicBoolean;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.NamedBean;
import jmri.Turnout;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrit.logixng.DigitalExpressionBean;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.digital.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.digital.actions.IfThenElse;

/**
 * Test And
 * 
 * @author Daniel Bergqvist 2018
 */
public class AndTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
//    private And expressionAnd;
//    private ActionAtomicBoolean actionAtomicBoolean;
//    private AtomicBoolean atomicBoolean;
//    private Memory memory;
    
    
    @Override
    public ConditionalNG getConditionalNG() {
        return conditionalNG;
    }
    
    @Override
    public LogixNG getLogixNG() {
        return logixNG;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "And%n" +
                "   ? E1%n" +
                "      Socket not connected%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         If E then A1 else A2%n" +
                "            ? E%n" +
                "               And%n" +
                "                  ? E1%n" +
                "                     Socket not connected%n" +
                "            ! A1%n" +
                "               Socket not connected%n" +
                "            ! A2%n" +
                "               Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new And(systemName, null);
    }
    
    @Test
    public void testCtor() {
        And expression2;
        
        expression2 = new And("IQDE321", null);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "And", expression2.getLongDescription());
        
        expression2 = new And("IQDE321", "My expression");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My expression", expression2.getUserName());
        Assert.assertEquals("String matches", "And", expression2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new And("IQE55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new And("IQE55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    @Test
    public void testGetChild() throws SocketAlreadyConnectedException {
        And expression2 = new And("IQDE321", null);
        
        for (int i=0; i < 3; i++) {
            Assert.assertTrue("getNumChilds() returns "+i, i+1 == expression2.getChildCount());

            Assert.assertNotNull("getChild(0) returns a non null value",
                    expression2.getChild(0));

            boolean hasThrown = false;
            try {
                expression2.getChild(i+1);
            } catch (IndexOutOfBoundsException ex) {
                hasThrown = true;
                String msg = String.format("Index: %d, Size: %d", i+1, i+1);
                Assert.assertEquals("Error message is correct", msg, ex.getMessage());
            }
            Assert.assertTrue("Exception is thrown", hasThrown);
            
            // Connect a new child expression
            True expr = new True("IQDE"+i, null);
            MaleSocket maleSocket =
                    InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expr);
            expression2.getChild(i).connect(maleSocket);
        }
    }
    
    @Test
    public void testDescription() {
        And e1 = new And("IQDE321", null);
        Assert.assertTrue("And".equals(e1.getShortDescription()));
        Assert.assertTrue("And".equals(e1.getLongDescription()));
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        
        _category = Category.COMMON;
        _isExternal = false;
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        conditionalNG.setRunOnGUIDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        IfThenElse ifThenElse = new IfThenElse("IQDA321", null, IfThenElse.Type.TRIGGER_ACTION);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        conditionalNG.getChild(0).connect(maleSocket);
        
        DigitalExpressionBean expression = new And("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
        ifThenElse.getChild(0).connect(maleSocket2);
        
        _base = expression;
        _baseMaleSocket = maleSocket2;
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
