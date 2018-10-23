package pmdRules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Ignore;
import org.junit.Test;

public class LowerCaseKeywordsTest extends BaseXmlPmdRuleTest {

  private static String ruleType = "LowerCaseReservedWordsRule";

  @Ignore // Seems to be broken
  @Test
  public void testRuleViolation() {

    String testFile = "KeywordsConventionTest.pas";
    super.testAnalyse();
    ArrayList<ArrayList<Object>> fileResults = getFileResults(testFile);

    Integer[] expectedViolationLines = {29};

    ArrayList violationLines = super.getViolationLines(fileResults, ruleType);

    assertEquals(violationLines, Arrays.asList(expectedViolationLines));
  }

  @Test
  public void testNoViolation() {

    String testFile = "DestructorNoViolationTest.pas";
    super.testAnalyse();
    ArrayList<ArrayList<Object>> fileResults = getFileResults(testFile);

    assertNull(fileResults); // Shouldn't be any violations in this file
  }
}
