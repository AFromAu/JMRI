package jmri.jmrix.can.cbus.swing.nodeconfig;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConfigurationManager;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Test simple functioning of CbusNodeRestoreFcuFrame
 *
 * @author Paul Bender Copyright (C) 2016, 2019
 * @author Steve Young Copyright (C) 2019
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class CbusNodeRestoreFcuFrameTest extends jmri.util.JmriJFrameTestBase {
    
    @Test
    public void testInit() {
        Assertions.assertNotNull(memo);
        ((CbusPreferences)memo.get(CbusPreferences.class)).setNodeBackgroundFetchDelay(0);
        CbusNodeTableDataModel nodeModel = ((CbusConfigurationManager)memo.get(CbusConfigurationManager.class))
            .provide(CbusNodeTableDataModel.class);
        
        NodeConfigToolPane mainpane = new NodeConfigToolPane();
        mainpane.initComponents(memo);
        Assert.assertNotNull("menu items initialised so restore frame can disable",mainpane.getMenus());
        
        CbusNodeRestoreFcuFrame t = new CbusNodeRestoreFcuFrame(mainpane);
        Assert.assertNotNull("exists",t);
        
        t.initComponents(memo);
        
        Assert.assertEquals("title",Bundle.getMessage("FcuImportTitle"),t.getTitle());
        
        JFrameOperator jfo = new JFrameOperator( t.getTitle() );
        
        Assert.assertFalse("can find button so frame has opened ok",getTeachNodeButtonEnabled(jfo));
        
        // Ask to close window
        jfo.requestClose();
        
        
        nodeModel.dispose();
        mainpane.dispose();
        
    }
    
    private boolean getTeachNodeButtonEnabled( JFrameOperator jfo ){
        return ( new JButtonOperator(jfo,Bundle.getMessage("UpdateNodeButton")).isEnabled() );
    }
    
    private CanSystemConnectionMemo memo = null;


    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();

        memo = new CanSystemConnectionMemo();
        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.MERGCBUS);
        frame = new CbusNodeRestoreFcuFrame(null);

    }

    @AfterEach
    @Override
    public void tearDown() {
        Assertions.assertNotNull(memo);
        memo.dispose();
        memo = null;
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeRestoreFcuFrameTest.class);

}
