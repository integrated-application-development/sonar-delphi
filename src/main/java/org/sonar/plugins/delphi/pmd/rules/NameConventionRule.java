/*
 * Sonar Delphi Plugin
 * Copyright (C) 2015 Fabricio Colombo
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
package org.sonar.plugins.delphi.pmd.rules;

import java.util.Collections;
import java.util.List;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public abstract class NameConventionRule extends DelphiRule implements NodeFinderInterface {

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    for (DelphiPMDNode nameNode : getNameNodes(node)) {
      if (nameNode != null && isViolation(nameNode)) {
        addViolation(ctx, nameNode);
      }
    }
  }

  private List<DelphiPMDNode> getNameNodes(DelphiPMDNode node) {
    DelphiPMDNode singleNode = findNode(node);
    if (singleNode != null) {
      return Collections.singletonList(singleNode);
    }

    return findNodes(node);
  }

  protected boolean compliesWithPrefixNamingConvention(final String name, final String prefix) {
    return name.startsWith(prefix) && compliesWithPascalCase(name, prefix);
  }

  protected boolean compliesWithPrefixNamingConvention(final String name, final String[] prefixes) {
    for (final String prefix : prefixes) {
      if (compliesWithPrefixNamingConvention(name, prefix)) {
        return true;
      }
    }

    return false;
  }

  private static boolean compliesWithPascalCase(final String name, final String prefix) {
    if (name.length() == prefix.length()) {
      return false;
    }

    char character = name.charAt(prefix.length());
    return Character.isUpperCase(character) || Character.isDigit(character);
  }

  protected abstract boolean isViolation(DelphiPMDNode nameNode);
}
