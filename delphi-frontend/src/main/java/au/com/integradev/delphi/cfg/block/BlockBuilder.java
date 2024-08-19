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
import au.com.integradev.delphi.cfg.api.Branch;
import au.com.integradev.delphi.cfg.api.Cases;
import au.com.integradev.delphi.cfg.api.ExitPath;
import au.com.integradev.delphi.cfg.api.Linear;
import au.com.integradev.delphi.cfg.api.Sink;
import au.com.integradev.delphi.cfg.api.Successors;
import au.com.integradev.delphi.cfg.api.Terminus;
import au.com.integradev.delphi.cfg.api.UnconditionalJump;
import au.com.integradev.delphi.cfg.api.UnknownException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;

public class BlockBuilder {
  private final int id;
  private Successors successor;

  public static BlockBuilder newBlock(int id) {
    return new BlockBuilder(id);
  }

  private BlockBuilder(int id) {
    this.id = id;
    this.successor = new TerminusImpl();
  }

  public BlockBuilder withTerminator(DelphiNode terminator) {
    this.successor = new SinkImpl(terminator);
    return this;
  }

  public BlockBuilder withBranch(DelphiNode terminator, Block trueBlock, Block falseBlock) {
    this.successor = new BranchImpl(terminator, trueBlock, falseBlock);
    return this;
  }

  public BlockBuilder withExitPath(Block successor, Block exitSuccessor) {
    this.successor = new ExitPathImpl(successor, exitSuccessor);
    return this;
  }

  public BlockBuilder withSuccessor(Block successor) {
    this.successor = new LinearImpl(successor);
    return this;
  }

  public BlockBuilder withJump(DelphiNode terminator, Block target, Block withoutJump) {
    this.successor = new UnconditionalJumpImpl(terminator, target, withoutJump);
    return this;
  }

  public BlockBuilder withExceptions(Block successor, Set<Block> exceptions) {
    this.successor = new UnknownExceptionImpl(successor, exceptions);
    return this;
  }

  public BlockBuilder withCases(DelphiNode terminator, Set<Block> cases) {
    this.successor = new CasesImpl(terminator, cases);
    return this;
  }

  public Block build() {
    return new BlockImpl(id, successor);
  }

  public abstract static class AbstractSuccessor implements Successors {
    public abstract Successors replaceInactiveBlock(Block inactiveBlock, Block target);

    public abstract String getDescription();

    final Block getNewTarget(Block subject, Block inactiveBlock, Block target) {
      if (subject == inactiveBlock) return target;
      return subject;
    }

    final String getBlocksString(Collection<Block> block) {
      return block.stream().map(this::getBlockString).collect(Collectors.joining(" "));
    }

    final String getBlockString(Block block) {
      return "B" + block.getId();
    }
  }

  static class UnknownExceptionImpl extends AbstractSuccessor implements UnknownException {
    private final Block successor;
    private final Set<Block> exceptions;

    public UnknownExceptionImpl(Block successor, Set<Block> exceptions) {
      this.successor = successor;
      this.exceptions = exceptions;
    }

    @Override
    public Block getSuccessor() {
      return successor;
    }

    @Override
    public Set<Block> getExceptions() {
      return Collections.unmodifiableSet(exceptions);
    }

    @Override
    public Successors replaceInactiveBlock(Block inactiveBlock, Block target) {
      if (exceptions.contains(inactiveBlock)) {
        exceptions.remove(inactiveBlock);
        exceptions.add(target);
      }
      return new UnknownExceptionImpl(getNewTarget(successor, inactiveBlock, target), exceptions);
    }

    @Override
    public String getDescription() {
      return String.format(
          "%n\tjumps to: %s%n\texceptions to: %s",
          getBlockString(successor), getBlocksString(exceptions));
    }
  }

  static class CasesImpl extends AbstractSuccessor implements Cases {
    private final Terminator terminator;
    private final Set<Block> cases;

    public CasesImpl(DelphiNode terminator, Set<Block> cases) {
      this(new Terminator(terminator), cases);
    }

    private CasesImpl(Terminator terminator, Set<Block> cases) {
      this.terminator = terminator;
      this.cases = cases;
    }

    @Override
    public Set<Block> getCaseSuccessors() {
      return Collections.unmodifiableSet(cases);
    }

    @Override
    public DelphiNode getTerminator() {
      return terminator.getTerminatorNode();
    }

    @Override
    public TerminatorKind getTerminatorKind() {
      return terminator.getKind();
    }

    @Override
    public Successors replaceInactiveBlock(Block inactiveBlock, Block target) {
      if (cases.contains(inactiveBlock)) {
        cases.remove(inactiveBlock);
        cases.add(target);
      }
      return new CasesImpl(terminator, cases);
    }

    @Override
    public String getDescription() {
      return String.format("%n\tcases to: %s", getBlocksString(cases));
    }
  }

