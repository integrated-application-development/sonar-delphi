package pmdRules;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;

public class ConstantNotationTest extends BaseXmlPmdRuleTest {

  private static String testFile = "ConstantsTest.pas";
  private static String ruleType = "ConstantNotationRule"; // Rule type being tested

  @Test
  public void testRuleViolation() {

    super.testAnalyse(); // fixme, be better if didnt have to run this every test, just once
    ArrayList<ArrayList<Object>> fileResults = getFileResults(testFile);

    Integer[] expectedViolationLines = {9, 10, 11, 12, 13, 15};

    // Collect the relevant results
    ArrayList violationLines = super.getViolationLines(fileResults, ruleType);

    assertEquals(violationLines, Arrays.asList(expectedViolationLines));
  }


}
