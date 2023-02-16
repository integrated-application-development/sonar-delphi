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

import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.TypeScope;
import org.sonar.plugins.communitydelphi.api.type.Type;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

public class ForbiddenFieldRule extends AbstractDelphiRule {
  public static final PropertyDescriptor<List<String>> BLACKLISTED_FIELDS =
      PropertyFactory.stringListProperty("blacklist")
          .desc("The list of forbidden field names.")
          .emptyDefaultValue()
          .build();

  public static final PropertyDescriptor<String> DECLARING_TYPE =
      PropertyFactory.stringProperty("declaringType")
          .desc("The type where the forbidden fields are declared.")
          .defaultValue("")
          .build();

  private static final PropertyDescriptor<String> MESSAGE =
      PropertyFactory.stringProperty("message").desc("The issue message").defaultValue("").build();

  private Set<String> blacklist;

  public ForbiddenFieldRule() {
    definePropertyDescriptor(BLACKLISTED_FIELDS);
    definePropertyDescriptor(DECLARING_TYPE);
    definePropertyDescriptor(MESSAGE);
  }

  @Override
  public void start(RuleContext data) {
    blacklist = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    blacklist.addAll(getProperty(BLACKLISTED_FIELDS));
  }

  @Override
  public RuleContext visit(NameReferenceNode reference, RuleContext data) {
    NameDeclaration declaration = reference.getNameDeclaration();
    if (declaration instanceof VariableNameDeclaration) {
      TypeScope scope = declaration.getScope().getEnclosingScope(TypeScope.class);
      if (scope != null) {
        Type type = scope.getType();
        String fieldName = declaration.getName();
        if (type.is(getProperty(DECLARING_TYPE)) && blacklist.contains(fieldName)) {
          addViolationWithMessage(data, reference.getIdentifier(), getProperty(MESSAGE));
        }
      }
    }
    return super.visit(reference, data);
  }
}
