/*
 * Sonar Delphi Plugin
 * Copyright (C) 2024 Integrated Application Development
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

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.directive.CompilerDirective;
import org.sonar.plugins.communitydelphi.api.directive.ResourceDirective;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;
import org.sonar.plugins.communitydelphi.api.type.Type;

abstract class AbstractFormResourceCheck extends DelphiCheck {
  protected abstract String getFrameworkName();

  protected abstract String getFormTypeImage();

  protected abstract String getFrameTypeImage();

  protected abstract String getResourceFileExtension();

  @Override
  public DelphiCheckContext visit(DelphiAst ast, DelphiCheckContext context) {
    if (context.getTokens().stream()
        .filter(DelphiToken::isCompilerDirective)
        .map(token -> context.getCompilerDirectiveParser().parse(token))
        .flatMap(Optional::stream)
        .anyMatch(this::isFormResource)) {
      return context;
    }
    return super.visit(ast, context);
  }

  @Override
  public DelphiCheckContext visit(TypeDeclarationNode declaration, DelphiCheckContext context) {
    DelphiNode location = declaration.getTypeNameNode();

    Type type = declaration.getType();
    if (!type.isAlias()) {
      if (type.isDescendantOf(getFormTypeImage())) {
        reportIssue(context, location, getMessage("form"));
      } else if (type.isDescendantOf(getFrameTypeImage())) {
        reportIssue(context, location, getMessage("frame"));
      }
    }

    return context;
  }

  private boolean isFormResource(CompilerDirective directive) {
    return directive instanceof ResourceDirective
        && StringUtils.endsWithIgnoreCase(
            ((ResourceDirective) directive).getResourceFile(), "." + getResourceFileExtension());
  }

  private String getMessage(String componentKind) {
    return String.format(
        "Add a '.%s' resource for this %s %s.",
        getResourceFileExtension(), getFrameworkName(), componentKind);
  }
}
