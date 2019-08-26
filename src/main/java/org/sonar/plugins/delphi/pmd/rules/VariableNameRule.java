package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.IdentifierNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarSectionNode;

/**
 * This rule looks at all variable names to ensure they follow basic Pascal Case. At the moment it
 * only checks if the first character is uppercase
 */
public class VariableNameRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(VarDeclarationNode varDecl, RuleContext data) {
    if (isAutoCreateFormVar(varDecl)) {
      return data;
    }

    for (IdentifierNode identifier : varDecl.getIdentifierList().getIdentifiers()) {
      if (!Character.isUpperCase(identifier.getImage().charAt(0))) {
        addViolation(data, identifier);
      }
    }

    return data;
  }

  private boolean isAutoCreateFormVar(VarDeclarationNode varDecl) {
    VarSectionNode varSection = varDecl.getVarSection();
    return varSection.isInterfaceSection()
        && varSection.getDeclarations().size() == 1
        && varSection.jjtGetChildIndex() == varSection.jjtGetParent().jjtGetNumChildren() - 1;
  }
}
