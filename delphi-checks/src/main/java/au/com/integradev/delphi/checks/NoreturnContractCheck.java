/*
 * Sonar Delphi Plugin
 * Copyright (C) 2026 Integrated Application Development
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
package au.com.integradev.delphi.checks;

import au.com.integradev.delphi.cfg.api.Block;
import au.com.integradev.delphi.cfg.api.ControlFlowGraph;
import au.com.integradev.delphi.cfg.api.Finally;
import au.com.integradev.delphi.cfg.api.PossibleException;
import au.com.integradev.delphi.cfg.api.Terminated;
import au.com.integradev.delphi.cfg.api.UnknownException;
import au.com.integradev.delphi.cfg.block.TerminatorKind;
import au.com.integradev.delphi.utils.ControlFlowGraphUtils;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.RoutineImplementationNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineDirective;

@Rule(key = "NoreturnContract")
public class NoreturnContractCheck extends DelphiCheck {
  private static final String MESSAGE =
      "This routine is marked 'noreturn' but can return normally.";

  @Override
  public DelphiCheckContext visit(RoutineImplementationNode routine, DelphiCheckContext context) {
    if (routine.hasDirective(RoutineDirective.NORETURN) && canReturnNormally(routine)) {
      reportIssue(context, routine.getRoutineNameNode(), MESSAGE);
    }
    return super.visit(routine, context);
  }

  private static boolean canReturnNormally(RoutineImplementationNode routine) {
    ControlFlowGraph cfg = ControlFlowGraphUtils.findContainingCFG(routine);
    return cfg != null && canReturnNormally(cfg, cfg.getEntryBlock());
  }

  private static boolean canReturnNormally(ControlFlowGraph cfg, Block block) {
    if (block.equals(cfg.getExitBlock())) {
      return true;
    }

    Set<Block> successors;

    if (isGuaranteedException(block)) {
      successors =
          block.getSuccessors().stream()
              .filter(s -> notFinallyOrExit(cfg, s))
              .collect(Collectors.toSet());
    } else if (block instanceof PossibleException) {
      successors = new HashSet<>();
      successors.add(((PossibleException) block).getSuccessor());
      ((PossibleException) block)
          .getExceptions().stream().filter(s -> notFinallyOrExit(cfg, s)).forEach(successors::add);
    } else {
      successors = block.getSuccessors();
    }

    return successors.stream().anyMatch(s -> canReturnNormally(cfg, s));
  }

  private static boolean isGuaranteedException(Block block) {
    return block instanceof UnknownException
        || (block instanceof Terminated
            && ((Terminated) block).getTerminatorKind() == TerminatorKind.RAISE);
  }

  private static boolean notFinallyOrExit(ControlFlowGraph cfg, Block block) {
    return !(block instanceof Finally || block.equals(cfg.getExitBlock()));
  }
}
