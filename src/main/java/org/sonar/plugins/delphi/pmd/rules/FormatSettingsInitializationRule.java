package org.sonar.plugins.delphi.pmd.rules;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.node.AssignmentStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.CompoundStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ConstStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.FinalizationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.ImplementationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.InitializationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.InterfaceSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.StatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarStatementNode;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration;

public class FormatSettingsInitializationRule extends AbstractDelphiRule {
  private static final String TFORMATSETTINGS = "System.SysUtils.TFormatSettings";
  private static final Set<String> TFORMATSETTINGS_INITIALIZERS =
      Set.of("System.SysUtils.TFormatSettings.Create", "System.SysUtils.TFormatSettings.Invariant");

  private final Map<NameDeclaration, Boolean> formatSettingsInitializedMap = new HashMap<>();

  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    CompoundStatementNode block = method.getStatementBlock();
    if (block != null) {
      formatSettingsInitializedMap.clear();
      collectFormatSettingsFromVarSections(method.getDeclarationSection());
      visitStatements(block, data);
    }
    return super.visit(method, data);
  }

  @Override
  public RuleContext visit(DelphiAST ast, RuleContext data) {
    formatSettingsInitializedMap.clear();
    collectFormatSettingsFromVarSections(ast.getFirstChildOfType(InterfaceSectionNode.class));
    collectFormatSettingsFromVarSections(ast.getFirstChildOfType(ImplementationSectionNode.class));
    visitStatements(ast.getFirstChildOfType(InitializationSectionNode.class), data);
    visitStatements(ast.getFirstChildOfType(FinalizationSectionNode.class), data);
    visitStatements(ast.getFirstChildOfType(CompoundStatementNode.class), data);
    return super.visit(ast, data);
  }

  private void collectFormatSettingsFromVarSections(DelphiNode node) {
    if (node == null) {
      return;
    }

    for (VarSectionNode section : node.findChildrenOfType(VarSectionNode.class)) {
      for (VarDeclarationNode var : section.getDeclarations()) {
        var.getNameDeclarationList().getDeclarations().stream()
            .map(NameDeclarationNode::getNameDeclaration)
            .filter(VariableNameDeclaration.class::isInstance)
            .map(VariableNameDeclaration.class::cast)
            .filter(declaration -> declaration.getType().is(TFORMATSETTINGS))
            .forEach(declaration -> formatSettingsInitializedMap.put(declaration, false));
      }
    }
  }

  private void visitStatements(DelphiNode node, RuleContext data) {
    if (node != null) {
      for (StatementNode statement : node.findDescendantsOfType(StatementNode.class)) {
        handleAssignment(statement);
        handleVarStatement(statement);
        handleConstStatement(statement);
        searchForViolations(statement, data);
      }
    }
  }

  private void handleAssignment(StatementNode statement) {
    if (statement instanceof AssignmentStatementNode) {
      var assignmentStatement = (AssignmentStatementNode) statement;
      if (isFormatSettingsInitializer(assignmentStatement.getValue())) {
        NameDeclaration declaration = getReferredDeclaration(assignmentStatement.getAssignee());
        formatSettingsInitializedMap.computeIfPresent(declaration, (key, value) -> true);
      }
    }
  }

  private void searchForViolations(StatementNode statement, RuleContext data) {
    for (NameReferenceNode reference : findNameReferences(statement)) {
      NameDeclaration declaration = reference.getNameDeclaration();
      Boolean initialized = formatSettingsInitializedMap.get(declaration);
      if (Boolean.FALSE.equals(initialized)) {
        formatSettingsInitializedMap.remove(declaration);
        addViolation(data, reference);
      }
    }
  }

  private static List<NameReferenceNode> findNameReferences(DelphiNode node) {
    List<NameReferenceNode> references = new ArrayList<>();
    findNameReferences(node, references);
    return references;
  }

  private static void findNameReferences(Node node, List<NameReferenceNode> references) {
    if (isLeftHandSideOfFormatSettingsAssignment(node)) {
      return;
    }

    if (node instanceof NameReferenceNode) {
      references.add((NameReferenceNode) node);
    }

    for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
      Node child = node.jjtGetChild(i);
      if (!(child instanceof StatementNode)) {
        findNameReferences(child, references);
      }
    }
  }

  private static boolean isLeftHandSideOfFormatSettingsAssignment(Node node) {
    Node parent = node.jjtGetParent();
    if (parent instanceof AssignmentStatementNode) {
      AssignmentStatementNode assignment = (AssignmentStatementNode) parent;
      return assignment.getAssignee() == node
          && isFormatSettingsVariable(getReferredDeclaration(assignment.getAssignee()));
    }
    return false;
  }

  private void handleVarStatement(StatementNode statement) {
    if (statement instanceof VarStatementNode) {
      var varStatement = (VarStatementNode) statement;
      boolean initialized = isFormatSettingsInitializer(varStatement.getExpression());
      varStatement.getNameDeclarationList().getDeclarations().stream()
          .map(NameDeclarationNode::getNameDeclaration)
          .filter(FormatSettingsInitializationRule::isFormatSettingsVariable)
          .forEach(declaration -> formatSettingsInitializedMap.put(declaration, initialized));
    }
  }

  private void handleConstStatement(StatementNode statement) {
    if (statement instanceof ConstStatementNode) {
      var constStatement = (ConstStatementNode) statement;
      NameDeclaration declaration = constStatement.getNameDeclarationNode().getNameDeclaration();
      if (isFormatSettingsVariable(declaration)) {
        boolean initialized = isFormatSettingsInitializer(constStatement.getExpression());
        formatSettingsInitializedMap.put(declaration, initialized);
      }
    }
  }

  private boolean isFormatSettingsInitializer(ExpressionNode expression) {
    NameDeclaration declaration = getReferredDeclaration(expression);
    if (declaration instanceof MethodNameDeclaration) {
      MethodNameDeclaration method = ((MethodNameDeclaration) declaration);
      return TFORMATSETTINGS_INITIALIZERS.contains(method.fullyQualifiedName());
    } else if (declaration instanceof VariableNameDeclaration) {
      return formatSettingsInitializedMap.getOrDefault(declaration, true);
    }
    return false;
  }

  private static boolean isFormatSettingsVariable(NameDeclaration declaration) {
    return declaration instanceof VariableNameDeclaration
        && ((VariableNameDeclaration) declaration).getType().is(TFORMATSETTINGS);
  }

  private static NameDeclaration getReferredDeclaration(ExpressionNode expression) {
    if (expression instanceof PrimaryExpressionNode) {
      PrimaryExpressionNode primary = (PrimaryExpressionNode) expression;
      List<NameReferenceNode> references = primary.findChildrenOfType(NameReferenceNode.class);
      NameReferenceNode reference = Iterables.getLast(references, null);
      if (reference != null) {
        return reference.getLastName().getNameDeclaration();
      }
    }
    return null;
  }
}
