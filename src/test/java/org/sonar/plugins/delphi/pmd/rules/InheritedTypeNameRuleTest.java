package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.utils.matchers.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.pmd.xml.DelphiRule;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class InheritedTypeNameRuleTest extends BasePmdRuleTest {

  private DelphiRuleProperty nameRegex;
  private DelphiRuleProperty parentRegex;

  @Before
  public void setup() {
    nameRegex = new DelphiRuleProperty(InheritedTypeNameRule.NAME_REGEX.name(), ".*_Child");
    parentRegex = new DelphiRuleProperty(InheritedTypeNameRule.PARENT_REGEX.name(), ".*_Parent");

    DelphiRule rule = new DelphiRule();
    rule.setName("TestInheritedNameRule");
    rule.setClazz("org.sonar.plugins.delphi.pmd.rules.InheritedTypeNameRule");
    rule.setPriority(5);
    rule.addProperty(nameRegex);
    rule.addProperty(parentRegex);

    addRule(rule);
  }

  @Test
  public void testCompliesWithNamingConventionShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TType_Child = class(TType_Parent)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testFailsNamingConventionShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TType = class(TType_Parent)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("TestInheritedNameRule", builder.getOffsetDecl() + 2)));
  }

  @Test
  public void testFailsNamingConventionWithMultipleParentsShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TType = class(IType, TType_Parent)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("TestInheritedNameRule", builder.getOffsetDecl() + 2)));
  }

  @Test
  public void testDoesNotInheritFromExpectedTypeShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TType = class(TSomeOtherType)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testDoesNotInheritFromAnyTypeShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TType = class");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testBadNameRegexShouldNotAddIssue() {
    nameRegex.setValue("*");

    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TType = class(TType_Parent)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testBadParentRegexShouldNotAddIssue() {
    parentRegex.setValue("*");

    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TType = class(TType_Parent)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues(empty());
  }
}
