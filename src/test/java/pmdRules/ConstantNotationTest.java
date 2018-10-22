package pmdRules;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;

public class ConstantNotationTest extends BaseXmlPmdRuleTest {

  private static String ruleType = "ConstantNotationRule"; // Rule type being tested

  @Test
  public void testRuleViolation() {

    String testFile = "ConstantsTest.pas";
    super.testAnalyse();
    ArrayList<ArrayList<Object>> fileResults = getFileResults(testFile);

    Integer[] expectedViolationLines = {9, 10, 11, 12, 13, 15};

    // Collect the relevant results
    ArrayList violationLines = super.getViolationLines(fileResults, ruleType);

    assertEquals(violationLines, Arrays.asList(expectedViolationLines));
  }

  @Test
  public void testNoViolation(){

    String testFile = "EnumTest.pas";
    super.testAnalyse();
    ArrayList<ArrayList<Object>> fileResults = getFileResults(testFile);

    ArrayList violationLines = super.getViolationLines(fileResults, ruleType);

    assertEquals(0, violationLines.size());
  }


}
