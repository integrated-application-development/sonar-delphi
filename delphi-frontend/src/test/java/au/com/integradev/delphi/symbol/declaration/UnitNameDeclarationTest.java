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
package au.com.integradev.delphi.symbol.declaration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.antlr.DelphiLexer;
import au.com.integradev.delphi.antlr.ast.node.IdentifierNodeImpl;
import au.com.integradev.delphi.antlr.ast.node.QualifiedNameDeclarationNodeImpl;
import au.com.integradev.delphi.file.DelphiFile;
import java.io.File;
import java.nio.file.Path;
import org.antlr.runtime.CommonToken;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.FileHeaderNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitNameDeclaration;

class UnitNameDeclarationTest {
  @Test
  void testEquals() {
    UnitNameDeclaration foo = createUnit("Foo", "/foo.pas");
    UnitNameDeclaration otherFoo = createUnit("Foo", "/foo.pas");
    UnitNameDeclaration fooWithDifferentPath = createUnit("Foo", "/bar/foo.pas");
    UnitNameDeclaration bar = createUnit("Bar", "/bar.pas");

    assertThat(foo)
        .isEqualTo(foo)
        .isNotEqualTo(null)
        .isNotEqualTo(new Object())
        .isEqualTo(otherFoo)
        .hasSameHashCodeAs(otherFoo)
        .isNotEqualTo(fooWithDifferentPath)
        .isNotEqualByComparingTo(fooWithDifferentPath)
        .doesNotHaveSameHashCodeAs(fooWithDifferentPath)
        .isNotEqualTo(bar)
        .isNotEqualByComparingTo(bar)
        .doesNotHaveSameHashCodeAs(bar);
  }

  @Test
  void testToString() {
    UnitNameDeclaration foo = createUnit("Foo", "/foo.pas");
    assertThat(foo).hasToString("Unit Foo");
  }

  private static UnitNameDeclaration createUnit(String name, String path) {
    var identifierToken = new CommonToken(DelphiLexer.TkIdentifier, name);
    var identifierNode = new IdentifierNodeImpl(identifierToken);

    var nameNode = new QualifiedNameDeclarationNodeImpl(DelphiLexer.TkNameDeclaration);
    nameNode.addChild(identifierNode);

    File file = mock(File.class);
    when(file.getAbsolutePath()).thenReturn(path);

    DelphiFile delphiFile = mock(DelphiFile.class);
    when(delphiFile.getSourceCodeFile()).thenReturn(file);

    DelphiAst ast = mock(DelphiAst.class);
    when(ast.getDelphiFile()).thenReturn(delphiFile);

    FileHeaderNode location = mock(FileHeaderNode.class);
    when(location.getNameNode()).thenReturn(nameNode);
    when(location.getNamespace()).thenReturn("");
    when(location.getAst()).thenReturn(ast);

    return new UnitNameDeclarationImpl(location, null, Path.of(path));
  }
}
