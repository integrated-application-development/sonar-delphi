/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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

import au.com.integradev.delphi.operator.UnaryOperator;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.AssignmentStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.BlockDeclarationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.CommonDelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ConstStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.ForLoopVarNode;
import org.sonar.plugins.communitydelphi.api.ast.ForLoopVarReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.ForStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.RepeatStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;
import org.sonar.plugins.communitydelphi.api.ast.UnaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.VarDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.VarSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.VarStatementNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypedDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;
import org.sonar.plugins.communitydelphi.api.type.Parameter;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType;
import org.sonar.plugins.communitydelphi.api.type.Type.StructType;
import org.sonar.plugins.communitydelphi.api.type.TypeUtils;
import org.sonar.plugins.communitydelphi.api.type.Typed;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "VariableInitializationRule", repositoryKey = "delph")
@Rule(key = "VariableInitialization")
public class VariableInitializationCheck extends DelphiCheck {
  private static final String MESSAGE = "Initialize this variable before using it.";

  private final IdentityHashMap<NameDeclaration, InitializationState> initializationStateMap =
      new IdentityHashMap<>();
  private final IdentityHashMap<NameDeclaration, MethodImplementationNode> subroutines =
      new IdentityHashMap<>();

  @Override
  public DelphiCheckContext visit(MethodImplementationNode method, DelphiCheckContext context) {
    initializationStateMap.clear();
    subroutines.clear();
    visitMethod(method, context);
    return context;
  }

  private void visitMethod(MethodImplementationNode method, DelphiCheckContext context) {
    CompoundStatementNode block = method.getStatementBlock();
    if (block != null) {
      collectSubroutines(method);
      collectDeclarationsFromVarSections(method);
      visitStatements(block, context);
    }
  }

  private void collectSubroutines(MethodImplementationNode method) {
    BlockDeclarationSectionNode declarationSection = method.getDeclarationSection();
    if (declarationSection == null) {
      return;
    }

    declarationSection
        .findChildrenOfType(MethodImplementationNode.class)
        .forEach(subroutine -> subroutines.put(subroutine.getMethodNameDeclaration(), subroutine));
  }

  private void collectDeclarationsFromVarSections(MethodImplementationNode method) {
    BlockDeclarationSectionNode declarationSection = method.getDeclarationSection();
    if (declarationSection == null) {
      return;
    }

    for (VarSectionNode section : declarationSection.findChildrenOfType(VarSectionNode.class)) {
      for (VarDeclarationNode variable : section.getDeclarations()) {
        variable.getNameDeclarationList().getDeclarations().stream()
            .map(NameDeclarationNode::getNameDeclaration)
            .filter(VariableNameDeclaration.class::isInstance)
            .map(VariableNameDeclaration.class::cast)
            .filter(VariableInitializationCheck::isUnmanagedVariable)
            .forEach(
                declaration -> {
                  InitializationState state = InitializationState.from(declaration.getType());
                  if (declaration.isUnion()) {
                    state.assignedTo();
                  }
                  initializationStateMap.put(declaration, state);
                });
      }
    }
  }

  private void visitStatements(DelphiNode node, DelphiCheckContext context) {
    boolean visitChildrenFirst = node instanceof RepeatStatementNode;

    if (!visitChildrenFirst) {
      visitStatement(node, context);
    }

    for (int i = 0; i < node.getChildrenCount(); i++) {
      visitStatements(node.getChild(i), context);
    }

    if (visitChildrenFirst) {
      visitStatement(node, context);
    }
  }

  private void visitStatement(Node node, DelphiCheckContext context) {
    if (node instanceof StatementNode) {
      StatementNode statement = (StatementNode) node;
      searchForInitializationsByOutArgument(statement);
      searchForInitializationsByAddressOfHeuristic(statement);
      searchForInitializationsByRecordInvocationHeuristic(statement);
      handleSubroutineInvocations(statement, context);
      handleAssignment(statement);
      handleVarStatement(statement);
      handleConstStatement(statement);
      handleForStatement(statement);
      searchForViolations(statement, context);
    }
  }

