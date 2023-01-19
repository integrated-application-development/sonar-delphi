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
package au.com.integradev.delphi.pmd.rules;

import au.com.integradev.delphi.antlr.DelphiLexer;
import au.com.integradev.delphi.antlr.ast.node.ArgumentListNode;
import au.com.integradev.delphi.antlr.ast.node.CommonDelphiNode;
import au.com.integradev.delphi.antlr.ast.node.ExpressionNode;
import au.com.integradev.delphi.antlr.ast.node.NameReferenceNode;
import au.com.integradev.delphi.symbol.declaration.DelphiNameDeclaration;
import au.com.integradev.delphi.symbol.declaration.TypeNameDeclaration;
import au.com.integradev.delphi.type.DelphiType;
import au.com.integradev.delphi.type.Type;
import au.com.integradev.delphi.type.Type.ProceduralType;
import au.com.integradev.delphi.type.Type.StructType;
import au.com.integradev.delphi.type.TypeUtils;
import au.com.integradev.delphi.type.intrinsic.IntrinsicType;
import java.util.List;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;

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
