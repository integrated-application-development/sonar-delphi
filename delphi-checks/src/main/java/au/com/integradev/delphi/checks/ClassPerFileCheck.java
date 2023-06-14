/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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
import org.sonar.check.RuleProperty;
import org.sonar.plugins.communitydelphi.api.ast.StructTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "ClassPerFileRule", repositoryKey = "delph")
@Rule(key = "ClassPerFile")
public class ClassPerFileCheck extends DelphiCheck {
  private static final int DEFAULT_LIMIT = 1;

  @RuleProperty(
      key = "limit",
      description = "Maximum number of classes allowed in a file.",
      defaultValue = DEFAULT_LIMIT + "")
  public int limit = DEFAULT_LIMIT;

  private int count;

  @Override
  public void start(DelphiCheckContext context) {
    count = 0;
  }

  @Override
  public void end(DelphiCheckContext context) {
    if (count > limit) {
      context
          .newIssue()
          .withMessage(
              String.format("File has %d classes, maximum number of classes is %d.", count, limit))
          .report();
    }
  }

  @Override
  public DelphiCheckContext visit(TypeDeclarationNode type, DelphiCheckContext context) {
    if (shouldCount(type)) {
      ++count;
    }
    return super.visit(type, context);
  }

  private static boolean shouldCount(TypeDeclarationNode type) {
    return type.isClass()
        && !type.isNestedType()
        && !type.isForwardDeclaration()
        && !((StructTypeNode) type.getTypeNode()).getVisibilitySections().isEmpty();
  }
}
