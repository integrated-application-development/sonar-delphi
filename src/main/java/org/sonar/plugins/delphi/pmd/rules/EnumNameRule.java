package org.sonar.plugins.delphi.pmd.rules;

import java.util.List;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.utils.NameConventionUtils;

public class EnumNameRule extends AbstractDelphiRule {
  private static final PropertyDescriptor<List<String>> PREFIXES =
      PropertyFactory.stringListProperty("prefixes")
          .desc("If defined, enum names must begin with one of these prefixes.")
          .defaultValue(List.of("T"))
          .build();

  public EnumNameRule() {
    definePropertyDescriptor(PREFIXES);
  }

  @Override
  public RuleContext visit(TypeDeclarationNode type, RuleContext data) {
    if (type.isEnum()
        && !NameConventionUtils.compliesWithPrefix(type.simpleName(), getProperty(PREFIXES))) {
      addViolation(data, type.getTypeNameNode());
    }
    return super.visit(type, data);
  }
}