  private void handleSubroutineInvocations(StatementNode statement, DelphiCheckContext context) {
    for (NameReferenceNode name : findNameReferences(statement)) {
      for (NameReferenceNode namePart : name.flatten()) {
        NameDeclaration declaration = namePart.getNameDeclaration();
        if (!subroutines.containsKey(declaration)) {
          continue;
        }
        MethodImplementationNode subroutine = subroutines.get(declaration);
        subroutines.remove(declaration);
        visitMethod(subroutine, context);
        subroutines.put(declaration, subroutine);
      }
    }
  }

  private void searchForInitializationsByOutArgument(StatementNode statement) {
    findNameReferences(statement).stream()
        .filter(VariableInitializationCheck::isOutArgument)
        .map(this::getReferredInitializationState)
        .filter(Objects::nonNull)
        .forEach(InitializationState::assignedTo);
  }

  private void searchForInitializationsByAddressOfHeuristic(StatementNode statement) {
    findNameReferences(statement).stream()
        .filter(VariableInitializationCheck::isAddressOfReference)
        .map(this::getReferredInitializationState)
        .filter(Objects::nonNull)
        .forEach(InitializationState::assignedTo);
  }

  private void searchForInitializationsByRecordInvocationHeuristic(StatementNode statement) {
    findNameReferences(statement)
        .forEach(this::searchForInitializationsByRecordInvocationHeuristic);
  }

  private void searchForInitializationsByRecordInvocationHeuristic(NameReferenceNode name) {
    NameDeclaration declaration = name.getNameDeclaration();
    InitializationState initializationState = this.initializationStateMap.get(declaration);
    while (initializationState != null) {
      if (isRecordInvocation(name)) {
        initializationState.assignedTo();
      }
      name = name.nextName();
      if (name == null) {
        break;
      }
      declaration = name.getNameDeclaration();
      initializationState = initializationState.getInitializationState(declaration);
    }
  }

  private static boolean isRecordInvocation(NameReferenceNode name) {
    NameDeclaration declaration = name.getNameDeclaration();
    if (!(declaration instanceof TypedDeclaration
        && ((TypedDeclaration) declaration).getType().isRecord())) {
      return false;
    }

    NameReferenceNode next = name.nextName();
    if (next == null) {
      return false;
    }

    NameDeclaration nextDeclaration = next.getNameDeclaration();
    return nextDeclaration instanceof MethodNameDeclaration
        || nextDeclaration instanceof PropertyNameDeclaration;
  }

  private void handleAssignment(StatementNode statement) {
    if (!(statement instanceof AssignmentStatementNode)) {
      return;
    }

    var assignmentStatement = (AssignmentStatementNode) statement;
    ExpressionNode assignee = skipHardCasts(assignmentStatement.getAssignee());

    NameReferenceNode assigneeName = getLastNameReference(assignee);
    if (assigneeName == null) {
      return;
    }

    NameDeclaration declaration = assigneeName.getNameDeclaration();
    InitializationState previousInitializationState = null;
    InitializationState initializationState = this.initializationStateMap.get(declaration);

    while (initializationState != null) {
      NameReferenceNode next = assigneeName.nextName();
      if (next == null) {
        break;
      }
      assigneeName = next;
      declaration = assigneeName.getNameDeclaration();
      previousInitializationState = initializationState;
      initializationState = initializationState.getInitializationState(declaration);
    }

    for (NameReferenceNode name : findNameReferences(assignmentStatement.getValue())) {
      if (name.getNameDeclaration() == declaration && !isOutArgument(name)) {
        return;
      }
    }

    InitializationState assignedState =
        getReferredInitializationState(assignmentStatement.getValue());

    if (assignedState != null) {
      if (previousInitializationState != null) {
        previousInitializationState.setInitializationState(declaration, assignedState.copy());
      } else {
        this.initializationStateMap.put(declaration, assignedState.copy());
      }
    } else if (initializationState != null) {
      initializationState.assignedTo();
    }
  }

