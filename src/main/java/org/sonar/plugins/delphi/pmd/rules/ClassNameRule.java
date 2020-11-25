package org.sonar.plugins.delphi.pmd.rules;

import java.util.List;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.utils.NameConventionUtils;

public class ClassNameRule extends AbstractDelphiRule {
  private static final PropertyDescriptor<List<String>> PREFIXES =
      PropertyFactory.stringListProperty("prefixes")
          .desc("Class names must begin with one of these prefixes.")
          .defaultValue(List.of("T", "E"))
          .build();

  public ClassNameRule() {
    definePropertyDescriptor(PREFIXES);
  }

  @Override
  public RuleContext visit(TypeDeclarationNode type, RuleContext data) {
    if (type.isClass()
        && !NameConventionUtils.compliesWithPrefix(type.simpleName(), getProperty(PREFIXES))) {
      addViolation(data, type.getTypeNameNode());
    }
    return super.visit(type, data);
  }
}
