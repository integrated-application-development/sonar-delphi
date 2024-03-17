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
import au.com.integradev.delphi.symbol.occurrence.AttributeNameOccurrenceImpl;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.AttributeNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;

public final class AttributeNodeImpl extends DelphiNodeImpl implements AttributeNode {
  public AttributeNodeImpl(Token token) {
    super(token);
  }

  public AttributeNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public boolean isAssembly() {
    return getChild(0).getTokenType() == DelphiTokenType.ASSEMBLY;
  }

  @Override
  public NameReferenceNode getNameReference() {
    return (NameReferenceNode) getChild(isAssembly() ? 1 : 0);
  }

  @Override
  public NameOccurrence getTypeNameOccurrence() {
    return getNameReference().getLastName().getNameOccurrence();
  }

  @Override
  public NameOccurrence getConstructorNameOccurrence() {
    NameOccurrence occurrence = getTypeNameOccurrence();
    if (occurrence != null) {
      return ((AttributeNameOccurrenceImpl) occurrence).getImplicitConstructorNameOccurrence();
    }
    return null;
  }

  @Override
  public ArgumentListNode getArgumentList() {
    DelphiNode node = getChild(isAssembly() ? 2 : 1);
    if (node instanceof ArgumentListNode) {
      return (ArgumentListNode) node;
    }
    return null;
  }

  @Override
  public String getImage() {
    return "[" + getNameReference().fullyQualifiedName() + "]";
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }
}
