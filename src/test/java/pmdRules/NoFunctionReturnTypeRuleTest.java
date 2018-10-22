package pmdRules;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;

public class NoFunctionReturnTypeRuleTest extends BaseXmlPmdRuleTest {

  private static String testFile = "NoReturnTest.pas";
  private static String ruleType = "NoFunctionReturnTypeRule";// This is implemented as an XPath expression rule, which is defined in rules.xml
  // Rule type being tested

  @Test
  public void testRuleViolation() {

    super.testAnalyse();
    ArrayList<ArrayList<Object>> fileResults = getFileResults(testFile);

    Integer[] expectedViolationLines = {21};

    // Collect the relevant results
    ArrayList violationLines = super.getViolationLines(fileResults, ruleType);

    assertEquals(violationLines, Arrays.asList(expectedViolationLines));
  }

  @Test
  public void testNoViolation(){

    String testFile = "EmptyBracketsTest.pas";
    super.testAnalyse();
    ArrayList<ArrayList<Object>> fileResults = getFileResults(testFile);

    ArrayList violationLines = super.getViolationLines(fileResults, ruleType);

    assertEquals(0, violationLines.size());
  }

}