  private static boolean isOutArgument(NameReferenceNode name) {
    ExpressionNode argument = getArgumentExpression(name);
    if (argument == null) {
      return false;
    }

    ArgumentListNode argumentList = (ArgumentListNode) argument.getParent();
    ProceduralType procedural = getInvokedProcedural(argumentList);
    if (procedural == null) {
      return false;
    }

    int argumentIndex = argumentList.getArguments().indexOf(argument);
    Parameter parameter = procedural.getParameter(argumentIndex);

    return (parameter.isOut() || parameter.isVar()) && !isOutArgumentExclusion(argumentList);
  }

  private static boolean isSizeOfArgument(NameReferenceNode name) {
    ExpressionNode argument = getArgumentExpression(name);
    if (argument == null) {
      return false;
    }

    ArgumentListNode argumentList = (ArgumentListNode) argument.getParent();
    MethodNameDeclaration method = getMethodDeclaration(argumentList);
    if (method == null) {
      return false;
    }

    return method.fullyQualifiedName().equals("System.SizeOf");
  }

  private static ExpressionNode getArgumentExpression(NameReferenceNode name) {
    if (name.getChildIndex() != name.getParent().getChildrenCount() - 1) {
      return null;
    }

    if (!(name.getParent() instanceof PrimaryExpressionNode)) {
      return null;
    }

    ExpressionNode expression = null;
    ArgumentListNode argumentList;
    Node current = name.getParent();

    while (current instanceof PrimaryExpressionNode) {
      expression = ((ExpressionNode) current).skipParentheses();
      if (!(expression.getParent() instanceof ArgumentListNode)) {
        return null;
      }

      argumentList = (ArgumentListNode) expression.getParent();
      current = findHardCast(argumentList);
    }

    return expression;
  }

  private static Node findHardCast(ArgumentListNode argumentList) {
    if (isHardCast(argumentList)) {
      return argumentList.getParent();
    }
    return null;
  }

  private static ExpressionNode skipHardCasts(ExpressionNode expression) {
    while (true) {
      ArgumentListNode argumentList = expression.getFirstChildOfType(ArgumentListNode.class);
      if (argumentList == null || !isHardCast(argumentList)) {
        return expression;
      }
      expression = (ExpressionNode) argumentList.getChild(0);
    }
  }

  private static boolean isHardCast(ArgumentListNode argumentList) {
    Node previous = argumentList.getParent().getChild(argumentList.getChildIndex() - 1);
    if (previous instanceof NameReferenceNode) {
      NameReferenceNode nameReference = ((NameReferenceNode) previous);
      NameDeclaration declaration = nameReference.getLastName().getNameDeclaration();
      return declaration instanceof TypeNameDeclaration;
    } else if (previous instanceof CommonDelphiNode) {
      DelphiTokenType tokenType = previous.getTokenType();
      return tokenType == DelphiTokenType.STRING || tokenType == DelphiTokenType.FILE;
    }
    return false;
  }

  private static ProceduralType getInvokedProcedural(ArgumentListNode argumentList) {
    Node previous = argumentList.getParent().getChild(argumentList.getChildIndex() - 1);
    if (!(previous instanceof Typed)) {
      return null;
    }

    Type type = ((Typed) previous).getType();
    if (!type.isProcedural()) {
      return null;
    }

    return (ProceduralType) type;
  }

  private static MethodNameDeclaration getMethodDeclaration(ArgumentListNode argumentList) {
    Node previous = argumentList.getParent().getChild(argumentList.getChildIndex() - 1);
    if (!(previous instanceof NameReferenceNode)) {
      return null;
    }

    NameReferenceNode proceduralReference = ((NameReferenceNode) previous).getLastName();
    NameDeclaration declaration = proceduralReference.getNameDeclaration();
    if (!(declaration instanceof MethodNameDeclaration)) {
      return null;
    }

    return (MethodNameDeclaration) declaration;
  }

  private static boolean isOutArgumentExclusion(ArgumentListNode argumentList) {
    MethodNameDeclaration method = getMethodDeclaration(argumentList);
    if (method == null) {
      return false;
    }

    return method.fullyQualifiedName().equals("System.SysUtils.FreeAndNil")
        || method.fullyQualifiedName().equals("System.Assigned");
  }

