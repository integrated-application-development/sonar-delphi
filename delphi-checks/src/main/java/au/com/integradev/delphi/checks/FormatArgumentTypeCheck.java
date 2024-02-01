/*
 * Sonar Delphi Plugin
 * Copyright (C) 2024 Integrated Application Development
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

import au.com.integradev.delphi.utils.format.DelphiFormatArgument;
import au.com.integradev.delphi.utils.format.DelphiFormatString;
import au.com.integradev.delphi.utils.format.FormatSpecifierType;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.ArrayConstructorNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.PointerType;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType;

@Rule(key = "FormatArgumentType")
public class FormatArgumentTypeCheck extends AbstractFormatArgumentCheck {
  @Override
  protected void checkFormatStringViolation(
      DelphiFormatString formatString,
      ArrayConstructorNode arrayConstructor,
      DelphiCheckContext context) {
    List<DelphiFormatArgument> formatArgs = formatString.getArguments();
    List<ExpressionNode> expressions = arrayConstructor.getElements();

    for (int i = 0; i < Math.min(formatArgs.size(), expressions.size()); i++) {
      DelphiFormatArgument arg = formatArgs.get(i);
      Type exprType = expressions.get(i).getType();

      if (!arg.getTypes().stream()
          .allMatch(specifierType -> isAcceptedType(exprType, specifierType))) {
        reportIssue(context, expressions.get(i), getMessage(arg.getTypes()));
      }
    }
  }

  private static String getMessage(Set<FormatSpecifierType> argTypes) {
    String itsSpecifiersText =
        argTypes.size() == 1 ? "the corresponding specifier" : "all corresponding specifiers";
    String specifierList =
        argTypes.stream()
            .map(FormatSpecifierType::getImage)
            .sorted()
            .map(typeChar -> "%" + typeChar)
            .collect(Collectors.joining());

    return String.format(
        "Change this formatting argument to match the type of %s (%s).",
        itsSpecifiersText, specifierList);
  }

  private static boolean isAcceptedType(Type exprType, FormatSpecifierType specifierType) {
    if (exprType instanceof ProceduralType) {
      exprType = ((ProceduralType) exprType).returnType();
    }

    switch (specifierType) {
      case DECIMAL:
      case UNSIGNED_DECIMAL:
      case HEXADECIMAL:
        return exprType.isInteger();
      case SCIENTIFIC:
      case FIXED:
      case GENERAL:
      case NUMBER:
      case MONEY:
        return exprType.isReal();
      case POINTER:
        return exprType.isPointer();
      case STRING:
        return exprType.isString()
            || exprType.isChar()
            || ((exprType instanceof PointerType)
                && ((PointerType) exprType).dereferencedType().isChar());
      default:
        throw new AssertionError("Unknown format specifier type");
    }
  }
}
