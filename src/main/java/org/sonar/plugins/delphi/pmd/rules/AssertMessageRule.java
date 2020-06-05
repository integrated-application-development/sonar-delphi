package org.sonar.plugins.delphi.pmd.rules;

import javax.annotation.Nullable;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.plugins.delphi.antlr.ast.node.ArgumentListNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;

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
