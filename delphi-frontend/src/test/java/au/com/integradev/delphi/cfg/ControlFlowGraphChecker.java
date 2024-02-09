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
package au.com.integradev.delphi.cfg;

import static org.assertj.core.api.Assertions.*;

import au.com.integradev.delphi.cfg.ControlFlowGraphImpl.Block;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.sonar.plugins.communitydelphi.api.ast.BinaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.CaseStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ForInStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ForStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ForToStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.GotoStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.IdentifierNode;
import org.sonar.plugins.communitydelphi.api.ast.IfStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.IntegerLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.RaiseStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.RepeatStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.WhileStatementNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;

public class ControlFlowGraphChecker {
  public static CFGChecker checker(BlockChecker... blocks) {
    return new CFGChecker(blocks);
  }

  public static BlockChecker block(ElementChecker... elements) {
    return new BlockChecker(elements);
  }

  public static BlockChecker terminator(Class<? extends DelphiNode> kind, int... successorIDs) {
    return new BlockChecker(kind, successorIDs);
  }

  public static BlockChecker terminator(
      Class<? extends DelphiNode> kind, Predicate<DelphiNode> predicate, int... successorIDs) {
    return new BlockChecker(kind, predicate, successorIDs);
  }

  public static BlockChecker terminator(StatementTerminator kind, int... successorIDs) {
    return new BlockChecker().terminator(kind).successors(successorIDs);
  }

  public static ElementChecker element(Class<? extends DelphiNode> kind, String name) {
    return new ElementChecker(kind, name);
  }

  public static ElementChecker element(Class<? extends DelphiNode> kind) {
    return new ElementChecker(kind, (node) -> true);
  }

  public static ElementChecker element(
      Class<? extends DelphiNode> kind, Predicate<DelphiNode> predicate) {
    return new ElementChecker(kind, predicate);
  }

  public static class CFGChecker {
    private final List<BlockChecker> checkers = new ArrayList<>();

    CFGChecker(BlockChecker... checkers) {
      Collections.addAll(this.checkers, checkers);
    }

    public void check(final ControlFlowGraphImpl cfg) {
      assertThat(cfg.blocks()).as("Expected number of blocks").hasSize(checkers.size() + 1);
      final Iterator<BlockChecker> checkerIterator = checkers.iterator();
      final List<Block> blocks = new ArrayList<>(cfg.blocks());
      final Block exitBlock = blocks.remove(blocks.size() - 1);
      for (final Block block : blocks) {
        checkerIterator.next().check(block);
        checkLinkedBlocks(block.id(), "Successor", cfg.blocks(), block.successors());
        checkLinkedBlocks(block.id(), "Predecessors", cfg.blocks(), block.predecessors());
      }
      assertThat(exitBlock.elements()).isEmpty();
      assertThat(exitBlock.successors()).isEmpty();
      assertThat(cfg.blocks())
          .as("CFG entry block is no longer in the list of blocks!")
          .contains(cfg.entryBlock());
    }

    private void checkLinkedBlocks(
        int id, String type, List<Block> blocks, Set<Block> linkedBlocks) {
      for (Block block : linkedBlocks) {
        assertThat(block)
            .as(type + " block " + id + " is missing from he list of blocks")
            .isIn(blocks);
      }
    }
  }

  public static class BlockChecker {
    private int[] successorIDs = new int[] {};
    private int[] exceptionsIDs = new int[] {};
    private final List<ElementChecker> checkers = new ArrayList<>();

    private TerminatorChecker terminatorChecker;
    private int ifTrue = -1;
    private int ifFalse = -1;

    private int exitId = -1;
    private Integer successorWithoutJump = null;

    private boolean isExceptBlock = false;
    private boolean isFinallyBlock = false;
    private boolean isSink = false;

    BlockChecker(final ElementChecker... checkers) {
      Collections.addAll(this.checkers, checkers);
    }

    BlockChecker(final Class<? extends DelphiNode> kind, final int... ids) {
      this(kind, (node) -> true, ids);
    }

