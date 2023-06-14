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

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;

class InterfaceGuidCheckTest {
  @Test
  void testInterfaceWithGuidShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InterfaceGuidCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  IPublisher = interface")
                .appendDecl("    ['{E1787C21-0FF2-11D5-A978-006067000685}']")
                .appendDecl("      procedure RegisterSubscriber(Handler: TNotifyEvent);")
                .appendDecl("      procedure DeregisterSubscriber(Handler: TNotifyEvent);")
                .appendDecl("      procedure Notify(Event: TObject);")
                .appendDecl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testInterfaceWithoutGuidShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InterfaceGuidCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  IPublisher = interface")
                .appendDecl("    procedure RegisterSubscriber(Handler: TNotifyEvent);")
                .appendDecl("    procedure DeregisterSubscriber(Handler: TNotifyEvent);")
                .appendDecl("    procedure Notify(Event: TObject);")
                .appendDecl("  end;"))
        .verifyIssueOnLine(6);
  }
}
