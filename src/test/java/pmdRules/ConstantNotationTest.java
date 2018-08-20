package pmdRules;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class ConstantNotationTest extends BaseXmlPmdRuleTest {

    private static String testFile = "ConstantsTest.pas";
    private static String ruleType = "ConstantNotationRule"; // Rule type being tested

    @Test
    public void testRuleViolation(){

        super.analyse(); // fixme, be better if didnt have to run this every test, just once
        ArrayList<ArrayList<Object>> fileResults = getFileResults(testFile);
        ArrayList<Integer> violationLines = new ArrayList<Integer>();

        Integer[] expectedViolationLines = {9, 10, 11, 12, 13, 15};

        // Collect the relevant results
        for(ArrayList violation : fileResults){
            if (violation.get(RULETYPE_POS).equals(ruleType)) {
                violationLines.add((Integer) violation.get(VIOLATION_LINE_POS));
            }
        }

        assertEquals(violationLines, Arrays.asList(expectedViolationLines));
    }


}
