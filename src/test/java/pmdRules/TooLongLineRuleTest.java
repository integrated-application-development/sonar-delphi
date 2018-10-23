package pmdRules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Ignore;
import org.junit.Test;

public class TooLongLineRuleTest extends BaseXmlPmdRuleTest {

  private static String ruleType = "TooLongLineRule";

  @Ignore
  @Test
  public void testRuleViolation() {

    String testFile = "TooLongLineTest.pas";
    super.testAnalyse();
    ArrayList<ArrayList<Object>> fileResults = getFileResults(testFile);

    Integer[] expectedViolationLines = {19, 22};

    ArrayList violationLines = super.getViolationLines(fileResults, ruleType);

    assertEquals(violationLines, Arrays.asList(expectedViolationLines));
  }

  @Ignore
  @Test
  public void testNoViolation() {

    String testFile = "NoReturnTest.pas";
    super.testAnalyse();
    ArrayList<ArrayList<Object>> fileResults = getFileResults(testFile);

    assertNull(fileResults); // Shouldn't be any violations in this file
  }
}
