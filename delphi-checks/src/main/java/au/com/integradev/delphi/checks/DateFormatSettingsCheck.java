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

import com.google.common.collect.Iterables;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Parameter;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "DateFormatSettingsRule", repositoryKey = "delph")
@Rule(key = "DateFormatSettings")
public class DateFormatSettingsCheck extends DelphiCheck {
  private static final String MESSAGE = "Pass a 'TFormatSettings' argument into this method.";

  private static final String TFORMATSETTINGS = "System.SysUtils.TFormatSettings";
  private static final Set<String> METHOD_SIGNATURES =
      Set.of(
          "System.SysUtils.DateToStr",
          "System.SysUtils.DateTimeToStr",
          "System.SysUtils.StrToDate",
          "System.SysUtils.StrToDateDef",
          "System.SysUtils.TryStrToDate",
          "System.SysUtils.StrToDateTime",
          "System.SysUtils.StrToDateTimeDef",
          "System.SysUtils.TryStrToDateTime");

  @Override
  public DelphiCheckContext visit(NameReferenceNode reference, DelphiCheckContext context) {
    NameDeclaration declaration = reference.getNameDeclaration();
    if (declaration instanceof MethodNameDeclaration) {
      MethodNameDeclaration method = (MethodNameDeclaration) declaration;
      if (METHOD_SIGNATURES.contains(method.fullyQualifiedName())) {
        Parameter lastParameter = Iterables.getLast(method.getParameters());
        if (!lastParameter.getType().is(TFORMATSETTINGS)) {
          reportIssue(context, reference, MESSAGE);
        }
      }
    }
    return super.visit(reference, context);
  }
}
