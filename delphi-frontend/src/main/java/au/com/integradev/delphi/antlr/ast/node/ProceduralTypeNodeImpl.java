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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.communitydelphi.api.ast.ProceduralTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.ProcedureTypeHeadingNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineParametersNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineReturnTypeNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineDirective;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

public abstract class ProceduralTypeNodeImpl extends TypeNodeImpl implements ProceduralTypeNode {
  protected ProceduralTypeNodeImpl(Token token) {
    super(token);
  }

  protected ProceduralTypeNodeImpl(int tokenType) {
    super(tokenType);
  }

  private ProcedureTypeHeadingNode getHeading() {
    return (ProcedureTypeHeadingNode) getChild(0);
  }

  @Override
  public Type getReturnType() {
    RoutineReturnTypeNode returnTypeNode = getHeading().getRoutineReturnTypeNode();
    return returnTypeNode == null ? TypeFactory.voidType() : returnTypeNode.getTypeNode().getType();
  }

  @Override
  public List<FormalParameterData> getParameters() {
    RoutineParametersNode parametersNode = getHeading().getRoutineParametersNode();
    return parametersNode == null ? Collections.emptyList() : parametersNode.getParameters();
  }

  @Override
  public Set<RoutineDirective> getDirectives() {
    return getHeading().getDirectives();
  }

  @Override
  public boolean hasDirective(RoutineDirective directive) {
    return getDirectives().contains(directive);
  }
}