  private void searchForViolations(StatementNode statement, DelphiCheckContext context) {
    if (statement instanceof AssignmentStatementNode) {
      searchForViolationsInAssignment((AssignmentStatementNode) statement, context);
    } else {
      searchForViolationsInStatement(statement, context);
    }
  }

  private void searchForViolationsInAssignment(
      AssignmentStatementNode assignment, DelphiCheckContext context) {
    searchForViolationsInAssignmentAssignee(assignment.getAssignee(), context);
    searchForViolationsInAssignmentValue(assignment.getValue(), context);
  }

  private void searchForViolationsInAssignmentAssignee(
      ExpressionNode assignee, DelphiCheckContext context) {
    NameReferenceNode lastName = getLastNameReference(assignee);
    searchForViolationsInNameReference(
        lastName, context, VariableInitializationCheck::isAssignmentAssigneeViolation);

    findNameReferences(assignee).stream()
        .filter(reference -> reference != lastName)
        .forEach(
            reference ->
                searchForViolationsInNameReference(
                    reference, context, VariableInitializationCheck::isRegularViolation));
  }

  private void searchForViolationsInAssignmentValue(
      ExpressionNode value, DelphiCheckContext context) {
    NameReferenceNode lastName = getLastNameReference(value);
    searchForViolationsInNameReference(
        lastName, context, VariableInitializationCheck::isAssignmentValueViolation);

    findNameReferences(value).stream()
        .filter(reference -> reference != lastName)
        .forEach(
            reference ->
                searchForViolationsInNameReference(
                    reference, context, VariableInitializationCheck::isRegularViolation));
  }

  private static boolean isRegularViolation(NameReferenceNode name, InitializationState state) {
    if (name.nextName() == null) {
      return !state.isInitialized() && !isSizeOfArgument(name);
    }
    return !state.canBeReferencedInQualifiedName();
  }

  private static boolean isAssignmentAssigneeViolation(
      NameReferenceNode name, InitializationState state) {
    return name.nextName() != null && !state.canBeReferencedInQualifiedName();
  }

  private static boolean isAssignmentValueViolation(
      NameReferenceNode name, InitializationState state) {
    if (name.nextName() == null) {
      return !state.canBeAssignedFrom();
    }
    return !state.canBeReferencedInQualifiedName();
  }

  private static boolean isAddressOfReference(NameReferenceNode name) {
    if (name.nextName() != null) {
      return false;
    }

    DelphiNode node = name;
    do {
      node = node.getParent();
    } while (node instanceof NameReferenceNode);

    if (node instanceof PrimaryExpressionNode) {
      node = ((PrimaryExpressionNode) node).findParentheses().getParent();
      if (node instanceof UnaryExpressionNode) {
        UnaryExpressionNode unary = (UnaryExpressionNode) node;
        return unary.getOperator() == UnaryOperator.ADDRESS;
      }
    }

    return false;
  }

  private static NameReferenceNode getLastNameReference(ExpressionNode expression) {
    expression = expression.skipParentheses();
    if (expression instanceof PrimaryExpressionNode) {
      PrimaryExpressionNode primary = (PrimaryExpressionNode) expression;
      Node lastChild = primary.getChild(primary.getChildrenCount() - 1);
      if (lastChild instanceof NameReferenceNode) {
        return ((NameReferenceNode) lastChild);
      }
    }
    return null;
  }

  private void searchForViolationsInStatement(StatementNode statement, DelphiCheckContext context) {
    for (NameReferenceNode reference : findNameReferences(statement)) {
      searchForViolationsInNameReference(
          reference, context, VariableInitializationCheck::isRegularViolation);
    }
  }

