package pmdRules;

import java.util.ArrayList;
import java.util.HashMap;
import org.junit.Test;

/**
 * Base class for a rule parsing for PMD results from the pmd-report.xml and testing results
 * in the Junit tests
 */

public class BaseXmlPmdRuleTest {

  private PMDReportXMLParser pmdParser = new PMDReportXMLParser();
  private HashMap<String, ArrayList<ArrayList<Object>>> violationsMap = new HashMap<>();

  // Positions in arraylist results to use for getting results
  int RULETYPE_POS = 0;
  int VIOLATION_LINE_POS = 1;

  /**
   * Creates the hashmap of violations by parsing the XML results of pmd-report.xml
   */
  @Test
  public void testAnalyse() {
    pmdParser.parsePmdReportXML();
    violationsMap = pmdParser.violationsMap;
  }

  ArrayList<ArrayList<Object>> getFileResults(String testFilename) {

    if (violationsMap.containsKey(testFilename)) {
      return violationsMap.get(testFilename);
    }

    return null;

  }

  /**
   * Returns the results of only the rule that is being tested, and returns a list of the lines
   * that violation was found on.
   * @param fileResults The list of results from parsing the XML file, but all results.
   * @param ruleType The rule that only violations of that type will be returned.
   * @return The list of line numbers where violations of the passes rule type are located.
   */
  ArrayList<Integer> getViolationLines(ArrayList<ArrayList<Object>> fileResults, String ruleType) {

    ArrayList<Integer> violationLines = new ArrayList<Integer>();

    for (ArrayList violation : fileResults) {
      if (violation.get(RULETYPE_POS).equals(ruleType)) {
        violationLines.add((Integer) violation.get(VIOLATION_LINE_POS));
      }
    }
    return violationLines;
  }


}
