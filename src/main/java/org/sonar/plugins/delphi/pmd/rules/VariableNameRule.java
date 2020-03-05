package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameter;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarSectionNode;
import org.sonar.plugins.delphi.symbol.scope.UnitScope;
import org.sonar.plugins.delphi.utils.NameConventionUtils;

/**
 * This rule looks at all variable names to ensure they follow basic Pascal Case. Also checks naming
 * conventions for global variables.
 */
public class VariableNameRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(VarDeclarationNode varDecl, RuleContext data) {
    if (isAutoCreateFormVar(varDecl)) {
      return data;
    }

    boolean globalVariable = varDecl.getScope() instanceof UnitScope;

    for (NameDeclarationNode declaration : varDecl.getNameDeclarationList().getDeclarations()) {
      if (isViolation(declaration, globalVariable)) {
        addViolation(data, declaration);
      }
    }

    return data;
  }

  @Override
  public RuleContext visit(FormalParameterNode parameter, RuleContext data) {
    for (FormalParameter param : parameter.getParameters()) {
      NameDeclarationNode node = param.getNode();
      if (isViolation(node, false)) {
        addViolation(data, node);
      }
    }
    return data;
  }

  private static boolean isAutoCreateFormVar(VarDeclarationNode varDecl) {
    VarSectionNode varSection = varDecl.getVarSection();
    return varSection.isInterfaceSection()
        && varSection.getDeclarations().size() == 1
        && varSection.jjtGetChildIndex() == varSection.jjtGetParent().jjtGetNumChildren() - 1;
  }

  private static boolean isViolation(NameDeclarationNode identifier, boolean globalVariable) {
    String image = identifier.getImage();
    if (globalVariable) {
      return !NameConventionUtils.compliesWithPrefix(image, "G") || image.contains("_");
    }
    return !Character.isUpperCase(image.charAt(0));
  }
}
