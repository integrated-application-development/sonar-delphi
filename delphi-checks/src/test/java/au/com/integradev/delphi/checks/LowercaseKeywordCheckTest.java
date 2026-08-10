/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;

class LowercaseKeywordCheckTest {
  private static DelphiCheck createCheck() {
    LowercaseKeywordCheck check = new LowercaseKeywordCheck();
    check.excludedKeywords = "string";
    return check;
  }

  @Test
  void testUppercaseKeywordShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("// Fix qf1@[+1:0 to +1:5] <<begin>>")
                .appendImpl("Begin // Noncompliant")
                .appendImpl("  MyVar := True;")
                .appendImpl("// Fix qf2@[+1:0 to +1:3] <<end>>")
                .appendImpl("END; // Noncompliant"))
        .verifyIssues();
  }

  @Test
  void testUppercaseKeywordPairOperatorsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Foo(Obj: TObject; B: Byte): Boolean;")
                .appendImpl("begin")
                .appendImpl("  // Fix qf1@[+1:16 to +1:18] <<is>>")
                .appendImpl("  Result := Obj IS not TObject; // Noncompliant")
                .appendImpl("  // Fix qf2@[+1:19 to +1:22] <<not>>")
                .appendImpl("  Result := Obj is NOT TObject; // Noncompliant")
                .appendImpl("  // Fix qf3@[+1:18 to +1:20] <<in>>")
                .appendImpl("  Result := B not IN [1, 2]; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testFullyUppercaseKeywordPairShouldAddIssues() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Foo(Obj: TObject): Boolean;")
                .appendImpl("begin")
                .appendImpl("  // Fix qf1@[+4:16 to +4:18] <<is>>")
                .appendImpl("  // Fix qf2@[+3:19 to +3:22] <<not>>")
                .appendImpl("  // Noncompliant@+2")
                .appendImpl("  // Noncompliant@+1")
                .appendImpl("  Result := Obj IS NOT TObject;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testLowercaseKeywordPairOperatorsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Foo(Obj: TObject; B: Byte): Boolean;")
                .appendImpl("begin")
                .appendImpl("  Result := Obj is not TObject;")
                .appendImpl("  Result := B not in [1, 2];")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUppercaseKeywordPairWithCommentBetweenKeywordsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Foo(Obj: TObject): Boolean;")
                .appendImpl("begin")
                .appendImpl("  // Fix qf1@[+1:16 to +1:18] <<is>>")
                .appendImpl("  Result := Obj IS {comment} not TObject; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testAsmBlockShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Bar;")
                .appendImpl("asm")
                .appendImpl("  SHR   eax, 16")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUppercaseExcludedKeywordShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Foo(")
                .appendImpl("  VarA: String;")
                .appendImpl("  VarB: string")
                .appendImpl("): STRING;")
                .appendImpl("begin")
                .appendImpl("  Result := VarA + VarB;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUppercaseRootTokenKeywordShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  // Fix@[+1:2 to +1:5] <<for>>")
                .appendImpl("  FOR I := 0 to 5 do // Noncompliant")
                .appendImpl("  begin")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }
}
