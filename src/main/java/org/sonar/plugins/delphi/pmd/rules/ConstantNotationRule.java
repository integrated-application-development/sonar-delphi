package org.sonar.plugins.delphi.pmd.rules;

import java.util.List;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.sonar.plugins.delphi.antlr.ast.node.ConstDeclarationNode;
import org.sonar.plugins.delphi.utils.NameConventionUtils;

public class ConstantNotationRule extends AbstractDelphiRule {
  public static final PropertyDescriptor<List<String>> PREFIXES =
      PropertyFactory.stringListProperty("prefixes")
          .desc("If defined, constants must begin with one of these prefixes.")
          .emptyDefaultValue()
          .build();

  public ConstantNotationRule() {
    definePropertyDescriptor(PREFIXES);
  }

  @Override
  public RuleContext visit(ConstDeclarationNode declaration, RuleContext data) {
    if (!NameConventionUtils.compliesWithPrefix(
        declaration.getNameDeclarationNode().getImage(), getProperty(PREFIXES))) {
      addViolation(data, declaration.getNameDeclarationNode());
    }
    return super.visit(declaration, data);
  }
}
