/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package au.com.integradev.delphi.pmd.rules;

import au.com.integradev.delphi.antlr.ast.node.FileHeaderNode;
import au.com.integradev.delphi.antlr.ast.node.UnitImportNode;
import au.com.integradev.delphi.symbol.declaration.UnitImportNameDeclaration;
import au.com.integradev.delphi.symbol.declaration.UnitNameDeclaration;
import com.google.common.base.Preconditions;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

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
