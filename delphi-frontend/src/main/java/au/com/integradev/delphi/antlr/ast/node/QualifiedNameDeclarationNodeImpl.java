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
import au.com.integradev.delphi.symbol.QualifiedNameImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.FileHeaderNode;
import org.sonar.plugins.communitydelphi.api.ast.IdentifierNode;
import org.sonar.plugins.communitydelphi.api.ast.QualifiedNameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.UnitImportNode;
import org.sonar.plugins.communitydelphi.api.symbol.QualifiedName;

public final class QualifiedNameDeclarationNodeImpl extends NameDeclarationNodeImpl
    implements QualifiedNameDeclarationNode {

  private QualifiedName qualifiedName;

  public QualifiedNameDeclarationNodeImpl(Token token) {
    super(token);
  }

  public QualifiedNameDeclarationNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public QualifiedName getQualifiedName() {
    if (qualifiedName == null) {
      StringBuilder namePart = new StringBuilder();
      List<String> names = new ArrayList<>();

      for (int i = this.getChildren().size() - 1; i >= 0; --i) {
        DelphiNode child = this.getChild(i);
        namePart.insert(0, child.getImage());
        if (child instanceof IdentifierNode) {
          names.add(namePart.toString());
          namePart.setLength(0);
        }
      }

      Collections.reverse(names);

      qualifiedName = new QualifiedNameImpl(names);
    }
    return qualifiedName;
  }

  @Override
  public String getImage() {
    return fullyQualifiedName();
  }

  @Override
  public DeclarationKind getKind() {
    DeclarationKind kind = null;
    if (parent instanceof FileHeaderNode) {
      kind = DeclarationKind.UNIT;
    } else if (parent instanceof UnitImportNode) {
      kind = DeclarationKind.IMPORT;
    }
    return Objects.requireNonNull(kind, "Unhandled DeclarationKind");
  }
}
