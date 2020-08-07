package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;

public class MixedNamesRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(NameReferenceNode reference, RuleContext data) {
    DelphiNameOccurrence occurrence = reference.getNameOccurrence();
    DelphiNameDeclaration declaration = reference.getNameDeclaration();

    if (occurrence != null && declaration != null && !occurrence.isSelf()) {
      String actual = occurrence.getImage();
      String expected = declaration.getImage();
      if (!expected.equals(actual)) {
        addViolationWithMessage(
            data,
            reference.getIdentifier(),
            "Avoid mixing names (found: ''{0}'' expected: ''{1}'').",
            new Object[] {actual, expected});
      }
    }

    return super.visit(reference, data);
  }
}
