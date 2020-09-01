package org.sonar.plugins.delphi.pmd.rules;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.PropertyNameDeclaration;

public class ForbiddenPropertyRule extends AbstractDelphiRule {
  public static final PropertyDescriptor<List<String>> BLACKLISTED_PROPERTIES =
      PropertyFactory.stringListProperty("blacklist")
          .desc("The list of forbidden (fully qualified) property names.")
          .emptyDefaultValue()
          .build();

  private static final PropertyDescriptor<String> MESSAGE =
      PropertyFactory.stringProperty("message").desc("The issue message").defaultValue("").build();

  private Set<String> blacklist;

  public ForbiddenPropertyRule() {
    definePropertyDescriptor(BLACKLISTED_PROPERTIES);
    definePropertyDescriptor(MESSAGE);
  }

  @Override
  public void start(RuleContext data) {
    blacklist = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    blacklist.addAll(getProperty(BLACKLISTED_PROPERTIES));
  }

  @Override
  public RuleContext visit(NameReferenceNode reference, RuleContext data) {
    DelphiNameDeclaration declaration = reference.getNameDeclaration();
    if (declaration instanceof PropertyNameDeclaration
        && blacklist.contains(((PropertyNameDeclaration) declaration).fullyQualifiedName())) {
      addViolationWithMessage(data, reference.getIdentifier(), getProperty(MESSAGE));
    }
    return super.visit(reference, data);
  }
}