  private void searchForViolationsInNameReference(
      NameReferenceNode name,
      DelphiCheckContext context,
      BiPredicate<NameReferenceNode, InitializationState> isViolation) {
    if (name == null) {
      return;
    }

    NameDeclaration declaration = name.getNameDeclaration();
    InitializationState initializationState = this.initializationStateMap.get(declaration);

    while (initializationState != null) {
      if (!initializationState.isViolation() && isViolation.test(name, initializationState)) {
        initializationState.flagViolation();
        reportIssue(context, name.getIdentifier(), MESSAGE);
      }

      name = name.nextName();
      if (name == null) {
        break;
      }

      declaration = name.getNameDeclaration();
      initializationState = initializationState.getInitializationState(declaration);
    }
  }

  private static List<NameReferenceNode> findNameReferences(DelphiNode node) {
    List<NameReferenceNode> references = new ArrayList<>();
    findNameReferences(node, references);
    return references;
  }

  private static void findNameReferences(DelphiNode node, List<NameReferenceNode> references) {
    if (node instanceof NameReferenceNode) {
      references.add((NameReferenceNode) node);
    } else {
      for (int i = 0; i < node.getChildrenCount(); ++i) {
        DelphiNode child = node.getChild(i);
        if (!(child instanceof StatementNode)) {
          findNameReferences(child, references);
        }
      }
    }
  }

  private void handleVarStatement(StatementNode statement) {
    if (statement instanceof VarStatementNode) {
      var varStatement = (VarStatementNode) statement;
      boolean assigned = varStatement.getExpression() != null;

      ExpressionNode expression = varStatement.getExpression();
      InitializationState assignedState;
      if (expression != null) {
        assignedState = getReferredInitializationState(expression);
      } else {
        assignedState = null;
      }

      varStatement.getNameDeclarationList().getDeclarations().stream()
          .map(NameDeclarationNode::getNameDeclaration)
          .filter(VariableInitializationCheck::isUnmanagedVariable)
          .map(VariableNameDeclaration.class::cast)
          .forEach(
              declaration -> {
                InitializationState initializationState;
                if (assignedState != null) {
                  initializationState = assignedState.copy();
                } else {
                  initializationState = InitializationState.from(declaration.getType());
                  if (assigned) {
                    initializationState.assignedTo();
                  }
                }
                initializationStateMap.put(declaration, initializationState);
              });
    }
  }

  private void handleConstStatement(StatementNode statement) {
    if (statement instanceof ConstStatementNode) {
      var constStatement = (ConstStatementNode) statement;
      NameDeclaration declaration = constStatement.getNameDeclarationNode().getNameDeclaration();
      if (isUnmanagedVariable(declaration)) {
        var varDeclaration = (VariableNameDeclaration) declaration;
        InitializationState assignedState =
            getReferredInitializationState(constStatement.getExpression());
        InitializationState initializationState;
        if (assignedState != null) {
          initializationState = assignedState.copy();
        } else {
          initializationState = InitializationState.from(varDeclaration.getType());
          initializationState.assignedTo();
        }
        initializationStateMap.put(declaration, initializationState);
      }
    }
  }

  private void handleForStatement(StatementNode statement) {
    if (statement instanceof ForStatementNode) {
      var forStatement = (ForStatementNode) statement;
      ForLoopVarNode forLoopVar = forStatement.getVariable();
      if (forLoopVar instanceof ForLoopVarReferenceNode) {
        NameReferenceNode name = ((ForLoopVarReferenceNode) forLoopVar).getNameReference();
        NameDeclaration declaration = name.getNameDeclaration();
        InitializationState initializationState = initializationStateMap.get(declaration);
        if (initializationState != null) {
          initializationState.assignedTo();
        }
      }
    }
  }

  private InitializationState getReferredInitializationState(ExpressionNode expression) {
    expression = expression.skipParentheses();
    if (expression instanceof PrimaryExpressionNode) {
      PrimaryExpressionNode primary = (PrimaryExpressionNode) expression;
      Node lastChild = primary.getChild(primary.getChildrenCount() - 1);
      if (lastChild instanceof NameReferenceNode) {
        return this.getReferredInitializationState((NameReferenceNode) lastChild);
      }
    }
    return null;
  }

