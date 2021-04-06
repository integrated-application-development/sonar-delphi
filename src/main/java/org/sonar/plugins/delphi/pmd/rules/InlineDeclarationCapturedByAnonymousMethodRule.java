package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.plugins.delphi.antlr.ast.node.AnonymousMethodNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;

public class InlineDeclarationCapturedByAnonymousMethodRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(NameReferenceNode node, RuleContext data) {
    if (isViolation(node)) {
      addViolation(data, node);
    }
    return super.visit(node, data);
  }

  private static boolean isViolation(NameReferenceNode node) {
    NameDeclaration declaration = node.getNameDeclaration();
    if (declaration instanceof VariableNameDeclaration) {
      VariableNameDeclaration varDeclaration = (VariableNameDeclaration) declaration;
      if (!varDeclaration.isInline()) {
        return false;
      }

      AnonymousMethodNode anonymousMethod = node.getFirstParentOfType(AnonymousMethodNode.class);
      if (anonymousMethod == null) {
        return false;
      }

      DelphiScope declarationScope = varDeclaration.getScope();
      DelphiScope scope = anonymousMethod.getScope();
      while ((scope = scope.getParent()) != null) {
        if (scope == declarationScope) {
          return true;
        }
      }
    }
    return false;
  }
}
