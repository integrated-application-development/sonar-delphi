package org.sonar.plugins.delphi.pmd.rules;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;

public class ForbiddenIdentifiersRule extends AbstractDelphiRule {
  public static final PropertyDescriptor<List<String>> BLACKLISTED_NAMES =
      PropertyFactory.stringListProperty("blacklist")
          .desc("The list of forbidden identifiers. (case-insensitive)")
          .defaultValue(Collections.emptyList())
          .build();

  private Set<String> blacklist;

  public ForbiddenIdentifiersRule() {
    definePropertyDescriptor(BLACKLISTED_NAMES);
  }

  @Override
  public void start(RuleContext data) {
    blacklist = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    blacklist.addAll(getProperty(BLACKLISTED_NAMES));
  }

  @Override
  public RuleContext visit(NameDeclarationNode node, RuleContext data) {
    if (blacklist.contains(node.getImage())) {
      addViolation(data, node);
    }
    return super.visit(node, data);
  }
}
