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

import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "AssertMessageRule", repositoryKey = "delph")
@Rule(key = "AssertMessage")
public class AssertMessageCheck extends DelphiCheck {
  private static final String MESSAGE = "Add a message to this assertion.";

  @Override
  public DelphiCheckContext visit(NameReferenceNode nameNode, DelphiCheckContext context) {
    if (isAssert(nameNode) && isMissingErrorMessage(nameNode)) {
      reportIssue(context, nameNode, MESSAGE);
    }
    return super.visit(nameNode, context);
  }

  private static boolean isAssert(NameReferenceNode nameNode) {
    NameDeclaration nameDeclaration = nameNode.getNameDeclaration();
    return nameDeclaration instanceof RoutineNameDeclaration
        && ((RoutineNameDeclaration) nameDeclaration).fullyQualifiedName().equals("System.Assert");
  }

  private static boolean isMissingErrorMessage(NameReferenceNode nameNode) {
    ArgumentListNode argumentList = getArgumentList(nameNode);
    if (argumentList != null) {
      return argumentList.getArgumentNodes().size() < 2;
    }
    return false;
  }

  @Nullable
  private static ArgumentListNode getArgumentList(NameReferenceNode nameNode) {
    Node nextNode = nameNode.getParent().getChild(nameNode.getChildIndex() + 1);
    if (nextNode instanceof ArgumentListNode) {
      return (ArgumentListNode) nextNode;
    }
    return null;
  }
}
