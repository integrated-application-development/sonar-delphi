package pmdRules;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;
import org.sonar.plugins.delphi.pmd.BasePmdRuleTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ConstructorCreateRuleTest extends BaseXmlPmdRuleTest {

    private static String testFile = "ConstructorTest.pas";
    private static String ruleType = "ConstructorCreateRule"; // Rule type being tested

    @Test
    public void testRuleViolation(){

        super.analyse(); // fixme, be better if didnt have to run this every test, just once
        ArrayList<ArrayList<Object>> fileResults = getFileResults(testFile);
        ArrayList<Integer> violationLines = new ArrayList<Integer>();

        Integer[] expectedViolationLines = {20, 21};

        // Collect the relevant results
        for(ArrayList violation : fileResults){
            if (violation.get(RULETYPE_POS).equals(ruleType)) {
                violationLines.add((Integer) violation.get(VIOLATION_LINE_POS));
            }
        }

        assertEquals(violationLines, Arrays.asList(expectedViolationLines));
    }

}
