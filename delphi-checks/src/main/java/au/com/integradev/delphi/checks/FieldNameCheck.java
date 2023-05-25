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
import com.google.common.base.Splitter;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.communitydelphi.api.ast.FieldDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "FieldNameRule", repositoryKey = "delph")
@Rule(key = "FieldName")
public class FieldNameCheck extends DelphiCheck {
  private static final String MESSAGE =
      "Rename this field to match the expected naming convention.";
  private static final String DEFAULT_PREFIXES = "F";

  @RuleProperty(
      key = "prefixes",
      description = "Comma-delimited list of prefixes, field names must begin with one of these.",
      defaultValue = DEFAULT_PREFIXES)
  private String prefixes = DEFAULT_PREFIXES;

  private List<String> prefixesList;

  @Override
  public void start(DelphiCheckContext context) {
    prefixesList = Splitter.on(',').trimResults().splitToList(prefixes);
  }

  @Override
  public DelphiCheckContext visit(FieldDeclarationNode field, DelphiCheckContext context) {
    if (field.isPrivate() || field.isProtected()) {
      for (NameDeclarationNode identifier : field.getDeclarationList().getDeclarations()) {
        if (!NameConventionUtils.compliesWithPrefix(identifier.getImage(), prefixesList)) {
          reportIssue(context, identifier, MESSAGE);
        }
      }
    }
    return super.visit(field, context);
  }
}
