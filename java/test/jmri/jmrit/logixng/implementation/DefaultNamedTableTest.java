package jmri.jmrit.logixng.implementation;

import java.io.File;
import java.io.IOException;
import jmri.jmrit.logixng.NamedTable;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test DefaultLogixNG
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultNamedTableTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", new DefaultNamedTable("IQT10", "A table", 10, 15));
    }
    
    @Test
    public void testCSVFile() throws IOException {
        NamedTable table = DefaultNamedTable.loadTableFromCSV_File(
                new File("java/test/jmri/jmrit/logixng/panel_and_data_files/turnout_and_signals.csv"));
        
        FileUtil.createDirectory(FileUtil.getUserFilesPath() + "temp");
        File file = new File(FileUtil.getUserFilesPath() + "temp/" + "turnout_and_signals.csv");
        System.out.format("Temporary file: %s%n", file.getAbsoluteFile());
        table.storeTableAsCSV(file);
        Assert.assertNotNull("exists", table);
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
