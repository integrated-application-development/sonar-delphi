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

class CastAndFreeCheckTest extends CheckTest {

  @Test
  void testRegularFreeShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Bar: Baz);")
            .appendImpl("begin")
            .appendImpl("  Bar.Free;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("CastAndFreeRule"));
  }

  @Test
  void testPointerToObjectCastShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("  System.SysUtils;")
            .appendImpl("procedure Foo(Bar: Pointer);")
            .appendImpl("begin")
            .appendImpl("  (TObject(Bar)).Free;")
            .appendImpl("  FreeAndNil(TObject(Bar));")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("CastAndFreeRule"));
  }

  @Test
  void testUntypedToObjectCastShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("  System.SysUtils;")
            .appendImpl("procedure Foo(Bar);")
            .appendImpl("begin")
            .appendImpl("  (TObject(Bar)).Free;")
            .appendImpl("  FreeAndNil(TObject(Bar));")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("CastAndFreeRule"));
  }

  @Test
  void testWeirdPointerToObjectCastShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("  System.SysUtils;")
            .appendDecl("type")
            .appendDecl("  TList = class(TObject)")
            .appendDecl("  property Default[Index: Integer]: Pointer; default;")
            .appendDecl("end;")
            .appendImpl("procedure Foo(List: TList);")
            .appendImpl("begin")
            .appendImpl("  (TList(List[0])).Free;")
            .appendImpl("  FreeAndNil(TObject(List[0]));")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("CastAndFreeRule"));
  }

  @Test
  void testWeirdSoftCastFreeShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TXyzz = class(TObject) end;")
            .appendImpl("procedure Foo(Bar: Baz);")
            .appendImpl("begin")
            .appendImpl("  (Bar as TXyzz).Free;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("CastAndFreeRule", builder.getOffset() + 3));
  }

  @Test
  void testRegularFreeAndNilShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Bar: Baz);")
            .appendImpl("begin")
            .appendImpl("  FreeAndNil(Bar);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKeyAtLine("CastAndFreeRule", builder.getOffset() + 3));
  }

  @Test
  void testSoftCastFreeShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Bar: Baz);")
            .appendImpl("begin")
            .appendImpl("  (Bar as Xyzzy).Free;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("CastAndFreeRule", builder.getOffset() + 3));
  }

  @Test
  void testHardCastFreeShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Bar: Baz);")
            .appendImpl("begin")
            .appendImpl("  TObject(Bar).Free;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("CastAndFreeRule", builder.getOffset() + 3));
  }

  @Test
  void testSoftCastFreeAndNilShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Bar: Baz);")
            .appendImpl("begin")
            .appendImpl("  FreeAndNil(Bar as TObject);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("CastAndFreeRule", builder.getOffset() + 3));
  }

  @Test
  void testHardCastFreeAndNilShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Bar: Baz);")
            .appendImpl("begin")
            .appendImpl("  FreeAndNil(TObject(Bar));")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("CastAndFreeRule", builder.getOffset() + 3));
  }

  @Test
  void testNestedSoftCastFreeShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Bar: Baz);")
            .appendImpl("begin")
            .appendImpl("  (((Bar as TObject))).Free;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("CastAndFreeRule", builder.getOffset() + 3));
  }

  @Test
  void testNestedHardCastFreeShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Bar: Baz);")
            .appendImpl("begin")
            .appendImpl("  ((Xyzzy(Bar))).Free;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("CastAndFreeRule", builder.getOffset() + 3));
  }

  @Test
  void testNestedSoftCastFreeAndNilShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Bar: Baz);")
            .appendImpl("begin")
            .appendImpl("  FreeAndNil(((Bar as Xyzzy)));")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("CastAndFreeRule", builder.getOffset() + 3));
  }

  @Test
  void testNestedHardCastFreeAndNilShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Bar: Baz);")
            .appendImpl("begin")
            .appendImpl("  FreeAndNil(((Xyzzy(Bar))));")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("CastAndFreeRule", builder.getOffset() + 3));
  }
}
