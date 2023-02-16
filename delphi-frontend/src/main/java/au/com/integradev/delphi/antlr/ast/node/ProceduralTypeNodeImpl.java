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
package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.type.DelphiType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import java.util.Collections;
import java.util.List;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.communitydelphi.api.ast.MethodParametersNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodReturnTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.ProceduralTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.ProcedureTypeHeadingNode;

public abstract class ProceduralTypeNodeImpl extends TypeNodeImpl implements ProceduralTypeNode {
  protected ProceduralTypeNodeImpl(Token token) {
    super(token);
  }

  protected ProceduralTypeNodeImpl(int tokenType) {
    super(tokenType);
  }

  private ProcedureTypeHeadingNode getHeading() {
    return (ProcedureTypeHeadingNode) jjtGetChild(0);
  }

  @Override
  public Type getReturnType() {
    MethodReturnTypeNode returnTypeNode = getHeading().getMethodReturnTypeNode();
    return returnTypeNode == null ? DelphiType.voidType() : returnTypeNode.getTypeNode().getType();
  }

  @Override
  public List<FormalParameterData> getParameters() {
    MethodParametersNode parametersNode = getHeading().getMethodParametersNode();
    return parametersNode == null ? Collections.emptyList() : parametersNode.getParameters();
  }
}
