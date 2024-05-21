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
package au.com.integradev.delphi.checks;

import au.com.integradev.delphi.utils.NameConventionUtils;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.PointerTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeReferenceNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "PointerNameRule", repositoryKey = "delph")
@Rule(key = "PointerName")
public class PointerNameCheck extends DelphiCheck {
  private static final String MESSAGE = "Rename this type to match the expected naming convention.";
  private static final String POINTER_PREFIX = "P";
  private static final List<String> EXTENDED_TYPE_PREFIXES = List.of("T", "E", "I");

  @Override
  public DelphiCheckContext visit(TypeDeclarationNode type, DelphiCheckContext context) {
    if (isViolation(type)) {
      reportIssue(context, type.getTypeNameNode(), MESSAGE);
    }

    return super.visit(type, context);
  }

  private boolean isViolation(TypeDeclarationNode type) {
    if (!type.isPointer()) {
      return false;
    }

    TypeNode typeNode = ((PointerTypeNode) type.getTypeNode()).getDereferencedTypeNode();
    if (!(typeNode instanceof TypeReferenceNode)) {
      return false;
    }

    TypeReferenceNode referenceNode = (TypeReferenceNode) typeNode;
    String dereferencedName = referenceNode.simpleName();
    String typeName = type.simpleName();

    for (String extendedTypePrefix : EXTENDED_TYPE_PREFIXES) {
      if (NameConventionUtils.compliesWithPrefix(dereferencedName, extendedTypePrefix)) {
        String expected = POINTER_PREFIX + dereferencedName.substring(1);
        return !typeName.equals(expected);
      }

      if (dereferencedName.startsWith(extendedTypePrefix)) {
        String expectedAddingPrefix = POINTER_PREFIX + dereferencedName;
        String expectedOverridingPrefix = POINTER_PREFIX + dereferencedName.substring(1);

        return !(typeName.equals(expectedAddingPrefix)
            || typeName.equals(expectedOverridingPrefix));
      }
    }

    if (NameConventionUtils.compliesWithPascalCase(dereferencedName)) {
      String expected = POINTER_PREFIX + dereferencedName;
      return !typeName.equals(expected);
    }

    return !NameConventionUtils.compliesWithPrefix(typeName, POINTER_PREFIX);
  }
}
