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

import au.com.integradev.delphi.symbol.NameDeclaration;
import au.com.integradev.delphi.symbol.NameOccurrence;
import java.util.Collections;
import java.util.List;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.GenericDefinitionNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;

public abstract class NameDeclarationNodeImpl extends DelphiNodeImpl
    implements NameDeclarationNode {
  private NameDeclaration declaration;
  private List<NameOccurrence> usages;

  protected NameDeclarationNodeImpl(Token token) {
    super(token);
  }

  protected NameDeclarationNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public GenericDefinitionNode getGenericDefinition() {
    DelphiNode result = jjtGetChild(jjtGetNumChildren() - 1);
    return (result instanceof GenericDefinitionNode) ? (GenericDefinitionNode) result : null;
  }

  @Override
  public List<GenericDefinitionNode.TypeParameter> getTypeParameters() {
    GenericDefinitionNode genericDefinition = getGenericDefinition();
    if (genericDefinition != null) {
      return genericDefinition.getTypeParameters();
    }
    return Collections.emptyList();
  }

  @Override
  public NameDeclaration getNameDeclaration() {
    return declaration;
  }

  public void setNameDeclaration(NameDeclaration declaration) {
    this.declaration = declaration;
  }

  @Override
  public List<NameOccurrence> getUsages() {
    if (usages == null) {
      if (declaration != null) {
        usages = declaration.getScope().getOccurrencesFor(declaration);
      } else {
        usages = Collections.emptyList();
      }
    }
    return usages;
  }
}
