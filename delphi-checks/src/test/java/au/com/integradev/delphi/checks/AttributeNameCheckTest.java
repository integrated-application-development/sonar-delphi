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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;

class AttributeNameCheckTest {
  private static DelphiCheck createCheck(String setting) {
    AttributeNameCheck check = new AttributeNameCheck();
    check.attributeSuffix = setting;
    return check;
  }

  @ParameterizedTest
  @ValueSource(strings = {"allowed", "required"})
  void testAllowedOrRequiredAttributeTypeWithSuffixShouldNotAddIssue(String setting) {
    CheckVerifier.newVerifier()
        .withCheck(createCheck(setting))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  FooAttribute = class(TCustomAttribute)")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {"allowed", "forbidden"})
  void testAllowedOrForbiddenAttributeTypeWithoutSuffixShouldNotAddIssue(String setting) {
    CheckVerifier.newVerifier()
        .withCheck(createCheck(setting))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  Foo = class(TCustomAttribute)")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testRequiredAttributeTypeWithoutSuffixShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck("required"))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  Foo = class(TCustomAttribute)")
                .appendDecl("  end;"))
        .verifyIssueOnLine(6);
  }

  @Test
  void testForbiddenAttributeTypeWithSuffixShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck("forbidden"))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  FooAttribute = class(TCustomAttribute)")
                .appendDecl("  end;"))
        .verifyIssueOnLine(6);
  }

  @ParameterizedTest
  @ValueSource(strings = {"allowed", "required", "forbidden"})
  void testNonPascalCaseAttributeTypeShouldAddIssue(String setting) {
    CheckVerifier.newVerifier()
        .withCheck(createCheck(setting))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  fooAttribute = class(TCustomAttribute)")
                .appendDecl("  end;"))
        .verifyIssueOnLine(6);
  }
}
