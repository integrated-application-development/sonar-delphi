package pmdRules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;

public class MethodTooLongTest extends BaseXmlPmdRuleTest {

  private static String testFile = "MethodTooLongTest.pas";
  private static String ruleType = "TooLongMethodRule"; // Rule type being tested

  @Test
  public void testRuleViolation(){

    super.testAnalyse();
    ArrayList<ArrayList<Object>> fileResults = getFileResults(testFile);

    Integer[] expectedViolationLines = {8};

    // Collect the relevant results
    ArrayList violationLines = super.getViolationLines(fileResults, ruleType);

    assertEquals(violationLines, Arrays.asList(expectedViolationLines));
  }

  @Test
  public void testNoViolation(){
    testFile = "IfTrueTest.pas";
    super.testAnalyse();
    ArrayList<ArrayList<Object>> fileResults = getFileResults(testFile);

    ArrayList violationLines = super.getViolationLines(fileResults, ruleType);

    assertEquals(0, violationLines.size()); // Should be no results of MethodTooLong type

  }

}
