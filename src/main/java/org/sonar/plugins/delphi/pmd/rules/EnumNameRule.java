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
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.utils.NameConventionUtils;

public class EnumNameRule extends AbstractDelphiRule {
  private static final PropertyDescriptor<List<String>> PREFIXES =
      PropertyFactory.stringListProperty("prefixes")
          .desc("If defined, enum names must begin with one of these prefixes.")
          .defaultValue(List.of("T"))
          .build();

  public EnumNameRule() {
    definePropertyDescriptor(PREFIXES);
  }

  @Override
  public RuleContext visit(TypeDeclarationNode type, RuleContext data) {
    if (type.isEnum()
        && !NameConventionUtils.compliesWithPrefix(type.simpleName(), getProperty(PREFIXES))) {
      addViolation(data, type.getTypeNameNode());
    }
    return super.visit(type, data);
  }
}
