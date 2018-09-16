package pmdRules;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class EmptyBracketsRuleTest extends BaseXmlPmdRuleTest {

    private static String testFile = "EmptyBracketsTest.pas";
    private static String ruleType = "EmptyBracketsRule"; // Rule type being tested

    @Test
    public void testRuleViolation(){

        super.analyse();
        ArrayList<ArrayList<Object>> fileResults = getFileResults(testFile);

        Integer[] expectedViolationLines = {10};

        // Collect the relevant results
        ArrayList violationLines = super.getViolationLines(fileResults, ruleType);

        assertEquals(violationLines, Arrays.asList(expectedViolationLines));
    }

}
