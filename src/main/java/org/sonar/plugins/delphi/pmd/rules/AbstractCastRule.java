package org.sonar.plugins.delphi.pmd.rules;

import java.util.List;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.node.ArgumentListNode;
import org.sonar.plugins.delphi.antlr.ast.node.BinaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.operator.BinaryOperator;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ClassReferenceType;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicType;

public abstract class AbstractCastRule extends AbstractDelphiRule {
  protected abstract boolean isViolation(Type originalType, Type castType);

  @Override
  public RuleContext visit(BinaryExpressionNode binaryExpression, RuleContext data) {
    if (binaryExpression.getOperator() == BinaryOperator.AS) {
      Type originalType = getOriginalType(binaryExpression.getLeft());
      Type castedType = getSoftCastedType(binaryExpression.getRight());

      if (isViolation(originalType, castedType)
          && !originalType.isUnknown()
          && !castedType.isUnknown()) {
        addViolation(data, binaryExpression);
      }
    }
    return super.visit(binaryExpression, data);
  }

  @Override
  public RuleContext visit(ArgumentListNode argumentList, RuleContext data) {
    List<ExpressionNode> arguments = argumentList.getArguments();
    if (arguments.size() == 1) {
      Type originalType = getOriginalType(arguments.get(0));
      Type castedType = getHardCastedType(argumentList);
      if (castedType != null && isViolation(originalType, castedType)) {
        addViolation(data, argumentList);
      }
    }
    return super.visit(argumentList, data);
  }

  private static Type getOriginalType(ExpressionNode expression) {
    Type result = expression.getType();
    if (result.isMethod()) {
      result = ((ProceduralType) result).returnType();
    }
    return result;
  }

  private static Type getSoftCastedType(ExpressionNode expression) {
    Type result = expression.getType();
    if (result.isClassReference()) {
      result = ((ClassReferenceType) result).classType();
    }
    return result;
  }

  private static Type getHardCastedType(ArgumentListNode argumentList) {
    Node previous = argumentList.jjtGetParent().jjtGetChild(argumentList.jjtGetChildIndex() - 1);
    if (previous instanceof NameReferenceNode) {
      NameReferenceNode nameReference = ((NameReferenceNode) previous);
      DelphiNameDeclaration declaration = nameReference.getLastName().getNameDeclaration();
      if (declaration instanceof TypeNameDeclaration) {
        return ((TypeNameDeclaration) declaration).getType();
      }
    }

    switch (previous.jjtGetId()) {
      case DelphiLexer.STRING:
        return argumentList.getTypeFactory().getIntrinsic(IntrinsicType.STRING);
      case DelphiLexer.FILE:
        return argumentList.getTypeFactory().untypedFile();
      default:
        return null;
    }
  }
}
