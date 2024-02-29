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

import au.com.integradev.delphi.utils.format.DelphiFormatString;
import au.com.integradev.delphi.utils.format.DelphiFormatStringException;
import au.com.integradev.delphi.utils.format.FormatStringParser;
import java.util.List;
import java.util.Optional;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.ArrayConstructorNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.TextLiteralNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;

public abstract class AbstractFormatArgumentCheck extends DelphiCheck {
  @Override
  public DelphiCheckContext visit(NameReferenceNode nameReference, DelphiCheckContext context) {
    if (isSystemFormatCall(nameReference)) {
      checkViolation(nameReference, context);
    }

    return context;
  }

  private boolean isSystemFormatCall(NameReferenceNode nameReference) {
    NameDeclaration declaration = nameReference.getNameDeclaration();

    return (declaration instanceof RoutineNameDeclaration)
        && ((RoutineNameDeclaration) declaration)
            .fullyQualifiedName()
            .equalsIgnoreCase("System.SysUtils.Format");
  }

  private void checkViolation(NameReferenceNode nameReference, DelphiCheckContext context) {
    ArgumentListNode argumentList =
        nameReference.getParent().getFirstChildOfType(ArgumentListNode.class);
    if (argumentList == null) {
      return;
    }

    List<ExpressionNode> arguments = argumentList.getArguments();
    if (arguments.size() < 2) {
      return;
    }

    Optional<TextLiteralNode> textLiteral = getLiteralArgument(arguments.get(0));
    Optional<ArrayConstructorNode> arrayConstructor = getArrayConstructorArgument(arguments.get(1));

    if (textLiteral.isEmpty() || arrayConstructor.isEmpty()) {
      return;
    }

    String rawFormatString = textLiteral.get().getValue();
    FormatStringParser parser = new FormatStringParser(rawFormatString);
    try {
      checkFormatStringViolation(parser.parse(), arrayConstructor.get(), context);
    } catch (DelphiFormatStringException e) {
      handleInvalidFormatString(textLiteral.get(), context);
    }
  }

  protected void handleInvalidFormatString(
      TextLiteralNode textLiteral, DelphiCheckContext context) {}

  protected void checkFormatStringViolation(
      DelphiFormatString formatString,
      ArrayConstructorNode arrayConstructor,
      DelphiCheckContext context) {}

  private Optional<TextLiteralNode> getLiteralArgument(DelphiNode argument) {
    if (argument instanceof PrimaryExpressionNode
        && argument.getChild(0) instanceof TextLiteralNode) {
      return Optional.of((TextLiteralNode) argument.getChild(0));
    }

    return Optional.empty();
  }

  private Optional<ArrayConstructorNode> getArrayConstructorArgument(DelphiNode argument) {
    if (argument instanceof PrimaryExpressionNode
        && argument.getChild(0) instanceof ArrayConstructorNode) {
      return Optional.of((ArrayConstructorNode) argument.getChild(0));
    }

    return Optional.empty();
  }
}
