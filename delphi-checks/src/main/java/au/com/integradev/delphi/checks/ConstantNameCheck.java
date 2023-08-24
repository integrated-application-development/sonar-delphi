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
import com.google.common.base.Splitter;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.communitydelphi.api.ast.ConstDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.ConstStatementNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "ConstantNotationRule", repositoryKey = "delph")
@Rule(key = "ConstantName")
public class ConstantNameCheck extends DelphiCheck {
  private static final String MESSAGE =
      "Rename this constant to match the expected naming convention";

  @RuleProperty(
      key = "prefixes",
      description = "Comma-delimited list of prefixes, class names must begin with one of these.")
  public String prefixes = "";

  private List<String> prefixesList;

  @Override
  public void start(DelphiCheckContext context) {
    prefixesList = Splitter.on(',').trimResults().splitToList(prefixes);
  }

  @Override
  public DelphiCheckContext visit(ConstDeclarationNode declaration, DelphiCheckContext context) {
    if (!NameConventionUtils.compliesWithPrefix(
        declaration.getNameDeclarationNode().getImage(), prefixesList)) {
      reportIssue(context, declaration.getNameDeclarationNode(), MESSAGE);
    }
    return super.visit(declaration, context);
  }

  @Override
  public DelphiCheckContext visit(ConstStatementNode statement, DelphiCheckContext context) {
    if (!NameConventionUtils.compliesWithPrefix(
        statement.getNameDeclarationNode().getImage(), prefixes)) {
      reportIssue(context, statement.getNameDeclarationNode(), MESSAGE);
    }
    return super.visit(statement, context);
  }
}
