package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit tests for the LnNetworkPortController class.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LnNetworkPortControllerTest extends jmri.jmrix.AbstractNetworkPortControllerTestBase {

    private LocoNetSystemConnectionMemo memo;

    @Test
    @Override
    public void testGetAndSetOutputInterval() {
        ((LnNetworkPortController) apc).getSystemConnectionMemo().setOutputInterval(50);
        Assert.assertEquals("Output Interval after set", 50, ((LnNetworkPortController) apc).getSystemConnectionMemo().getOutputInterval());
    }

    @Override
    @Before
    public void setUp(){
       JUnitUtil.setUp();
       memo = new LocoNetSystemConnectionMemo();
       apc = new LnNetworkPortController(memo){
            @Override
            public void configure(){
            }
       };
    }

    @Override
    @After
    public void tearDown(){
       memo.dispose();
       JUnitUtil.tearDown();
    }

}
