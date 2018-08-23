package pmdRules;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class NoFunctionReturnTypeRuleTest extends BaseXmlPmdRuleTest {

    private static String testFile = "NoReturnTest.pas";
    private static String ruleType = "NoFunctionReturnTypeRule";// This is implemented as an XPath expression rule, which is defined in rules.xml
                                                                // Rule type being tested

    @Test
    public void testRuleViolation(){

        super.analyse(); // fixme, be better if didnt have to run this every test, just once
        ArrayList<ArrayList<Object>> fileResults = getFileResults(testFile);

        Integer[] expectedViolationLines = {21};

        // Collect the relevant results
        ArrayList violationLines = super.getViolationLines(fileResults, ruleType);

        assertEquals(violationLines, Arrays.asList(expectedViolationLines));
    }
}
