package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.symbol.declaration.PropertyNameDeclaration;

public class ExplicitDefaultPropertyReferenceRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(NameReferenceNode nameReference, RuleContext data) {
    if (isExplicitDefaultArrayPropertyReference(nameReference)) {
      addViolation(data, nameReference);
    }
    return super.visit(nameReference, data);
  }

  private static boolean isExplicitDefaultArrayPropertyReference(NameReferenceNode nameReference) {
    if (nameReference.prevName() != null && nameReference.nextName() == null) {
      NameDeclaration declaration = nameReference.getNameDeclaration();
      if (declaration instanceof PropertyNameDeclaration) {
        PropertyNameDeclaration property = (PropertyNameDeclaration) declaration;
        return property.isArrayProperty() && property.isDefaultProperty();
      }
    }
    return false;
  }
}
