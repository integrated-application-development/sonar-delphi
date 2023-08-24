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
package org.sonar.plugins.communitydelphi.api.directive;

import java.util.Optional;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;

/**
 * Parses a CompilerDirective object from a {@code COMPILER_DIRECTIVE} token.
 *
 * <p>Example: A token with the text "{$include unit.pas}" will create an {@link IncludeDirective}.
 */
public interface CompilerDirectiveParser {
  /**
   * Produce a compiler directive from a string
   *
   * @param token token to parse into a CompilerDirective object
   * @return compiler directive
   */
  Optional<CompilerDirective> parse(DelphiToken token);
}
