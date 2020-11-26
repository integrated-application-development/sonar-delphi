package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class ShortIdentifiersRuleTest extends BasePmdRuleTest {
  @Test
  void testShortIdentifiersShouldAddIssues() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  A = class")
            .appendDecl("    procedure B;")
            .appendDecl("  end;")
            .appendImpl("procedure A.B;")
            .appendImpl("var")
            .appendImpl("  C: Boolean;")
            .appendImpl("begin")
            .appendImpl("  if C then begin")
            .appendImpl("    Proc;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("ShortIdentifiersRule", builder.getOffsetDecl() + 2))
        .areExactly(1, ruleKeyAtLine("ShortIdentifiersRule", builder.getOffsetDecl() + 3))
        .areExactly(1, ruleKeyAtLine("ShortIdentifiersRule", builder.getOffset() + 3));
  }

  @Test
  void testWhitelistedIdentifiersShouldNotAddIssues() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  X = class")
            .appendDecl("    procedure Y;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ShortIdentifiersRule"));
  }

  @Test
  void testUnitImportsShouldNotAddIssues() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder().appendDecl("uses").appendDecl("  DB;");

    execute(builder);

    assertIssues().isEmpty();
  }
}
