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
package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.Collections;
import java.util.List;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.node.GenericDefinitionNode.TypeParameter;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;

public abstract class NameDeclarationNode extends DelphiNode {
  private DelphiNameDeclaration declaration;
  private List<NameOccurrence> usages;

  protected NameDeclarationNode(Token token) {
    super(token);
  }

  protected NameDeclarationNode(int tokenType) {
    super(tokenType);
  }

  public GenericDefinitionNode getGenericDefinition() {
    Node result = jjtGetChild(jjtGetNumChildren() - 1);
    return (result instanceof GenericDefinitionNode) ? (GenericDefinitionNode) result : null;
  }

  public List<TypeParameter> getTypeParameters() {
    GenericDefinitionNode genericDefinition = getGenericDefinition();
    if (genericDefinition != null) {
      return genericDefinition.getTypeParameters();
    }
    return Collections.emptyList();
  }

  public DelphiNameDeclaration getNameDeclaration() {
    return declaration;
  }

  public void setNameDeclaration(DelphiNameDeclaration declaration) {
    this.declaration = declaration;
  }

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

  public abstract DeclarationKind getKind();

  public enum DeclarationKind {
    CONST,
    ENUM_ELEMENT,
    EXCEPT_ITEM,
    FIELD,
    IMPORT,
    INLINE_CONST,
    INLINE_VAR,
    LOOP_VAR,
    METHOD,
    PARAMETER,
    PROPERTY,
    RECORD_VARIANT_TAG,
    TYPE,
    TYPE_PARAMETER,
    VAR,
    UNIT
  }
}
