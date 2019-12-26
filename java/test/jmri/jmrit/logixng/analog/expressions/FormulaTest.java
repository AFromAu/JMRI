package jmri.jmrit.logixng.analog.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.Turnout;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.AnalogActionManager;
import jmri.jmrit.logixng.AnalogExpressionManager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.FemaleAnalogExpressionSocket;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.analog.expressions.Formula.ExpressionEntry;
import jmri.jmrit.logixng.digital.actions.DoAnalogAction;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrit.logixng.AnalogExpressionBean;
import jmri.jmrit.logixng.analog.actions.AnalogActionMemory;

/**
 * Test And
 * 
 * @author Daniel Bergqvist 2018
 */
public class FormulaTest extends AbstractAnalogExpressionTestBase {

//    private static final boolean EXPECT_SUCCESS = true;
//    private static final boolean EXPECT_FAILURE = false;

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private Formula expressionFormula;
    private AnalogActionMemory analogActionMemory;
    private Memory analogMemory;
    
    
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
        AnalogExpressionBean childExpression = new AnalogExpressionConstant("IQAE999", null);
        MaleSocket maleSocketChild =
                InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(childExpression);
        return maleSocketChild;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "Formula: E1%n" +
                "   ? E1%n" +
                "      Get constant value 1%n" +
                "   ? E2%n" +
                "      Socket not connected%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         Read analog E and set analog A%n" +
                "            ?~ E%n" +
                "               Formula: E1%n" +
                "                  ? E1%n" +
                "                     Get constant value 1%n" +
                "                  ? E2%n" +
                "                     Socket not connected%n" +
                "            !~ A%n" +
                "               Set memory IM2%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) throws Exception {
        Formula a = new Formula(systemName, null);
//        a.setFormula("R1");
        return a;
    }
    
    @Test
    public void testCtor() throws Exception {
        Formula expression2;
        
        expression2 = new Formula("IQAE321", null);
//        expression2.setFormula("R1");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
//        Assert.assertEquals("String matches", "Formula: R1", expression2.getLongDescription());
        
        expression2 = new Formula("IQAE321", "My expression");
//        expression2.setFormula("R1");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My expression", expression2.getUserName());
//        Assert.assertEquals("String matches", "Formula: R1", expression2.getLongDescription());
        
        expression2 = new Formula("IQAE321", null);
//        expression2.setFormula("R1 and R2");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
//        Assert.assertEquals("String matches", "Formula: R1 and R2", expression2.getLongDescription());
        
        expression2 = new Formula("IQAE321", "My expression");
//        expression2.setFormula("R1 or R2");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My expression", expression2.getUserName());
//        Assert.assertEquals("String matches", "Formula: R1 or R2", expression2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new Formula("IQE55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new Formula("IQE55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    @Test
    public void testGetChild() throws Exception {
        Formula expression2 = new Formula("IQAE321", null);
//        expression2.setFormula("R1");
        
        for (int i=0; i < 3; i++) {
            Assert.assertTrue("getChildCount() returns "+i, i+1 == expression2.getChildCount());

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
            AnalogExpressionConstant expr = new AnalogExpressionConstant("IQAE"+i, null);
            MaleSocket maleSocket =
                    InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(expr);
            expression2.getChild(i).connect(maleSocket);
        }
    }
    
    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.COMMON == _base.getCategory());
    }
    
    @Test
    public void testIsExternal() {
        Assert.assertFalse("is external", _base.isExternal());
    }
    
    @Test
    public void testDescription() {
        Formula e1 = new Formula("IQAE321", null);
        Assert.assertEquals("strings matches", "Formula", e1.getShortDescription());
        Assert.assertEquals("strings matches", "Formula: empty", e1.getLongDescription());
    }