    BlockChecker(
        final Class<? extends DelphiNode> kind, Predicate<DelphiNode> predicate, final int... ids) {
      successors(ids);
      terminator(kind, predicate);
    }

    BlockChecker ifTrue(final int id) {
      if (successorIDs.length > 0) {
        throw new IllegalArgumentException("Cannot mix true/false with generic successors!");
      }
      ifTrue = id;
      return this;
    }

    BlockChecker ifFalse(final int id) {
      if (successorIDs.length > 0) {
        throw new IllegalArgumentException("Cannot mix true/false with generic successors!");
      }
      ifFalse = id;
      return this;
    }

    BlockChecker exit(final int id) {
      exitId = id;
      return this;
    }

    BlockChecker terminator(final StatementTerminator kind) {
      this.terminatorChecker = new StatementTerminatorChecker(kind);
      return this;
    }

    BlockChecker terminator(final Class<? extends DelphiNode> kind) {
      this.terminatorChecker = new TerminatorChecker(kind);
      return this;
    }

    BlockChecker terminator(
        final Class<? extends DelphiNode> kind, Predicate<DelphiNode> predicate) {
      this.terminatorChecker = new TerminatorChecker(kind, predicate);
      return this;
    }

    BlockChecker successors(final int... ids) {
      if (ifTrue != -1 || ifFalse != -1) {
        throw new IllegalArgumentException("Cannot mix true/false with generic successors!");
      }
      successorIDs = new int[ids.length];
      int n = 0;
      for (int i : ids) {
        successorIDs[n++] = i;
      }
      Arrays.sort(successorIDs);
      return this;
    }

    BlockChecker successorWithoutJump(final int id) {
      this.successorWithoutJump = id;
      return this;
    }

    BlockChecker exceptions(final int... ids) {
      exceptionsIDs = new int[ids.length];
      int n = 0;
      for (int i : ids) {
        exceptionsIDs[n++] = i;
      }
      Arrays.sort(exceptionsIDs);
      return this;
    }

    BlockChecker isExceptBlock() {
      isExceptBlock = true;
      return this;
    }

    BlockChecker isFinallyBlock() {
      isFinallyBlock = true;
      return this;
    }

    BlockChecker isSink() {
      isSink = true;
      return this;
    }

    public void check(final Block block) {
      assertThat(block.elements())
          .as("Expected number of elements in block " + block.id())
          .hasSize(checkers.size());
      final Iterator<ElementChecker> checkerIterator = checkers.iterator();
      for (final DelphiNode element : block.elements()) {
        checkerIterator.next().check(element);
      }
      if (successorIDs.length == 0) {
        if (ifTrue != -1) {
          assertThat(block.trueBlock().id())
              .as("Expected true successor block " + block.id())
              .isEqualTo(ifTrue);
        }
        if (ifFalse != -1) {
          assertThat(block.falseBlock().id())
              .as("Expected true successor block " + block.id())
              .isEqualTo(ifFalse);
        }
        if (exitId != -1) {
          assertThat(block.exitBlock().id())
              .as("Expected exit successor block " + block.id())
              .isEqualTo(exitId);
        }
      } else {
        assertThat(block.successors())
            .as("Expected number of successors in block " + block.id())
            .hasSize(successorIDs.length);
        assertThat(block.falseBlock())
            .as("ifFalse must be used when conditional block exists in block " + block.id())
            .isNull();
        assertThat(block.trueBlock())
            .as("ifTrue must be used when conditional block exists in block " + block.id())
            .isNull();
        final int[] actualSuccessorIDs = new int[successorIDs.length];
        int n = 0;
        for (final Block successor : block.successors()) {
          actualSuccessorIDs[n++] = successor.id();
        }
        Arrays.sort(actualSuccessorIDs);
        assertThat(actualSuccessorIDs)
            .as("Expected successors in block " + block.id())
            .isEqualTo(successorIDs);
      }
      assertThat(block.exceptions().stream().mapToInt(Block::id).sorted().toArray())
          .as("Expected exceptions in block " + block.id())
          .isEqualTo(exceptionsIDs);
      if (terminatorChecker != null) {
        terminatorChecker.check(block.terminator());
      }
      if (isExceptBlock) {
        assertThat(block.isExceptBlock())
            .as("Block B" + block.id() + " expected to be flagged as 'except' block")
            .isTrue();
      }
      if (isFinallyBlock) {
        assertThat(block.isFinallyBlock())
            .as("Block B" + block.id() + " expected to be flagged as 'finally' block")
            .isTrue();
      }
      if (isSink) {
        assertThat(block.isSink()).as("Expected sink block " + block.id()).isTrue();
      }
      if (successorWithoutJump != null) {
        assertThat(block.successorWithoutJump()).isNotNull();
        assertThat(block.successorWithoutJump().id()).isEqualTo(successorWithoutJump);
      }
    }
  }

