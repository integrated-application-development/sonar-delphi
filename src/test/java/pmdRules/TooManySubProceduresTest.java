package pmdRules;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;

public class TooManySubProceduresTest extends BaseXmlPmdRuleTest {

  private static String testFile = "TooManySubProceduresTest.pas";
  private static String ruleType = "TooManySubProceduresRule"; // Rule type being tested

  @Test
  public void testRuleViolation() {

    super.testAnalyse();
    ArrayList<ArrayList<Object>> fileResults = getFileResults(testFile);

    Integer[] expectedViolationLines = {19};

    // Collect the relevant results
    ArrayList violationLines = super.getViolationLines(fileResults, ruleType);

    assertEquals(violationLines, Arrays.asList(expectedViolationLines));
  }

}
