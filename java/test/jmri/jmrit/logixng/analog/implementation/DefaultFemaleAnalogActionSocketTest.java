package jmri.jmrit.logixng.analog.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.FemaleSocketTestBase;
import jmri.jmrit.logixng.analog.actions.AnalogActionMemory;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrit.logixng.AnalogActionBean;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.swing.SwingTools;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 * Test ExpressionTimer
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultFemaleAnalogActionSocketTest extends FemaleSocketTestBase {

    private String _memorySystemName;
    private Memory _memory;
    private MyAnalogActionMemory _action;
    
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void testBundleClass() {
        Assert.assertEquals("bundle is correct", "Test Bundle bb aa cc", Bundle.getMessage("TestBundle", "aa", "bb", "cc"));
        Assert.assertEquals("bundle is correct", "Generic", Bundle.getMessage(Locale.US, "SocketTypeGeneric"));
        Assert.assertEquals("bundle is correct", "Test Bundle bb aa cc", Bundle.getMessage(Locale.US, "TestBundle", "aa", "bb", "cc"));
    }
    
    @Test
    public void testGetName() {
        Assert.assertTrue("String matches", "A1".equals(femaleSocket.getName()));
    }
    
    @Test
    public void testGetDescription() {
        Assert.assertEquals("String matches", "!~", femaleSocket.getShortDescription());
        Assert.assertEquals("String matches", "!~ A1", femaleSocket.getLongDescription());
    }
    
    @Override
    protected FemaleSocket getFemaleSocket(String name) {
        return new DefaultFemaleAnalogActionSocket(null, new FemaleSocketListener() {
            @Override
            public void connected(FemaleSocket socket) {
            }

            @Override
            public void disconnected(FemaleSocket socket) {
            }
        }, name);
    }
    
    @Override
    protected boolean hasSocketBeenSetup() {
        return _action._hasBeenSetup;
    }
    
    @Test
    public void testSystemName() {
        Assert.assertEquals("String matches", "IQAA10", femaleSocket.getExampleSystemName());
        Assert.assertEquals("String matches", "IQAA:AUTO:0001", femaleSocket.getNewSystemName());
    }
    
    @Test
    public void testSetValue() throws Exception {
        // Every test method should have an assertion
        Assert.assertNotNull("femaleSocket is not null", femaleSocket);
        Assert.assertFalse("femaleSocket is not connected", femaleSocket.isConnected());
        // Test setValue() when not connected
        ((DefaultFemaleAnalogActionSocket)femaleSocket).setValue(0.0);
    }
    
    @Test
    public void testGetConnectableClasses() {
        Map<Category, List<Class<? extends Base>>> map = new HashMap<>();
        
        List<Class<? extends Base>> classes = new ArrayList<>();
        classes.add(jmri.jmrit.logixng.analog.actions.AnalogActionMemory.class);
        map.put(Category.ITEM, classes);
        
        classes = new ArrayList<>();
        map.put(Category.COMMON, classes);
        
        classes = new ArrayList<>();
//        classes.add(jmri.jmrix.loconet.logixng.AnalogActionLocoNet_OPC_PEER.class);
        map.put(Category.OTHER, classes);
        
        classes = new ArrayList<>();
        map.put(Category.EXRAVAGANZA, classes);
        
        Assert.assertTrue("maps are equal",
                isConnectionClassesEquals(map, femaleSocket.getConnectableClasses()));
    }
/*    
    @Test
    public void testCategory() {
        // Test that the classes method getCategory() returns the same value as
        // the factory.
        Map<Category, List<Class<? extends Base>>> map = femaleSocket.getConnectableClasses();
        
        for (Map.Entry<Category, List<Class<? extends Base>>> entry : map.entrySet()) {
            
            for (Class<? extends Base> clazz : entry.getValue()) {
                // The class SwingToolsTest does not have a swing configurator
                SwingConfiguratorInterface iface = SwingTools.getSwingConfiguratorForClass(clazz);
                iface.getConfigPanel();
                Base obj = iface.createNewObject(iface.getAutoSystemName(), null);
                Assert.assertEquals("category is correct", entry.getKey(), obj.getCategory());
            }
        }
    }
*/    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        
        flag = new AtomicBoolean();
        errorFlag = new AtomicBoolean();
        _memorySystemName = "IM1";
        _memory = InstanceManager.getDefault(MemoryManager.class).provide(_memorySystemName);
        _action = new MyAnalogActionMemory("IQAA321");
        _action.setMemory(_memory);
        AnalogActionBean otherAction = new AnalogActionMemory("IQAA322", null);
        maleSocket = new DefaultMaleAnalogActionSocket(_action);
        otherMaleSocket = new DefaultMaleAnalogActionSocket(otherAction);
        femaleSocket = new DefaultFemaleAnalogActionSocket(null, new FemaleSocketListener() {
            @Override
            public void connected(FemaleSocket socket) {
                flag.set(true);
            }

            @Override
            public void disconnected(FemaleSocket socket) {
                flag.set(true);
            }
        }, "A1");
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    
    
    private class MyAnalogActionMemory extends AnalogActionMemory {
        
        private boolean _hasBeenSetup = false;
        
        public MyAnalogActionMemory(String systemName) {
            super(systemName, null);
        }
        
        /** {@inheritDoc} */
        @Override
        public void setup() {
            _hasBeenSetup = true;
        }
    }
    
}
