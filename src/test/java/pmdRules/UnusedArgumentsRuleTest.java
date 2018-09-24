package pmdRules;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class UnusedArgumentsRuleTest extends BaseXmlPmdRuleTest {

    private static String testFile = "UnusedArgumentsTest.pas";
    private static String ruleType = "UnusedArgumentsRule"; // Rule type being tested

    @Test
    public void testRuleViolation(){

        super.testAnalyse();
        ArrayList<ArrayList<Object>> fileResults = getFileResults(testFile);

        Integer[] expectedViolationLines = {30};

        // Collect the relevant results
        ArrayList violationLines = super.getViolationLines(fileResults, ruleType);

        assertEquals(violationLines, Arrays.asList(expectedViolationLines));
    }

}
