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
package org.sonar.plugins.delphi.pmd.rules;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.apache.commons.lang3.StringUtils;
import org.sonar.plugins.delphi.antlr.ast.node.AsmStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.core.DelphiKeywords;

public class LowerCaseReservedWordsRule extends AbstractDelphiRule {
  public static final PropertyDescriptor<List<String>> EXCLUDED_KEYWORDS =
      PropertyFactory.stringListProperty("excludedKeywords")
          .desc("The list of keywords that this rule ignores (case-insensitive).")
          .emptyDefaultValue()
          .build();

  private Set<String> excluded;

  public LowerCaseReservedWordsRule() {
    definePropertyDescriptor(EXCLUDED_KEYWORDS);
  }

  @Override
  public void start(RuleContext data) {
    excluded = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    excluded.addAll(getProperty(EXCLUDED_KEYWORDS));
  }

  @Override
  public RuleContext visit(DelphiNode node, RuleContext data) {
    if (isViolationNode(node)) {
      addViolation(data, node.getToken());
    }
    return super.visit(node, data);
  }

  @Override
  public RuleContext visit(AsmStatementNode node, RuleContext data) {
    // Do not look inside of asm blocks
    return data;
  }

  private boolean isViolationNode(DelphiNode node) {
    if (!DelphiKeywords.KEYWORDS.contains(node.jjtGetId())) {
      return false;
    }

    var image = node.getToken().getImage();
    return !excluded.contains(image.toLowerCase()) && !StringUtils.isAllLowerCase(image);
  }
}
