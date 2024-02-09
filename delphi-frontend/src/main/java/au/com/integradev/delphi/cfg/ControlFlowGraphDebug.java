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
package au.com.integradev.delphi.cfg;

import au.com.integradev.delphi.cfg.api.Block;
import au.com.integradev.delphi.cfg.api.ControlFlowGraph;
import au.com.integradev.delphi.cfg.api.Terminated;
import au.com.integradev.delphi.cfg.block.BlockImpl;
import java.util.Optional;
import java.util.stream.IntStream;
import org.sonar.plugins.communitydelphi.api.ast.BinaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;

public final class ControlFlowGraphDebug {
  private static final int MAX_NODE_TYPE_NAME = 30;

  private ControlFlowGraphDebug() {
    // Utility class
  }

  public static String toString(ControlFlowGraph cfg) {
    StringBuilder buffer = new StringBuilder();
    buffer.append("Starts at ");
    buffer.append(getBlockString(cfg.getEntryBlock()));
    buffer.append('\n');
    buffer.append('\n');
    for (Block block : cfg.getBlocks()) {
      buffer.append(toString(block));
    }
    return buffer.toString();
  }

  public static String toString(Block block) {
    StringBuilder buffer = new StringBuilder();
    buffer.append(getBlockString(block));

    IntStream.range(0, block.getElements().size())
        .forEach(index -> appendElement(buffer, index, block.getElements().get(index)));

    getAs(block, Terminated.class)
        .ifPresent(
            successors -> {
              buffer.append("\nT:\t");
              appendKind(buffer, successors.getTerminator());
              buffer.append(successors.getTerminator().getImage());
            });

    buffer.append(getAs(block, BlockImpl.class).orElseThrow().getDescription());
    buffer.append("\n\n");
    return buffer.toString();
  }

  private static void appendKind(StringBuilder buffer, DelphiNode node) {
    String name = node.getClass().getSimpleName();
    if (node instanceof BinaryExpressionNode) {
      name += " " + ((BinaryExpressionNode) node).getOperator();
    }
    buffer.append(String.format("%-" + MAX_NODE_TYPE_NAME + "s\t", name));
  }

  private static void appendElement(StringBuilder buffer, int index, DelphiNode node) {
    buffer.append('\n');
    buffer.append(index);
    buffer.append(":\t");
    appendKind(buffer, node);
    buffer.append(node.getImage());
  }

  private static String getBlockString(Block block) {
    return "B" + ((BlockImpl) block).getId();
  }

  private static <T> Optional<T> getAs(Block block, Class<T> clazz) {
    if (clazz.isInstance(block)) {
      return Optional.of(clazz.cast(block));
    }
    return Optional.empty();
  }
}
