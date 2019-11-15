package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class EmptyInterfaceRuleTest extends BasePmdRuleTest {
  @Test
  public void testInterfaceWithMethodsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  IPublisher = interface")
            .appendDecl("    ['{E1787C21-0FF2-11D5-A978-006067000685}']")
            .appendDecl("      procedure RegisterSubscriber(Handler: TNotifyEvent);")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testInterfaceWithMethodsAndNoGuidShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  IPublisher = interface")
            .appendDecl("    procedure RegisterSubscriber(Handler: TNotifyEvent);")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("EmptyInterfaceRule"));
  }

  @Test
  public void testInterfaceWithoutMethodsWithGuidShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  IPublisher = interface")
            .appendDecl("    ['{E1787C21-0FF2-11D5-A978-006067000685}']")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("EmptyInterfaceRule", builder.getOffsetDecl() + 2));
  }

  @Test
  public void testInterfaceThatLooksLikeForwardDeclarationShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  // Looks like a forward declaration, but isn't.")
            .appendDecl("  IPublisher = interface;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("EmptyInterfaceRule", builder.getOffsetDecl() + 3));
  }

  @Test
  public void testInterfaceForwardDeclarationShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  // Forward declaration")
            .appendDecl("  IPublisher = interface;")
            .appendDecl("  // Actual interface")
            .appendDecl("  IPublisher = interface")
            .appendDecl("    ['{E1787C21-0FF2-11D5-A978-006067000685}']")
            .appendDecl("      procedure RegisterSubscriber(Handler: TNotifyEvent);")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }
}