  private InitializationState getReferredInitializationState(NameReferenceNode name) {
    NameDeclaration declaration = name.getNameDeclaration();
    InitializationState initializationState = this.initializationStateMap.get(declaration);
    while (initializationState != null) {
      name = name.nextName();
      if (name == null) {
        break;
      }
      declaration = name.getNameDeclaration();
      initializationState = initializationState.getInitializationState(declaration);
    }
    return initializationState;
  }

  private static boolean isUnmanagedVariable(NameDeclaration declaration) {
    return declaration instanceof VariableNameDeclaration
        && isUnmanagedType(((VariableNameDeclaration) declaration).getType());
  }

  private static boolean isUnmanagedType(Type type) {
    return !type.isString() && !type.isArray() && !type.isVariant() && !type.isInterface();
  }

  private interface InitializationState {
    boolean isInitialized();

    boolean canBeReferencedInQualifiedName();

    boolean canBeAssignedFrom();

    boolean isViolation();

    void flagViolation();

    void assignedTo();

    InitializationState getInitializationState(NameDeclaration declaration);

    void setInitializationState(NameDeclaration declaration, InitializationState state);

    InitializationState copy();

    static InitializationState from(Type type) {
      type = TypeUtils.findBaseType(type);
      if (type.isRecord()) {
        return new RecordInitializationState((StructType) type);
      } else {
        return new DefaultInitializationState();
      }
    }
  }

  private abstract static class AbstractInitializationState implements InitializationState {
    private boolean violation;

    @Override
    public void flagViolation() {
      violation = true;
    }

    @Override
    public boolean isViolation() {
      return violation;
    }
  }

  private static class DefaultInitializationState extends AbstractInitializationState {
    private boolean initialized;

    public DefaultInitializationState() {
      initialized = false;
    }

    private DefaultInitializationState(DefaultInitializationState initializationState) {
      initialized = initializationState.initialized;
    }

    @Override
    public boolean isInitialized() {
      return initialized;
    }

    @Override
    public boolean canBeReferencedInQualifiedName() {
      return isInitialized();
    }

    @Override
    public boolean canBeAssignedFrom() {
      return isInitialized();
    }

    @Override
    public void assignedTo() {
      initialized = true;
    }

    @Nullable
    @Override
    public InitializationState getInitializationState(NameDeclaration declaration) {
      return null;
    }

    @Override
    public void setInitializationState(NameDeclaration declaration, InitializationState state) {
      // Do nothing
    }

    @Override
    public InitializationState copy() {
      return new DefaultInitializationState(this);
    }
  }

  private static class RecordInitializationState extends AbstractInitializationState {
    private final IdentityHashMap<NameDeclaration, InitializationState> fields =
        new IdentityHashMap<>();

    public RecordInitializationState(StructType type) {
      type.typeScope().getVariableDeclarations().stream()
          .filter(VariableNameDeclaration::isField)
          .filter(Predicate.not(VariableNameDeclaration::isClassVariable))
          .filter(VariableInitializationCheck::isUnmanagedVariable)
          .forEach(
              declaration -> {
                InitializationState state = InitializationState.from(declaration.getType());
                if (declaration.isUnion()) {
                  state.assignedTo();
                }
                fields.put(declaration, state);
              });
    }

    private RecordInitializationState(RecordInitializationState initializationState) {
      initializationState.fields.forEach((key, value) -> fields.put(key, value.copy()));
    }

    @Override
    public boolean isInitialized() {
      return fields.values().stream().allMatch(InitializationState::isInitialized);
    }

    @Override
    public boolean canBeReferencedInQualifiedName() {
      return true;
    }

    @Override
    public boolean canBeAssignedFrom() {
      return true;
    }

    @Override
    public void assignedTo() {
      fields.values().forEach(InitializationState::assignedTo);
    }

    @Nullable
    @Override
    public InitializationState getInitializationState(NameDeclaration declaration) {
      return fields.get(declaration);
    }

    @Override
    public void setInitializationState(NameDeclaration declaration, InitializationState state) {
      fields.computeIfPresent(declaration, (key, value) -> state);
    }

    @Override
    public InitializationState copy() {
      return new RecordInitializationState(this);
    }
  }
}
