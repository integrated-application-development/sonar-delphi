package pmdRules;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;

public class UnusedArgumentsRuleTest extends BaseXmlPmdRuleTest {

  private static String ruleType = "UnusedArgumentsRule"; // Rule type being tested

  @Test
  public void testRuleViolation() {

    String testFile = "UnusedArgumentsTest.pas";
    super.testAnalyse();
    ArrayList<ArrayList<Object>> fileResults = getFileResults(testFile);

    Integer[] expectedViolationLines = {30};

    // Collect the relevant results
    ArrayList violationLines = super.getViolationLines(fileResults, ruleType);

    assertEquals(violationLines, Arrays.asList(expectedViolationLines));
  }

}
