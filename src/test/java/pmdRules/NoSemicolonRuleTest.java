package pmdRules;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class NoSemicolonRuleTest extends BaseXmlPmdRuleTest {

    private static String testFile = "NoSemicolonTest.pas";
    private static String ruleType = "NoSemicolonRule"; // Rule type being tested

    @Test
    public void testRuleViolation(){

        super.testAnalyse();
        ArrayList<ArrayList<Object>> fileResults = getFileResults(testFile);

        Integer[] expectedViolationLines = {32};

        // Collect the relevant results
        ArrayList violationLines = super.getViolationLines(fileResults, ruleType);

        assertEquals(violationLines, Arrays.asList(expectedViolationLines));
    }

  @Test
  public void testNoViolation(){

    String testFile = "MethodNameTest.pas";
    super.testAnalyse();
    ArrayList<ArrayList<Object>> fileResults = getFileResults(testFile);

    ArrayList violationLines = super.getViolationLines(fileResults, ruleType);

    assertEquals(0, violationLines.size());
  }


}
