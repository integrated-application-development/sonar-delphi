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
package au.com.integradev.delphi.checks.verifier;

import au.com.integradev.delphi.builders.DelphiTestFile;
import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;

public interface CheckVerifier {
  static CheckVerifier newVerifier() {
    return new CheckVerifierImpl();
  }

  CheckVerifier withCheck(DelphiCheck check);

  CheckVerifier withUnitScopeName(String unitScope);

  CheckVerifier withUnitAlias(String alias, String unitName);

  CheckVerifier withSearchPathUnit(DelphiTestUnitBuilder builder);

  CheckVerifier withStandardLibraryUnit(DelphiTestUnitBuilder builder);

  CheckVerifier onFile(DelphiTestFile builder);

  void verifyIssueOnLine(int... lines);

  void verifyIssueOnFile();

  void verifyIssueOnProject();

  void verifyNoIssues();

  void verifyIssues();
}
