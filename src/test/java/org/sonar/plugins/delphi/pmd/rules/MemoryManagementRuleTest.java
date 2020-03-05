package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class MemoryManagementRuleTest extends BasePmdRuleTest {

  @Test
  public void testRequiresMemoryManagementShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBar = class(TObject)")
            .appendDecl("  end;")
            .appendDecl("  TFoo = class(TBar)")
            .appendDecl("    constructor Create;")
            .appendDecl("    function Clone(X: Integer): TFoo;")
            .appendDecl("    function Clone(X: String): TBar;")
            .appendDecl("  end;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: TFoo;")
            .appendImpl("  Bar: TBar;")
            .appendImpl("begin")
            .appendImpl("  Foo := TFoo.Create;")
            .appendImpl("  Foo := Foo.Clone(12345);")
            .appendImpl("  Bar := Foo.Clone('12345');")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("MemoryManagementRule", builder.getOffset() + 6))
        .areExactly(1, ruleKeyAtLine("MemoryManagementRule", builder.getOffset() + 7))
        .areExactly(1, ruleKeyAtLine("MemoryManagementRule", builder.getOffset() + 8));
  }

  @Test
  public void testNestedUnmanagedShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(IBar)")
            .appendDecl("    constructor Create;")
            .appendDecl("    procedure Foo;")
            .appendDecl("  end;")
            .appendImpl("function SomeFunction(Foo: TFoo): TFoo;")
            .appendImpl("begin")
            .appendImpl("  Result := Foo;")
            .appendImpl("end;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: TFoo;")
            .appendImpl("begin")
            .appendImpl("  Foo := SomeFunction(TFoo.Create);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("MemoryManagementRule", builder.getOffset() + 9));
  }

  @Test
  public void testInheritedShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("    constructor Create;")
            .appendDecl("  end;")
            .appendImpl("constructor TFoo.Create;")
            .appendImpl("begin")
            .appendImpl("  inherited Create;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MemoryManagementRule"));
  }

  @Test
  public void testCreateOnSelfShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("    constructor Create; override;")
            .appendDecl("    constructor Create(Arg: String); override;")
            .appendDecl("  end;")
            .appendImpl("constructor TFoo.Create;")
            .appendImpl("begin")
            .appendImpl("  Create('String');")
            .appendImpl("  Self.Create('String');")
            .appendImpl("end;")
            .appendImpl("constructor TFoo.Create(Arg: String);")
            .appendImpl("begin")
            .appendImpl("  // Do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MemoryManagementRule"));
  }

  @Test
  public void testWhitelistedShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    constructor CreateNew;")
            .appendDecl("  end;")
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  TFoo.CreateNew;")
            .appendImpl("  TPoint.Create;")
            .appendImpl("  TRect.Create;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testMemoryManagedShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(IBar)")
            .appendDecl("    constructor Create;")
            .appendDecl("    procedure Foo;")
            .appendDecl("  end;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: TFoo;")
            .appendImpl("begin")
            .appendImpl("  Foo := Local(TFoo.Create).Obj as TFoo;")
            .appendImpl("  Foo := Managed(TFoo.Create).Obj as TFoo;")
            .appendImpl("  Foo := Unmanaged(TFoo.Create).Obj as TFoo;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testRaisingExceptionShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  EException = class(Exception)")
            .appendDecl("    constructor Create;")
            .appendDecl("  end;")
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  raise EException.Create;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testInterfaceAssignmentShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  IBar = interface")
            .appendDecl("    ['{ACCD0A8C-A60F-464A-8152-52DD36F86356}']")
            .appendDecl("    procedure Foo;")
            .appendDecl("  end;")
            .appendDecl("  TFoo = class(TObject, IBar)")
            .appendDecl("    procedure Foo;")
            .appendDecl("  end;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: IBar;")
            .appendImpl("begin")
            .appendImpl("  Foo := TFoo.Create;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testInterfaceArgumentShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  IBar = interface")
            .appendDecl("    ['{ACCD0A8C-A60F-464A-8152-52DD36F86356}']")
            .appendDecl("    procedure Foo;")
            .appendDecl("  end;")
            .appendDecl("  TFoo = class(TObject, IBar)")
            .appendDecl("    procedure Baz(Bar: IBar);")
            .appendDecl("  end;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: TFoo;")
            .appendImpl("begin")
            .appendImpl("  Foo.Baz(TFoo.Create);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testProceduralHardCastShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  IBar = interface")
            .appendDecl("    ['{ACCD0A8C-A60F-464A-8152-52DD36F86356}']")
            .appendDecl("    procedure Foo;")
            .appendDecl("  end;")
            .appendDecl("  TBar = procedure;")
            .appendDecl("  TFoo = class(TObject, IBar)")
            .appendDecl("    procedure Baz(Bar: IBar);")
            .appendDecl("  end;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Bar: TBar;")
            .appendImpl("begin")
            .appendImpl("  Bar := TBar(TFoo.Create);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("MemoryManagementRule", builder.getOffset() + 5));
  }

  @Test
  public void testNonConstructorsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(IBar)")
            .appendDecl("    procedure SomeProcedure;")
            .appendDecl("    function SomeFunction: TFoo;")
            .appendDecl("    function Clone: Integer;")
            .appendDecl("  end;")
            .appendImpl("function Clone: Integer;")
            .appendImpl("begin")
            .appendImpl("  Result := 42;")
            .appendImpl("end;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: TFoo;")
            .appendImpl("begin")
            .appendImpl("  Foo.SomeProcedure;")
            .appendImpl("  Foo.SomeFunction;")
            .appendImpl("  Foo.Clone;")
            .appendImpl("  Clone;")
            .appendImpl("  Foo.ProcedureThatDoesNotExist;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testNestedInObscureProceduralUsageShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(IBar)")
            .appendDecl("    constructor Create;")
            .appendDecl("  end;")
            .appendDecl("  TProcType = procedure(Argument: TFoo);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  ProcArray: array of TProcType;")
            .appendImpl("begin")
            .appendImpl("  ProcArray[0](TFoo.Create);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("MemoryManagementRule", builder.getOffset() + 5));
  }
}
