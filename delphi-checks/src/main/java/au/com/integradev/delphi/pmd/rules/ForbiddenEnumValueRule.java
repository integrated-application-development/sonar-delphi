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

import au.com.integradev.delphi.symbol.NameDeclaration;
import au.com.integradev.delphi.symbol.declaration.EnumElementNameDeclaration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

public class ForbiddenEnumValueRule extends AbstractDelphiRule {
  public static final PropertyDescriptor<List<String>> BLACKLISTED_ENUM_VALUES =
      PropertyFactory.stringListProperty("blacklist")
          .desc("The list of forbidden enum values.")
          .emptyDefaultValue()
          .build();

  public static final PropertyDescriptor<String> ENUM_NAME =
      PropertyFactory.stringProperty("enumName")
          .desc("The fully qualified name of the enum type.")
          .defaultValue("")
          .build();

  private static final PropertyDescriptor<String> MESSAGE =
      PropertyFactory.stringProperty("message").desc("The issue message").defaultValue("").build();

  private Set<String> blacklist;

  public ForbiddenEnumValueRule() {
    definePropertyDescriptor(BLACKLISTED_ENUM_VALUES);
    definePropertyDescriptor(ENUM_NAME);
    definePropertyDescriptor(MESSAGE);
  }

  @Override
  public void start(RuleContext data) {
    blacklist = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    blacklist.addAll(getProperty(BLACKLISTED_ENUM_VALUES));
  }

  @Override
  public RuleContext visit(NameReferenceNode reference, RuleContext data) {
    NameDeclaration declaration = reference.getNameDeclaration();
    if (declaration instanceof EnumElementNameDeclaration) {
      var element = (EnumElementNameDeclaration) declaration;
      if (element.getType().is(getProperty(ENUM_NAME)) && blacklist.contains(element.getName())) {
        addViolation(data, reference);
      }
    }
    return super.visit(reference, data);
  }
}
