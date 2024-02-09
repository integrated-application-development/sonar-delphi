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
package au.com.integradev.delphi.cfg.api;

import au.com.integradev.delphi.cfg.block.TerminatorKind;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;

/**
 * Some {@link Block}s are terminated by a particular control flow operation, e.g., a {@code goto}.
 */
public interface Terminated {
  /**
   * The node that terminates this block
   *
   * @return the terminator
   */
  DelphiNode getTerminator();

  /**
   * The type of terminator for this block
   *
   * @return the kind of the terminator
   */
  TerminatorKind getTerminatorKind();
}
