package org.sonar.plugins.delphi.utils;

import org.sonar.plugins.delphi.antlr.ast.node.VarDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarSectionNode;
import org.sonar.plugins.delphi.symbol.scope.FileScope;

public class VariableUtils {
  private VariableUtils() {
    // Utility class
  }

  public static boolean isGeneratedFormVariable(VarDeclarationNode varDecl) {
    VarSectionNode varSection = varDecl.getVarSection();
    if (!varSection.isInterfaceSection()) {
      return false;
    }

    if (!(varSection.getScope() instanceof FileScope)) {
      return false;
    }

    if (varSection.getDeclarations().size() != 1) {
      return false;
    }

    if (varSection.jjtGetChildIndex() != varSection.jjtGetParent().jjtGetNumChildren() - 1) {
      return false;
    }

    return varDecl.getType().isSubTypeOf("System.Classes.TComponent");
  }
}
