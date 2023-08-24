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

import static org.apache.commons.lang3.StringUtils.countMatches;

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "TabulationCharactersRule", repositoryKey = "delph")
@Rule(key = "TabulationCharacter")
public class TabulationCharacterCheck extends DelphiCheck {
  private int tabCount;

  @Override
  public DelphiCheckContext visit(DelphiAst ast, DelphiCheckContext context) {
    tabCount = 0;
    super.visit(ast, context);

    if (tabCount > 0) {
      context
          .newIssue()
          .withMessage(
              String.format(
                  "Tabulation characters should not be used (%d found in file)", tabCount))
          .report();
    }

    return context;
  }

  @Override
  public void visitToken(DelphiToken token, DelphiCheckContext context) {
    if (token.isWhitespace()) {
      tabCount += countMatches(token.getImage(), '\t');
    }
  }
}
