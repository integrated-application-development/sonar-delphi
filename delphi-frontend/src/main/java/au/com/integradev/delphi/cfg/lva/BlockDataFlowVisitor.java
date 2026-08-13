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
package au.com.integradev.delphi.cfg.lva;

import static au.com.integradev.delphi.cfg.lva.LiveVariableUtils.newLiveVariableSet;

import au.com.integradev.delphi.antlr.ast.node.AnonymousMethodNodeImpl;
import au.com.integradev.delphi.cfg.api.Block;
import au.com.integradev.delphi.cfg.api.ControlFlowGraph;
import au.com.integradev.delphi.cfg.api.Terminated;
import java.util.Set;
import java.util.function.Consumer;
import org.sonar.plugins.communitydelphi.api.ast.AnonymousMethodNode;
import org.sonar.plugins.communitydelphi.api.ast.AssignmentStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.CommonDelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExceptItemNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.ForLoopVarDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.ForLoopVarNode;
import org.sonar.plugins.communitydelphi.api.ast.ForLoopVarReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.ForStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.RaiseStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.SimpleNameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.TryStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.VarStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.utils.ExpressionNodeUtils;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;

public class BlockDataFlowVisitor {

  private Consumer<LiveVariable> onAssign;
  private Consumer<LiveVariable> onReference;

  private void registerUsage(DelphiNode declaration) {
    if (!(declaration instanceof LiveVariable)) return;
    registerUsage((LiveVariable) declaration);
  }

  private void registerUsage(LiveVariable usage) {
    if (hasVariableReference(usage)) {
      onReference.accept(usage);
    }
  }

  private void registerAssignment(DelphiNode reference) {
    if (!(reference instanceof LiveVariable)) return;
    registerAssignment((LiveVariable) reference);
  }

  private void registerAssignment(LiveVariable assignee) {
    if (hasVariableReference(assignee)) {
      onAssign.accept(assignee);
    }
  }

  public BlockDataFlowVisitor() {
    onAssign = node -> {};
    onReference = node -> {};
  }

  /// Handler to call when there is an assignment to a variable.
  public BlockDataFlowVisitor withOnAssign(Consumer<LiveVariable> onAssign) {
    this.onAssign = onAssign;
    return this;
  }

  /// Handler to call when there is a reference to variables. E.g., A reference to a variable, or a
  /// reference to an invoked routine. Additionally, in the case of implicit references, e.g., bare
  /// `raise` and `inherited`, the implicitly referenced variables will be handled.
  public BlockDataFlowVisitor withOnReference(Consumer<LiveVariable> onReference) {
    this.onReference = onReference;
    return this;
  }

  public void visit(Block block) {
    for (DelphiNode element : block.getElements()) {
      handleNode(element);
    }

    if (block instanceof Terminated) {
      DelphiNode terminator = ((Terminated) block).getTerminator();
      handleNode(terminator);
    }
  }

  private void handleNode(DelphiNode node) {
    if (node instanceof SimpleNameDeclarationNode) {
      handleSimpleNameDeclaration((SimpleNameDeclarationNode) node);
    } else if (node instanceof AssignmentStatementNode) {
      handleAssignmentStatement((AssignmentStatementNode) node);
    } else if (node instanceof ForStatementNode) {
      handleForStatement((ForStatementNode) node);
    } else if (node instanceof NameReferenceNode) {
      handleNameReference((NameReferenceNode) node);
    } else if (node instanceof AnonymousMethodNode) {
      handleAnonymousRoutine((AnonymousMethodNode) node);
    } else if (node instanceof CommonDelphiNode) {
      handleCommonDelphiNode((CommonDelphiNode) node);
    } else if (node instanceof RaiseStatementNode) {
      handleRaiseStatement((RaiseStatementNode) node);
    }
  }

  private void handleSimpleNameDeclaration(SimpleNameDeclarationNode declaration) {
    VarStatementNode varStatement = declaration.getFirstParentOfType(VarStatementNode.class);
    if (varStatement == null) {
      registerAssignment(declaration);
      return;
    }
    if (varStatement.getExpression() != null) {
      registerAssignment(declaration);
    }
  }

  private void handleAssignmentStatement(AssignmentStatementNode assignment) {
    NameReferenceNode assigneeReference =
        assignment.getAssignee().skipParentheses().getFirstChildOfType(NameReferenceNode.class);

    if (assigneeReference == null) {
      // There is no symbol being referenced in the assignee
      return;
    }

    DelphiNode parent = assigneeReference.getParent();
    if (parent != null && parent.getChildren().size() != 1) {
      // There are other elements to this assignment, e.g., array access
      return;
    }

    NameReferenceNode assigneeName = getReferent(assigneeReference);

    if (assigneeName == null || assigneeName != assigneeName.getLastName()) {
      // Declarations only occur when the assignee is the variable itself.
      // When a field is being accessed on the assignee, this is a reference, not a declaration.
      // The reference case is handled in `handleNameReference`.
      return;
    }

    registerAssignment(assigneeName);
  }

