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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;

public class BuilderBlock {
  private final List<DelphiNode> elements = new ArrayList<>();

  // The construction and the population of the object is separated such that blocks can succeed to
  // each other.
  private Function<List<DelphiNode>, Block> blockSupplier;
  private BiConsumer<Map<BuilderBlock, Block>, Block> dataSetter;

  public BuilderBlock(
      Function<List<DelphiNode>, Block> blockSupplier,
      BiConsumer<Map<BuilderBlock, Block>, Block> dataSetter) {
    this.blockSupplier = blockSupplier;
    this.dataSetter = dataSetter;
  }

  public void addElement(DelphiNode element) {
    this.elements.add(element);
  }

  public void update(BlockBuilder blockBuilder) {
    this.blockSupplier = blockBuilder.build().blockSupplier;
    this.dataSetter = blockBuilder.build().dataSetter;
  }

  public Block buildBlock() {
    return blockSupplier.apply(this.elements);
  }

  public void updateBlockData(Map<BuilderBlock, Block> blockMap) {
    dataSetter.accept(blockMap, blockMap.get(this));
  }
}