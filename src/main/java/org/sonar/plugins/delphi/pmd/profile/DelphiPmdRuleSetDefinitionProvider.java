package org.sonar.plugins.delphi.pmd.profile;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.ServerSide;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleSet;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleSetHelper;

@ScannerSide
@ServerSide
public class DelphiPmdRuleSetDefinitionProvider {

  private DelphiRuleSet definition;

  public DelphiRuleSet getDefinition() {
    if (definition == null) {
      InputStream rulesResource = getClass().getResourceAsStream(DelphiPmdConstants.RULES_XML);
      Reader rulesReader = new InputStreamReader(rulesResource, UTF_8);
      definition = DelphiRuleSetHelper.createFrom(rulesReader);
    }

    return definition;
  }
}
