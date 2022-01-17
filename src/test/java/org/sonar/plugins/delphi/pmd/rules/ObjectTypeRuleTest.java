package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class ObjectTypeRuleTest extends BasePmdRuleTest {
  @Test
  void testClassShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TClassType = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ObjectTypeRule"));
  }

  @Test
  void testObjectShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TObjectType = object")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("ObjectTypeRule", builder.getOffsetDecl() + 2));
  }
}
