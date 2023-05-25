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
package au.com.integradev.delphi.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.PropertyNameDeclaration;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "ExplicitDefaultPropertyReferenceRule", repositoryKey = "delph")
@Rule(key = "ExplicitDefaultPropertyReference")
public class ExplicitDefaultPropertyReferenceCheck extends DelphiCheck {
  private static final String MESSAGE = "Replace this explicit property reference with '[]'";

  @Override
  public DelphiCheckContext visit(NameReferenceNode nameReference, DelphiCheckContext context) {
    if (isExplicitDefaultArrayPropertyReference(nameReference)) {
      reportIssue(context, nameReference, MESSAGE);
    }
    return super.visit(nameReference, context);
  }

  private static boolean isExplicitDefaultArrayPropertyReference(NameReferenceNode nameReference) {
    if (nameReference.prevName() != null && nameReference.nextName() == null) {
      NameDeclaration declaration = nameReference.getNameDeclaration();
      if (declaration instanceof PropertyNameDeclaration) {
        PropertyNameDeclaration property = (PropertyNameDeclaration) declaration;
        return property.isArrayProperty() && property.isDefaultProperty();
      }
    }
    return false;
  }
}
