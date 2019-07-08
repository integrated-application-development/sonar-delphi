package org.sonar.plugins.delphi.pmd;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;

public class EnumNameTest extends BasePmdRuleTest {

  @Test
  public void testAcceptT() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TEnum = (someEnum, someOtherEnum, someThirdEnum);");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testNotAcceptLowercaseT() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  tEnum = (someEnum, someOtherEnum, someThirdEnum);");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("ClassNameRule", builder.getOffsetDecl() + 2)));
  }

  @Test
  public void testNotAcceptBadPascalCase() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  Tenum = (someEnum, someOtherEnum, someThirdEnum);");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("ClassNameRule", builder.getOffsetDecl() + 2)));
  }

  @Test
  public void testNotAcceptPrefixAlone() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  T = (someEnum, someOtherEnum, someThirdEnum);");

    execute(builder);

    assertIssues(hasItem(hasRuleKeyAtLine("ClassNameRule", builder.getOffsetDecl() + 2)));
  }

}
