/*
 * Sonar Delphi Plugin
 * Copyright (C) 2024 Integrated Application Development
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** A block whose control flow depends on previous exceptions, e.g., <code>finally</code> block */
public interface Finally extends Block {
  /** Next block without exceptional circumstances */
  Block getSuccessor();

  /** Next block with exceptional circumstances */
  Block getExceptionSuccessor();

  @Override
  default Set<Block> getSuccessors() {
    Set<Block> successors = new HashSet<>();
    successors.add(getSuccessor());
    successors.add(getExceptionSuccessor());
    return Collections.unmodifiableSet(successors);
  }
}
