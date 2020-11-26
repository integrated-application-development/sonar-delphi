package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class ImportSpecificityRuleTest extends BasePmdRuleTest {

  @Test
  void testImportUsedInImplementationShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("  System.UITypes;")
            .appendImpl("type")
            .appendImpl("  Alias = System.UITypes.TMsgDlgType;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("ImportSpecificityRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testImportUsedInInterfaceShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("  System.UITypes;")
            .appendDecl("type")
            .appendDecl("  Alias = System.UITypes.TMsgDlgType;");

    execute(builder);

    assertIssues().areNot(ruleKey("ImportSpecificityRule"));
  }

  @Test
  void testUnusedImportShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendImpl("uses System.UITypes;");

    execute(builder);

    assertIssues().areNot(ruleKey("ImportSpecificityRule"));
  }
}
