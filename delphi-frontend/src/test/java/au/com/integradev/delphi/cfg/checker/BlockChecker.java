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
package au.com.integradev.delphi.cfg.checker;

import static org.assertj.core.api.Assertions.*;

import au.com.integradev.delphi.cfg.api.Block;
import au.com.integradev.delphi.cfg.api.Branch;
import au.com.integradev.delphi.cfg.api.Cases;
import au.com.integradev.delphi.cfg.api.ExitPath;
import au.com.integradev.delphi.cfg.api.Linear;
import au.com.integradev.delphi.cfg.api.Sink;
import au.com.integradev.delphi.cfg.api.Successors;
import au.com.integradev.delphi.cfg.api.Terminated;
import au.com.integradev.delphi.cfg.api.UnconditionalJump;
import au.com.integradev.delphi.cfg.api.UnknownException;
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
    assertThat(block.getElements())
        .as(getBlockId(block) + " is expected to have " + elementCheckers.size() + " elements")
        .hasSize(elementCheckers.size());
    for (int elementId = 0; elementId < elementCheckers.size(); elementId++) {
      elementCheckers
          .get(elementId)
          .withBlockId(block.getId(), elementId)
          .check(block.getElements().get(elementId));
    }
    assertThat(successorChecker)
        .as(getBlockId(block) + " should have its successors declared")
        .isNotNull();
    successorChecker.check(block);

    if (terminatorChecker != null) {
      terminatorChecker.check(block);
    } else {
      assertThat(block.getSuccessors())
          .as(getBlockId(block) + " should have its terminator specified")
          .isNotInstanceOf(Terminated.class);
    }
    terminatorNodeChecks.forEach(check -> check.check(block));
  }

  public BlockChecker succeedsTo(int successor) {
    this.successorChecker =
        new BlockDetailChecker(
            block -> {
              Linear branch = assertBlockSuccessorIsType(block, Linear.class);
              assertThat(branch.getSuccessor().getId())
                  .as(getBlockId(block) + " is expected to have successor of B" + successor)
                  .isEqualTo(successor);
            });
    return this;
  }

  public BlockChecker succeedsToWithExit(int successor, int exitSuccessor) {
    this.successorChecker =
        new BlockDetailChecker(
            block -> {
              ExitPath branch = assertBlockSuccessorIsType(block, ExitPath.class);
              assertThat(branch.getSuccessor().getId())
                  .as(getBlockId(block) + " is expected to have successor of B" + successor)
                  .isEqualTo(successor);
              assertThat(branch.getExitSuccessor().getId())
                  .as(
                      getBlockId(block)
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
              Branch branch = assertBlockSuccessorIsType(block, Branch.class);
              assertThat(branch.getTrueBlock().getId())
                  .as(getBlockId(block) + " is expected to have true successor of B" + trueBlock)
                  .isEqualTo(trueBlock);
              assertThat(branch.getFalseBlock().getId())
                  .as(getBlockId(block) + " is expected to have false successor of B" + falseBlock)
                  .isEqualTo(falseBlock);
            });
    return this;
  }

  public BlockChecker jumpsTo(int successor, int successorWithoutJump) {
    this.successorChecker =
        new BlockDetailChecker(
            block -> {
              UnconditionalJump branch = assertBlockSuccessorIsType(block, UnconditionalJump.class);
              assertThat(branch.getSuccessor().getId())
                  .as(getBlockId(block) + " is expected to have successor of B" + successor)
                  .isEqualTo(successor);
              assertThat(branch.getSuccessorWithoutJump().getId())
                  .as(
                      getBlockId(block)
                          + " is expected to have successor without jump of B"
                          + successorWithoutJump)
                  .isEqualTo(successorWithoutJump);
            });
    return this;
  }

  public BlockChecker succeedsToCases(int... cases) {
    Set<Integer> expectedCases = Arrays.stream(cases).boxed().collect(Collectors.toSet());
    this.successorChecker =
        new BlockDetailChecker(
            block -> {
              Cases caseSuccessor = assertBlockSuccessorIsType(block, Cases.class);
              Set<Integer> caseIds =
                  caseSuccessor.getCaseSuccessors().stream()
                      .map(Block::getId)
                      .collect(Collectors.toSet());
              assertThat(caseIds)
                  .as(
                      getBlockId(block)
                          + " is expected to have case successors of ["
                          + expectedCases.stream()
                              .map(id -> "B" + id)
                              .collect(Collectors.joining(", "))
                          + "]")
                  .containsExactlyInAnyOrderElementsOf(expectedCases);
            });
    return this;
  }

  public BlockChecker succeedsToWithExceptions(int successor, int... unknownExceptions) {
    Set<Integer> exceptions = Arrays.stream(unknownExceptions).boxed().collect(Collectors.toSet());
    this.successorChecker =
        new BlockDetailChecker(
            block -> {
              UnknownException branch = assertBlockSuccessorIsType(block, UnknownException.class);
              assertThat(branch.getSuccessor().getId())
                  .as(getBlockId(block) + " is expected to have successor of B" + successor)
                  .isEqualTo(successor);
              Set<Integer> blockIds =
                  branch.getExceptions().stream().map(Block::getId).collect(Collectors.toSet());
              assertThat(blockIds)
                  .as(
                      getBlockId(block)
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
    successorChecker =
        new BlockDetailChecker(block -> assertBlockSuccessorIsType(block, Sink.class));
    return this;
  }

  private <T extends Successors> T assertBlockSuccessorIsType(Block block, Class<T> type) {
    Successors blockSuccessor = block.getSuccessors();
    assertThat(blockSuccessor)
        .as(getBlockId(block) + " is expected to have a successor of " + type.getTypeName())
        .isInstanceOf(type);
    return type.cast(blockSuccessor);
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
                  .as(
                      getBlockId(block)
                          + " is expected to be terminated with kind "
                          + terminator.getTerminatorKind())
                  .isEqualTo(terminator.getTerminatorKind());

              assertThat(terminated.getTerminator())
                  .as(getBlockId(block) + " is expected to be terminated with name reference")
                  .isInstanceOf(NameReferenceNode.class);
              NameReferenceNode nameReferenceNode = (NameReferenceNode) terminated.getTerminator();
              assertThat(nameReferenceNode.getLastName().getNameDeclaration())
                  .as(getBlockId(block) + " is expected to be terminated with routine")
                  .isInstanceOf(RoutineNameDeclaration.class);
              assertThat(
                      ((RoutineNameDeclaration)
                              nameReferenceNode.getLastName().getNameDeclaration())
                          .fullyQualifiedName())
                  .as(
                      getBlockId(block)
                          + " is expected to be terminated with "
                          + terminator.getMethodName())
                  .isEqualTo(terminator.getMethodName());
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
                  .as(
                      getBlockId(block)
                          + " is expected to be terminated with "
                          + terminatorClass.getTypeName())
                  .isInstanceOf(terminatorClass);
              assertThat(terminated.getTerminatorKind())
                  .as(getBlockId(block) + " is expected to be terminated with kind " + kind)
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
    Successors blockSuccessor = block.getSuccessors();
    assertThat(blockSuccessor)
        .as(getBlockId(block) + " is expected to be terminated")
        .isInstanceOf(Terminated.class);
    return (Terminated) blockSuccessor;
  }

  private String getBlockId(Block block) {
    return "B" + block.getId();
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
