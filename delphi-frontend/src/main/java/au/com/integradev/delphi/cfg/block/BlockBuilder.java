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
import au.com.integradev.delphi.cfg.api.Branch;
import au.com.integradev.delphi.cfg.api.Cases;
import au.com.integradev.delphi.cfg.api.Finally;
import au.com.integradev.delphi.cfg.api.Linear;
import au.com.integradev.delphi.cfg.api.Sink;
import au.com.integradev.delphi.cfg.api.Terminus;
import au.com.integradev.delphi.cfg.api.UnconditionalJump;
import au.com.integradev.delphi.cfg.api.UnknownException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;

public class BlockBuilder {
  private Function<List<DelphiNode>, Block> blockSupplier;
  private BiConsumer<Map<BuilderBlock, Block>, Block> dataSetter;

  public static BlockBuilder newBlock() {
    return new BlockBuilder();
  }

  private BlockBuilder() {
    this.blockSupplier = TerminusImpl::new;
    this.dataSetter = (blocks, block) -> {};
  }

  public BlockBuilder withTerminator(DelphiNode terminator) {
    this.blockSupplier = SinkImpl::new;
    this.dataSetter = (blocks, block) -> ((SinkImpl) block).setData(terminator);
    return this;
  }

  public BlockBuilder withBranch(
      DelphiNode terminator, BuilderBlock trueBlock, BuilderBlock falseBlock) {
    this.blockSupplier = BranchImpl::new;
    this.dataSetter =
        (blocks, block) ->
            ((BranchImpl) block).setData(terminator, blocks.get(trueBlock), blocks.get(falseBlock));
    return this;
  }

  public BlockBuilder withFinallyPath(BuilderBlock successor, BuilderBlock finallySuccessor) {
    this.blockSupplier = FinallyImpl::new;
    this.dataSetter =
        (blocks, block) ->
            ((FinallyImpl) block).setData(blocks.get(successor), blocks.get(finallySuccessor));
    return this;
  }

  public BlockBuilder withSuccessor(BuilderBlock successor) {
    this.blockSupplier = LinearImpl::new;
    this.dataSetter = (blocks, block) -> ((LinearImpl) block).setData(blocks.get(successor));
    return this;
  }

  public BlockBuilder withJump(
      DelphiNode terminator, BuilderBlock target, BuilderBlock withoutJump) {
    this.blockSupplier = UnconditionalJumpImpl::new;
    this.dataSetter =
        (blocks, block) ->
            ((UnconditionalJumpImpl) block)
                .setData(terminator, blocks.get(target), blocks.get(withoutJump));
    return this;
  }

  public BlockBuilder withExceptions(BuilderBlock successor, Set<BuilderBlock> exceptions) {
    this.blockSupplier = UnknownExceptionImpl::new;
    this.dataSetter =
        (blocks, block) ->
            ((UnknownExceptionImpl) block)
                .setData(
                    blocks.get(successor),
                    exceptions.stream().map(blocks::get).collect(Collectors.toSet()));
    return this;
  }

  public BlockBuilder withCases(DelphiNode terminator, Set<BuilderBlock> cases) {
    this.blockSupplier = CasesImpl::new;
    this.dataSetter =
        (blocks, block) ->
            ((CasesImpl) block)
                .setData(terminator, cases.stream().map(blocks::get).collect(Collectors.toSet()));
    return this;
  }

  public BuilderBlock build() {
    return new BuilderBlock(blockSupplier, dataSetter);
  }

  private static String getBlocksString(Collection<Block> block) {
    return block.stream().map(BlockBuilder::getBlockString).collect(Collectors.joining(" "));
  }

  private static String getBlockString(Block block) {
    return "B" + ((BlockImpl) block).getId();
  }

  static class UnknownExceptionImpl extends BlockImpl implements UnknownException {
    private Block successor;
    private Set<Block> exceptions;

    public UnknownExceptionImpl(List<DelphiNode> elements) {
      super(elements);
    }

    public void setData(Block successor, Set<Block> exceptions) {
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
    public void replaceInactiveSuccessor(Block inactiveBlock, Block target) {
      if (exceptions.remove(inactiveBlock)) {
        exceptions.add(target);
      }
      this.successor = getNewTarget(this.successor, inactiveBlock, target);
    }

    @Override
    public String getDescription() {
      return String.format(
          "%n\tjumps to: %s%n\texceptions to: %s",
          getBlockString(successor), getBlocksString(exceptions));
    }
  }

  static class CasesImpl extends BlockImpl implements Cases {
    private Terminator terminator;
    private Set<Block> cases;

