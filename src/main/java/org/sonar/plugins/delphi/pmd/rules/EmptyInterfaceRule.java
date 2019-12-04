package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.VisibilitySectionNode;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;

public class EmptyInterfaceRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(TypeDeclarationNode typeDecl, RuleContext data) {
    if (typeDecl.isInterface()) {
      TypeNode typeNode = typeDecl.getTypeNode();
      boolean isEmpty = typeNode.getFirstChildOfType(VisibilitySectionNode.class) == null;
      TypeNameDeclaration declaration = typeDecl.getTypeNameDeclaration();

      if (isEmpty && (declaration == null || !declaration.isForwardDeclaration())) {
        addViolation(data, typeDecl.getTypeNameNode());
      }
    }

    return super.visit(typeDecl, data);
  }
}
