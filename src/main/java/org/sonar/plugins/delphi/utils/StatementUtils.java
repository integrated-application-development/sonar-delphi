package org.sonar.plugins.delphi.utils;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.plugins.delphi.antlr.ast.node.ArgumentListNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.StatementNode;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;

public class StatementUtils {
  private StatementUtils() {
    // Utility class
  }

  public static boolean isMethodInvocation(
      StatementNode statement,
      String fullyQualifiedName,
      Predicate<List<ExpressionNode>> argumentListPredicate) {
    if (!(statement instanceof ExpressionStatementNode)) {
      return false;
    }

    var expression = ((ExpressionStatementNode) statement).getExpression().skipParentheses();
    if (!(expression instanceof PrimaryExpressionNode) || expression.jjtGetNumChildren() > 2) {
      return false;
    }

    Node name = expression.jjtGetChild(0);
    if (!(name instanceof NameReferenceNode)) {
      return false;
    }

    NameDeclaration declaration = ((NameReferenceNode) name).getLastName().getNameDeclaration();
    List<ExpressionNode> arguments = extractArguments(expression.jjtGetChild(1));
    return declaration instanceof MethodNameDeclaration
        && ((MethodNameDeclaration) declaration).fullyQualifiedName().equals(fullyQualifiedName)
        && argumentListPredicate.test(arguments);
  }

  private static List<ExpressionNode> extractArguments(Node argumentList) {
    if (argumentList instanceof ArgumentListNode) {
      return ((ArgumentListNode) argumentList).getArguments();
    }
    return Collections.emptyList();
  }
}
