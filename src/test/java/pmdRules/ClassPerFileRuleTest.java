package pmdRules;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class ClassPerFileRuleTest extends BaseXmlPmdRuleTest {

    private static String testFile = "ClassPerFileTest.pas";
    private static String ruleType = "OneClassPerFileRule"; // Rule type being tested
    // Note this rule corresponds to the rule defined in ClassPerFileRule.java (file name does not
    // match exactly, missing leading "One")

    @Test
    public void testRuleViolation(){

        super.analyse();
        ArrayList<ArrayList<Object>> fileResults = getFileResults(testFile);

        Integer[] expectedViolationLines = {15, 21};

        // Collect the relevant results
        ArrayList violationLines = super.getViolationLines(fileResults, ruleType);

        assertEquals(violationLines, Arrays.asList(expectedViolationLines));
    }

}
