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

import au.com.integradev.delphi.antlr.ast.node.RoutineImplementationNodeImpl;
import au.com.integradev.delphi.cfg.ControlFlowGraphUtils;
import au.com.integradev.delphi.cfg.api.Block;
import au.com.integradev.delphi.cfg.api.ControlFlowGraph;
import au.com.integradev.delphi.cfg.lva.BlockDataFlowVisitor;
import au.com.integradev.delphi.cfg.lva.LiveVariable;
import au.com.integradev.delphi.cfg.lva.LiveVariables;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.AnonymousMethodNode;
import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.FinalizationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.InitializationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.LocalDeclarationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementListNode;
import org.sonar.plugins.communitydelphi.api.ast.UnaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ArrayConstructorType;
import org.sonar.plugins.communitydelphi.api.type.Type.BooleanType;
import org.sonar.plugins.communitydelphi.api.type.Type.IntegerType;
import org.sonar.plugins.communitydelphi.api.type.Type.PointerType;

@Rule(key = "DeadStore")
public class DeadStoreCheck extends DelphiCheck {
  private final Map<RoutineNameDeclaration, RoutineImplementationNodeImpl> subRoutines =
      new HashMap<>();

  @Override
  public DelphiCheckContext visit(RoutineImplementationNode node, DelphiCheckContext data) {
    ControlFlowGraph cfg = ControlFlowGraphUtils.findContainingCFG(node);
    addIssues(cfg, data);
    return super.visit(node, data);
  }

  @Override
  public DelphiCheckContext visit(StatementListNode node, DelphiCheckContext data) {
    if (node.getParent() instanceof InitializationSectionNode
        || node.getParent() instanceof FinalizationSectionNode) {
      ControlFlowGraph cfg = ControlFlowGraphUtils.findContainingCFG(node);
      addIssues(cfg, data);
    }
    return super.visit(node, data);
  }

  @Override
  public DelphiCheckContext visit(AnonymousMethodNode node, DelphiCheckContext data) {
    ControlFlowGraph cfg = ControlFlowGraphUtils.findContainingCFG(node);
    addIssues(cfg, data);
    return super.visit(node, data);
  }

  @Override
  public DelphiCheckContext visit(CompoundStatementNode node, DelphiCheckContext data) {
    if (node.getParent() instanceof DelphiAst) {
      ControlFlowGraph cfg = ControlFlowGraphUtils.findContainingCFG(node);
      addIssues(cfg, data);
    }
    return super.visit(node, data);
  }

  private void addIssues(ControlFlowGraph cfg, DelphiCheckContext data) {
    if (cfg == null) {
      return;
    }

    LiveVariables liveVariables = LiveVariables.analyze(cfg);
    Set<LiveVariable> deadStores = new HashSet<>();
    subRoutines.clear();
    streamSubRoutines(cfg)
        .forEach(subRoutine -> subRoutines.put(subRoutine.getRoutineNameDeclaration(), subRoutine));

    for (Block block : cfg.getBlocks()) {
      deadStores.addAll(getDeadStores(block, liveVariables));
    }

    raiseIssues(deadStores, data);
  }

  private List<LiveVariable> getDeadStores(Block block, LiveVariables liveVariables) {
    Set<LiveVariable> blockOutputs = liveVariables.getBlockOutputs(block);
    Map<NameDeclaration, LiveVariable> unusedAssignments = new TreeMap<>(Comparator.naturalOrder());
    List<LiveVariable> deadStores = new ArrayList<>();

    Consumer<LiveVariable> onAssign =
        liveVariable -> {
          NameDeclaration declaration = getTargetDeclaration(liveVariable, true);
          if (declaration == null) {
            return;
          }

          LiveVariable unusedAssignment = unusedAssignments.put(declaration, liveVariable);
          if (unusedAssignment != null && unusedAssignment != liveVariable) {
            deadStores.add(unusedAssignment);
          }
        };
    Consumer<LiveVariable> onReference =
        liveVariable -> {
          NameDeclaration declaration = getTargetDeclaration(liveVariable, false);
          if (declaration == null) {
            return;
          }

          unusedAssignments.remove(declaration);
          if (declaration instanceof RoutineNameDeclaration
              || (declaration instanceof VariableNameDeclaration
                  && ((VariableNameDeclaration) declaration).getType().isProcedural())) {
            handleRoutineReference(liveVariables, liveVariable, unusedAssignments);
          }
        };

    new BlockDataFlowVisitor().setOnAssign(onAssign).setOnReference(onReference).visit(block);

    // If an assignment is used in another block, it isn't unused
    unusedAssignments.values().stream()
        .filter(entry -> !blockOutputs.contains(entry))
        .filter(liveVariable -> !isExcludedDeadStore(liveVariables, liveVariable))
        .forEach(deadStores::add);

    return deadStores;
  }

  private static void raiseIssues(Set<LiveVariable> deadStores, DelphiCheckContext data) {
    for (LiveVariable deadStore : deadStores) {
      String name = deadStore.getNameDeclaration().getName();
      data.newIssue()
          .onFilePosition(deadStore.getFilePosition())
          .withMessage("Remove redundant assignment to '%s'", name)
          .report();
    }
  }

