package org.sonar.plugins.delphi.pmd.rules;

import java.util.List;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.plugins.delphi.antlr.ast.node.ArgumentListNode;
import org.sonar.plugins.delphi.antlr.ast.node.AssignmentStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.symbol.declaration.TypedDeclaration;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.TypeUtils;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicType;

public class PlatformDependentTruncationRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(AssignmentStatementNode assignment, RuleContext data) {
    if (isViolation(assignment.getValue().getType(), assignment.getAssignee().getType())) {
      addViolation(data, assignment);
    }
    return super.visit(assignment, data);
  }

  @Override
  public RuleContext visit(ArgumentListNode argumentList, RuleContext data) {
    Node previous = argumentList.jjtGetParent().jjtGetChild(argumentList.jjtGetChildIndex() - 1);
    if (!(previous instanceof NameReferenceNode)) {
      return super.visit(argumentList, data);
    }

    ProceduralType procedural = getProceduralType((NameReferenceNode) previous);
    if (procedural == null) {
      return super.visit(argumentList, data);
    }

    List<ExpressionNode> arguments = argumentList.getArguments();
    List<Type> parameterTypes = procedural.parameterTypes();
    for (int i = 0; i < arguments.size() && i < parameterTypes.size(); ++i) {
      ExpressionNode argument = arguments.get(i);
      if (isViolation(argument.getType(), parameterTypes.get(i))) {
        addViolation(data, argument);
      }
    }
    return super.visit(argumentList, data);
  }

  private static ProceduralType getProceduralType(NameReferenceNode reference) {
    NameDeclaration declaration = reference.getNameDeclaration();
    if (declaration instanceof TypedDeclaration) {
      Type type = ((TypedDeclaration) declaration).getType();
      if (type instanceof ProceduralType) {
        return (ProceduralType) type;
      }
    }
    return null;
  }

  private static boolean isViolation(Type from, Type to) {
    if (!from.isInteger() || !to.isInteger()) {
      return false;
    }

    if (isNativeInteger(from) == isNativeInteger(to)) {
      return false;
    }

    return (isNativeInteger(from) && to.size() < 8) || (isNativeInteger(to) && from.size() > 4);
  }

  private static boolean isNativeInteger(Type type) {
    type = TypeUtils.findBaseType(type);
    return type.is(IntrinsicType.NATIVEINT) || type.is(IntrinsicType.NATIVEUINT);
  }
}
