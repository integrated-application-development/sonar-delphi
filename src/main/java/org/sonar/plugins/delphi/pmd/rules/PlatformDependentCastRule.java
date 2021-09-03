package org.sonar.plugins.delphi.pmd.rules;

import java.util.List;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.node.ArgumentListNode;
import org.sonar.plugins.delphi.antlr.ast.node.CommonDelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.Type.StructType;
import org.sonar.plugins.delphi.type.TypeUtils;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicType;

public class PlatformDependentCastRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(ArgumentListNode argumentList, RuleContext data) {
    List<ExpressionNode> arguments = argumentList.getArguments();
    if (arguments.size() == 1) {
      ExpressionNode expression = arguments.get(0);
      if (!expression.isLiteral()) {
        Type originalType = TypeUtils.findBaseType(getOriginalType(expression));
        Type castedType = TypeUtils.findBaseType(getHardCastedType(argumentList));

        if (isPlatformDependentCast(originalType, castedType)) {
          addViolation(data, argumentList);
        }
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

  private static Type getHardCastedType(ArgumentListNode argumentList) {
    Node previous = argumentList.jjtGetParent().jjtGetChild(argumentList.jjtGetChildIndex() - 1);
    if (previous instanceof NameReferenceNode) {
      NameReferenceNode nameReference = ((NameReferenceNode) previous);
      DelphiNameDeclaration declaration = nameReference.getLastName().getNameDeclaration();
      if (declaration instanceof TypeNameDeclaration) {
        return ((TypeNameDeclaration) declaration).getType();
      }
    } else if (previous instanceof CommonDelphiNode) {
      int tokenType = ((CommonDelphiNode) previous).getToken().getType();
      if (tokenType == DelphiLexer.STRING) {
        return argumentList.getTypeFactory().getIntrinsic(IntrinsicType.UNICODESTRING);
      }
    }
    return DelphiType.unknownType();
  }

  private static boolean isPlatformDependentCast(Type originalType, Type castedType) {
    return isApplicableType(originalType)
        && isApplicableType(castedType)
        && isPlatformDependentType(originalType) != isPlatformDependentType(castedType);
  }

  private static boolean isApplicableType(Type type) {
    return isPointerBasedType(type) || type.isInteger();
  }

  private static boolean isPlatformDependentType(Type type) {
    return isPointerBasedType(type)
        || type.is(IntrinsicType.NATIVEINT)
        || type.is(IntrinsicType.NATIVEUINT);
  }

  private static boolean isPointerBasedType(Type type) {
    if (type.isPointer() || type.isString() || type.isArray()) {
      return true;
    }

    if (type.isStruct()) {
      switch (((StructType) type).kind()) {
        case CLASS:
        case INTERFACE:
          return true;
        default:
          // do nothing
      }
    }

    return false;
  }
}
