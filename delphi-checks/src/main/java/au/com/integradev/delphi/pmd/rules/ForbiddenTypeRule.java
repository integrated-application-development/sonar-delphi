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

import au.com.integradev.delphi.antlr.ast.node.MethodNameNode;
import au.com.integradev.delphi.symbol.NameDeclaration;
import au.com.integradev.delphi.symbol.declaration.TypeNameDeclaration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

public class ForbiddenTypeRule extends AbstractDelphiRule {
  public static final PropertyDescriptor<List<String>> BLACKLISTED_TYPES =
      PropertyFactory.stringListProperty("blacklist")
          .desc("The list of forbidden (fully qualified) type names.")
          .emptyDefaultValue()
          .build();

  private static final PropertyDescriptor<String> MESSAGE =
      PropertyFactory.stringProperty("message").desc("The issue message").defaultValue("").build();

  private Set<String> blacklist;

  public ForbiddenTypeRule() {
    definePropertyDescriptor(BLACKLISTED_TYPES);
    definePropertyDescriptor(MESSAGE);
  }

  @Override
  public void start(RuleContext data) {
    blacklist = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    blacklist.addAll(getProperty(BLACKLISTED_TYPES));
  }

  @Override
  public RuleContext visit(NameReferenceNode reference, RuleContext data) {
    NameDeclaration declaration = reference.getNameDeclaration();
    if (declaration instanceof TypeNameDeclaration) {
      String fullyQualifiedMethodName = ((TypeNameDeclaration) declaration).fullyQualifiedName();
      if (blacklist.contains(fullyQualifiedMethodName)) {
        addViolationWithMessage(data, reference.getIdentifier(), getProperty(MESSAGE));
      }
    }
    return super.visit(reference, data);
  }

  @Override
  public RuleContext visit(MethodNameNode methodName, RuleContext data) {
    // It would be rude to flag a type's method implementations just for existing.
    return data;
  }
}
