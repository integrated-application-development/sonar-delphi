package pmdRules;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class PointerNameRuleTest extends BaseXmlPmdRuleTest {

    private static String testFile = "PointerNameTest.pas";
    private static String ruleType = "PointerNameRule"; // Rule type being tested

    @Test
    public void testRuleViolation(){

        super.testAnalyse();
        ArrayList<ArrayList<Object>> fileResults = getFileResults(testFile);

        Integer[] expectedViolationLines = {17};

        // Collect the relevant results
        ArrayList violationLines = super.getViolationLines(fileResults, ruleType);

        assertEquals(violationLines, Arrays.asList(expectedViolationLines));
    }

}