  static class UnconditionalJumpImpl extends AbstractSuccessor implements UnconditionalJump {
    private final Block target;
    private final Block withoutJump;
    private final Terminator terminator;

    public UnconditionalJumpImpl(DelphiNode terminator, Block target, Block withoutJump) {
      this(new Terminator(terminator), target, withoutJump);
    }

    private UnconditionalJumpImpl(Terminator terminator, Block target, Block withoutJump) {
      this.terminator = terminator;
      this.target = target;
      this.withoutJump = withoutJump;
    }

    @Override
    public Block getSuccessor() {
      return target;
    }

    @Override
    public Block getSuccessorWithoutJump() {
      return withoutJump;
    }

    @Override
    public DelphiNode getTerminator() {
      return terminator.getTerminatorNode();
    }

    @Override
    public TerminatorKind getTerminatorKind() {
      return terminator.getKind();
    }

    @Override
    public Successors replaceInactiveBlock(Block inactiveBlock, Block target) {
      return new UnconditionalJumpImpl(
          terminator,
          getNewTarget(this.target, inactiveBlock, target),
          getNewTarget(withoutJump, inactiveBlock, target));
    }

    @Override
    public String getDescription() {
      return String.format(
          "%n\tjumps to: %s%n\twithout jump to: %s",
          getBlockString(target), getBlockString(withoutJump));
    }
  }

  static class LinearImpl extends AbstractSuccessor implements Linear {
    private final Block successor;

    public LinearImpl(Block successor) {
      this.successor = successor;
    }

    @Override
    public Block getSuccessor() {
      return successor;
    }

    @Override
    public Successors replaceInactiveBlock(Block inactiveBlock, Block target) {
      return new LinearImpl(getNewTarget(successor, inactiveBlock, target));
    }

    @Override
    public String getDescription() {
      return String.format("%n\tjumps to: %s", getBlockString(successor));
    }
  }

  static class ExitPathImpl extends AbstractSuccessor implements ExitPath {
    private final Block successor;
    private final Block exitSuccessor;

    public ExitPathImpl(Block successor, Block exitSuccessor) {
      this.successor = successor;
      this.exitSuccessor = exitSuccessor;
    }

    @Override
    public Block getSuccessor() {
      return successor;
    }

    @Override
    public Block getExitSuccessor() {
      return exitSuccessor;
    }

    @Override
    public Successors replaceInactiveBlock(Block inactiveBlock, Block target) {
      return new ExitPathImpl(
          getNewTarget(successor, inactiveBlock, target),
          getNewTarget(exitSuccessor, inactiveBlock, target));
    }

    @Override
    public String getDescription() {
      return String.format(
          "%n\tjumps to: %s%n\texits to: %s",
          getBlockString(successor), getBlockString(exitSuccessor));
    }
  }

  static class SinkImpl extends AbstractSuccessor implements Sink {
    private final Terminator terminator;

    public SinkImpl(DelphiNode terminator) {
      this.terminator = new Terminator(terminator);
    }

    @Override
    public DelphiNode getTerminator() {
      return terminator.getTerminatorNode();
    }

    @Override
    public TerminatorKind getTerminatorKind() {
      return terminator.getKind();
    }

    @Override
    public Successors replaceInactiveBlock(Block inactiveBlock, Block target) {
      return this;
    }

    @Override
    public String getDescription() {
      return String.format("%n\tno successors");
    }
  }

  private static class TerminusImpl extends AbstractSuccessor implements Terminus {
    @Override
    public Successors replaceInactiveBlock(Block inactiveBlock, Block target) {
      return this;
    }

    @Override
    public String getDescription() {
      return String.format("%n\t(Exit)");
    }
  }

  static class BranchImpl extends AbstractSuccessor implements Branch {
    private final Block trueBlock;
    private final Block falseBlock;
    private final Terminator terminator;

    public BranchImpl(DelphiNode terminator, Block trueBlock, Block falseBlock) {
      this(new Terminator(terminator), trueBlock, falseBlock);
    }

    private BranchImpl(Terminator terminator, Block trueBlock, Block falseBlock) {
      this.terminator = terminator;
      this.trueBlock = trueBlock;
      this.falseBlock = falseBlock;
    }

    @Override
    public Block getTrueBlock() {
      return trueBlock;
    }

    @Override
    public Block getFalseBlock() {
      return falseBlock;
    }

    @Override
    public DelphiNode getTerminator() {
      return terminator.getTerminatorNode();
    }

    @Override
    public TerminatorKind getTerminatorKind() {
      return terminator.getKind();
    }

    @Override
    public Successors replaceInactiveBlock(Block inactiveBlock, Block target) {
      return new BranchImpl(
          terminator,
          getNewTarget(trueBlock, inactiveBlock, target),
          getNewTarget(falseBlock, inactiveBlock, target));
    }

    @Override
    public String getDescription() {
      return String.format(
          "%n\tjumps to: %s(true) %s(false)",
          getBlockString(trueBlock), getBlockString(falseBlock));
    }
  }
}
