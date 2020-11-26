package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class NoGuidRuleTest extends BasePmdRuleTest {
  @Test
  void testInterfaceWithGuidShouldNotAddIssue() {
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

    assertIssues().isEmpty();
  }

  @Test
  void testInterfaceWithoutGuidShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  IPublisher = interface")
            .appendDecl("    procedure RegisterSubscriber(Handler: TNotifyEvent);")
            .appendDecl("    procedure DeregisterSubscriber(Handler: TNotifyEvent);")
            .appendDecl("    procedure Notify(Event: TObject);")
            .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("NoGuidRule", builder.getOffsetDecl() + 2));
  }
}
