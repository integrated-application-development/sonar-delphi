/*
 * Sonar Delphi Plugin
 * Copyright (C) 2025 Integrated Application Development
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
package au.com.integradev.delphi.cfg.block;

import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.GotoStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.RaiseStatementNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;

public class Terminator {
  private final TerminatorKind kind;
  private final DelphiNode terminatorNode;

  public Terminator(DelphiNode terminator) {
    this.kind = findTerminatorKind(terminator);
    this.terminatorNode = terminator;
  }

  private static TerminatorKind findTerminatorKind(DelphiNode terminator) {
    if (terminator instanceof RaiseStatementNode) {
      return TerminatorKind.RAISE;
    } else if (terminator instanceof GotoStatementNode) {
      return TerminatorKind.GOTO;
    } else if (terminator instanceof NameReferenceNode) {
      NameDeclaration nameDeclarationNode =
          ((NameReferenceNode) terminator).getLastName().getNameDeclaration();
      if (nameDeclarationNode instanceof RoutineNameDeclaration) {
        switch (((RoutineNameDeclaration) nameDeclarationNode).fullyQualifiedName()) {
          case "System.Exit":
            return TerminatorKind.EXIT;
          case "System.Break":
            return TerminatorKind.BREAK;
          case "System.Halt":
            return TerminatorKind.HALT;
          case "System.Continue":
            return TerminatorKind.CONTINUE;
          default:
            // fallthrough
        }
      }
    }
    return TerminatorKind.NODE;
  }

  public TerminatorKind getKind() {
    return kind;
  }

  public DelphiNode getTerminatorNode() {
    return terminatorNode;
  }
}
