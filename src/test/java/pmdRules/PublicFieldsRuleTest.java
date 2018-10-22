package pmdRules;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;

public class PublicFieldsRuleTest extends BaseXmlPmdRuleTest {

  private static String testFile = "PublicFieldsTest.pas";
  private static String ruleType = "PublicFieldsRule"; // Rule type being tested

  @Test
  public void testRuleViolation() {

    super.testAnalyse();
    ArrayList<ArrayList<Object>> fileResults = getFileResults(testFile);

    Integer[] expectedViolationLines = {39};

    // Collect the relevant results
    ArrayList violationLines = super.getViolationLines(fileResults, ruleType);

    assertEquals(violationLines, Arrays.asList(expectedViolationLines));
  }

}

