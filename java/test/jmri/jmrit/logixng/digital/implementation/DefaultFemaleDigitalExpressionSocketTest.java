package jmri.jmrit.logixng.digital.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.digital.expressions.ExpressionTurnout;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test DefaultFemaleDigitalExpressionSocket
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultFemaleDigitalExpressionSocketTest extends FemaleSocketTestBase {

    private MyExpressionTurnout _expression;
    
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Override
    protected Manager<? extends NamedBean> getManager() {
        return InstanceManager.getDefault(DigitalExpressionManager.class);
    }
    
    @Test
    public void testGetName() {
        Assert.assertTrue("String matches", "E1".equals(_femaleSocket.getName()));
    }
    
    @Test
    public void testGetDescription() {
        Assert.assertTrue("String matches", "?".equals(_femaleSocket.getShortDescription()));
        Assert.assertTrue("String matches", "? E1".equals(_femaleSocket.getLongDescription()));
    }
    
    @Override
    protected FemaleSocket getFemaleSocket(String name) {
        return new DefaultFemaleDigitalExpressionSocket(null, new FemaleSocketListener() {
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
        return _expression._hasBeenSetup;
    }
    
    @Test
    public void testSetValue() throws Exception {
        // Every test method should have an assertion
        Assert.assertNotNull("femaleSocket is not null", _femaleSocket);
        Assert.assertFalse("femaleSocket is not connected", _femaleSocket.isConnected());
        // Test evaluate() when not connected
        Assert.assertFalse("result is false", ((DefaultFemaleDigitalExpressionSocket)_femaleSocket).evaluate());
        // Test evaluate() when connected
        _femaleSocket.connect(maleSocket);
        Turnout t = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        _expression.setTurnout(t);
        _expression.setTurnoutState(ExpressionTurnout.TurnoutState.THROWN);
        t.setState(Turnout.CLOSED);
        Assert.assertFalse("turnout is not thrown", ((DefaultFemaleDigitalExpressionSocket)_femaleSocket).evaluate());
        t.setState(Turnout.THROWN);
        Assert.assertTrue("turnout is thrown", ((DefaultFemaleDigitalExpressionSocket)_femaleSocket).evaluate());
    }
    
    @Test
    public void testReset() throws SocketAlreadyConnectedException {
        // Every test method should have an assertion
        Assert.assertNotNull("femaleSocket is not null", _femaleSocket);
        Assert.assertFalse("femaleSocket is not connected", _femaleSocket.isConnected());
        // Test reset() when not connected
        ((DefaultFemaleDigitalExpressionSocket)_femaleSocket).reset();
        // Test reset() when connected
        _femaleSocket.connect(maleSocket);
        ((DefaultFemaleDigitalExpressionSocket)_femaleSocket).reset();
    }
    
    @Test
    public void testGetConnectableClasses() {
        Map<Category, List<Class<? extends Base>>> map = new HashMap<>();
        
        List<Class<? extends Base>> classes = new ArrayList<>();
        classes.add(jmri.jmrit.logixng.digital.expressions.ExpressionClock.class);
        classes.add(jmri.jmrit.logixng.digital.expressions.ExpressionConditional.class);
        classes.add(jmri.jmrit.logixng.digital.expressions.ExpressionEntryExit.class);
        classes.add(jmri.jmrit.logixng.digital.expressions.ExpressionLight.class);
        classes.add(jmri.jmrit.logixng.digital.expressions.ExpressionLocalVariable.class);
        classes.add(jmri.jmrit.logixng.digital.expressions.ExpressionMemory.class);
        classes.add(jmri.jmrit.logixng.digital.expressions.ExpressionOBlock.class);
        classes.add(jmri.jmrit.logixng.digital.expressions.ExpressionReference.class);
        classes.add(jmri.jmrit.logixng.digital.expressions.ExpressionScript.class);
        classes.add(jmri.jmrit.logixng.digital.expressions.ExpressionSensor.class);
        classes.add(jmri.jmrit.logixng.digital.expressions.ExpressionSignalHead.class);
        classes.add(jmri.jmrit.logixng.digital.expressions.ExpressionSignalMast.class);
        classes.add(jmri.jmrit.logixng.digital.expressions.ExpressionTurnout.class);
        classes.add(jmri.jmrit.logixng.digital.expressions.ExpressionWarrant.class);
        map.put(Category.ITEM, classes);
        
        classes = new ArrayList<>();
        classes.add(jmri.jmrit.logixng.digital.expressions.And.class);
        classes.add(jmri.jmrit.logixng.digital.expressions.Antecedent.class);
        classes.add(jmri.jmrit.logixng.digital.expressions.Formula.class);
        classes.add(jmri.jmrit.logixng.digital.expressions.Or.class);
        classes.add(jmri.jmrit.logixng.digital.expressions.ExpressionTimer.class);
        map.put(Category.COMMON, classes);
        
        classes = new ArrayList<>();
        classes.add(jmri.jmrit.logixng.digital.expressions.False.class);
        classes.add(jmri.jmrit.logixng.digital.expressions.Hold.class);
        classes.add(jmri.jmrit.logixng.digital.expressions.ResetOnTrue.class);
        classes.add(jmri.jmrit.logixng.digital.expressions.TriggerOnce.class);
        classes.add(jmri.jmrit.logixng.digital.expressions.True.class);
        map.put(Category.OTHER, classes);
        
        classes = new ArrayList<>();
        map.put(Category.EXRAVAGANZA, classes);
        
        Assert.assertTrue("maps are equal",
                isConnectionClassesEquals(map, _femaleSocket.getConnectableClasses()));
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
        
        flag = new AtomicBoolean();
        errorFlag = new AtomicBoolean();
        _expression = new MyExpressionTurnout("IQDE321");
        ExpressionTurnout otherExpression = new ExpressionTurnout("IQDE322", null);
        manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        maleSocket = ((DigitalExpressionManager)manager).registerExpression(_expression);
        otherMaleSocket = ((DigitalExpressionManager)manager).registerExpression(otherExpression);
        _femaleSocket = new DefaultFemaleDigitalExpressionSocket(null, new FemaleSocketListener() {
            @Override
            public void connected(FemaleSocket socket) {
                flag.set(true);
            }

            @Override
            public void disconnected(FemaleSocket socket) {
                flag.set(true);
            }
        }, "E1");
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    
    
    private class MyExpressionTurnout extends ExpressionTurnout {
        
        private boolean _hasBeenSetup = false;
        
        public MyExpressionTurnout(String systemName) {
            super(systemName, null);
        }
        
        /** {@inheritDoc} */
        @Override
        public void setup() {
            _hasBeenSetup = true;
        }
    }
    
}
