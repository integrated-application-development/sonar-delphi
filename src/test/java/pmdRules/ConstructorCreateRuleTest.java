package pmdRules;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;

public class ConstructorCreateRuleTest extends BaseXmlPmdRuleTest {

  private static String testFile = "ConstructorTest.pas";
  private static String ruleType = "ConstructorCreateRule"; // Rule type being tested

  @Test
  public void testRuleViolation() {

    super.testAnalyse(); // fixme, be better if didnt have to run this every testDefinitionsIncludes, just once
    ArrayList<ArrayList<Object>> fileResults = getFileResults(testFile);

    Integer[] expectedViolationLines = {20, 21};

    // Collect the relevant results
    ArrayList violationLines = super.getViolationLines(fileResults, ruleType);

    assertEquals(violationLines, Arrays.asList(expectedViolationLines));
  }

}
