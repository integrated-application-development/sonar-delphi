package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class EnumNameRuleTest extends BasePmdRuleTest {

  @Test
  void testAcceptT() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TEnum = (someEnum, someOtherEnum, someThirdEnum);");

    execute(builder);

    assertIssues().areNot(ruleKey("EnumNameRule"));
  }

  @Test
  void testNotAcceptLowercaseT() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  tEnum = (someEnum, someOtherEnum, someThirdEnum);");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("EnumNameRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testNotAcceptBadPascalCase() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  Tenum = (someEnum, someOtherEnum, someThirdEnum);");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("EnumNameRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testNotAcceptPrefixAlone() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  T = (someEnum, someOtherEnum, someThirdEnum);");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("EnumNameRule", builder.getOffsetDecl() + 2));
  }
}
