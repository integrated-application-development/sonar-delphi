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

import au.com.integradev.delphi.antlr.ast.node.ArgumentListNode;
import au.com.integradev.delphi.antlr.ast.node.NameReferenceNode;
import au.com.integradev.delphi.symbol.declaration.MethodNameDeclaration;
import javax.annotation.Nullable;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;

public class AssertMessageRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(NameReferenceNode nameNode, RuleContext data) {
    if (isAssert(nameNode) && isMissingErrorMessage(nameNode)) {
      addViolation(data, nameNode);
    }
    return super.visit(nameNode, data);
  }

  private static boolean isAssert(NameReferenceNode nameNode) {
    NameDeclaration nameDeclaration = nameNode.getNameDeclaration();
    return nameDeclaration instanceof MethodNameDeclaration
        && ((MethodNameDeclaration) nameDeclaration).fullyQualifiedName().equals("System.Assert");
  }

  private static boolean isMissingErrorMessage(NameReferenceNode nameNode) {
    ArgumentListNode argumentList = getArgumentList(nameNode);
    if (argumentList != null) {
      return argumentList.getArguments().size() < 2;
    }
    return false;
  }

  @Nullable
  private static ArgumentListNode getArgumentList(NameReferenceNode nameNode) {
    Node nextNode = nameNode.jjtGetParent().jjtGetChild(nameNode.jjtGetChildIndex() + 1);
    if (nextNode instanceof ArgumentListNode) {
      return (ArgumentListNode) nextNode;
    }
    return null;
  }
}
