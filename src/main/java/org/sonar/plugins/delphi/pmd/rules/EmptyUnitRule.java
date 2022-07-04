package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.node.ConstDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodNode;
import org.sonar.plugins.delphi.antlr.ast.node.StatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarDeclarationNode;
import org.sonar.plugins.delphi.pmd.FilePosition;

public class EmptyUnitRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(DelphiAST ast, RuleContext data) {
    if (!ast.isPackage() && !hasMeaningfulCode(ast)) {
      newViolation(data).atPosition(FilePosition.atFileLevel()).save();
    }
    return data;
  }

  private static boolean hasMeaningfulCode(DelphiNode node) {
    return node.hasDescendantOfAnyType(
        MethodNode.class,
        StatementNode.class,
        VarDeclarationNode.class,
        ConstDeclarationNode.class,
        TypeDeclarationNode.class);
  }
}
