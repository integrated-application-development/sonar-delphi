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

import static au.com.integradev.delphi.pmd.DelphiPmdConstants.LIMIT;

import org.sonar.plugins.communitydelphi.api.ast.GenericDefinitionNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.UnitImportNode;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

public class ShortIdentifiersRule extends AbstractDelphiRule {

  private static final PropertyDescriptor<List<String>> WHITELISTED_NAMES =
      PropertyFactory.stringListProperty("whitelist")
          .desc("The list of short identifiers that we allow. (case-insensitive)")
          .emptyDefaultValue()
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
