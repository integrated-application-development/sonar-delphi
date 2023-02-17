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
package au.com.integradev.delphi.type.parameter;

import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.communitydelphi.api.type.Parameter;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.TypeSpecializationContext;

public final class FormalParameter extends AbstractParameter {
  private final String image;

  private FormalParameter(
      String image,
      Type type,
      boolean hasDefaultValue,
      boolean isOut,
      boolean isVar,
      boolean isConst) {
    super(type, hasDefaultValue, isOut, isVar, isConst);
    this.image = image;
  }

  public static Parameter create(FormalParameterData parameter) {
    return new FormalParameter(
        parameter.getImage(),
        parameter.getType(),
        parameter.hasDefaultValue(),
        parameter.isOut(),
        parameter.isVar(),
        parameter.isConst());
  }

  @Override
  public String getImage() {
    return image;
  }

  @Override
  public Parameter specialize(TypeSpecializationContext context) {
    FormalParameter specialized =
        new FormalParameter(
            image, getType().specialize(context), hasDefaultValue(), isOut(), isVar(), isConst());
    if (this.equals(specialized)) {
      return this;
    }
    return specialized;
  }
}
