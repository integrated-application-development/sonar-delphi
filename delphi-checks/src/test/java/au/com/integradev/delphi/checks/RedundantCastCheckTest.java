/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package au.com.integradev.delphi.checks;

import static au.com.integradev.delphi.conditions.RuleKey.ruleKey;
import static au.com.integradev.delphi.conditions.RuleKeyAtLine.ruleKeyAtLine;

import au.com.integradev.delphi.CheckTest;
import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import org.junit.jupiter.api.Test;

class RedundantCastCheckTest extends CheckTest {

  @Test
  void testNoCastShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("  end;")
            .appendDecl("  TBar = class (TFoo)")
            .appendDecl("  end;")
            .appendImpl("procedure Foo(Bar: TBar);")
            .appendImpl("var")
            .appendImpl("  Foo: TFoo;")
            .appendImpl("begin")
            .appendImpl("  Foo := Bar;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("RedundantCastRule"));
  }

  @Test
  void testRequiredCastShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("  end;")
            .appendDecl("  TBar = class (TFoo)")
            .appendDecl("  end;")
            .appendImpl("procedure Foo(Foo: TFoo);")
            .appendImpl("var")
            .appendImpl("  Bar: TBar;")
            .appendImpl("begin")
            .appendImpl("  Bar := TBar(Foo);")
            .appendImpl("  Bar := Foo as TBar;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("RedundantCastRule"));
  }

  @Test
  void testRedundantCastShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("  end;")
            .appendImpl("procedure Foo(Foo: TFoo);")
            .appendImpl("var")
            .appendImpl("  Foo2: TFoo;")
            .appendImpl("begin")
            .appendImpl("  Foo2 := TFoo(Foo);")
            .appendImpl("  Foo2 := Foo as TFoo;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("RedundantCastRule", builder.getOffset() + 5))
        .areExactly(1, ruleKeyAtLine("RedundantCastRule", builder.getOffset() + 6));
  }

  @Test
  void testRedundantCastWithConstructorShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("  end;")
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Foo: TFoo;")
            .appendImpl("begin")
            .appendImpl("  Foo := TFoo(TFoo.Create);")
            .appendImpl("  Foo := TFoo.Create as TFoo;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("RedundantCastRule", builder.getOffset() + 5))
        .areExactly(1, ruleKeyAtLine("RedundantCastRule", builder.getOffset() + 6));
  }

  @Test
  void testRedundantCastWithStringKeywordShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Arg: String);")
            .appendImpl("var")
            .appendImpl("  Str: String;")
            .appendImpl("begin")
            .appendImpl("  Str := String(Arg);")
            .appendImpl("  Str := Arg as String;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("RedundantCastRule", builder.getOffset() + 5))
        .areExactly(1, ruleKeyAtLine("RedundantCastRule", builder.getOffset() + 6));
  }

  @Test
  void testRedundantCastWithFileKeywordShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Arg: file);")
            .appendImpl("var")
            .appendImpl("  FileVar: file;")
            .appendImpl("begin")
            .appendImpl("  FileVar := file(Arg);")
            .appendImpl("  FileVar := Arg as file;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("RedundantCastRule", builder.getOffset() + 5))
        .areExactly(1, ruleKeyAtLine("RedundantCastRule", builder.getOffset() + 6));
  }

  @Test
  void testRedundantCastWithAmbiguousMethodCallShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("function GetString: String;")
            .appendImpl("procedure Foo(Arg: String);")
            .appendImpl("var")
            .appendImpl("  Str: String;")
            .appendImpl("begin")
            .appendImpl("  Str := String(GetString);")
            .appendImpl("  Str := GetString as String;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("RedundantCastRule", builder.getOffset() + 5))
        .areExactly(1, ruleKeyAtLine("RedundantCastRule", builder.getOffset() + 6));
  }

  @Test
  void testTClassToTObjectShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Clazz: TClass;")
            .appendImpl("  Obj: TObject;")
            .appendImpl("begin")
            .appendImpl("  Obj := TObject(Clazz);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("RedundantCastRule"));
  }

  @Test
  void testUnknownTypesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("  end;")
            .appendImpl("function Foo(Foo: TFoo): TFoo;")
            .appendImpl("var")
            .appendImpl("  Unknown: TUnknown;")
            .appendImpl("begin")
            .appendImpl("  Result := TBar(Unknown);")
            .appendImpl("  Result := Foo as TUnknown;")
            .appendImpl("  Result := TUnknown(Foo);")
            .appendImpl("  Result := Unknown as TFoo;")
            .appendImpl("  Result := Unknown as TUnknown;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("RedundantCastRule"));
  }
}
