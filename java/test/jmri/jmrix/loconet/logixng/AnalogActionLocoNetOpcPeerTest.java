package jmri.jmrix.loconet.logixng;

import jmri.jmrit.logixng.analog.actions.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.jmrit.logixng.AnalogActionManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.digital.actions.DoAnalogAction;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test AnalogActionLocoNet_OPC_PEER
 * 
 * @author Daniel Bergqvist 2018
 */
public class AnalogActionLocoNetOpcPeerTest extends AbstractAnalogActionTestBase {

    LogixNG logixNG;
    ConditionalNG conditionalNG;
    protected Memory _memory;
    
    @Override
    public ConditionalNG getConditionalNG() {
        return conditionalNG;
    }
    
    @Override
    public LogixNG getLogixNG() {
        return logixNG;
    }
    
    @Override
    public MaleSocket getConnectableChild() {
        Many action = new Many("IQAA999", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(AnalogActionManager.class).registerAction(action);
        return maleSocket;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format("LocoNet OPC_PEER analog action%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         Read analog E and set analog A%n" +
                "            ?~ E%n" +
                "               Socket not connected%n" +
                "            !~ A%n" +
                "               LocoNet OPC_PEER analog action%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new Many(systemName, null);
    }
    
    @Test
    public void testCtor() {
        Assert.assertTrue("object exists", _base != null);
        
        AnalogActionLocoNet_OPC_PEER action2;
        Assert.assertNotNull("memory is not null", _memory);
        _memory.setValue(10.2);
        
        action2 = new AnalogActionLocoNet_OPC_PEER("IQAA11", null);
        Assert.assertNotNull("object exists", action2);
        Assert.assertTrue("Username matches", null == action2.getUserName());
        Assert.assertTrue("String matches", "LocoNet OPC_PEER analog action".equals(action2.getLongDescription()));
        
        action2 = new AnalogActionLocoNet_OPC_PEER("IQAA11", "My memory");
        Assert.assertNotNull("object exists", action2);
        Assert.assertTrue("Username matches", "My memory".equals(action2.getUserName()));
        Assert.assertTrue("String matches", "LocoNet OPC_PEER analog action".equals(action2.getLongDescription()));
        
        action2 = new AnalogActionLocoNet_OPC_PEER("IQAA11", null);
        action2.setMemory(_memory);
        Assert.assertNotNull("object exists", action2);
        Assert.assertTrue("Username matches", null == action2.getUserName());
        Assert.assertTrue("String matches", "LocoNet OPC_PEER analog action".equals(action2.getLongDescription()));
        
        action2 = new AnalogActionLocoNet_OPC_PEER("IQAA11", "My memory");
        action2.setMemory(_memory);
        Assert.assertNotNull("object exists", action2);
        Assert.assertTrue("Username matches", "My memory".equals(action2.getUserName()));
        Assert.assertTrue("String matches", "LocoNet OPC_PEER analog action".equals(action2.getLongDescription()));
        
        boolean thrown = false;
        try {
            // Illegal system name
            new AnalogActionLocoNet_OPC_PEER("IQA55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new AnalogActionLocoNet_OPC_PEER("IQA55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    @Test
    public void testAction() throws SocketAlreadyConnectedException, SocketAlreadyConnectedException {
        AnalogActionLocoNet_OPC_PEER action = (AnalogActionLocoNet_OPC_PEER)_base;
        action.setValue(0.0d);
        Assert.assertTrue("Memory has correct value", 0.0d == (Double)_memory.getValue());
        action.setValue(1.0d);
        Assert.assertTrue("Memory has correct value", 1.0d == (Double)_memory.getValue());
        action.setMemory((Memory)null);
        action.setValue(2.0d);
        Assert.assertTrue("Memory has correct value", 1.0d == (Double)_memory.getValue());
    }
    
    @Test
    public void testMemory() {
        AnalogActionLocoNet_OPC_PEER action = (AnalogActionLocoNet_OPC_PEER)_base;
        action.setMemory((Memory)null);
        Assert.assertNull("Memory is null", action.getMemory());
        ((AnalogActionLocoNet_OPC_PEER)_base).setMemory(_memory);
        Assert.assertTrue("Memory matches", _memory == action.getMemory().getBean());
        
        action.setMemory((NamedBeanHandle<Memory>)null);
        Assert.assertNull("Memory is null", action.getMemory());
        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
        Assert.assertNotNull("memory is not null", otherMemory);
        NamedBeanHandle<Memory> memoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(otherMemory.getDisplayName(), otherMemory);
        ((AnalogActionLocoNet_OPC_PEER)_base).setMemory(memoryHandle);
        Assert.assertTrue("Memory matches", memoryHandle == action.getMemory());
        Assert.assertTrue("Memory matches", otherMemory == action.getMemory().getBean());
        
        action.setMemory((String)null);
        Assert.assertNull("Memory is null", action.getMemory());
        action.setMemory(memoryHandle.getName());
        Assert.assertTrue("Memory matches", memoryHandle == action.getMemory());
        
        // Test setMemory with a memory name that doesn't exists
        action.setMemory("Non existent memory");
        Assert.assertTrue("Memory matches", memoryHandle == action.getMemory());
        JUnitAppender.assertWarnMessage("memory 'Non existent memory' does not exists");
    }
    
    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Get some other memory for later use
        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
        Assert.assertNotNull("Memory is not null", otherMemory);
        Assert.assertNotEquals("Memory is not equal", _memory, otherMemory);
        
        // Get the expression and set the memory
        AnalogActionLocoNet_OPC_PEER action = (AnalogActionLocoNet_OPC_PEER)_base;
        action.setMemory(_memory);
        Assert.assertEquals("Memory matches", _memory, action.getMemory().getBean());
        
        // Test vetoableChange() for some other propery
        action.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertEquals("Memory matches", _memory, action.getMemory().getBean());
        
        // Test vetoableChange() for a string
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertEquals("Memory matches", _memory, action.getMemory().getBean());
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertEquals("Memory matches", _memory, action.getMemory().getBean());
        
        // Test vetoableChange() for another memory
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherMemory, null));
        Assert.assertEquals("Memory matches", _memory, action.getMemory().getBean());
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherMemory, null));
        Assert.assertEquals("Memory matches", _memory, action.getMemory().getBean());
        
        // Test vetoableChange() for its own memory
        boolean thrown = false;
        try {
            action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", _memory, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        Assert.assertEquals("Memory matches", _memory, action.getMemory().getBean());
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", _memory, null));
        Assert.assertNull("Memory is null", action.getMemory());
    }
    
    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.OTHER == _base.getCategory());
    }
    
    @Test
    public void testIsExternal() {
        Assert.assertTrue("is external", _base.isExternal());
    }
    
    @Test
    public void testShortDescription() {
        Assert.assertTrue("String matches", "LocoNet OPC_PEER analog action".equals(_base.getShortDescription()));
    }
    
    @Test
    public void testLongDescription() {
        Assert.assertTrue("String matches", "LocoNet OPC_PEER analog action".equals(_base.getLongDescription()));
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
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        conditionalNG.setEnabled(true);
        conditionalNG.setRunOnGUIDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        
        DoAnalogAction doAnalogAction = new DoAnalogAction("IQDA321", null);
        MaleSocket maleSocketDoAnalogAction =
                InstanceManager.getDefault(DigitalActionManager.class)
                        .registerAction(doAnalogAction);
        conditionalNG.getChild(0).connect(maleSocketDoAnalogAction);
        _memory = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        AnalogActionLocoNet_OPC_PEER analogActionMemory =
                new AnalogActionLocoNet_OPC_PEER("IQAA321", "AnalogIO_Memory");
        MaleSocket maleSocketAnalogActionLocoNet_OPC_PEER =
                InstanceManager.getDefault(AnalogActionManager.class)
                        .registerAction(analogActionMemory);
        doAnalogAction.getChild(1).connect(maleSocketAnalogActionLocoNet_OPC_PEER);
        analogActionMemory.setMemory(_memory);
        _base = analogActionMemory;
        _baseMaleSocket = maleSocketAnalogActionLocoNet_OPC_PEER;
        
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
