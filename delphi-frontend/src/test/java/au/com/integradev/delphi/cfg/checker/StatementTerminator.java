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
package au.com.integradev.delphi.cfg.checker;

import au.com.integradev.delphi.cfg.block.TerminatorKind;

public enum StatementTerminator {
  EXIT("System.Exit", TerminatorKind.EXIT),
  BREAK("System.Break", TerminatorKind.BREAK),
  HALT("System.Halt", TerminatorKind.HALT),
  CONTINUE("System.Continue", TerminatorKind.CONTINUE);

  private final String routineName;
  private final TerminatorKind terminatorKind;

  StatementTerminator(String routineName, TerminatorKind terminatorKind) {
    this.routineName = routineName;
    this.terminatorKind = terminatorKind;
  }

  public String getRoutineName() {
    return routineName;
  }

  public TerminatorKind getTerminatorKind() {
    return terminatorKind;
  }
}
