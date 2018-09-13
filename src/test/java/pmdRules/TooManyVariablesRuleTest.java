package pmdRules;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class TooManyVariablesRuleTest extends BaseXmlPmdRuleTest {

    private static String testFile = "TooManyVariablesTest.pas";
    private static String ruleType = "TooManyVariablesRule"; // Rule type being tested

    @Test
    public void testRuleViolation(){

        super.testAnalyse(); // fixme, be better if didnt have to run this every testDefinitionsIncludes, just once
        ArrayList<ArrayList<Object>> fileResults = getFileResults(testFile);

        Integer[] expectedViolationLines = {6};

        // Collect the relevant results
        ArrayList violationLines = super.getViolationLines(fileResults, ruleType);

        assertEquals(violationLines, Arrays.asList(expectedViolationLines));
    }

}

