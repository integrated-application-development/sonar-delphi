package org.sonar.plugins.delphi.pmd.rules;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.EnumElementNameDeclaration;

public class ForbiddenEnumValueRule extends AbstractDelphiRule {
  public static final PropertyDescriptor<List<String>> BLACKLISTED_ENUM_VALUES =
      PropertyFactory.stringListProperty("blacklist")
          .desc("The list of forbidden enum values.")
          .emptyDefaultValue()
          .build();

  public static final PropertyDescriptor<String> ENUM_NAME =
      PropertyFactory.stringProperty("enumName")
          .desc("The fully qualified name of the enum type.")
          .defaultValue("")
          .build();

  private static final PropertyDescriptor<String> MESSAGE =
      PropertyFactory.stringProperty("message").desc("The issue message").defaultValue("").build();

  private Set<String> blacklist;

  public ForbiddenEnumValueRule() {
    definePropertyDescriptor(BLACKLISTED_ENUM_VALUES);
    definePropertyDescriptor(ENUM_NAME);
    definePropertyDescriptor(MESSAGE);
  }

  @Override
  public void start(RuleContext data) {
    blacklist = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    blacklist.addAll(getProperty(BLACKLISTED_ENUM_VALUES));
  }

  @Override
  public RuleContext visit(NameReferenceNode reference, RuleContext data) {
    DelphiNameDeclaration declaration = reference.getNameDeclaration();
    if (declaration instanceof EnumElementNameDeclaration) {
      var element = (EnumElementNameDeclaration) declaration;
      if (element.getType().is(getProperty(ENUM_NAME)) && blacklist.contains(element.getName())) {
        addViolation(data, reference);
      }
    }
    return super.visit(reference, data);
  }
}
