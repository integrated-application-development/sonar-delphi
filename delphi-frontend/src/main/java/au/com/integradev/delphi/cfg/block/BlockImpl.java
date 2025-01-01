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

import au.com.integradev.delphi.cfg.api.Block;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;

public abstract class BlockImpl implements Block {
  private int id = 0;
  private final List<DelphiNode> elements;
  private final Set<Block> predecessors = new HashSet<>();

  protected BlockImpl(List<DelphiNode> elements) {
    this.elements = elements;
  }

  @Override
  public List<DelphiNode> getElements() {
    return Collections.unmodifiableList(Lists.reverse(elements));
  }

  public void addPredecessor(Block predecessor) {
    predecessors.add(predecessor);
  }

  @Override
  public Set<Block> getPredecessors() {
    return Collections.unmodifiableSet(predecessors);
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public abstract void replaceInactiveSuccessor(Block inactiveBlock, Block target);

  protected static Block getNewTarget(Block subject, Block inactiveBlock, Block target) {
    if (subject == inactiveBlock) {
      return target;
    }
    return subject;
  }

  public abstract String getDescription();
}
