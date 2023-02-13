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

import au.com.integradev.delphi.antlr.DelphiLexer;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode.FormalParameterData;
import au.com.integradev.delphi.antlr.ast.token.DelphiToken;
import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import au.com.integradev.delphi.symbol.declaration.MethodDirective;
import au.com.integradev.delphi.symbol.declaration.MethodKind;
import au.com.integradev.delphi.type.Type;
import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.MethodDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodHeadingNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodNameNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodParametersNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodReturnTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;

public final class MethodHeadingNodeImpl extends DelphiNodeImpl implements MethodHeadingNode {
  private String image;
  private Boolean isClassMethod;
  private String typeName;
  private String qualifiedName;
  private MethodKind methodKind;
  private Set<MethodDirective> directives;

  public MethodHeadingNodeImpl(Token token) {
    super(token);
  }

  public MethodHeadingNodeImpl(int tokenType) {
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
    MethodParametersNode parameters = getMethodParametersNode();
    return parameters != null ? getMethodParametersNode().getImage() : "";
  }

  @Override
  public MethodNameNode getMethodNameNode() {
    return (MethodNameNode) jjtGetChild(1);
  }

  @Override
  public MethodParametersNode getMethodParametersNode() {
    return getFirstChildOfType(MethodParametersNode.class);
  }

  @Override
  public List<FormalParameterData> getParameters() {
    MethodParametersNode parameters = getMethodParametersNode();
    return parameters != null ? parameters.getParameters() : Collections.emptyList();
  }

  @Override
  public List<Type> getParameterTypes() {
    MethodParametersNode parameters = getMethodParametersNode();
    return parameters != null ? parameters.getParameterTypes() : Collections.emptyList();
  }

  @Override
  public MethodReturnTypeNode getMethodReturnType() {
    return getFirstChildOfType(MethodReturnTypeNode.class);
  }

  @Override
  public boolean isClassMethod() {
    if (isClassMethod == null) {
      isClassMethod = getFirstChildWithId(DelphiLexer.CLASS) != null;
    }
    return isClassMethod;
  }

  @Override
  public String simpleName() {
    return getMethodNameNode().simpleName();
  }

  @Override
  public String fullyQualifiedName() {
    if (qualifiedName == null) {
      MethodHeadingNode node = this;
      StringBuilder name = new StringBuilder();

      while (node != null) {
        String methodName = getMethodNameNode().simpleNameWithTypeParameters();

        if (name.length() != 0) {
          name.insert(0, ".");
        }

        name.insert(0, methodName);
        node = findParentMethodHeading(node);
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
      if (isMethodImplementation()) {
        typeName = getTypeNameForMethodImplementation();
      } else if (isMethodDeclaration()) {
        typeName = getTypeNameForMethodDeclaration();
      }
    }
    return typeName;
  }

  private String getTypeNameForMethodImplementation() {
    MethodHeadingNode heading = findParentMethodHeading(this);
    if (heading == null) {
      String methodName = getMethodNameNode().fullyQualifiedName();
      int dotIndex = methodName.lastIndexOf('.');
      return (dotIndex > 0) ? methodName.substring(0, dotIndex) : "";
    }
    return heading.getTypeName();
  }

  private String getTypeNameForMethodDeclaration() {
    TypeDeclarationNode type = getFirstParentOfType(TypeDeclarationNode.class);
    if (type != null) {
      return type.qualifiedNameExcludingUnit();
    }
    return "";
  }

  @Override
  public boolean isMethodImplementation() {
    return jjtGetParent() instanceof MethodImplementationNode;
  }

  @Override
  public boolean isMethodDeclaration() {
    return jjtGetParent() instanceof MethodDeclarationNode;
  }

  @Override
  public MethodKind getMethodKind() {
    if (methodKind == null) {
      methodKind = MethodKind.fromTokenType(jjtGetChildId(0));
    }
    return methodKind;
  }

  @Override
  public Set<MethodDirective> getDirectives() {
    if (directives == null) {
      var builder = new ImmutableSet.Builder<MethodDirective>();
      for (int i = 0; i < jjtGetNumChildren(); ++i) {
        DelphiToken token = ((DelphiNode) jjtGetChild(i)).getToken();
        MethodDirective directive = MethodDirective.fromToken(token);
        if (directive != null) {
          builder.add(directive);
        }
      }
      directives = builder.build();
    }
    return directives;
  }

  private static MethodHeadingNode findParentMethodHeading(MethodHeadingNode headingNode) {
    MethodImplementationNode parentMethod =
        headingNode.jjtGetParent().getFirstParentOfType(MethodImplementationNode.class);
    return parentMethod == null ? null : parentMethod.getMethodHeading();
  }
}
