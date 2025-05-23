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

class TabulationCharacterCheckTest {

  @Test
  void testFileWithoutTabsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TabulationCharacterCheck())
        .onFile(new DelphiTestUnitBuilder())
        .verifyNoIssues();
  }

  @Test
  void testFileWithTabsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TabulationCharacterCheck())
        .onFile(new DelphiTestUnitBuilder().appendDecl("\t"))
        .verifyIssueOnFile();
  }

  @Test
  void testFileWithMultipleTabsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TabulationCharacterCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("\t")
                .appendDecl("var")
                .appendDecl("\t\tGBoolean:\tBoolean;")
                .appendDecl("\t"))
        .verifyIssueOnFile();
  }

  @Test
  void testFileWithTabsInMultilineStringIndentationShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TabulationCharacterCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("const")
                .appendDecl(" Foo = '''")
                .appendDecl("\t\tBar")
                .appendDecl("\t\t''';"))
        .verifyIssueOnFile();
  }
}
