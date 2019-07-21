package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.pmd.DelphiTestUnitBuilder;

public class EmptyMethodRuleTest extends BasePmdRuleTest {

  @Test
  public void testValidRule() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    builder.appendDecl("type");
    builder.appendDecl("  TEmptyProcs = class");
    builder.appendDecl("  public");
    builder.appendDecl("    procedure One;");
    builder.appendDecl("    procedure Two;");
    builder.appendDecl("    procedure Three;");
    builder.appendDecl("  end;");

    builder.appendImpl("procedure TEmptyProcs.One;");
    builder.appendImpl("begin");
    builder.appendImpl("  Writeln('OK');");
    builder.appendImpl("end;");
    builder.appendImpl("procedure TEmptyProcs.Two;");
    builder.appendImpl("begin");
    builder.appendImpl("  Writeln('OK');");
    builder.appendImpl("end;");
    builder.appendImpl("procedure TEmptyProcs.Three;");
    builder.appendImpl("begin");
    builder.appendImpl("  Writeln('OK');");
    builder.appendImpl("end;");
    builder.appendImpl("procedure GlobalProcedureFour;");
    builder.appendImpl("begin");
    builder.appendImpl("  Writeln('OK');");
    builder.appendImpl("end;");
    builder.appendImpl("procedure GlobalProcedureFive;");
    builder.appendImpl("begin");
    builder.appendImpl("  Writeln('OK');");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testEmptyMethods() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    builder.appendDecl("type");
    builder.appendDecl("  TEmptyProcs = class");
    builder.appendDecl("  public");
    builder.appendDecl("    procedure One;");
    builder.appendDecl("    procedure Two;");
    builder.appendDecl("    procedure Three;");
    builder.appendDecl("    procedure Four;");
    builder.appendDecl("    procedure Five;");
    builder.appendDecl("  end;");

    builder.appendImpl("procedure TEmptyProcs.One;");
    builder.appendImpl("begin");
    builder.appendImpl("end;");
    builder.appendImpl("procedure TEmptyProcs.Two;");
    builder.appendImpl("begin");
    builder.appendImpl("end;");
    builder.appendImpl("procedure TEmptyProcs.Three;");
    builder.appendImpl("begin");
    builder.appendImpl("end;");
    builder.appendImpl("procedure TEmptyProcs.Four;");
    builder.appendImpl("begin");
    builder.appendImpl("end;");
    builder.appendImpl("procedure TEmptyProcs.Five;");
    builder.appendImpl("begin");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(5));
    assertIssues(hasItem(hasRuleKeyAtLine("EmptyMethodRule", builder.getOffSet() + 2)));
    assertIssues(hasItem(hasRuleKeyAtLine("EmptyMethodRule", builder.getOffSet() + 5)));
    assertIssues(hasItem(hasRuleKeyAtLine("EmptyMethodRule", builder.getOffSet() + 8)));
    assertIssues(hasItem(hasRuleKeyAtLine("EmptyMethodRule", builder.getOffSet() + 11)));
    assertIssues(hasItem(hasRuleKeyAtLine("EmptyMethodRule", builder.getOffSet() + 14)));
  }
}
