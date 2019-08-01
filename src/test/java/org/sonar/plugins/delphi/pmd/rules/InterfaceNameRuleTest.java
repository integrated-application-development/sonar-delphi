package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.pmd.DelphiTestUnitBuilder;

public class InterfaceNameRuleTest extends BasePmdRuleTest {

  @Test
  public void testValidNameShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  IPublisher = interface")
            .appendDecl("    ['{E1787C21-0FF2-11D5-A978-006067000685}']")
            .appendDecl("      procedure RegisterSubscriber(Handler: TNotifyEvent);")
            .appendDecl("      procedure DeregisterSubscriber(Handler: TNotifyEvent);")
            .appendDecl("      procedure Notify(Event: TObject);")
            .appendDecl("end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testInvalidNameShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  Publisher = interface")
            .appendDecl("    ['{E1787C21-0FF2-11D5-A978-006067000685}']")
            .appendDecl("      procedure RegisterSubscriber(Handler: TNotifyEvent);")
            .appendDecl("      procedure DeregisterSubscriber(Handler: TNotifyEvent);")
            .appendDecl("      procedure Notify(Event: TObject);")
            .appendDecl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("InterfaceNameRule", builder.getOffsetDecl() + 2)));
  }
}
