package org.sonar.plugins.delphi.pmd.rules;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import javax.annotation.Nullable;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.node.ArgumentListNode;
import org.sonar.plugins.delphi.antlr.ast.node.AssignmentStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.BlockDeclarationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.CommonDelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.CompoundStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ConstStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.ForLoopVarNode;
import org.sonar.plugins.delphi.antlr.ast.node.ForLoopVarReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.ForStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.RepeatStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.StatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.UnaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarStatementNode;
import org.sonar.plugins.delphi.operator.UnaryOperator;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypedDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.Type.StructType;
import org.sonar.plugins.delphi.type.TypeUtils;
import org.sonar.plugins.delphi.type.Typed;
import org.sonar.plugins.delphi.type.parameter.Parameter;

public class VariableInitializationRule extends AbstractDelphiRule {
  private final IdentityHashMap<NameDeclaration, InitializationState> initializationStateMap =
      new IdentityHashMap<>();
  private final IdentityHashMap<NameDeclaration, MethodImplementationNode> subprocedures =
      new IdentityHashMap<>();

  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    initializationStateMap.clear();
    subprocedures.clear();
    visitMethod(method, data);
    return data;
  }

  private void visitMethod(MethodImplementationNode method, RuleContext data) {
    CompoundStatementNode block = method.getStatementBlock();
    if (block != null) {
      collectSubprocedures(method);
      collectDeclarationsFromVarSections(method);
      visitStatements(block, data);
    }
  }

  private void collectSubprocedures(MethodImplementationNode method) {
    BlockDeclarationSectionNode declarationSection = method.getDeclarationSection();
    if (declarationSection == null) {
      return;
    }

    declarationSection
        .findChildrenOfType(MethodImplementationNode.class)
        .forEach(
            subprocedure ->
                subprocedures.put(subprocedure.getMethodNameDeclaration(), subprocedure));
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
            .filter(VariableInitializationRule::isUnmanagedVariable)
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

  private void visitStatements(Node node, RuleContext data) {
    if (node != null) {
      boolean visitChildrenFirst = node instanceof RepeatStatementNode;

      if (!visitChildrenFirst) {
        visitStatement(node, data);
      }

      for (int i = 0; i < node.jjtGetNumChildren(); i++) {
        visitStatements(node.jjtGetChild(i), data);
      }

      if (visitChildrenFirst) {
        visitStatement(node, data);
      }
    }
  }

  private void visitStatement(Node node, RuleContext data) {
    if (node instanceof StatementNode) {
      StatementNode statement = (StatementNode) node;
      searchForInitializationsByOutArgument(statement);
      searchForInitializationsByAddressOfHeuristic(statement);
      searchForInitializationsByRecordInvocationHeuristic(statement);
      handleSubprocedureInvocations(statement, data);
      handleAssignment(statement);
      handleVarStatement(statement);
      handleConstStatement(statement);
      handleForStatement(statement);
      searchForViolations(statement, data);
    }
  }

  private void handleSubprocedureInvocations(StatementNode statement, RuleContext data) {
    for (NameReferenceNode name : findNameReferences(statement)) {
      for (NameReferenceNode namePart : name.flatten()) {
        NameDeclaration declaration = namePart.getNameDeclaration();
        if (!subprocedures.containsKey(declaration)) {
          continue;
        }
        MethodImplementationNode subprocedure = subprocedures.get(declaration);
        subprocedures.remove(declaration);
        visitMethod(subprocedure, data);
        subprocedures.put(declaration, subprocedure);
      }
    }
  }

  private void searchForInitializationsByOutArgument(StatementNode statement) {
    findNameReferences(statement).stream()
        .filter(VariableInitializationRule::isOutArgument)
        .map(this::getReferredInitializationState)
        .filter(Objects::nonNull)
        .forEach(InitializationState::assignedTo);
  }

  private void searchForInitializationsByAddressOfHeuristic(StatementNode statement) {
    findNameReferences(statement).stream()
        .filter(VariableInitializationRule::isAddressOfReference)
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
    if (!((TypedDeclaration) declaration).getType().isRecord()) {
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

    ArgumentListNode argumentList = (ArgumentListNode) argument.jjtGetParent();
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

    ArgumentListNode argumentList = (ArgumentListNode) argument.jjtGetParent();
    MethodNameDeclaration method = getMethodDeclaration(argumentList);
    if (method == null) {
      return false;
    }

    return method.fullyQualifiedName().equals("System.SizeOf");
  }

  private static ExpressionNode getArgumentExpression(NameReferenceNode name) {
    if (name.jjtGetChildIndex() != name.jjtGetParent().jjtGetNumChildren() - 1) {
      return null;
    }

    if (!(name.jjtGetParent() instanceof PrimaryExpressionNode)) {
      return null;
    }

    ExpressionNode expression = null;
    ArgumentListNode argumentList;
    Node current = name.jjtGetParent();

    while (current instanceof PrimaryExpressionNode) {
      expression = ((ExpressionNode) current).skipParentheses();
      if (!(expression.jjtGetParent() instanceof ArgumentListNode)) {
        return null;
      }

      argumentList = (ArgumentListNode) expression.jjtGetParent();
      current = findHardCast(argumentList);
    }

    return expression;
  }

  private static Node findHardCast(ArgumentListNode argumentList) {
    if (isHardCast(argumentList)) {
      return argumentList.jjtGetParent();
    }
    return null;
  }

  private static ExpressionNode skipHardCasts(ExpressionNode expression) {
    while (true) {
      ArgumentListNode argumentList = expression.getFirstChildOfType(ArgumentListNode.class);
      if (argumentList == null || !isHardCast(argumentList)) {
        return expression;
      }
      expression = (ExpressionNode) argumentList.jjtGetChild(0);
    }
  }

  private static boolean isHardCast(ArgumentListNode argumentList) {
    Node previous = argumentList.jjtGetParent().jjtGetChild(argumentList.jjtGetChildIndex() - 1);
    if (previous instanceof NameReferenceNode) {
      NameReferenceNode nameReference = ((NameReferenceNode) previous);
      DelphiNameDeclaration declaration = nameReference.getLastName().getNameDeclaration();
      return declaration instanceof TypeNameDeclaration;
    } else if (previous instanceof CommonDelphiNode) {
      int tokenType = ((CommonDelphiNode) previous).getToken().getType();
      return tokenType == DelphiLexer.STRING || tokenType == DelphiLexer.FILE;
    }
    return false;
  }

  private static ProceduralType getInvokedProcedural(ArgumentListNode argumentList) {
    Node previous = argumentList.jjtGetParent().jjtGetChild(argumentList.jjtGetChildIndex() - 1);
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
    Node previous = argumentList.jjtGetParent().jjtGetChild(argumentList.jjtGetChildIndex() - 1);
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

  private void searchForViolations(StatementNode statement, RuleContext data) {
    if (statement instanceof AssignmentStatementNode) {
      searchForViolationsInAssignment((AssignmentStatementNode) statement, data);
    } else {
      searchForViolationsInStatement(statement, data);
    }
  }

  private void searchForViolationsInAssignment(
      AssignmentStatementNode assignment, RuleContext data) {
    searchForViolationsInAssignmentAssignee(assignment.getAssignee(), data);
    searchForViolationsInAssignmentValue(assignment.getValue(), data);
  }

  private void searchForViolationsInAssignmentAssignee(ExpressionNode assignee, RuleContext data) {
    NameReferenceNode lastName = getLastNameReference(assignee);
    searchForViolationsInNameReference(
        lastName, data, VariableInitializationRule::isAssignmentAssigneeViolation);

    findNameReferences(assignee).stream()
        .filter(reference -> reference != lastName)
        .forEach(
            reference ->
                searchForViolationsInNameReference(
                    reference, data, VariableInitializationRule::isRegularViolation));
  }

  private void searchForViolationsInAssignmentValue(ExpressionNode value, RuleContext data) {
    NameReferenceNode lastName = getLastNameReference(value);
    searchForViolationsInNameReference(
        lastName, data, VariableInitializationRule::isAssignmentValueViolation);

    findNameReferences(value).stream()
        .filter(reference -> reference != lastName)
        .forEach(
            reference ->
                searchForViolationsInNameReference(
                    reference, data, VariableInitializationRule::isRegularViolation));
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

    Node node = name;
    do {
      node = node.jjtGetParent();
    } while (node instanceof NameReferenceNode);

    if (node instanceof PrimaryExpressionNode) {
      node = ((PrimaryExpressionNode) node).findParentheses().jjtGetParent();
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
      Node lastChild = primary.jjtGetChild(primary.jjtGetNumChildren() - 1);
      if (lastChild instanceof NameReferenceNode) {
        return ((NameReferenceNode) lastChild);
      }
    }
    return null;
  }

  private void searchForViolationsInStatement(StatementNode statement, RuleContext data) {
    for (NameReferenceNode reference : findNameReferences(statement)) {
      searchForViolationsInNameReference(
          reference, data, VariableInitializationRule::isRegularViolation);
    }
  }

  private void searchForViolationsInNameReference(
      NameReferenceNode name,
      RuleContext data,
      BiPredicate<NameReferenceNode, InitializationState> isViolation) {
    if (name == null) {
      return;
    }

    NameDeclaration declaration = name.getNameDeclaration();
    InitializationState initializationState = this.initializationStateMap.get(declaration);

    while (initializationState != null) {
      if (!initializationState.isViolation() && isViolation.test(name, initializationState)) {
        initializationState.flagViolation();
        addViolation(data, name.getIdentifier());
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

  private static void findNameReferences(Node node, List<NameReferenceNode> references) {
    if (node instanceof NameReferenceNode) {
      references.add((NameReferenceNode) node);
    } else {
      for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
        Node child = node.jjtGetChild(i);
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
          .filter(VariableInitializationRule::isUnmanagedVariable)
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
      Node lastChild = primary.jjtGetChild(primary.jjtGetNumChildren() - 1);
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
          .filter(VariableInitializationRule::isUnmanagedVariable)
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
