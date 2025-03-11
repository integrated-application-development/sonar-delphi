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

import static org.assertj.core.api.Assertions.*;

import au.com.integradev.delphi.cfg.api.Block;
import au.com.integradev.delphi.cfg.api.Branch;
import au.com.integradev.delphi.cfg.api.Cases;
import au.com.integradev.delphi.cfg.api.Finally;
import au.com.integradev.delphi.cfg.api.Halt;
import au.com.integradev.delphi.cfg.api.Linear;
import au.com.integradev.delphi.cfg.api.Terminated;
import au.com.integradev.delphi.cfg.api.UnconditionalJump;
import au.com.integradev.delphi.cfg.api.UnknownException;
import au.com.integradev.delphi.cfg.block.BlockImpl;
import au.com.integradev.delphi.cfg.block.TerminatorKind;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;

public class BlockChecker {
  private BlockDetailChecker successorChecker = null;
  private BlockDetailChecker terminatorChecker = null;
  private final List<BlockDetailChecker> terminatorNodeChecks = new ArrayList<>();
  private final List<ElementChecker> elementCheckers = new ArrayList<>();

  public static BlockChecker block(ElementChecker... elementCheckers) {
    return new BlockChecker(elementCheckers);
  }

  public static <T extends DelphiNode> BlockChecker terminator(Class<T> terminatorClass) {
    return new BlockChecker().withTerminator(terminatorClass);
  }

  public static <T extends DelphiNode> BlockChecker terminator(
      Class<T> terminatorClass, TerminatorKind kind) {
    return new BlockChecker().withTerminator(terminatorClass, kind);
  }

  public static BlockChecker terminator(StatementTerminator terminatorClass) {
    return new BlockChecker().withTerminator(terminatorClass);
  }

  private BlockChecker(ElementChecker... elementCheckers) {
    Collections.addAll(this.elementCheckers, elementCheckers);
  }

  public void check(final Block block) {
    assertThat(block.getElements()).as("elements count").hasSize(elementCheckers.size());
    for (int elementId = 0; elementId < elementCheckers.size(); elementId++) {
      elementCheckers
          .get(elementId)
          .withBlockId(((BlockImpl) block).getId(), elementId)
          .check(block.getElements().get(elementId));
    }
    assertThat(successorChecker)
        .withFailMessage("%s should have its successors declared", getBlockDisplay(block))
        .isNotNull();
    successorChecker.check(block);

    if (terminatorChecker != null) {
      terminatorChecker.check(block);
    } else {
      assertThat(block)
          .withFailMessage("%s should have its terminator specified", getBlockDisplay(block))
          .isNotInstanceOf(Terminated.class);
    }
    terminatorNodeChecks.forEach(check -> check.check(block));
  }

  public BlockChecker succeedsTo(int successor) {
    this.successorChecker =
        new BlockDetailChecker(
            block -> {
              Linear branch = assertBlockIsType(block, Linear.class);
              assertThat(getBlockId(branch.getSuccessor()))
                  .withFailMessage(
                      getBlockDisplay(block) + " is expected to have successor of B" + successor)
                  .isEqualTo(successor);
            });
    return this;
  }

  public BlockChecker succeedsToWithExit(int successor, int exitSuccessor) {
    this.successorChecker =
        new BlockDetailChecker(
            block -> {
              Finally branch = assertBlockIsType(block, Finally.class);
              assertThat(getBlockId(branch.getSuccessor()))
                  .withFailMessage(
                      getBlockDisplay(block) + " is expected to have successor of B" + successor)
                  .isEqualTo(successor);
              assertThat(getBlockId(branch.getExceptionSuccessor()))
                  .withFailMessage(
                      getBlockDisplay(block)
                          + " is expected to have exit successor of B"
                          + exitSuccessor)
                  .isEqualTo(exitSuccessor);
            });
    return this;
  }

  public BlockChecker branchesTo(int trueBlock, int falseBlock) {
    successorChecker =
        new BlockDetailChecker(
            block -> {
              Branch branch = assertBlockIsType(block, Branch.class);
              assertThat(getBlockId(branch.getTrueBlock()))
                  .withFailMessage(
                      getBlockDisplay(block)
                          + " is expected to have true successor of B"
                          + trueBlock)
                  .isEqualTo(trueBlock);
              assertThat(getBlockId(branch.getFalseBlock()))
                  .withFailMessage(
                      getBlockDisplay(block)
                          + " is expected to have false successor of B"
                          + falseBlock)
                  .isEqualTo(falseBlock);
            });
    return this;
  }

  public BlockChecker jumpsTo(int successor, int successorWithoutJump) {
    this.successorChecker =
        new BlockDetailChecker(
            block -> {
              UnconditionalJump branch = assertBlockIsType(block, UnconditionalJump.class);
              assertThat(getBlockId(branch.getSuccessor()))
                  .withFailMessage(
                      getBlockDisplay(block) + " is expected to have successor of B" + successor)
                  .isEqualTo(successor);
              assertThat(getBlockId(branch.getSuccessorIfRemoved()))
                  .withFailMessage(
                      getBlockDisplay(block)
                          + " is expected to have successor without jump of B"
                          + successorWithoutJump)
                  .isEqualTo(successorWithoutJump);
            });
    return this;
  }

