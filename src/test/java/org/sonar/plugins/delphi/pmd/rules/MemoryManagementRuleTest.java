package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class MemoryManagementRuleTest extends BasePmdRuleTest {
  @BeforeEach
  void setup() {
    var rule = getRule(MemoryManagementRule.class);

    var memoryFunctions = rule.getProperty(MemoryManagementRule.MEMORY_FUNCTIONS.name());
    Objects.requireNonNull(memoryFunctions).setValue("Test.TMemory.Manage<T>");

    var whitelist = rule.getProperty(MemoryManagementRule.WHITELISTED_NAMES.name());
    Objects.requireNonNull(whitelist).setValue("CreateNew");
  }

  @Test
  void testRequiresMemoryManagementShouldAddIssue() {
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
  void testNestedUnmanagedShouldAddIssue() {
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

    assertIssues().areExactly(1, ruleKeyAtLine("MemoryManagementRule", builder.getOffset() + 9));
  }

  @Test
  void testInheritedShouldNotAddIssue() {
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
  void testCreateOnSelfShouldNotAddIssue() {
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
  void testWhitelistedShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    constructor CreateNew;")
            .appendDecl("  end;")
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  TFoo.CreateNew;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MemoryManagementRule"));
  }

  @Test
  void testMemoryManagedShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(IBar)")
            .appendDecl("    constructor Create;")
            .appendDecl("    procedure Foo;")
            .appendDecl("  end;")
            .appendDecl("  TMemory = class")
            .appendDecl("    class function Manage<T>(Obj: T): T;")
            .appendDecl("  end;")
            .appendImpl("function TMemory.Manage<T>(Obj: T): T;")
            .appendImpl("begin")
            .appendImpl("  // Memory management voodoo")
            .appendImpl("  Result := Obj;")
            .appendImpl("end;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: TFoo;")
            .appendImpl("begin")
            .appendImpl("  Foo := TMemory.Manage(TFoo.Create);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MemoryManagementRule"));
  }

  @Test
  void testMemoryManagedWithCastShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(IBar)")
            .appendDecl("    constructor Create;")
            .appendDecl("    procedure Foo;")
            .appendDecl("  end;")
            .appendDecl("  TMemory = class")
            .appendDecl("    class function Manage<T>(Obj: T): T;")
            .appendDecl("  end;")
            .appendImpl("function TMemory.Manage<T>(Obj: T): T;")
            .appendImpl("begin")
            .appendImpl("  // Memory management voodoo")
            .appendImpl("  Result := Obj;")
            .appendImpl("end;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: TObject;")
            .appendImpl("begin")
            .appendImpl("  Foo := TMemory.Manage(TFoo.Create as TObject);")
            .appendImpl("  Foo := TMemory.Manage(TObject(TFoo.Create));")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MemoryManagementRule"));
  }

  @Test
  void testRaisingExceptionShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  ESpookyError = class(Exception)")
            .appendDecl("    constructor Create;")
            .appendDecl("  end;")
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  raise ESpookyError.Create;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MemoryManagementRule"));
  }

  @Test
  void testInterfaceAssignmentShouldNotAddIssue() {
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

    assertIssues().areNot(ruleKey("MemoryManagementRule"));
  }

  @Test
  void testInterfaceAssignmentWithCastsShouldNotAddIssue() {
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
            .appendImpl("  Foo := TFoo.Create as IBar;")
            .appendImpl("  Foo := IBar(TFoo.Create);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MemoryManagementRule"));
  }

  @Test
  void testInterfaceArgumentShouldNotAddIssue() {
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
            .appendImpl("procedure Test(Foo: TFoo);")
            .appendImpl("begin")
            .appendImpl("  Foo.Baz(TFoo.Create);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MemoryManagementRule"));
  }

  @Test
  void testInterfaceArgumentWithCastsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  IBar = interface")
            .appendDecl("    ['{ACCD0A8C-A60F-464A-8152-52DD36F86356}']")
            .appendDecl("    procedure Baz(Bar: IBar);")
            .appendDecl("  end;")
            .appendDecl("  TFoo = class(TObject, IBar)")
            .appendDecl("    procedure Baz(Bar: IBar);")
            .appendDecl("  end;")
            .appendImpl("procedure Test(Foo: TFoo);")
            .appendImpl("begin")
            .appendImpl("  Foo.Baz(IBar(TFoo.Create));")
            .appendImpl("  Foo.Baz(TFoo.Create as IBar);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MemoryManagementRule"));
  }

  @Test
  void testRecordConstructorsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = record")
            .appendDecl("    constructor Create;")
            .appendDecl("  end;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: TFoo;")
            .appendImpl("begin")
            .appendImpl("  Foo := TFoo.Create;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MemoryManagementRule"));
  }

  @Test
  void testNonConstructorsShouldNotAddIssue() {
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
            .appendImpl("procedure Test(Foo: TFoo);")
            .appendImpl("begin")
            .appendImpl("  Foo.SomeProcedure;")
            .appendImpl("  Foo.SomeFunction;")
            .appendImpl("  Foo.Clone;")
            .appendImpl("  Clone;")
            .appendImpl("  Foo.ProcedureThatDoesNotExist;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MemoryManagementRule"));
  }

  @Test
  void testNestedInObscureProceduralUsageShouldAddIssue() {
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

    assertIssues().areExactly(1, ruleKeyAtLine("MemoryManagementRule", builder.getOffset() + 5));
  }
}