  public static class ElementChecker {
    private final Class<? extends DelphiNode> kind;
    private final Predicate<DelphiNode> predicate;

    ElementChecker(Class<? extends DelphiNode> kind, String value) {
      this(kind, (node) -> value == null || node.getImage().equals(value));
    }

    ElementChecker(Class<? extends DelphiNode> kind, Predicate<DelphiNode> predicate) {
      this.kind = kind;
      this.predicate = predicate;
    }

    public void check(final DelphiNode element) {
      assertThat(element).as("Element kind").isInstanceOf(kind);
      assertThat(predicate.test(element)).isTrue();
    }
  }

  public static class TerminatorChecker {
    private final Class<? extends DelphiNode> kind;
    private final Predicate<DelphiNode> predicate;
    private static final List<Class<? extends DelphiNode>> TERMINATOR_TYPES =
        List.of(
            IfStatementNode.class,
            BinaryExpressionNode.class,
            CaseStatementNode.class,
            ForStatementNode.class,
            ForToStatementNode.class,
            ForInStatementNode.class,
            WhileStatementNode.class,
            RepeatStatementNode.class,
            RaiseStatementNode.class,
            GotoStatementNode.class,
            // For Exit, Continue, and Break
            NameReferenceNode.class,
            // For labels
            IdentifierNode.class,
            IntegerLiteralNode.class);

    private TerminatorChecker(final Class<? extends DelphiNode> kind) {
      this(kind, (node) -> true);
    }

    private TerminatorChecker(
        final Class<? extends DelphiNode> kind, Predicate<DelphiNode> predicate) {
      this.kind = kind;
      this.predicate = predicate;
      if (!TERMINATOR_TYPES.contains(kind)) {
        throw new IllegalArgumentException("Unexpected terminator kind!");
      }
    }

    public void check(final DelphiNode element) {
      assertThat(element).as("Element kind").isNotNull();
      assertThat(element).as("Element kind").isInstanceOf(kind);
      assertThat(predicate.test(element)).isTrue();
    }
  }

  public enum StatementTerminator {
    EXIT("System.Exit"),
    BREAK("System.Break"),
    HALT("System.Halt"),
    CONTINUE("System.Continue");

    private final String methodName;

    StatementTerminator(String methodName) {
      this.methodName = methodName;
    }
  }

  public static class StatementTerminatorChecker extends TerminatorChecker {
    private final StatementTerminator kind;

    StatementTerminatorChecker(StatementTerminator kind) {
      super(NameReferenceNode.class);
      this.kind = kind;
    }

    @Override
    public void check(final DelphiNode node) {
      super.check(node);

      NameDeclaration declarationNode = ((NameReferenceNode) node).getNameDeclaration();
      assertThat(declarationNode)
          .as("Name reference node kind")
          .isInstanceOf(RoutineNameDeclaration.class);
      assertThat(((RoutineNameDeclaration) declarationNode).fullyQualifiedName())
          .isEqualTo(kind.methodName);
    }
  }
}
