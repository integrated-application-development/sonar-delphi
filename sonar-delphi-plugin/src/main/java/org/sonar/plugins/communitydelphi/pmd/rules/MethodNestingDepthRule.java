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
package org.sonar.plugins.communitydelphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.sonar.plugins.communitydelphi.antlr.ast.node.MethodImplementationNode;

public class MethodNestingDepthRule extends AbstractDelphiRule {
  private static final PropertyDescriptor<Integer> DEPTH =
      PropertyFactory.intProperty("depth")
          .desc("The maximum nesting level allowed for a nested method.")
          .defaultValue(1)
          .build();

  public MethodNestingDepthRule() {
    definePropertyDescriptor(DEPTH);
  }

  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    int depth = method.getParentsOfType(MethodImplementationNode.class).size();
    int limit = getProperty(DEPTH);

    if (depth > limit) {
      addViolationWithMessage(
          data,
          method.getMethodNameNode(),
          "Extract this deeply nested method. Nesting level is {0}. (Limit is {1})",
          new Object[] {depth, limit});
    }

    return super.visit(method, data);
  }
}
