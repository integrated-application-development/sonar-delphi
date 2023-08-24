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
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Collectors;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterListNode;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode;
import org.sonar.plugins.communitydelphi.api.type.Type;

public final class FormalParameterListNodeImpl extends DelphiNodeImpl
    implements FormalParameterListNode {
  private List<FormalParameterNode.FormalParameterData> parameters;
  private List<Type> parameterTypes;
  private String image;

  public FormalParameterListNodeImpl(Token token) {
    super(token);
  }

  public FormalParameterListNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public List<FormalParameterNode.FormalParameterData> getParameters() {
    if (parameters == null) {
      var builder = new ImmutableList.Builder<FormalParameterNode.FormalParameterData>();
      for (FormalParameterNode parameterNode : findChildrenOfType(FormalParameterNode.class)) {
        builder.addAll(parameterNode.getParameters());
      }
      parameters = builder.build();
    }
    return parameters;
  }

  @Override
  public List<Type> getParameterTypes() {
    if (parameterTypes == null) {
      parameterTypes =
          getParameters().stream()
              .map(FormalParameterNode.FormalParameterData::getType)
              .collect(Collectors.toUnmodifiableList());
    }
    return parameterTypes;
  }

  @Override
  public String getImage() {
    if (image == null) {
      StringBuilder imageBuilder = new StringBuilder();
      for (FormalParameterNode.FormalParameterData parameter : getParameters()) {
        if (imageBuilder.length() != 0) {
          imageBuilder.append(';');
        }
        imageBuilder.append(parameter.getType().getImage());
      }
      image = imageBuilder.toString();
    }

    return image;
  }
}
