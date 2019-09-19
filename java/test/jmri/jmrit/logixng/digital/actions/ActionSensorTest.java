package jmri.jmrit.logixng.digital.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ActionSensor
 * 
 * @author Daniel Bergqvist 2018
 */
public class ActionSensorTest extends AbstractDigitalActionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ActionSensor actionSensor;
    private Sensor sensor;
    
    
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
        return String.format("Set sensor IS1 to Active%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         Set sensor IS1 to Active%n");
    }
    
    @Test
    public void testCtor() throws JmriException {
        Assert.assertTrue("object exists", _base != null);
        
        ActionSensor action2;
        Assert.assertNotNull("sensor is not null", sensor);
        sensor.setState(Sensor.ON);
        
        action2 = new ActionSensor("IQDA321", null);
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username matches", action2.getUserName());
        Assert.assertEquals("String matches", "Set sensor '' to Active", action2.getLongDescription());
        
        action2 = new ActionSensor("IQDA321", "My sensor");
        Assert.assertNotNull("object exists", action2);
        Assert.assertEquals("Username matches", "My sensor", action2.getUserName());
        Assert.assertEquals("String matches", "Set sensor '' to Active", action2.getLongDescription());
        
        action2 = new ActionSensor("IQDA321", null);
        action2.setSensor(sensor);
        Assert.assertTrue("atomic boolean is correct", sensor == action2.getSensor().getBean());
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username matches", action2.getUserName());
        Assert.assertEquals("String matches", "Set sensor IS1 to Active", action2.getLongDescription());
        
        Sensor l = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        action2 = new ActionSensor("IQDA321", "My sensor");
        action2.setSensor(l);
        Assert.assertTrue("atomic boolean is correct", l == action2.getSensor().getBean());
        Assert.assertNotNull("object exists", action2);
        Assert.assertEquals("Username matches", "My sensor", action2.getUserName());
        Assert.assertEquals("String matches", "Set sensor IS1 to Active", action2.getLongDescription());
        
        // Test template
        action2 = (ActionSensor)_base.getNewObjectBasedOnTemplate();
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username is null", action2.getUserName());
//        Assert.assertTrue("Username matches", "My sensor".equals(expression2.getUserName()));
        Assert.assertEquals("String matches", "Set sensor '' to Active", action2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new ActionSensor("IQA55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new ActionSensor("IQA55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        // Test setup(). This method doesn't do anything, but execute it for coverage.
        _base.setup();
    }
    
    @Test
    public void testAction() throws SocketAlreadyConnectedException, JmriException {
        // The action is not yet executed so the sensor should be closed
        Assert.assertTrue("sensor is closed",sensor.getCommandedState() == Sensor.INACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the sensor should be thrown
        Assert.assertTrue("sensor is thrown",sensor.getCommandedState() == Sensor.ACTIVE);
        
        // Test to set sensor to closed
        actionSensor.setSensorState(ActionSensor.SensorState.INACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the sensor should be thrown
        Assert.assertTrue("sensor is thrown",sensor.getCommandedState() == Sensor.INACTIVE);
        
        // Test to set sensor to toggle
        actionSensor.setSensorState(ActionSensor.SensorState.TOGGLE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the sensor should be thrown
        Assert.assertTrue("sensor is thrown",sensor.getCommandedState() == Sensor.ACTIVE);
        
        // Test to set sensor to toggle
        actionSensor.setSensorState(ActionSensor.SensorState.TOGGLE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the sensor should be thrown
        Assert.assertTrue("sensor is thrown",sensor.getCommandedState() == Sensor.INACTIVE);
    }
    
    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Get the action and set the sensor
        Sensor sensor = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        Assert.assertNotNull("Sensor is not null", sensor);
        ActionSensor action = new ActionSensor(InstanceManager.getDefault(DigitalActionManager.class).getNewSystemName(), null);
        action.setSensor(sensor);
        
        // Get some other sensor for later use
        Sensor otherSensor = InstanceManager.getDefault(SensorManager.class).provide("IM99");
        Assert.assertNotNull("Sensor is not null", otherSensor);
        Assert.assertNotEquals("Sensor is not equal", sensor, otherSensor);
        
        // Test vetoableChange() for some other propery
        action.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertEquals("Sensor matches", sensor, action.getSensor().getBean());
        
        // Test vetoableChange() for a string
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertEquals("Sensor matches", sensor, action.getSensor().getBean());
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertEquals("Sensor matches", sensor, action.getSensor().getBean());
        
        // Test vetoableChange() for another sensor
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherSensor, null));
        Assert.assertEquals("Sensor matches", sensor, action.getSensor().getBean());
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherSensor, null));
        Assert.assertEquals("Sensor matches", sensor, action.getSensor().getBean());
        
        // Test vetoableChange() for its own sensor
        boolean thrown = false;
        try {
            action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", sensor, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        Assert.assertEquals("Sensor matches", sensor, action.getSensor().getBean());
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", sensor, null));
        Assert.assertNull("Sensor is null", action.getSensor());
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
        Assert.assertEquals("String matches", "Set sensor", _base.getShortDescription());
    }
    
    @Test
    public void testLongDescription() {
        Assert.assertEquals("String matches", "Set sensor IS1 to Active", _base.getLongDescription());
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
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initLogixNG();
        
        sensor = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        sensor.setCommandedState(Sensor.INACTIVE);
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setEnabled(true);
        actionSensor = new ActionSensor(InstanceManager.getDefault(DigitalActionManager.class).getNewSystemName(), null);
        actionSensor.setSensor(sensor);
        actionSensor.setSensorState(ActionSensor.SensorState.ACTIVE);
        MaleSocket socket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionSensor);
        conditionalNG.getChild(0).connect(socket);
        
        _base = actionSensor;
        _baseMaleSocket = socket;
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
