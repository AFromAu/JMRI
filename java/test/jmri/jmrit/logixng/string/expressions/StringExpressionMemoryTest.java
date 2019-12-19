package jmri.jmrit.logixng.string.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.StringActionManager;
import jmri.jmrit.logixng.StringExpressionManager;
import jmri.jmrit.logixng.digital.actions.DoStringAction;
import jmri.jmrit.logixng.string.actions.StringActionMemory;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test StringExpressionMemory
 * 
 * @author Daniel Bergqvist 2018
 */
public class StringExpressionMemoryTest extends AbstractStringExpressionTestBase {

    LogixNG logixNG;
    ConditionalNG conditionalNG;
    StringExpressionMemory stringExpressionMemory;
    protected Memory _memory;
    protected Memory _memoryOut;
    
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
        return String.format("Get memory IM1%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         Read string E1 and set string A1%n" +
                "            ?s E1%n" +
                "               Get memory IM1%n" +
                "            !s A1%n" +
                "               Set memory IM2%n");
    }
    
    @Test
    public void testCtor() {
        Assert.assertTrue("object exists", _base != null);
        
        StringExpressionMemory expression2;
        Assert.assertNotNull("memory is not null", _memory);
        _memory.setValue(10.2);
        
        expression2 = new StringExpressionMemory("IQSE11", null);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertTrue("Username matches", null == expression2.getUserName());
        Assert.assertTrue("String matches", "Get memory none".equals(expression2.getLongDescription()));
        
        expression2 = new StringExpressionMemory("IQSE11", "My memory");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertTrue("Username matches", "My memory".equals(expression2.getUserName()));
        Assert.assertTrue("String matches", "Get memory none".equals(expression2.getLongDescription()));
        
        expression2 = new StringExpressionMemory("IQSE11", null);
        expression2.setMemory(_memory);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertTrue("Username matches", null == expression2.getUserName());
        Assert.assertTrue("String matches", "Get memory IM1".equals(expression2.getLongDescription()));
        
        expression2 = new StringExpressionMemory("IQSE11", "My memory");
        expression2.setMemory(_memory);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertTrue("Username matches", "My memory".equals(expression2.getUserName()));
        Assert.assertTrue("String matches", "Get memory IM1".equals(expression2.getLongDescription()));
        
        boolean thrown = false;
        try {
            // Illegal system name
            new StringExpressionMemory("IQA55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new StringExpressionMemory("IQA55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    @Test
    public void testEvaluate() throws SocketAlreadyConnectedException, SocketAlreadyConnectedException {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);
        
        StringExpressionMemory expression = (StringExpressionMemory)_base;
        _memory.setValue("");
        Assert.assertEquals("Evaluate matches", "", expression.evaluate());
        _memory.setValue("Other");
        Assert.assertEquals("Evaluate matches", "Other", expression.evaluate());
        expression.setMemory((Memory)null);
        Assert.assertEquals("Evaluate matches", "", expression.evaluate());
        expression.reset();
    }
    
    @Test
    public void testEvaluateAndAction() throws SocketAlreadyConnectedException, SocketAlreadyConnectedException {
        // Disable the conditionalNG
        conditionalNG.setEnabled(false);
        
        // Set the memory
        _memoryOut.setValue("");
        // The memory should have the value ""
        Assert.assertEquals("memory is \"\"", "", _memoryOut.getValue());
        // Set the value of the memory. This should not execute the conditional.
        _memory.setValue("Test");
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the memory should be 0.0
        Assert.assertEquals("memory is \"\"", "", _memoryOut.getValue());
        // Set the value of the memory. This should not execute the conditional.
        _memory.setValue("Other test");
        // Enable the logixNG and all its children.
        logixNG.setEnabled(true);
        // The action is not yet executed so the memory should be 0.0
        Assert.assertEquals("memory is \"\"", "", _memoryOut.getValue());
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // Set the value of the memory. This should execute the conditional.
        _memory.setValue("Something else");
        // The action should now be executed so the memory should be 3.0
        Assert.assertEquals("memory is \"Something else\"", "Something else", _memoryOut.getValue());
        // Disable the conditionalNG and all its children.
        conditionalNG.setEnabled(false);
        // The action is not yet executed so the memory should be 0.0
        Assert.assertEquals("memory is \"something else\"", "Something else", _memoryOut.getValue());
        // Set the value of the memory. This should not execute the conditional.
        _memory.setValue("Something new");
        // The action should not be executed so the memory should still be 3.0
        Assert.assertEquals("memory is \"something else\"", "Something else", _memoryOut.getValue());
        // Unregister listeners. This should do nothing since the listeners are
        // already unregistered.
        stringExpressionMemory.unregisterListeners();
        // The action is not yet executed so the memory should be 0.0
        Assert.assertEquals("memory is \"something else\"", "Something else", _memoryOut.getValue());
        // Set the value of the memory. This should not execute the conditional.
        _memory.setValue("Something different");
        // The action should not be executed so the memory should still be 3.0
        Assert.assertEquals("memory is \"something else\"", "Something else", _memoryOut.getValue());
        
        // Test register listeners when there is no memory.
        stringExpressionMemory.setMemory((Memory)null);
        stringExpressionMemory.registerListeners();
    }
    
    @Test
    public void testMemory() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);
        
        StringExpressionMemory expressionString = (StringExpressionMemory)_base;
        expressionString.setMemory((Memory)null);
        Assert.assertNull("Memory is null", expressionString.getMemory());
        expressionString.setMemory(_memory);
        Assert.assertTrue("Memory matches", _memory == expressionString.getMemory().getBean());
        
        expressionString.setMemory((NamedBeanHandle<Memory>)null);
        Assert.assertNull("Memory is null", expressionString.getMemory());
        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
        Assert.assertNotNull("memory is not null", otherMemory);
        NamedBeanHandle<Memory> memoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(otherMemory.getDisplayName(), otherMemory);
        expressionString.setMemory(memoryHandle);
        Assert.assertTrue("Memory matches", memoryHandle == expressionString.getMemory());
        Assert.assertTrue("Memory matches", otherMemory == expressionString.getMemory().getBean());
        
        expressionString.setMemory((String)null);
        Assert.assertNull("Memory is null", expressionString.getMemory());
        expressionString.setMemory(memoryHandle.getName());
        Assert.assertTrue("Memory matches", memoryHandle == expressionString.getMemory());
        
        // Test setMemory with a memory name that doesn't exists
        expressionString.setMemory("Non existent memory");
        Assert.assertTrue("Memory matches", memoryHandle == expressionString.getMemory());
        JUnitAppender.assertWarnMessage("memory 'Non existent memory' does not exists");
        
        // Test setMemory() when listeners are registered
        Assert.assertNotNull("Memory is not null", expressionString.getMemory());
        // Enable the conditionalNG. This will register the listeners
        conditionalNG.setEnabled(true);
        boolean thrown = false;
        try {
            expressionString.setMemory((String)null);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setMemory must not be called when listeners are registered");
        
        thrown = false;
        try {
            expressionString.setMemory((NamedBeanHandle<Memory>)null);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setMemory must not be called when listeners are registered");
        
        thrown = false;
        try {
            expressionString.setMemory((Memory)null);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setMemory must not be called when listeners are registered");
    }
    
    @Test
    public void testRegisterListeners() {
        StringExpressionMemory expressionString = (StringExpressionMemory)_base;
        // Test registerListeners() when the ExpressionLight has no light
        conditionalNG.setEnabled(false);
        expressionString.setMemory((Memory)null);
        conditionalNG.setEnabled(true);
    }
    
    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);
        
        // Get some other memory for later use
        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
        Assert.assertNotNull("Memory is not null", otherMemory);
        Assert.assertNotEquals("Memory is not equal", _memory, otherMemory);
        
        // Get the stringExpressionMemory and set the memory
        StringExpressionMemory expression = (StringExpressionMemory)_base;
        expression.setMemory(_memory);
        Assert.assertEquals("Memory matches", _memory, expression.getMemory().getBean());
        
        // Test vetoableChange() for some other propery
        expression.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertEquals("Memory matches", _memory, expression.getMemory().getBean());
        
        // Test vetoableChange() for a string
        expression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertEquals("Memory matches", _memory, expression.getMemory().getBean());
        expression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertEquals("Memory matches", _memory, expression.getMemory().getBean());
        
        // Test vetoableChange() for another memory
        expression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherMemory, null));
        Assert.assertEquals("Memory matches", _memory, expression.getMemory().getBean());
        expression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherMemory, null));
        Assert.assertEquals("Memory matches", _memory, expression.getMemory().getBean());
        
        // Test vetoableChange() for its own memory
        boolean thrown = false;
        try {
            expression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", _memory, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        Assert.assertEquals("Memory matches", _memory, expression.getMemory().getBean());
        expression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", _memory, null));
        Assert.assertNull("Memory is null", expression.getMemory());
    }
    
    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.ITEM == _base.getCategory());
    }
    
    @Test
    public void testIsExternal() {
        Assert.assertTrue("is external", _base.isExternal());
    }
    
    @Test
    public void testShortDescription() {
        Assert.assertTrue("String matches", "Get memory IM1".equals(_base.getShortDescription()));
    }
    
    @Test
    public void testLongDescription() {
        Assert.assertTrue("String matches", "Get memory IM1".equals(_base.getLongDescription()));
    }
    
    @Test
    public void testChild() {
        Assert.assertTrue("Num children is zero", 0 == _base.getChildCount());
        boolean hasThrown = false;
        try {
            _base.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertTrue("Error message is correct", "Not supported.".equals(ex.getMessage()));
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initMemoryManager();
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        conditionalNG.setRunOnGUIDelayed(false);
        conditionalNG.setEnabled(true);
        
        logixNG.addConditionalNG(conditionalNG);
        
        DoStringAction doStringAction = new DoStringAction("IQDA321", null);
        MaleSocket maleSocketDoStringAction =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(doStringAction);
        conditionalNG.getChild(0).connect(maleSocketDoStringAction);
        _memory = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        stringExpressionMemory = new StringExpressionMemory("IQSE321", "StringIO_Memory");
        MaleSocket maleSocketStringExpressionMemory =
                InstanceManager.getDefault(StringExpressionManager.class).registerExpression(stringExpressionMemory);
        doStringAction.getChild(0).connect(maleSocketStringExpressionMemory);
        stringExpressionMemory.setMemory(_memory);
        _base = stringExpressionMemory;
        _baseMaleSocket = maleSocketStringExpressionMemory;
        
        _memory = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        Assert.assertNotNull("memory is not null", _memory);
        _memory.setValue(10.2);
        
        _memoryOut = InstanceManager.getDefault(MemoryManager.class).provide("IM2");
        _memoryOut.setValue("");
        StringActionMemory actionMemory = new StringActionMemory("IQSA1", null);
        actionMemory.setMemory(_memoryOut);
        MaleSocket socketAction = InstanceManager.getDefault(StringActionManager.class).registerAction(actionMemory);
        maleSocketDoStringAction.getChild(1).connect(socketAction);
        
	logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
        logixNG.activateLogixNG();
    }

    @After
    public void tearDown() {
        _base.dispose();
        JUnitUtil.tearDown();
    }
    
}
