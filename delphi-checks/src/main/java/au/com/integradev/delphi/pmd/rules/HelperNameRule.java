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

import org.sonar.plugins.communitydelphi.api.ast.FileTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.HelperTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.StringTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeReferenceNode;
import au.com.integradev.delphi.utils.NameConventionUtils;
import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.apache.commons.lang3.StringUtils;

public class HelperNameRule extends AbstractDelphiRule {
  public static final PropertyDescriptor<List<String>> HELPER_PREFIXES =
      PropertyFactory.stringListProperty("helperPrefixes")
          .desc("Helper names must begin with one of these prefixes.")
          .defaultValue(List.of("T"))
          .build();
  public static final PropertyDescriptor<List<String>> EXTENDED_TYPE_PREFIXES =
      PropertyFactory.stringListProperty("extendedTypePrefixes")
          .desc("Names of types extended by helpers must begin with one of these prefixes.")
          .defaultValue(List.of("T", "E"))
          .build();

  public HelperNameRule() {
    definePropertyDescriptor(HELPER_PREFIXES);
    definePropertyDescriptor(EXTENDED_TYPE_PREFIXES);
  }

  @VisibleForTesting
  static String getExtendedTypeSimpleName(TypeNode typeNode) {
    if (typeNode instanceof TypeReferenceNode) {
      return ((TypeReferenceNode) typeNode).simpleName();
    } else if (typeNode instanceof StringTypeNode) {
      return "String";
    } else if (typeNode instanceof FileTypeNode) {
      return "File";
    } else {
      return null;
    }
  }

  private boolean compliesWithNameRule(String helperName, String extendedTypeName) {
    List<String> helperPrefixes = getProperty(HELPER_PREFIXES);

    if (!NameConventionUtils.compliesWithPrefix(helperName, helperPrefixes)
        || !StringUtils.endsWith(helperName, "Helper")) {
      return false;
    }

    var possibleNames = new ArrayList<String>();
    possibleNames.add(extendedTypeName + "Helper");

    List<String> extendedTypePrefixes = getProperty(EXTENDED_TYPE_PREFIXES);

    for (String typePrefix : extendedTypePrefixes) {
      possibleNames.add(typePrefix + extendedTypeName + "Helper");

      String baseClassName = StringUtils.removeStart(extendedTypeName, typePrefix);

      // Prefix must be stripped and must be followed by a capital letter
      if (baseClassName.equals(extendedTypeName)
          || baseClassName.isEmpty()
          || Character.isLowerCase(baseClassName.charAt(0))) {
        continue;
      }

      possibleNames.add(baseClassName + "Helper");
      for (String helperPrefix : helperPrefixes) {
        possibleNames.add(helperPrefix + baseClassName + "Helper");
      }
    }

    return possibleNames.stream().anyMatch(possibleName -> possibleName.equals(helperName));
  }

  @Override
  public RuleContext visit(TypeDeclarationNode declarationNode, RuleContext data) {
    TypeNode typeNode = declarationNode.getTypeNode();

    if (typeNode instanceof HelperTypeNode) {
      var helperTypeNode = (HelperTypeNode) typeNode;
      String forTypeName = getExtendedTypeSimpleName(helperTypeNode.getFor());

      if (forTypeName != null && !compliesWithNameRule(declarationNode.simpleName(), forTypeName)) {
        addViolation(data, declarationNode.getTypeNameNode());
      }
    }

    return super.visit(typeNode, data);
  }
}
