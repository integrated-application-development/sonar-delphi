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
import au.com.integradev.delphi.cfg.api.Halt;
import au.com.integradev.delphi.cfg.api.Linear;
import au.com.integradev.delphi.cfg.api.Terminus;
import au.com.integradev.delphi.cfg.api.UnconditionalJump;
import au.com.integradev.delphi.cfg.api.UnknownException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;

public final class ProtoBlockFactory {
  private ProtoBlockFactory() {
    // Utility class
  }

  public static ProtoBlock exitBlock() {
    return new ProtoBlock(TerminusImpl::new, (blocks, block) -> {});
  }

  public static ProtoBlock halt(DelphiNode terminator) {
    return new ProtoBlock(HaltImpl::new, (blocks, block) -> ((HaltImpl) block).setData(terminator));
  }

  public static ProtoBlock branch(
      DelphiNode terminator, ProtoBlock trueBlock, ProtoBlock falseBlock) {
    return new ProtoBlock(
        BranchImpl::new,
        (blocks, block) ->
            ((BranchImpl) block)
                .setData(terminator, blocks.get(trueBlock), blocks.get(falseBlock)));
  }

  public static ProtoBlock finallyBlock(ProtoBlock successor, ProtoBlock finallySuccessor) {
    return new ProtoBlock(
        FinallyImpl::new,
        (blocks, block) ->
            ((FinallyImpl) block).setData(blocks.get(successor), blocks.get(finallySuccessor)));
  }

  public static ProtoBlock linear(ProtoBlock successor) {
    return new ProtoBlock(
        LinearImpl::new, (blocks, block) -> ((LinearImpl) block).setData(blocks.get(successor)));
  }

  public static ProtoBlock jump(DelphiNode terminator, ProtoBlock target, ProtoBlock withoutJump) {
    return new ProtoBlock(
        UnconditionalJumpImpl::new,
        (blocks, block) ->
            ((UnconditionalJumpImpl) block)
                .setData(terminator, blocks.get(target), blocks.get(withoutJump)));
  }

  public static ProtoBlock withExceptions(ProtoBlock successor, Set<ProtoBlock> exceptions) {
    return new ProtoBlock(
        UnknownExceptionImpl::new,
        (blocks, block) ->
            ((UnknownExceptionImpl) block)
                .setData(
                    blocks.get(successor),
                    exceptions.stream().map(blocks::get).collect(Collectors.toSet())));
  }

  public static ProtoBlock cases(
      DelphiNode terminator, Set<ProtoBlock> cases, ProtoBlock fallthrough) {
    return new ProtoBlock(
        CasesImpl::new,
        (blocks, block) ->
            ((CasesImpl) block)
                .setData(
                    terminator,
                    cases.stream().map(blocks::get).collect(Collectors.toSet()),
                    blocks.get(fallthrough)));
  }

  private static String getBlocksString(Collection<Block> block) {
    return block.stream().map(ProtoBlockFactory::getBlockString).collect(Collectors.joining(" "));
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

    @Override
    public String getBlockType() {
      return "UnknownException";
    }
  }

  static class CasesImpl extends BlockImpl implements Cases {
    private Terminator terminator;
    private Set<Block> cases;
    private Block fallthrough;

    public CasesImpl(List<DelphiNode> elements) {
      super(elements);
    }

    private void setData(DelphiNode terminator, Set<Block> cases, Block fallthrough) {
      this.terminator = new Terminator(terminator);
      this.cases = cases;
      this.fallthrough = fallthrough;
    }

    @Override
    public Set<Block> getCaseSuccessors() {
      return Collections.unmodifiableSet(cases);
    }

    @Override
    public Block getFallthroughSuccessor() {
      return fallthrough;
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
      this.fallthrough = getNewTarget(this.fallthrough, inactiveBlock, target);
    }

    @Override
    public String getDescription() {
      return String.format(
          "%n\tcases to: %s%n\tfallthrough to: %s",
          getBlocksString(cases), getBlockString(fallthrough));
    }

    @Override
    public String getBlockType() {
      return "Cases";
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
    public Block getSuccessorIfRemoved() {
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

    @Override
    public String getBlockType() {
      return "UnconditionalJump";
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

    @Override
    public String getBlockType() {
      return "Linear";
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

    @Override
    public String getBlockType() {
      return "Finally";
    }
  }

  static class HaltImpl extends BlockImpl implements Halt {
    private Terminator terminator;

    protected HaltImpl(List<DelphiNode> elements) {
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

    @Override
    public String getBlockType() {
      return "Halt";
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

    @Override
    public String getBlockType() {
      return "Terminus";
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

    @Override
    public String getBlockType() {
      return "Branch";
    }
  }
}
