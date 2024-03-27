/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.ast.utils.ExpressionNodeUtils;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType;
import org.sonar.plugins.communitydelphi.api.type.Type.StructType;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "PlatformDependentCastRule", repositoryKey = "delph")
@Rule(key = "PlatformDependentCast")
public class PlatformDependentCastCheck extends DelphiCheck {
  private static final String MESSAGE =
      "Replace this problematic cast, which will behave differently on different target platforms.";

  @Override
  public DelphiCheckContext visit(ArgumentListNode argumentList, DelphiCheckContext context) {
    List<ArgumentNode> arguments = argumentList.getArgumentNodes();
    if (arguments.size() == 1) {
      ExpressionNode expression = arguments.get(0).getExpression();
      if (!ExpressionNodeUtils.isNilLiteral(expression)
          && !ExpressionNodeUtils.isIntegerLiteral(expression)) {
        Type originalType = getOriginalType(expression);
        Type castedType = getHardCastedType(argumentList, context);

        if (isPlatformDependentCast(originalType, castedType)) {
          DelphiNode name = argumentList.getParent().getChild(argumentList.getChildIndex() - 1);

          context
              .newIssue()
              .onFilePosition(
                  FilePosition.from(
                      name.getBeginLine(),
                      name.getBeginColumn(),
                      argumentList.getEndLine(),
                      argumentList.getEndColumn()))
              .withMessage(MESSAGE)
              .report();
        }
      }
    }
    return super.visit(argumentList, context);
  }

  private static Type getOriginalType(ExpressionNode expression) {
    Type result = expression.getType();
    if (result.isRoutine()) {
      result = ((ProceduralType) result).returnType();
    }
    return result;
  }

  private static Type getHardCastedType(ArgumentListNode argumentList, DelphiCheckContext context) {
    Node previous = argumentList.getParent().getChild(argumentList.getChildIndex() - 1);
    if (previous instanceof NameReferenceNode) {
      NameReferenceNode nameReference = ((NameReferenceNode) previous);
      NameDeclaration declaration = nameReference.getLastName().getNameDeclaration();
      if (declaration instanceof TypeNameDeclaration) {
        return ((TypeNameDeclaration) declaration).getType();
      }
    } else if (previous.getTokenType() == DelphiTokenType.STRING) {
      return context.getTypeFactory().getIntrinsic(IntrinsicType.UNICODESTRING);
    }
    return TypeFactory.unknownType();
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
        || isTypeOrAlias(type, IntrinsicType.NATIVEINT)
        || isTypeOrAlias(type, IntrinsicType.NATIVEUINT);
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

  private static boolean isTypeOrAlias(Type type, IntrinsicType intrinsicType) {
    while (true) {
      if (type.is(intrinsicType)) {
        return true;
      }

      if (type.isAlias()) {
        type = ((Type.AliasType) type).aliasedType();
      } else {
        break;
      }
    }
    return false;
  }
}