  private static NameDeclaration getTargetDeclaration(LiveVariable liveVariable, boolean isAssign) {
    NameDeclaration declaration = liveVariable.getNameDeclaration();
    if (declaration instanceof PropertyNameDeclaration) {
      PropertyNameDeclaration propertyDeclaration = (PropertyNameDeclaration) declaration;
      NameDeclaration readDeclaration = propertyDeclaration.getReadDeclaration();
      NameDeclaration writeDeclaration = propertyDeclaration.getWriteDeclaration();
      if (readDeclaration instanceof VariableNameDeclaration
          && writeDeclaration instanceof VariableNameDeclaration) {
        if (((VariableNameDeclaration) readDeclaration).getType().isArray()
            || ((VariableNameDeclaration) writeDeclaration).getType().isArray()) {
          // Cannot discern array elements, therefore they are excluded
          return null;
        }
        if (isAssign) {
          return writeDeclaration;
        } else {
          return readDeclaration;
        }
      } else {
        // Properties that aren't just a passthrough to a variable are excluded
        return null;
      }
    }
    return declaration;
  }

  private void handleRoutineReference(
      LiveVariables liveVariables,
      LiveVariable liveVariable,
      Map<NameDeclaration, LiveVariable> unusedAssignments) {
    if (liveVariable.getNameDeclaration() instanceof RoutineNameDeclaration) {
      RoutineNameDeclaration routine = (RoutineNameDeclaration) liveVariable.getNameDeclaration();

      if (subRoutines.containsKey(routine)) {
        Stream.ofNullable(subRoutines.get(routine).getControlFlowGraph())
            .flatMap(cfg -> LiveVariables.analyze(cfg).getBlockInputs(cfg.getEntryBlock()).stream())
            .map(LiveVariable::getNameDeclaration)
            .forEach(unusedAssignments::remove);
        return;
      }
    }

    Set<NameDeclaration> localVariables =
        liveVariables.getLocalScopeVariables().stream()
            .map(LiveVariable::getNameDeclaration)
            .collect(Collectors.toSet());

    unusedAssignments.keySet().retainAll(localVariables);
  }

  private static boolean isExcludedDeadStore(
      LiveVariables liveVariables, LiveVariable liveVariable) {
    NameDeclaration nameDeclaration = liveVariable.getNameDeclaration();

    // Exception handler declarations are excluded
    if (nameDeclaration instanceof VariableNameDeclaration) {
      VariableNameDeclaration variableDeclaration = (VariableNameDeclaration) nameDeclaration;
      if (variableDeclaration.isExceptItem()) {
        return true;
      }
    }

    // Excepted values are excluded
    if (!isExceptedValue(liveVariable)) {
      return false;
    }

    // If the excluded value's variable isn't referenced anywhere, then it isn't excluded
    return liveVariables.getAllAssignments().stream()
        .filter(Predicate.not(liveVariable::equals))
        .map(LiveVariable::getNameDeclaration)
        .anyMatch(liveVariable.getNameDeclaration()::equals);
  }

  private static boolean isExceptedValue(LiveVariable liveVariable) {
    ExpressionNode value = liveVariable.getExpressionNode();

    if (value == null) {
      return false;
    }
    value = value.skipParentheses();
    Type valueType = value.getType();
    if (valueType instanceof BooleanType) {
      // `True`, `False`
      return true;
    } else if (valueType instanceof IntegerType) {
      while (value instanceof UnaryExpressionNode) {
        value = ((UnaryExpressionNode) value).getOperand().skipParentheses();
      }
      // `-1`, `0`, `1`
      return "1".equals(value.getImage()) || "0".equals(value.getImage());
    } else if (valueType instanceof PointerType) {
      // `nil`
      return ((PointerType) valueType).isNilPointer();
    } else if (valueType instanceof ArrayConstructorType) {
      // `[]`
      return ((ArrayConstructorType) valueType).elementTypes().isEmpty();
    } else {
      // `''`
      return "''".equals(value.getImage());
    }
  }

  private static Stream<RoutineImplementationNodeImpl> streamSubRoutines(ControlFlowGraph cfg) {
    return streamLocalDeclarationSection(cfg)
        .flatMap(
            section -> section.findDescendantsOfType(RoutineImplementationNodeImpl.class).stream());
  }

  private static Stream<LocalDeclarationSectionNode> streamLocalDeclarationSection(
      ControlFlowGraph cfg) {
    StatementListNode statementList = cfg.getStatementListNode();
    AnonymousMethodNode anonymous = statementList.getFirstParentOfType(AnonymousMethodNode.class);
    if (anonymous != null) {
      return Stream.ofNullable(anonymous.getDeclarationSection());
    }
    RoutineImplementationNode routine =
        statementList.getFirstParentOfType(RoutineImplementationNode.class);
    if (routine != null) {
      return Stream.ofNullable(routine.getDeclarationSection());
    }

    return Stream.empty();
  }
}
