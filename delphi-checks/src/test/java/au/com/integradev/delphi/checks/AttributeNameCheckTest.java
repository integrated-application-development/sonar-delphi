/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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

class AttributeNameCheckTest {
  @Test
  void testAttributeTypeWithSuffixShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new AttributeNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  FooAttribute = class(TCustomAttribute)")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testAttributeTypeWithoutSuffixShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new AttributeNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  Foo = class(TCustomAttribute) // Noncompliant")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testAttributeTypeWithLowercaseSuffixShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new AttributeNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  fooAttribute = class(TCustomAttribute) // Noncompliant")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testNonPascalCaseAttributeTypeShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new AttributeNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  fooAttribute = class(TCustomAttribute) // Noncompliant")
                .appendDecl("  end;"))
        .verifyIssues();
  }
}
