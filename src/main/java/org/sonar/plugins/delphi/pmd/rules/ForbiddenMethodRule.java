package org.sonar.plugins.delphi.pmd.rules;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.sonar.plugins.delphi.antlr.ast.node.MethodNameNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;

public class ForbiddenMethodRule extends AbstractDelphiRule {
  public static final PropertyDescriptor<List<String>> BLACKLISTED_METHODS =
      PropertyFactory.stringListProperty("blacklist")
          .desc("The list of forbidden (fully qualified) method names.")
          .emptyDefaultValue()
          .build();

  private static final PropertyDescriptor<String> MESSAGE =
      PropertyFactory.stringProperty("message").desc("The issue message").defaultValue("").build();

  private Set<String> blacklist;

  public ForbiddenMethodRule() {
    definePropertyDescriptor(BLACKLISTED_METHODS);
    definePropertyDescriptor(MESSAGE);
  }

  @Override
  public void start(RuleContext data) {
    blacklist = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    blacklist.addAll(getProperty(BLACKLISTED_METHODS));
  }

  @Override
  public RuleContext visit(NameReferenceNode reference, RuleContext data) {
    DelphiNameDeclaration declaration = reference.getNameDeclaration();
    if (declaration instanceof MethodNameDeclaration
        && blacklist.contains(((MethodNameDeclaration) declaration).fullyQualifiedName())) {
      addViolationWithMessage(data, reference.getIdentifier(), getProperty(MESSAGE));
    }
    return super.visit(reference, data);
  }

  @Override
  public RuleContext visit(MethodNameNode methodName, RuleContext data) {
    // It would be rude to flag the method's implementation just for existing.
    return data;
  }
}
