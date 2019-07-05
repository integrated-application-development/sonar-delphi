package org.sonar.plugins.delphi.pmd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleKeyAtLine;

import java.io.File;
import java.util.List;
import net.sourceforge.pmd.lang.ast.Node;
import org.junit.Ignore;
import org.junit.Test;
import org.sonar.plugins.delphi.HasRuleLineNumber;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class EnumNameTest extends BasePmdRuleTest {

  @Test
  public void testAcceptT() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TEnum = (someEnum, someOtherEnum, someThirdEnum);");

    execute(builder);

    assertThat(stringifyIssues(), issues, is(empty()));
  }

  @Test
  public void testNotAcceptLowercaseT() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  tEnum = (someEnum, someOtherEnum, someThirdEnum);");

    execute(builder);

    assertThat(stringifyIssues(), issues, hasSize(1));
    assertThat(stringifyIssues(), issues, hasItem(
        hasRuleKeyAtLine("ClassNameRule", builder.getOffsetDecl() + 2)));
  }

  @Test
  public void testNotAcceptBadPascalCase() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  Tenum = (someEnum, someOtherEnum, someThirdEnum);");

    execute(builder);

    assertThat(stringifyIssues(), issues, hasSize(1));
    assertThat(stringifyIssues(), issues, hasItem(
        hasRuleKeyAtLine("ClassNameRule", builder.getOffsetDecl() + 2)));
  }

}
