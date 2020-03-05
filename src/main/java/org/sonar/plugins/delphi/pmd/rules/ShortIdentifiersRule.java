package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.pmd.DelphiPmdConstants.LIMIT;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.sonar.plugins.delphi.antlr.ast.node.GenericDefinitionNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.UnitImportNode;

public class ShortIdentifiersRule extends AbstractDelphiRule {

  private static final PropertyDescriptor<List<String>> WHITELISTED_NAMES =
      PropertyFactory.stringListProperty("whitelist")
          .desc("The list of short identifiers that we allow. (case-insensitive)")
          .defaultValue(Collections.emptyList())
          .build();

  private Set<String> whitelist;

  public ShortIdentifiersRule() {
    definePropertyDescriptor(WHITELISTED_NAMES);
  }

  @Override
  public void start(RuleContext data) {
    whitelist = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    whitelist.addAll(getProperty(WHITELISTED_NAMES));
  }

  @Override
  public RuleContext visit(UnitImportNode node, RuleContext data) {
    // If a unit name is too short, we want to flag it in that file instead.
    return data;
  }

  @Override
  public RuleContext visit(GenericDefinitionNode node, RuleContext data) {
    // We never want to check type parameters.
    return data;
  }

  @Override
  public RuleContext visit(NameDeclarationNode node, RuleContext data) {
    String image = node.getImage();
    int limit = getProperty(LIMIT);

    if (image.length() < limit && !whitelist.contains(image)) {
      addViolation(data, node);
    }

    return super.visit(node, data);
  }
}