/*    
    private void testValidate(boolean expectedResult, String formula, List<AnalogExpressionBean> conditionalVariablesList) throws Exception {
        Formula ix1 = new Formula("IQDE321", "IXIC 1");
        ix1.setFormula("R1");
        
        int count = 0;
        List<ExpressionEntry> expressionEntryList = new ArrayList<>();
        for (AnalogExpressionBean expressionFormula : conditionalVariablesList) {
            String socketName = "E"+Integer.toString(count++);
            FemaleAnalogExpressionSocket socket =
                    InstanceManager.getDefault(AnalogExpressionManager.class)
                            .createFemaleSocket(null, this, socketName);
            socket.connect((MaleSocket) expressionFormula);
            expressionEntryList.add(new ExpressionEntry(socket, socketName));
        }
        
        if (expectedResult) {
            Assert.assertTrue("validateFormula() returns null for '"+formula+"'",
                    ix1.validateFormula(formula, expressionEntryList) == null);
        } else {
            Assert.assertTrue("validateFormula() returns error message for '"+formula+"'",
                    ix1.validateFormula(formula, expressionEntryList) != null);
        }
    }
    
    private void testCalculate(int expectedResult, String formula,
            List<AnalogExpressionBean> conditionalVariablesList, String errorMessage)
            throws Exception {
        
        Formula ix1 = new Formula("IQDE321", "IXIC 1");
        ix1.setFormula(formula);
        
//        for (int i=0; i < ix1.getChildCount(); i++) {
//            ix1.getChild(i).disconnect();
//        }
        
        ix1.setChildCount(conditionalVariablesList.size());
        
        for (int i=0; i < conditionalVariablesList.size(); i++) {
            ix1.getChild(i).connect((MaleSocket)conditionalVariablesList.get(i));
        }
        
        switch (expectedResult) {
            case Formula.FALSE:
                Assert.assertFalse("validateFormula() returns FALSE for '"+formula+"'",
                        ix1.evaluate());
                break;
                
            case Formula.TRUE:
//                System.err.format("formula: %s%n", formula);
//                System.err.format("variable: %b%n", conditionalVariablesList.get(0).evaluate(isCompleted));
                Assert.assertTrue("validateFormula() returns TRUE for '"+formula+"'",
                        ix1.evaluate());
                break;
                
            default:
                throw new RuntimeException(String.format("Unknown expected result: %d", expectedResult));
        }
        
        if (! errorMessage.isEmpty()) {
            jmri.util.JUnitAppender.assertErrorMessageStartsWith(errorMessage);
        }
    }
    
    @Test
    public void testValidate() throws Exception {
        AnalogExpressionBean[] conditionalVariables_Empty = { };
        List<AnalogExpressionBean> conditionalVariablesList_Empty = Arrays.asList(conditionalVariables_Empty);
        
        AnalogExpressionBean trueExpression =
                InstanceManager.getDefault(
                        AnalogExpressionManager.class).registerExpression(
                                new True(InstanceManager.getDefault(AnalogExpressionManager.class).getAutoSystemName(), null));
//        AnalogExpressionBean falseExpression = InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(new False(conditionalNG));
        
        AnalogExpressionBean[] conditionalVariables_True
                = { trueExpression };
        List<AnalogExpressionBean> conditionalVariablesList_True = Arrays.asList(conditionalVariables_True);
        
        AnalogExpressionBean[] conditionalVariables_TrueTrueTrue
                = { trueExpression
                        , trueExpression
                        , trueExpression };
        List<AnalogExpressionBean> conditionalVariablesList_TrueTrueTrue = Arrays.asList(conditionalVariables_TrueTrueTrue);
        
        // Test empty formula string
        testValidate(EXPECT_FAILURE, "", conditionalVariablesList_Empty);
        
        testValidate(EXPECT_SUCCESS, "R1", conditionalVariablesList_True);
        testValidate(EXPECT_FAILURE, "R2", conditionalVariablesList_True);
        
        // Test parentheses
        testValidate(EXPECT_SUCCESS, "([{R1)}]", conditionalVariablesList_True);
        testValidate(EXPECT_FAILURE, "(R2", conditionalVariablesList_True);
        testValidate(EXPECT_FAILURE, "R2)", conditionalVariablesList_True);
        
        // Test several items
        testValidate(EXPECT_FAILURE, "R1 and R2 and R3", conditionalVariablesList_True);
        testValidate(EXPECT_FAILURE, "R1", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_SUCCESS, "R1 and R2 and R3", conditionalVariablesList_TrueTrueTrue);
        
        // Test uppercase and lowercase
        testValidate(EXPECT_SUCCESS, "R2 AND R1 or R3", conditionalVariablesList_TrueTrueTrue);
        
        // Test several items and parenthese
        testValidate(EXPECT_SUCCESS, "(R1 and R3) and not R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "(R1 and) R3 and not R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "R1( and R3) and not R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "R1 (and R3 and) not R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "(R1 and R3) and not R2)", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_SUCCESS, "(R1 and (R3) and not R2)", conditionalVariablesList_TrueTrueTrue);
        
        // Test invalid combinations
        testValidate(EXPECT_FAILURE, "R1 and or R3 and R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "R1 or or R3 and R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "R1 or and R3 and R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "R1 not R3 and R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "and R1 not R3 and R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "R1 or R3 and R2 or", conditionalVariablesList_TrueTrueTrue);
    }
    
    @Test
    @SuppressWarnings("unused") // test building in progress
    public void testCalculate() throws Exception {
        AnalogExpressionBean[] conditionalVariables_Empty = { };
        List<AnalogExpressionBean> conditionalVariablesList_Empty = Arrays.asList(conditionalVariables_Empty);
        
        AnalogExpressionBean trueExpression =
                InstanceManager.getDefault(
                        AnalogExpressionManager.class).registerExpression(
                                new True(InstanceManager.getDefault(AnalogExpressionManager.class).getAutoSystemName(), null));
        AnalogExpressionBean falseExpression =
                InstanceManager.getDefault(
                        AnalogExpressionManager.class).registerExpression(
                                new False(InstanceManager.getDefault(AnalogExpressionManager.class).getAutoSystemName(), null));
        
        AnalogExpressionBean[] conditionalVariables_True
                = { trueExpression };
        List<AnalogExpressionBean> conditionalVariablesList_True = Arrays.asList(conditionalVariables_True);
        
        AnalogExpressionBean[] conditionalVariables_False
                = { falseExpression };
        List<AnalogExpressionBean> conditionalVariablesList_False = Arrays.asList(conditionalVariables_False);
        
        AnalogExpressionBean[] conditionalVariables_TrueTrueTrue
                = { trueExpression
                        , trueExpression
                        , trueExpression };
        List<AnalogExpressionBean> conditionalVariablesList_TrueTrueTrue = Arrays.asList(conditionalVariables_TrueTrueTrue);
        
        // Test with two digit variable numbers
        AnalogExpressionBean[] conditionalVariables_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse
                = {trueExpression
                        , trueExpression
                        , falseExpression
                        , trueExpression
                        , trueExpression
                        , falseExpression
                        , trueExpression
                        , trueExpression
                        , falseExpression
                        , trueExpression
                        , trueExpression
                        , falseExpression };
        List<AnalogExpressionBean> conditionalVariablesList_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse =
                Arrays.asList(conditionalVariables_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse);
        
        
        // Test empty formula string
        testCalculate(Formula.FALSE, "", conditionalVariablesList_Empty, "");
//        testCalculate(Formula.FALSE, "", conditionalVariablesList_True,
//                "IXIC 1 parseCalculation error formula= , ex= java.lang.StringIndexOutOfBoundsException");
        testCalculate(Formula.FALSE, "", conditionalVariablesList_True, "");
        
        // Test illegal number
        testCalculate(Formula.FALSE, "R#", conditionalVariablesList_True,
                "IXIC 1 parseCalculation error formula= R#, ex= java.lang.NumberFormatException");
        testCalculate(Formula.FALSE, "R-", conditionalVariablesList_True,
                "IXIC 1 parseCalculation error formula= R-, ex= java.lang.NumberFormatException");
        testCalculate(Formula.FALSE, "Ra", conditionalVariablesList_True,
                "IXIC 1 parseCalculation error formula= Ra, ex= java.lang.NumberFormatException");
        
        // Test single condition
        testCalculate(Formula.TRUE, "R1", conditionalVariablesList_True, "");
        testCalculate(Formula.FALSE, "R1", conditionalVariablesList_False, "");
        testCalculate(Formula.FALSE, "not R1", conditionalVariablesList_True, "");
        testCalculate(Formula.TRUE, "not R1", conditionalVariablesList_False, "");
        
        // Test single item but wrong item (R2 instead of R1)
//        testCalculate(Formula.FALSE, "R2)", conditionalVariablesList_True,
//                "IXIC 1 parseCalculation error formula= R2), ex= java.lang.ArrayIndexOutOfBoundsException");
        
        // Test two digit variable numbers
        testCalculate(Formula.TRUE, "R3 and R12 or R5 and R10",
                conditionalVariablesList_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse, "");
        testCalculate(Formula.FALSE, "R3 and (R12 or R5) and R10",
                conditionalVariablesList_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse, "");
        testCalculate(Formula.FALSE, "R12 and R10",
                conditionalVariablesList_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse, "");
        testCalculate(Formula.TRUE, "R12 or R10",
                conditionalVariablesList_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse, "");
        testCalculate(Formula.FALSE, "not (R12 or R10)",
                conditionalVariablesList_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse, "");
        
        // Test parentheses
        testCalculate(Formula.TRUE, "([{R1)}]", conditionalVariablesList_True, "");
//        testCalculate(Formula.FALSE, "(R2", conditionalVariablesList_True,
//                "IXIC 1 parseCalculation error formula= (R2, ex= java.lang.ArrayIndexOutOfBoundsException");
        
        // Test several items
        testCalculate(Formula.FALSE, "R1 and R2 and R3", conditionalVariablesList_True,
                "IXIC 1 parseCalculation error formula= R1 and R2 and R3, ex= java.lang.IndexOutOfBoundsException");
        testCalculate(Formula.TRUE, "R1", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Formula.TRUE, "R2", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Formula.TRUE, "R3", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Formula.TRUE, "R1 and R2 and R3", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Formula.TRUE, "R2 AND R1 or R3", conditionalVariablesList_TrueTrueTrue, "");
        
        // Test invalid combinations of and, or, not
        testCalculate(Formula.FALSE, "R1 and or R3 and R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error formula= R1 and or R3 and R2, ex= jmri.JmriException: Unexpected operator or characters < ORR3ANDR2 >");
        testCalculate(Formula.FALSE, "R1 or or R3 and R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error formula= R1 or or R3 and R2, ex= jmri.JmriException: Unexpected operator or characters < ORR3ANDR2 >");
        testCalculate(Formula.FALSE, "R1 or and R3 and R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error formula= R1 or and R3 and R2, ex= jmri.JmriException: Unexpected operator or characters < ANDR3ANDR2 >");
        testCalculate(Formula.FALSE, "R1 not R3 and R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error formula= R1 not R3 and R2, ex= jmri.JmriException: Could not find expected operator < NOTR3ANDR2 >");
        testCalculate(Formula.FALSE, "and R1 not R3 and R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error formula= and R1 not R3 and R2, ex= jmri.JmriException: Unexpected operator or characters < ANDR1NOTR3ANDR2 >");
        testCalculate(Formula.FALSE, "R1 or R3 and R2 or", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error formula= R1 or R3 and R2 or, ex= java.lang.StringIndexOutOfBoundsException");
        
        // Test several items and parenthese
        testCalculate(Formula.TRUE, "(R1 and R3) and R2", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Formula.FALSE, "(R1 and R3) and not R2", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Formula.FALSE, "(R1 and) R3 and not R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error formula= (R1 and) R3 and not R2, ex= jmri.JmriException: Unexpected operator or characters < )R3ANDNOTR2 >");
        testCalculate(Formula.FALSE, "R1( and R3) and not R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error formula= R1( and R3) and not R2, ex= jmri.JmriException: Could not find expected operator < (ANDR3)ANDNOTR2 >");
        testCalculate(Formula.FALSE, "R1 (and R3 and) not R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error formula= R1 (and R3 and) not R2, ex= jmri.JmriException: Could not find expected operator < (ANDR3AND)NOTR2 >");
        testCalculate(Formula.FALSE, "(R1 and R3) and not R2)", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Formula.TRUE, "(R1 and (R3) and R2)", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Formula.FALSE, "(R1 and (R3) and not R2)", conditionalVariablesList_TrueTrueTrue, "");
    }
*/    
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
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
        conditionalNG.setEnabled(true);
        logixNG.addConditionalNG(conditionalNG);
        DoAnalogAction doAnalogAction = new DoAnalogAction("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(doAnalogAction);
        conditionalNG.getChild(0).connect(maleSocket);
        
        expressionFormula = new Formula("IQAE321", null);
//        expressionFormula.setFormula("1");
//        expressionFormula.setFormula("true");
        expressionFormula.setFormula("E1");
        MaleSocket maleSocketExpressionFormula =
                InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(expressionFormula);
        doAnalogAction.getChild(0).connect(maleSocketExpressionFormula);
        
        AnalogExpressionConstant childExpression = new AnalogExpressionConstant("IQAE322", null);
        childExpression.setValue(1.0);
        MaleSocket maleSocketChild =
                InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(childExpression);
        maleSocketExpressionFormula.getChild(0).connect(maleSocketChild);
        
        _base = expressionFormula;
        _baseMaleSocket = maleSocketExpressionFormula;
        
        analogMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM2");
        analogActionMemory = new AnalogActionMemory("IQAA1", null);
        analogActionMemory.setMemory(analogMemory);
        MaleSocket socketAnalogActionMemory = InstanceManager.getDefault(AnalogActionManager.class).registerAction(analogActionMemory);
        doAnalogAction.getChild(1).connect(socketAnalogActionMemory);
        
	logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
        logixNG.activateLogixNG();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
