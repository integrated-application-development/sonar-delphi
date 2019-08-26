package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.sonar.plugins.delphi.utils.matchers.HasRuleKey.hasRuleKey;
import static org.sonar.plugins.delphi.utils.matchers.IssueMatchers.hasRuleKeyAtLine;

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

    assertIssues(empty());
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

    assertIssues(not(hasItem(hasRuleKey("EmptyInterfaceRule"))));
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

    assertIssues(hasItem(hasRuleKeyAtLine("EmptyInterfaceRule", builder.getOffsetDecl() + 2)));
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

    assertIssues(empty());
  }
}
