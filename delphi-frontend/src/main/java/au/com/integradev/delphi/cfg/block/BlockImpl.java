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
package au.com.integradev.delphi.cfg.block;

import au.com.integradev.delphi.cfg.api.Block;
import au.com.integradev.delphi.cfg.api.Successors;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonarsource.analyzer.commons.collections.ListUtils;

public class BlockImpl implements Block, Successors {
  private int id;
  private final List<DelphiNode> elements = new ArrayList<>();
  private Successors successors;
  private Set<Block> predecessors = new HashSet<>();

  public BlockImpl(int id, Successors successors) {
    this.id = id;
    this.successors = successors;
  }

  @Override
  public int getId() {
    return this.id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @Override
  public List<DelphiNode> getElements() {
    return Collections.unmodifiableList(ListUtils.reverse(elements));
  }

  @Override
  public Successors getSuccessors() {
    return successors;
  }

  public void setSuccessors(Successors successor) {
    this.successors = successor;
  }

  @Override
  public Set<Block> getSuccessorBlocks() {
    return Collections.unmodifiableSet(successors.getSuccessorBlocks());
  }

  @Override
  public Set<Block> getPredecessorBlocks() {
    return Collections.unmodifiableSet(predecessors);
  }

  /** This is allows the mutation `predecessors` */
  public Set<Block> getPredecessors() {
    return predecessors;
  }

  public void addElement(DelphiNode element) {
    elements.add(element);
  }
}
