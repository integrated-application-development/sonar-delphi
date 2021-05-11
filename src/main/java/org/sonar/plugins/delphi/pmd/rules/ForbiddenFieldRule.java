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
import org.sonar.plugins.delphi.symbol.scope.TypeScope;
import org.sonar.plugins.delphi.type.Type;

public class ForbiddenFieldRule extends AbstractDelphiRule {
  public static final PropertyDescriptor<List<String>> BLACKLISTED_FIELDS =
      PropertyFactory.stringListProperty("blacklist")
          .desc("The list of forbidden field names.")
          .emptyDefaultValue()
          .build();

  public static final PropertyDescriptor<String> DECLARING_TYPE =
      PropertyFactory.stringProperty("declaringType")
          .desc("The type where the forbidden fields are declared.")
          .defaultValue("")
          .build();

  private static final PropertyDescriptor<String> MESSAGE =
      PropertyFactory.stringProperty("message").desc("The issue message").defaultValue("").build();

  private Set<String> blacklist;

  public ForbiddenFieldRule() {
    definePropertyDescriptor(BLACKLISTED_FIELDS);
    definePropertyDescriptor(DECLARING_TYPE);
    definePropertyDescriptor(MESSAGE);
  }

  @Override
  public void start(RuleContext data) {
    blacklist = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    blacklist.addAll(getProperty(BLACKLISTED_FIELDS));
  }

  @Override
  public RuleContext visit(NameReferenceNode reference, RuleContext data) {
    DelphiNameDeclaration declaration = reference.getNameDeclaration();
    if (declaration instanceof VariableNameDeclaration) {
      TypeScope scope = declaration.getScope().getEnclosingScope(TypeScope.class);
      if (scope != null) {
        Type type = scope.getType();
        String fieldName = declaration.getName();
        if (type.is(getProperty(DECLARING_TYPE)) && blacklist.contains(fieldName)) {
          addViolationWithMessage(data, reference.getIdentifier(), getProperty(MESSAGE));
        }
      }
    }
    return super.visit(reference, data);
  }
}
