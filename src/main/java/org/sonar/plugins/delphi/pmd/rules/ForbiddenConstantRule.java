package org.sonar.plugins.delphi.pmd.rules;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.symbol.scope.FileScope;

public class ForbiddenConstantRule extends AbstractDelphiRule {
  public static final PropertyDescriptor<List<String>> BLACKLISTED_CONSTANTS =
      PropertyFactory.stringListProperty("blacklist")
          .desc("The list of forbidden constant names.")
          .emptyDefaultValue()
          .build();

  public static final PropertyDescriptor<String> UNIT_NAME =
      PropertyFactory.stringProperty("unitName")
          .desc("The unit name where the forbidden constants are declared.")
          .defaultValue("")
          .build();

  private static final PropertyDescriptor<String> MESSAGE =
      PropertyFactory.stringProperty("message").desc("The issue message").defaultValue("").build();

  private Set<String> blacklist;

  public ForbiddenConstantRule() {
    definePropertyDescriptor(BLACKLISTED_CONSTANTS);
    definePropertyDescriptor(UNIT_NAME);
    definePropertyDescriptor(MESSAGE);
  }

  @Override
  public void start(RuleContext data) {
    blacklist = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    blacklist.addAll(getProperty(BLACKLISTED_CONSTANTS));
  }

  @Override
  public RuleContext visit(NameReferenceNode reference, RuleContext data) {
    DelphiNameDeclaration declaration = reference.getNameDeclaration();
    if (declaration instanceof VariableNameDeclaration) {
      DelphiScope scope = declaration.getScope();
      if (scope instanceof FileScope) {
        String unitName = ((FileScope) scope).getUnitDeclaration().fullyQualifiedName();
        String constantName = declaration.getName();
        if (unitName.equalsIgnoreCase(getProperty(UNIT_NAME)) && blacklist.contains(constantName)) {
          addViolationWithMessage(data, reference.getIdentifier(), getProperty(MESSAGE));
        }
      }
    }
    return super.visit(reference, data);
  }
}
