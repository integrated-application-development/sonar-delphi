package pmdRules;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

/**'
 * Base class for a rule testing for PMD rules
 */

public class BaseXmlPmdRuleTest {

    protected PMDReportXMLParser pmdParser = new PMDReportXMLParser();
    protected HashMap<String, ArrayList<ArrayList<Object>>> violationsMap = new HashMap<>();

    // Positions in arraylist results to use
    public int RULETYPE_POS = 0;
    public int VIOLATION_LINE_POS = 1;

    @Test
    public void analyse(){
        pmdParser.parsePmdReportXML();
        violationsMap = pmdParser.violationsMap;
    }

    public ArrayList<ArrayList<Object>> getFileResults(String testFilename){

        if(violationsMap.containsKey(testFilename)){
            return violationsMap.get(testFilename);
        }

        return null;

    }

}
