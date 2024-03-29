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
package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import java.util.Collections;
import java.util.List;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterListNode;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.communitydelphi.api.ast.RoutineParametersNode;
import org.sonar.plugins.communitydelphi.api.type.Type;

public final class RoutineParametersNodeImpl extends DelphiNodeImpl
    implements RoutineParametersNode {
  private String image;
  private FormalParameterListNode parameterList;

  public RoutineParametersNodeImpl(Token token) {
    super(token);
  }

  public RoutineParametersNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public List<FormalParameterData> getParameters() {
    return isEmpty() ? Collections.emptyList() : getFormalParametersList().getParameters();
  }

  @Override
  public List<Type> getParameterTypes() {
    return isEmpty() ? Collections.emptyList() : getFormalParametersList().getParameterTypes();
  }

  @Override
  public String getImage() {
    if (image == null) {
      image = "(" + (isEmpty() ? "" : getFormalParametersList().getImage()) + ")";
    }
    return image;
  }

  @Override
  public FormalParameterListNode getFormalParametersList() {
    if (parameterList == null && !isEmpty()) {
      parameterList = (FormalParameterListNode) getChild(1);
    }
    return parameterList;
  }

  @Override
  public boolean isEmpty() {
    return getChildren().size() < 3;
  }
}
