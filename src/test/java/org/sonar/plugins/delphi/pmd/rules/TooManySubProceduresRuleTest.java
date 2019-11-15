package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class TooManySubProceduresRuleTest extends BasePmdRuleTest {

  @Test
  public void testValidCaseShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;")
            .appendImpl("  function Bar: Integer;")
            .appendImpl("  begin")
            .appendImpl("    Result := 1;")
            .appendImpl("  end;")
            .appendImpl("  function Baz: Integer;")
            .appendImpl("  begin")
            .appendImpl("    Result := 2;")
            .appendImpl("  end;")
            .appendImpl("  function Qux: Integer;")
            .appendImpl("  begin")
            .appendImpl("    Result := 3;")
            .appendImpl("  end;")
            .appendImpl("begin")
            .appendImpl("  Result := Bar + Baz + Qux;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testTooManySubProceduresShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;")
            .appendImpl("  function Bar: Integer;")
            .appendImpl("  begin")
            .appendImpl("    Result := 1;")
            .appendImpl("  end;")
            .appendImpl("  function Baz: Integer;")
            .appendImpl("  begin")
            .appendImpl("    Result := 2;")
            .appendImpl("  end;")
            .appendImpl("  function Qux: Integer;")
            .appendImpl("  begin")
            .appendImpl("    Result := 3;")
            .appendImpl("  end;")
            .appendImpl("  function Xyzzy: Integer;")
            .appendImpl("  begin")
            .appendImpl("    Result := 4;")
            .appendImpl("  end;")
            .appendImpl("begin")
            .appendImpl("  Result := Bar + Baz + Qux + Xyzzy;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("TooManySubProceduresRule", builder.getOffset() + 1));
  }

  @Test
  public void testTooManyConstructorSubProceduresShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("constructor TMyObject.Create;")
            .appendImpl("  function Bar: Integer;")
            .appendImpl("  begin")
            .appendImpl("    Result := 1;")
            .appendImpl("  end;")
            .appendImpl("  function Baz: Integer;")
            .appendImpl("  begin")
            .appendImpl("    Result := 2;")
            .appendImpl("  end;")
            .appendImpl("  function Qux: Integer;")
            .appendImpl("  begin")
            .appendImpl("    Result := 3;")
            .appendImpl("  end;")
            .appendImpl("  function Xyzzy: Integer;")
            .appendImpl("  begin")
            .appendImpl("    Result := 4;")
            .appendImpl("  end;")
            .appendImpl("begin")
            .appendImpl("  inherited;")
            .appendImpl("  FMyField := Bar + Baz + Qux + Xyzzy;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("TooManySubProceduresRule", builder.getOffset() + 1));
  }

  @Test
  public void testTooManyDestructorSubProceduresShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("destructor TMyObject.Destroy;")
            .appendImpl("  function Bar: Integer;")
            .appendImpl("  begin")
            .appendImpl("    Result := 1;")
            .appendImpl("  end;")
            .appendImpl("  function Baz: Integer;")
            .appendImpl("  begin")
            .appendImpl("    Result := 2;")
            .appendImpl("  end;")
            .appendImpl("  function Qux: Integer;")
            .appendImpl("  begin")
            .appendImpl("    Result := 3;")
            .appendImpl("  end;")
            .appendImpl("  function Xyzzy: Integer;")
            .appendImpl("  begin")
            .appendImpl("    Result := 4;")
            .appendImpl("  end;")
            .appendImpl("begin")
            .appendImpl("  FMyField := Bar + Baz + Qux + Xyzzy;")
            .appendImpl("  inherited;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("TooManySubProceduresRule", builder.getOffset() + 1));
  }

  @Test
  public void testTooManyNestedProceduresShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("  function Bar: Integer;")
            .appendImpl("    function Baz: Integer;")
            .appendImpl("      function Qux: Integer;")
            .appendImpl("        function Xyzzy: Integer;")
            .appendImpl("        begin")
            .appendImpl("          Result := 4;")
            .appendImpl("        end;")
            .appendImpl("      begin")
            .appendImpl("        Result := Xyzzy;")
            .appendImpl("      end;")
            .appendImpl("    begin")
            .appendImpl("      Result := Qux;")
            .appendImpl("    end;")
            .appendImpl("  begin")
            .appendImpl("    Result := Baz;")
            .appendImpl("  end;")
            .appendImpl("begin")
            .appendImpl("  FMyField := Bar;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("TooManySubProceduresRule", builder.getOffset() + 1));
  }
}
