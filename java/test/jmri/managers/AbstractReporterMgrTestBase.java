package jmri.managers;

import java.beans.PropertyChangeListener;

import jmri.JmriException;
import jmri.Reporter;
import jmri.ReporterManager;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Abstract Base Class for ReporterManager tests in specific jmrix packages. This
 * is not itself a test class, e.g. should not be added to a suite. Instead,
 * this forms the base for test classes, including providing some common tests.
 *
 * @author Bob Jacobsen 2003, 2006, 2008
 * @author Paul Bender Copyright (C) 2016
 */
public abstract class AbstractReporterMgrTestBase extends AbstractProvidingManagerTestBase<ReporterManager, Reporter> {

    /**
     * Max number of Reporters supported.  Override to return 1 if
     * only 1 can be created, for example
     */
    protected int maxN() { return 100; }

    // implementing classes must provide these abstract members:
    abstract public void setUp(); // load l with actual object; create scaffolds as needed, tag @BeforeEach

    abstract public String getSystemName(String i);

    protected boolean listenerResult = false;

    protected class Listen implements PropertyChangeListener {

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            listenerResult = true;
        }
    }

    // start of common tests
    // test creation - real work is in the setup() routine
    @Test
    public void testCreate() {
        Assert.assertNotNull(l);
    }

    @Test
    public void testDispose() {
        l.dispose();  // all we're really doing here is making sure the method exists
    }

    @Test
    public void testProvideName() {
        // Create
        Reporter t = l.provide("" + getNameToTest1());
        Assert.assertNotNull("real object returned ", t );
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNameToTest1())));
    }

    @Test
    public void testReporterProvideReporter() {
        // Create
        Reporter t = l.provideReporter("" + getNameToTest1());
        t.setUserName("Fred");
        // check
        Assert.assertNotNull("real object returned ", t);
        Assert.assertTrue("user name correct ", t == l.getByUserName("Fred"));
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNameToTest1())));

        // Check that "providing" an already-created reporter returns the same object.
        Reporter t2 = l.provideReporter(t.getSystemName());
        Assert.assertTrue("provided same object ", t == t2);
    }

    @Test
    public void testProvideFailure() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> l.provideReporter(""));
        jmri.util.JUnitAppender.assertErrorMessage("Invalid system name for Reporter: System name must start with \"" + l.getSystemNamePrefix() + "\".");
    }

    @Test
    public void testReporterGetBySystemName() {
        // Create
        Reporter t = l.provideReporter("" + getNameToTest1());
        t.setUserName("Fred");

        // Try a successful one
        t = l.getBySystemName(getSystemName(getNameToTest1()));
        Assert.assertTrue("get retrieved existing object ", t != null);

        // Try a nonexistant one. Should return null
        if (maxN()<2) {
            return; // only 1 Reporter supported by hardware type
        }
        t = l.getBySystemName(getSystemName(getNameToTest2()));
        Assert.assertTrue("get nonexistant object ", t == null);
    }

    @Test
    public void testReporterGetByUserName() {
        // Create
        Reporter t = l.provideReporter("" + getNameToTest1());
        t.setUserName("Fred");

        // Try a successful one
        t = l.getByUserName("Fred");
        Assert.assertTrue("get retrieved existing object ", t != null);

        // Try a nonexistant one. Should return null
        t = l.getBySystemName("Barney");
        Assert.assertTrue("get nonexistant object ", t == null);
    }

    @Test
    public void testReporterGetByDisplayName() {
        // Create
        Reporter t = l.provideReporter("" + getNameToTest1());
        t.setUserName("Fred");

        // Try a successful one
        t = l.getByDisplayName(getSystemName(getNameToTest1()));
        Assert.assertTrue("get retrieved existing object ", t != null);

        Reporter t2 = l.getByDisplayName("Fred");
        Assert.assertTrue("get retrieved existing object ", t2 == t);
    }

    @Test
    public void testReporterProvideByNumber() {
        listenerResult = false;
        l.addPropertyChangeListener(new Listen());
        // Create
        Reporter t = l.provideReporter("1");
        Assert.assertNotNull("provide by number", t);
        Assert.assertTrue(listenerResult);
    }

    @Test
    public void testDefaultSystemName() {
        listenerResult = false;
        l.addPropertyChangeListener(new Listen());
        // create
        Reporter t = l.provideReporter("" + getNameToTest1());
        // check
        Assert.assertNotNull("real object returned ", t );
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNameToTest1())));
        Assert.assertTrue(listenerResult);
    }

    @Test
    public void testSingleObject() {
        // test that you always get the same representation
        Reporter t1 = l.newReporter(getSystemName(getNameToTest1()), "mine");
        Assert.assertNotNull("t1 real object returned ", t1 );
        Assert.assertTrue("same by user ", t1 == l.getByUserName("mine"));
        Assert.assertTrue("same by system ", t1 == l.getBySystemName(getSystemName(getNameToTest1())));

        Reporter t2 = l.newReporter(getSystemName(getNameToTest1()), "mine");
        Assert.assertNotNull("t2 real object returned ", t2 );
        // check
        Assert.assertTrue("same new ", t1 == t2);
    }

    @Test
    public void testMisses() {
        // try to get nonexistant Reporters
        Assert.assertTrue(null == l.getByUserName("foo"));
        Assert.assertTrue(null == l.getBySystemName("bar"));
    }

    @Test
    public void testUpperLower() {
        Reporter t = l.provideReporter("" + getNameToTest1());
        String name = t.getSystemName();
        Assert.assertNull(l.getReporter(name.toLowerCase()));
    }

    @Test
    public void testRename() {
        // get reporter
        Reporter t1 = l.newReporter(getSystemName(getNameToTest1()), "before");
        Assert.assertNotNull("t1 real object ", t1);
        t1.setUserName("after");
        Reporter t2 = l.getByUserName("after");
        Assert.assertEquals("same object", t1, t2);
        Assert.assertEquals("no old object", null, l.getByUserName("before"));
    }

    @Disabled("Reporter managers doesn't support auto system names")
    @Test
    @Override
    public void testAutoSystemNames() {
    }

    @Test
    public void testGetEntryToolTip() {
        Assert.assertNotNull("getEntryToolTip not null", l.getEntryToolTip());
        Assert.assertTrue("Entry ToolTip Contains text",(l.getEntryToolTip().length()>5));
    }

    /**
     * Number of Reporter to test. Made a separate method so it can be overridden
     * in subclasses that do or don't support various numbers
     */
    protected String getNameToTest1() {
        return "1";
    }

    protected String getNameToTest2() {
        return "2";
    }

}
