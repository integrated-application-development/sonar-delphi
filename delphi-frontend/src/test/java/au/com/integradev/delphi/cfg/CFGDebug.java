/*
 * SonarQube Java
 * Copyright (C) 2012-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package au.com.integradev.delphi.cfg;

import au.com.integradev.delphi.cfg.ControlFlowGraphImpl.Block;
import java.util.Comparator;
import java.util.stream.Collectors;
import org.sonar.plugins.communitydelphi.api.ast.BinaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;

public class CFGDebug {
  private CFGDebug() {}

  public static String toString(ControlFlowGraphImpl cfg) {
    StringBuilder buffer = new StringBuilder();
    buffer.append("Starts at B");
    buffer.append(cfg.entryBlock().id());
    buffer.append('\n');
    buffer.append('\n');
    for (Block block : cfg.blocks()) {
      buffer.append(toString(block));
    }
    return buffer.toString();
  }

  public static String toString(Block block) {
    StringBuilder buffer = new StringBuilder();
    buffer.append('B');
    buffer.append(block.id());
    if (block.id() == 0) {
      buffer.append(" (Exit):");
    }
    int i = 0;
    for (DelphiNode node : block.elements()) {
      buffer.append('\n');
      buffer.append(i);
      buffer.append(":\t");
      appendKind(buffer, node);
      buffer.append(node.getImage());
      i++;
    }
    DelphiNode terminator = block.terminator();
    if (terminator != null) {
      buffer.append("\nT:\t");
      appendKind(buffer, terminator);
      buffer.append(terminator.getImage());
    }
    boolean first = true;
    for (Block successor :
        block.successors().stream()
            .sorted(Comparator.comparingInt(Block::id).reversed())
            .collect(Collectors.toList())) {
      if (first) {
        first = false;
        buffer.append('\n');
        buffer.append("\tjumps to: ");
      } else {
        buffer.append(' ');
      }
      buffer.append('B');
      buffer.append(successor.id());
      if (successor == block.trueBlock()) {
        buffer.append("(true)");
      }
      if (successor == block.falseBlock()) {
        buffer.append("(false)");
      }
      if (successor == block.exitBlock()) {
        buffer.append("(exit)");
      }
    }
    if (block.successorWithoutJump() != null) {
      buffer.append('\n');
      buffer.append("\tsuccessor without jump to: ");
      buffer.append('B');
      buffer.append(block.successorWithoutJump().id());
    }
    first = true;
    for (Block exception : block.exceptions()) {
      if (first) {
        first = false;
        buffer.append('\n');
        buffer.append("\texceptions to: ");
      } else {
        buffer.append(' ');
      }
      buffer.append('B');
      buffer.append(exception.id());
    }
    buffer.append('\n');
    buffer.append('\n');
    return buffer.toString();
  }

  private static final int MAX_NODETYPENAME = 30;

  private static void appendKind(StringBuilder buffer, DelphiNode node) {
    String name = node.getClass().getSimpleName();
    if (node instanceof BinaryExpressionNode) {
      name += " " + ((BinaryExpressionNode) node).getOperator();
    }
    int n = MAX_NODETYPENAME - name.length();
    buffer.append(name);
    while (--n >= 0) {
      buffer.append(' ');
    }
    buffer.append('\t');
  }
}
