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
package au.com.integradev.delphi.type.parameter;

import au.com.integradev.delphi.type.intrinsic.IntrinsicRoutine.IntrinsicParameterData;
import org.sonar.plugins.communitydelphi.api.type.Parameter;
import org.sonar.plugins.communitydelphi.api.type.Type;

public final class IntrinsicParameter extends AbstractParameter {
  private IntrinsicParameter(
      Type type, boolean hasDefaultValue, boolean isOut, boolean isVar, boolean isConst) {
    super(type, hasDefaultValue, isOut, isVar, isConst);
  }

  public static Parameter create(IntrinsicParameterData data) {
    return new IntrinsicParameter(
        data.getType(), data.hasDefaultValue(), data.isOut(), data.isVar(), data.isConst());
  }

  public static Parameter create(Type type) {
    return new IntrinsicParameter(type, false, false, false, false);
  }

  @Override
  public String getImage() {
    return "_";
  }
}
