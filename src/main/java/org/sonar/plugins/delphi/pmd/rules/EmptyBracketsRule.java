package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import org.sonar.plugins.delphi.antlr.ast.node.ArgumentListNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodParametersNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Typed;

public class EmptyBracketsRule extends AbstractDelphiRule {
  private static final String SYSTEM_ASSIGNED_IMAGE = "System.Assigned";

  @Override
  public RuleContext visit(MethodParametersNode parameters, RuleContext data) {
    if (parameters.isEmpty()) {
      addViolation(data, parameters);
    }
    return super.visit(parameters, data);
  }

  @Override
  public RuleContext visit(ArgumentListNode arguments, RuleContext data) {
    if (arguments.isEmpty()
        && !isExplicitArrayConstructorInvocation(arguments)
        && !isRequiredToDistinguishProceduralFromReturn(arguments)) {
      addViolation(data, arguments);
    }
    return super.visit(arguments, data);
  }

  private static boolean isExplicitArrayConstructorInvocation(ArgumentListNode arguments) {
    Node previous = arguments.jjtGetParent().jjtGetChild(arguments.jjtGetChildIndex() - 1);
    return previous instanceof NameReferenceNode
        && ((NameReferenceNode) previous).isExplicitArrayConstructorInvocation();
  }

  private static boolean isRequiredToDistinguishProceduralFromReturn(ArgumentListNode arguments) {
    return isProcVarInvocation(arguments) || isPartOfSystemAssignedArgumentExpression(arguments);
  }

  private static boolean isProcVarInvocation(ArgumentListNode arguments) {
    Node previous = arguments.jjtGetParent().jjtGetChild(arguments.jjtGetChildIndex() - 1);
    if (previous instanceof Typed) {
      Type type = ((Typed) previous).getType();
      return type.isProcedural() && !type.isMethod();
    }
    return true;
  }

  private static boolean isPartOfSystemAssignedArgumentExpression(ArgumentListNode arguments) {
    Node parent = arguments.jjtGetParent();
    if (parent instanceof PrimaryExpressionNode) {
      Node grandparent = parent.jjtGetParent();
      if (grandparent instanceof ArgumentListNode) {
        Node prev = grandparent.jjtGetParent().jjtGetChild(grandparent.jjtGetChildIndex() - 1);
        if (prev instanceof NameReferenceNode) {
          var declaration = ((NameReferenceNode) prev).getLastName().getNameDeclaration();
          return declaration instanceof MethodNameDeclaration
              && ((MethodNameDeclaration) declaration)
                  .fullyQualifiedName()
                  .equals(SYSTEM_ASSIGNED_IMAGE);
        }
      }
    }
    return false;
  }
}
