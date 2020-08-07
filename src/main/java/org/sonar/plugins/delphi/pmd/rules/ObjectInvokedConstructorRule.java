package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.MethodKind;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.delphi.type.Typed;

public class ObjectInvokedConstructorRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(NameReferenceNode reference, RuleContext data) {
    if (isConstructor(reference) && isInvokedOnObject(reference)) {
      addViolation(data, reference.getIdentifier());
    }
    return super.visit(reference, data);
  }

  private static boolean isConstructor(NameReferenceNode reference) {
    DelphiNameDeclaration declaration = reference.getNameDeclaration();
    return declaration instanceof MethodNameDeclaration
        && ((MethodNameDeclaration) declaration).getMethodKind() == MethodKind.CONSTRUCTOR;
  }

  private static boolean isInvokedOnObject(NameReferenceNode reference) {
    if (reference.getFirstParentOfType(PrimaryExpressionNode.class) == null) {
      return false;
    }

    NameReferenceNode previous = reference.prevName();
    if (previous == null) {
      return false;
    }

    DelphiNameDeclaration declaration = previous.getNameDeclaration();
    return declaration instanceof VariableNameDeclaration
        && !((Typed) declaration).getType().isClassReference()
        && !previous.getNameOccurrence().isSelf();
  }
}
