/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.Streams;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.commons.lang3.Strings;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.communitydelphi.api.ast.FileTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.HelperTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.StringTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeReferenceNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "HelperNameRule", repositoryKey = "delph")
@Rule(key = "HelperName")
public class HelperNameCheck extends DelphiCheck {
  private static final String MESSAGE = "Rename this type to match the expected naming convention.";
  private static final String DEFAULT_HELPER_PREFIXES = "T";
  private static final String DEFAULT_EXTENDED_TYPE_PREFIXES = "T,E";

  @RuleProperty(
      key = "helperPrefixes",
      description = "Comma-delimited list of prefixes, helper names must begin with one of these.",
      defaultValue = DEFAULT_HELPER_PREFIXES)
  public String helperPrefixes = DEFAULT_HELPER_PREFIXES;

  @RuleProperty(
      key = "extendedTypePrefixes",
      description = "Comma-delimited list of prefixes that the extended type name may begin with.",
      defaultValue = DEFAULT_EXTENDED_TYPE_PREFIXES)
  public String extendedTypePrefixes = DEFAULT_EXTENDED_TYPE_PREFIXES;

  private List<String> helperPrefixesList;
  private List<String> extendedTypePrefixesList;

  @Override
  public void start(DelphiCheckContext context) {
    helperPrefixesList = Splitter.on(',').trimResults().splitToList(helperPrefixes);
    extendedTypePrefixesList = Splitter.on(',').trimResults().splitToList(extendedTypePrefixes);
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
    if (!NameConventionUtils.compliesWithPrefix(helperName, helperPrefixesList)
        || !Strings.CS.endsWith(helperName, "Helper")) {
      return false;
    }

    return Streams.concat(
            extendedTypePrefixesList.stream()
                .filter(extendedTypeName::startsWith)
                .map(prefix -> Strings.CS.removeStart(extendedTypeName, prefix)),
            Stream.of(extendedTypeName))
        .anyMatch(
            extendedNameNoPrefix ->
                Pattern.compile(Pattern.quote(extendedNameNoPrefix) + "([A-Z]|$)")
                    .matcher(helperName)
                    .find());
  }

  @Override
  public DelphiCheckContext visit(TypeDeclarationNode declarationNode, DelphiCheckContext context) {
    TypeNode typeNode = declarationNode.getTypeNode();

    if (typeNode instanceof HelperTypeNode) {
      var helperTypeNode = (HelperTypeNode) typeNode;
      String forTypeName = getExtendedTypeSimpleName(helperTypeNode.getFor());

      if (forTypeName != null && !compliesWithNameRule(declarationNode.simpleName(), forTypeName)) {
        reportIssue(context, declarationNode.getTypeNameNode(), MESSAGE);
      }
    }

    return super.visit(typeNode, context);
  }
}
