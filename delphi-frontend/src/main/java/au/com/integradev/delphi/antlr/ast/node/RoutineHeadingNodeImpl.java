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

import au.com.integradev.delphi.antlr.ast.node.utils.RoutineDirectiveUtils;
import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.AttributeListNode;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.communitydelphi.api.ast.RoutineDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineHeadingNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineNameNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineParametersNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineReturnTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineDirective;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineKind;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;
import org.sonar.plugins.communitydelphi.api.type.Type;

public final class RoutineHeadingNodeImpl extends DelphiNodeImpl implements RoutineHeadingNode {
  private String image;
  private Boolean isClassMethod;
  private String typeName;
  private String qualifiedName;
  private RoutineKind routineKind;
  private Set<RoutineDirective> directives;

  public RoutineHeadingNodeImpl(Token token) {
    super(token);
  }

  public RoutineHeadingNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public String getImage() {
    if (image == null) {
      image = fullyQualifiedName() + getParameterSignature();
    }
    return image;
  }

  private String getParameterSignature() {
    RoutineParametersNode parameters = getRoutineParametersNode();
    return parameters != null ? getRoutineParametersNode().getImage() : "";
  }

  @Override
  public RoutineNameNode getRoutineNameNode() {
    return (RoutineNameNode) getChild(1);
  }

  @Override
  public RoutineParametersNode getRoutineParametersNode() {
    return getFirstChildOfType(RoutineParametersNode.class);
  }

  @Override
  public List<FormalParameterData> getParameters() {
    RoutineParametersNode parameters = getRoutineParametersNode();
    return parameters != null ? parameters.getParameters() : Collections.emptyList();
  }

  @Override
  public List<Type> getParameterTypes() {
    RoutineParametersNode parameters = getRoutineParametersNode();
    return parameters != null ? parameters.getParameterTypes() : Collections.emptyList();
  }

  @Override
  public RoutineReturnTypeNode getRoutineReturnType() {
    return getFirstChildOfType(RoutineReturnTypeNode.class);
  }

  @Override
  public boolean isClassMethod() {
    if (isClassMethod == null) {
      isClassMethod = getFirstChildWithTokenType(DelphiTokenType.CLASS) != null;
    }
    return isClassMethod;
  }

  @Override
  public String simpleName() {
    return getRoutineNameNode().simpleName();
  }

  @Override
  public String fullyQualifiedName() {
    if (qualifiedName == null) {
      RoutineHeadingNode node = this;
      StringBuilder name = new StringBuilder();

      while (node != null) {
        String routineName = getRoutineNameNode().simpleNameWithTypeParameters();

        if (name.length() != 0) {
          name.insert(0, ".");
        }

        name.insert(0, routineName);
        node = findParentRoutineHeading(node);
      }

      if (!getTypeName().isEmpty()) {
        name.insert(0, getTypeName() + ".");
      }

      name.insert(0, getUnitName() + ".");

      qualifiedName = name.toString();
    }

    return qualifiedName;
  }

  @Override
  public String getTypeName() {
    if (typeName == null) {
      if (isRoutineImplementation()) {
        typeName = getTypeNameForRoutineImplementation();
      } else if (isRoutineDeclaration()) {
        typeName = getTypeNameForRoutineDeclaration();
      }
    }
    return typeName;
  }

  private String getTypeNameForRoutineImplementation() {
    RoutineHeadingNode heading = findParentRoutineHeading(this);
    if (heading == null) {
      String routineName = getRoutineNameNode().fullyQualifiedName();
      int dotIndex = routineName.lastIndexOf('.');
      return (dotIndex > 0) ? routineName.substring(0, dotIndex) : "";
    }
    return heading.getTypeName();
  }

  private String getTypeNameForRoutineDeclaration() {
    TypeDeclarationNode type = getFirstParentOfType(TypeDeclarationNode.class);
    if (type != null) {
      return type.qualifiedNameExcludingUnit();
    }
    return "";
  }

  @Override
  public boolean isRoutineImplementation() {
    return getParent() instanceof RoutineImplementationNode;
  }

  @Override
  public boolean isRoutineDeclaration() {
    return getParent() instanceof RoutineDeclarationNode;
  }

  @Override
  public RoutineKind getRoutineKind() {
    if (routineKind == null) {
      routineKind = RoutineKind.fromTokenType(getChild(0).getToken().getType());
    }
    return routineKind;
  }

  @Override
  public Set<RoutineDirective> getDirectives() {
    if (directives == null) {
      directives = RoutineDirectiveUtils.getDirectives(this);
    }
    return directives;
  }

  @Override
  public AttributeListNode getAttributeList() {
    return getFirstChildOfType(AttributeListNode.class);
  }

  private static RoutineHeadingNode findParentRoutineHeading(RoutineHeadingNode headingNode) {
    RoutineImplementationNode parentRoutine =
        headingNode.getParent().getFirstParentOfType(RoutineImplementationNode.class);
    return parentRoutine == null ? null : parentRoutine.getRoutineHeading();
  }
}