    public CasesImpl(List<DelphiNode> elements) {
      super(elements);
    }

    private void setData(DelphiNode terminator, Set<Block> cases) {
      this.terminator = new Terminator(terminator);
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
    public void replaceInactiveSuccessor(Block inactiveBlock, Block target) {
      if (cases.remove(inactiveBlock)) {
        cases.add(target);
      }
    }

    @Override
    public String getDescription() {
      return String.format("%n\tcases to: %s", getBlocksString(cases));
    }
  }

  static class UnconditionalJumpImpl extends BlockImpl implements UnconditionalJump {
    private Block target;
    private Block withoutJump;
    private Terminator terminator;

    public UnconditionalJumpImpl(List<DelphiNode> elements) {
      super(elements);
    }

    private void setData(DelphiNode terminator, Block target, Block withoutJump) {
      this.terminator = new Terminator(terminator);
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
    public void replaceInactiveSuccessor(Block inactiveBlock, Block target) {
      this.target = getNewTarget(this.target, inactiveBlock, target);
      this.withoutJump = getNewTarget(this.withoutJump, inactiveBlock, target);
    }

    @Override
    public String getDescription() {
      return String.format(
          "%n\tjumps to: %s%n\twithout jump to: %s",
          getBlockString(target), getBlockString(withoutJump));
    }
  }

  static class LinearImpl extends BlockImpl implements Linear {
    private Block successor;

    protected LinearImpl(List<DelphiNode> elements) {
      super(elements);
    }

    public void setData(Block successor) {
      this.successor = successor;
    }

    @Override
    public Block getSuccessor() {
      return successor;
    }

    @Override
    public void replaceInactiveSuccessor(Block inactiveBlock, Block target) {
      this.successor = getNewTarget(this.successor, inactiveBlock, target);
    }

    @Override
    public String getDescription() {
      return String.format("%n\tjumps to: %s", getBlockString(successor));
    }
  }

  static class FinallyImpl extends BlockImpl implements Finally {
    private Block successor;
    private Block exceptionSuccessor;

    protected FinallyImpl(List<DelphiNode> elements) {
      super(elements);
    }

    public void setData(Block successor, Block exitSuccessor) {
      this.successor = successor;
      this.exceptionSuccessor = exitSuccessor;
    }

    @Override
    public Block getSuccessor() {
      return successor;
    }

    @Override
    public Block getExceptionSuccessor() {
      return exceptionSuccessor;
    }

    @Override
    public void replaceInactiveSuccessor(Block inactiveBlock, Block target) {
      this.successor = getNewTarget(this.successor, inactiveBlock, target);
      this.exceptionSuccessor = getNewTarget(this.exceptionSuccessor, inactiveBlock, target);
    }

    @Override
    public String getDescription() {
      return String.format(
          "%n\tjumps to: %s%n\texits to: %s",
          getBlockString(successor), getBlockString(exceptionSuccessor));
    }
  }

  static class SinkImpl extends BlockImpl implements Sink {
    private Terminator terminator;

    protected SinkImpl(List<DelphiNode> elements) {
      super(elements);
    }

    public void setData(DelphiNode terminator) {
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
    public void replaceInactiveSuccessor(Block inactiveBlock, Block target) {
      // Block has no successors
    }

    @Override
    public String getDescription() {
      return String.format("%n\tno successors");
    }
  }

  private static class TerminusImpl extends BlockImpl implements Terminus {

    public TerminusImpl(List<DelphiNode> elements) {
      super(elements);
    }

    @Override
    public void replaceInactiveSuccessor(Block inactiveBlock, Block target) {
      // Block has no successors
    }

    @Override
    public String getDescription() {
      return String.format("%n\t(Exit)");
    }
  }

  static class BranchImpl extends BlockImpl implements Branch {
    private Block trueBlock;
    private Block falseBlock;
    private Terminator terminator;

    public BranchImpl(List<DelphiNode> elements) {
      super(elements);
    }

    private void setData(DelphiNode terminator, Block trueBlock, Block falseBlock) {
      this.terminator = new Terminator(terminator);
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
    public void replaceInactiveSuccessor(Block inactiveBlock, Block target) {
      this.trueBlock = getNewTarget(this.trueBlock, inactiveBlock, target);
      this.falseBlock = getNewTarget(this.falseBlock, inactiveBlock, target);
    }

    @Override
    public String getDescription() {
      return String.format(
          "%n\tjumps to: %s(true) %s(false)",
          getBlockString(trueBlock), getBlockString(falseBlock));
    }
  }
}