  public BlockChecker succeedsToCases(int fallthrough, int... cases) {
    Set<Integer> expectedCases = Arrays.stream(cases).boxed().collect(Collectors.toSet());
    this.successorChecker =
        new BlockDetailChecker(
            block -> {
              Cases caseSuccessor = assertBlockIsType(block, Cases.class);
              Set<Integer> caseIds =
                  caseSuccessor.getCaseSuccessors().stream()
                      .map(BlockChecker::getBlockId)
                      .collect(Collectors.toSet());
              assertThat(caseIds)
                  .withFailMessage(
                      getBlockDisplay(block)
                          + " is expected to have case successors of ["
                          + expectedCases.stream()
                              .map(id -> "B" + id)
                              .collect(Collectors.joining(", "))
                          + "]")
                  .containsExactlyInAnyOrderElementsOf(expectedCases);
              assertThat(getBlockId(caseSuccessor.getFallthroughSuccessor()))
                  .withFailMessage(
                      getBlockDisplay(block)
                          + " is expected to have fallthrough successor of B"
                          + fallthrough)
                  .isEqualTo(fallthrough);
            });
    return this;
  }

  public BlockChecker succeedsToWithExceptions(int successor, int... unknownExceptions) {
    Set<Integer> exceptions = Arrays.stream(unknownExceptions).boxed().collect(Collectors.toSet());
    this.successorChecker =
        new BlockDetailChecker(
            block -> {
              UnknownException branch = assertBlockIsType(block, UnknownException.class);
              assertThat(getBlockId(branch.getSuccessor()))
                  .withFailMessage(
                      getBlockDisplay(block) + " is expected to have successor of B" + successor)
                  .isEqualTo(successor);
              Set<Integer> blockIds =
                  branch.getExceptions().stream()
                      .map(BlockChecker::getBlockId)
                      .collect(Collectors.toSet());
              assertThat(blockIds)
                  .withFailMessage(
                      getBlockDisplay(block)
                          + " is expected to have exception successors of ["
                          + exceptions.stream()
                              .map(id -> "B" + id)
                              .collect(Collectors.joining(", "))
                          + "]")
                  .containsExactlyInAnyOrderElementsOf(exceptions);
            });
    return this;
  }

  public BlockChecker isSink() {
    successorChecker = new BlockDetailChecker(block -> assertBlockIsType(block, Halt.class));
    return this;
  }

  private <T extends Block> T assertBlockIsType(Block block, Class<T> type) {
    assertThat(block).as("block type").isInstanceOf(type);
    return type.cast(block);
  }

  public <T extends DelphiNode> BlockChecker withTerminator(Class<T> terminatorClass) {
    return withTerminator(terminatorClass, TerminatorKind.NODE);
  }

  public BlockChecker withTerminator(StatementTerminator terminator) {
    this.terminatorChecker =
        new BlockDetailChecker(
            block -> {
              Terminated terminated = assertBlockTerminated(block);
              assertThat(terminated.getTerminatorKind())
                  .withFailMessage(
                      getBlockDisplay(block)
                          + " is expected to be terminated with kind "
                          + terminator.getTerminatorKind())
                  .isEqualTo(terminator.getTerminatorKind());

              assertThat(terminated.getTerminator())
                  .withFailMessage(
                      getBlockDisplay(block) + " is expected to be terminated with name reference")
                  .isInstanceOf(NameReferenceNode.class);
              NameReferenceNode nameReferenceNode = (NameReferenceNode) terminated.getTerminator();
              assertThat(nameReferenceNode.getLastName().getNameDeclaration())
                  .withFailMessage(
                      getBlockDisplay(block) + " is expected to be terminated with routine")
                  .isInstanceOf(RoutineNameDeclaration.class);
              assertThat(
                      ((RoutineNameDeclaration)
                              nameReferenceNode.getLastName().getNameDeclaration())
                          .fullyQualifiedName())
                  .withFailMessage(
                      getBlockDisplay(block)
                          + " is expected to be terminated with "
                          + terminator.getRoutineName())
                  .isEqualTo(terminator.getRoutineName());
            });
    return this;
  }

  public <T extends DelphiNode> BlockChecker withTerminator(
      Class<T> terminatorClass, TerminatorKind kind) {
    this.terminatorChecker =
        new BlockDetailChecker(
            block -> {
              Terminated terminated = assertBlockTerminated(block);
              assertThat(terminated.getTerminator())
                  .withFailMessage(
                      getBlockDisplay(block)
                          + " is expected to be terminated with "
                          + terminatorClass.getTypeName())
                  .isInstanceOf(terminatorClass);
              assertThat(terminated.getTerminatorKind())
                  .withFailMessage(
                      getBlockDisplay(block) + " is expected to be terminated with kind " + kind)
                  .isEqualTo(kind);
            });
    return this;
  }

  public BlockChecker withTerminatorNodeCheck(Consumer<DelphiNode> extraChecker) {
    this.terminatorNodeChecks.add(
        new BlockDetailChecker(
            block -> {
              Terminated terminated = assertBlockTerminated(block);
              extraChecker.accept(terminated.getTerminator());
            }));
    return this;
  }

  private Terminated assertBlockTerminated(Block block) {
    assertThat(block)
        .withFailMessage(getBlockDisplay(block) + " is expected to be terminated")
        .isInstanceOf(Terminated.class);
    return (Terminated) block;
  }

  private String getBlockDisplay(Block block) {
    return "B" + getBlockId(block);
  }

  private static int getBlockId(Block block) {
    return ((BlockImpl) block).getId();
  }
}

class BlockDetailChecker {
  private final Consumer<Block> toCheck;

  public BlockDetailChecker(Consumer<Block> toCheck) {
    this.toCheck = toCheck;
  }

  public void check(final Block block) {
    this.toCheck.accept(block);
  }
}
