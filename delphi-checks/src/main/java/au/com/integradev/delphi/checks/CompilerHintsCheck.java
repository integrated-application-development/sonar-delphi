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

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;
import org.sonar.plugins.communitydelphi.api.directive.CompilerDirective;
import org.sonar.plugins.communitydelphi.api.directive.SwitchDirective;
import org.sonar.plugins.communitydelphi.api.directive.SwitchDirective.SwitchKind;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "CompilerHintsRule", repositoryKey = "delph")
@Rule(key = "CompilerHints")
public class CompilerHintsCheck extends DelphiCheck {
  private static final String MESSAGE = "Suppressing hints is not allowed.";

  @Override
  public void visitToken(DelphiToken token, DelphiCheckContext context) {
    if (token.isCompilerDirective()) {
      CompilerDirective directive = context.getCompilerDirectiveParser().parse(token).orElse(null);
      if (directive instanceof SwitchDirective) {
        var switchDirective = (SwitchDirective) directive;
        if (switchDirective.kind() == SwitchKind.HINTS && !switchDirective.isActive()) {
          context.newIssue().onFilePosition(FilePosition.from(token)).withMessage(MESSAGE).report();
        }
      }
    }
  }
}