  private static NameReferenceNode getReferent(NameReferenceNode assigneeReference) {
    NameReferenceNode assigneeName = assigneeReference.getFirstName();
    // Skip `Self` if there is more than one name
    if (assigneeName != assigneeName.getLastName()) {
      NameDeclaration assigneeDeclaration = assigneeName.getNameDeclaration();
      if (assigneeDeclaration instanceof VariableNameDeclaration) {
        VariableNameDeclaration declaration = (VariableNameDeclaration) assigneeDeclaration;
        if (declaration.isSelf()) {
          assigneeName = assigneeName.nextName();
        }
      }
    }
    return assigneeName;
  }

  private void handleForStatement(ForStatementNode forStatement) {
    ForLoopVarNode variable = forStatement.getVariable();
    if (variable instanceof ForLoopVarReferenceNode) {
      ForLoopVarReferenceNode forLoopVarNode = (ForLoopVarReferenceNode) variable;
      registerAssignment(forLoopVarNode.getNameReference());
      registerUsage(forLoopVarNode.getNameReference());
    } else if (variable instanceof ForLoopVarDeclarationNode) {
      ForLoopVarDeclarationNode forLoopVarNode = (ForLoopVarDeclarationNode) variable;
      // The assignment has been handled by the `SimpleNameDeclarationNode` element
      registerUsage(forLoopVarNode.getNameDeclarationNode());
    }
  }

  private void handleNameReference(NameReferenceNode reference) {
    NameReferenceNode referent = getReferent(reference);
    if (!isAssignee(referent)) {
      registerUsage(referent);
    }
  }

  private void handleAnonymousRoutine(AnonymousMethodNode reference) {
    registerUsage(reference);
    if (!(reference instanceof AnonymousMethodNodeImpl)) return;

    ControlFlowGraph cfg = ((AnonymousMethodNodeImpl) reference).getControlFlowGraph();
    if (cfg == null) return;

    LiveVariables liveVariables = LiveVariables.analyze(cfg);

    Set<LiveVariable> referencedVariables = newLiveVariableSet();
    referencedVariables.addAll(liveVariables.getBlockInputs(cfg.getEntryBlock()));
    referencedVariables.removeAll(liveVariables.getLocalScopeVariables());

    for (LiveVariable living : referencedVariables) {
      registerUsage(living);
    }
  }

  private void handleCommonDelphiNode(CommonDelphiNode commonDelphiNode) {
    ExpressionNode expression = commonDelphiNode.getFirstParentOfType(ExpressionNode.class);
    if (!ExpressionNodeUtils.isBareInherited(expression)) return;

    // A bare inherited statement passes through all parameters to parent definition
    RoutineImplementationNode routine =
        expression.getFirstParentOfType(RoutineImplementationNode.class);
    if (routine == null) return;
    routine.getParameters().forEach(parameter -> registerUsage(parameter.getNode()));
  }

  private void handleRaiseStatement(RaiseStatementNode raiseStatement) {
    if (raiseStatement.getRaiseExpression() != null) return;

    DelphiNode current = raiseStatement.getParent();
    while (current != null) {
      if (current instanceof TryStatementNode) return;
      if (current instanceof ExceptItemNode) {
        registerUsage(((ExceptItemNode) current).getExceptionName());
        return;
      }
      current = current.getParent();
    }
  }

  private static boolean hasVariableReference(LiveVariable variable) {
    if (variable instanceof NameDeclarationNode) return true;

    NameDeclaration declaration = variable.getNameDeclaration();
    if (declaration instanceof VariableNameDeclaration) {
      VariableNameDeclaration nameDeclaration = (VariableNameDeclaration) declaration;
      return nameDeclaration.isInline()
          || nameDeclaration.isImplementationDeclaration()
          || nameDeclaration.isSelf()
          || nameDeclaration.isVar()
          || nameDeclaration.isResult();
    }

    return declaration instanceof RoutineNameDeclaration
        || declaration instanceof PropertyNameDeclaration;
  }

  private static boolean isAssignee(NameReferenceNode node) {
    if (node.getFirstName() != node.getLastName()) {
      // Consider it a reference to a variable if there is more than one part to the name
      return false;
    }

    ExpressionNode expression = node.getFirstParentOfType(ExpressionNode.class);
    AssignmentStatementNode assignment = node.getFirstParentOfType(AssignmentStatementNode.class);
    if (expression != null && expression.getChildren().size() != 1) {
      // There are other parts to the assignee, it is a reference
      return false;
    }
    if (assignment != null && assignment.getAssignee() == expression) {
      // This reference is being assigned to
      return true;
    }

    // Check if this is a for-loop var, and therefore being assigned to.
    ForLoopVarReferenceNode forLoopVar = node.getFirstParentOfType(ForLoopVarReferenceNode.class);
    return forLoopVar != null;
  }
}
