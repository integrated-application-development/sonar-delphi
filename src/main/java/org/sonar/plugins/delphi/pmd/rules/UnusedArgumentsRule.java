/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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
package org.sonar.plugins.delphi.pmd.rules;

import static java.util.Collections.disjoint;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import org.sonar.plugins.delphi.antlr.ast.node.AsmStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.delphi.antlr.ast.node.IdentifierNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.symbol.declaration.MethodDirective;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;

/** Rule violation for unused function/procedure/method arguments */
public class UnusedArgumentsRule extends AbstractDelphiRule {
  private static final Set<MethodDirective> EXCLUDED_DIRECTIVES =
      EnumSet.of(MethodDirective.OVERRIDE, MethodDirective.VIRTUAL, MethodDirective.MESSAGE);

  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    List<FormalParameterData> unusedArguments =
        method.getParameters().stream()
            .filter(argument -> isUnusedArgument(method, argument.getNode()))
            .collect(Collectors.toList());

    if (!unusedArguments.isEmpty() && isExcludedMethod(method)) {
      unusedArguments.clear();
    }

    for (FormalParameterData unusedArgument : unusedArguments) {
      addViolationWithMessage(
          data,
          unusedArgument.getNode(),
          "Unused argument: ''{0}'' at {1}",
          new Object[] {unusedArgument.getImage(), method.fullyQualifiedName()});
    }

    return super.visit(method, data);
  }

  private static boolean isUnusedArgument(MethodNode method, NameDeclarationNode argument) {
    if (!argument.getUsages().isEmpty()) {
      return false;
    }

    // Since we don't actually parse assembler statements, we have to do a bit of guesswork here.
    // Ideally we would eventually expand the grammar to include inline assembler statements.
    // Then we could resolve name references within them.
    for (AsmStatementNode asmStatement : method.findDescendantsOfType(AsmStatementNode.class)) {
      for (IdentifierNode identifier : asmStatement.findDescendantsOfType(IdentifierNode.class)) {
        if (identifier.hasImageEqualTo(argument.getImage())) {
          return false;
        }
      }
    }

    return true;
  }

  private static boolean isExcludedMethod(MethodImplementationNode method) {
    MethodNameDeclaration declaration = method.getMethodNameDeclaration();
    if (declaration != null) {
      if (declaration.isPublished()) {
        return true;
      }

      if (!disjoint(declaration.getDirectives(), EXCLUDED_DIRECTIVES)) {
        return true;
      }

      for (NameOccurrence occurrence : method.getMethodNameNode().getUsages()) {
        if (((DelphiNameOccurrence) occurrence).isMethodReference()) {
          return true;
        }
      }
    }

    return false;
  }
}
