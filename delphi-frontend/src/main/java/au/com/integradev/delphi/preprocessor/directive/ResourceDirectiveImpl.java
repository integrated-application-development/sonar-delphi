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
package au.com.integradev.delphi.preprocessor.directive;

import java.util.List;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.directive.ResourceDirective;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;

class ResourceDirectiveImpl extends ParameterDirectiveImpl implements ResourceDirective {
  private final String resourceFile;
  private final String resourceScriptFile;
  private final List<String> predicates;

  ResourceDirectiveImpl(
      DelphiToken token,
      String resourceFile,
      @Nullable String resourceScriptFile,
      List<String> predicates) {
    super(token, ParameterKind.RESOURCE);
    this.resourceFile = resourceFile;
    this.resourceScriptFile = resourceScriptFile;
    this.predicates = predicates;
  }

  @Override
  public String getResourceFile() {
    return resourceFile;
  }

  @Nullable
  @Override
  public String getResourceScriptFile() {
    return resourceScriptFile;
  }

  @Override
  public List<String> getPredicates() {
    return predicates;
  }
}
