package org.sonar.plugins.delphi.pmd.rules;

import com.google.common.base.Preconditions;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.sonar.plugins.delphi.antlr.ast.node.FileHeaderNode;
import org.sonar.plugins.delphi.antlr.ast.node.UnitImportNode;
import org.sonar.plugins.delphi.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.UnitNameDeclaration;

public abstract class AbstractImportRule extends AbstractDelphiRule {
  private static final PropertyDescriptor<List<String>> EXCLUSIONS_PROPERTY =
      PropertyFactory.stringListProperty("exclusions")
          .desc("Fully-qualified names of unit imports to exclude (only in the interface section)")
          .emptyDefaultValue()
          .build();

  private Set<String> exclusions = new HashSet<>();
  private UnitNameDeclaration unitDeclaration;

  protected AbstractImportRule() {
    definePropertyDescriptor(EXCLUSIONS_PROPERTY);
  }

  @Override
  public void start(RuleContext ctx) {
    exclusions = Set.copyOf(getProperty(EXCLUSIONS_PROPERTY));
    unitDeclaration = null;
  }

  @Override
  public RuleContext visit(FileHeaderNode headerNode, RuleContext data) {
    unitDeclaration = (UnitNameDeclaration) headerNode.getNameNode().getNameDeclaration();
    return data;
  }

  @Override
  public RuleContext visit(UnitImportNode unitImport, RuleContext data) {
    if (!isExcluded(unitImport) && isViolation(unitImport)) {
      addViolation(data, unitImport);
    }
    return data;
  }

  private boolean isExcluded(UnitImportNode unitImport) {
    if (!unitImport.isResolvedImport()) {
      return true;
    }

    if (unitImport.isInterfaceSection()) {
      UnitImportNameDeclaration importDeclaration = unitImport.getImportNameDeclaration();
      UnitNameDeclaration dependency = importDeclaration.getOriginalDeclaration();
      Objects.requireNonNull(dependency);
      return exclusions.contains(dependency.getImage());
    }

    return false;
  }

  protected UnitNameDeclaration getUnitDeclaration() {
    Preconditions.checkNotNull(unitDeclaration);
    return unitDeclaration;
  }

  protected abstract boolean isViolation(UnitImportNode unitImport);
}
