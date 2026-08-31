/*
 * Sonar Delphi Plugin
 * Copyright (C) 2026 Integrated Application Development
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
package au.com.integradev.delphi.antlr.ast.visitors;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.integradev.delphi.DelphiProperties;
import au.com.integradev.delphi.compiler.Platform;
import au.com.integradev.delphi.file.DelphiFile;
import au.com.integradev.delphi.preprocessor.DelphiPreprocessorFactory;
import au.com.integradev.delphi.symbol.SymbolTable;
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import au.com.integradev.delphi.utils.files.DelphiFileUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.plugins.communitydelphi.api.ast.ArrayAccessorNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ForInStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineNameNode;
import org.sonar.plugins.communitydelphi.api.ast.StructTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.VarDeclarationNode;
import org.sonar.plugins.communitydelphi.api.symbol.scope.RoutineScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.TypeScope;

class SymbolAssociationVisitorTest {
  @TempDir private Path tempDir;

  @Test
  void testScopesAreAttached() {
    DelphiAst ast =
        associate(
            "unit Test;",
            "interface",
            "type",
            "  TFoo = class(TObject)",
            "    procedure Bar;",
            "  end;",
            "implementation",
            "procedure TFoo.Bar;",
            "begin",
            "end;",
            "end.");

    assertThat(descendant(ast, StructTypeNode.class).getScope()).isInstanceOf(TypeScope.class);
    assertThat(descendant(ast, RoutineImplementationNode.class).getScope())
        .isInstanceOf(RoutineScope.class);
  }

  @Test
  void testNameDeclarationsAreAttached() {
    DelphiAst ast =
        associate(
            "unit Test;", //
            "interface",
            "var",
            "  GFoo: Integer;",
            "implementation",
            "end.");

    NameDeclarationNode declaration =
        descendant(ast, VarDeclarationNode.class).getNameDeclarationList().getDeclarations().get(0);

    assertThat(declaration.getNameDeclaration()).isNotNull();
  }

  @Test
  void testNameReferenceOccurrencesAreAttached() {
    DelphiAst ast =
        associate(
            "unit Test;",
            "interface",
            "var",
            "  GFoo: Integer;",
            "implementation",
            "procedure Bar;",
            "begin",
            "  GFoo := 0;",
            "  Exit;",
            "end;",
            "end.");

    assertThat(nameReference(ast, "GFoo").getNameOccurrence()).isNotNull();
    assertThat(nameReference(ast, "Exit").getNameOccurrence()).isNotNull();
  }

  @Test
  void testQualifiedNameReferenceOccurrencesAreAttached() {
    DelphiAst ast =
        associate(
            "unit Test;",
            "interface",
            "type",
            "  TFoo = class(TObject)",
            "    Bar: Integer;",
            "  end;",
            "var",
            "  GFoo: TFoo;",
            "implementation",
            "procedure Baz;",
            "begin",
            "  GFoo.Bar := 0;",
            "end;",
            "end.");

    assertThat(nameReference(ast, "GFoo").getNameOccurrence()).isNotNull();
    assertThat(nameReference(ast, "Bar").getNameOccurrence()).isNotNull();
  }

  @Test
  void testUnitNameReferenceOccurrencesAreAttached() {
    DelphiAst ast =
        associate(
            "unit Test;",
            "interface",
            "uses System.SysUtils;",
            "implementation",
            "procedure Foo;",
            "begin",
            "  System.SysUtils.Bar;",
            "end;",
            "end.");

    assertThat(nameReference(ast, "System").getNameOccurrence()).isNotNull();
  }

  @Test
  void testRoutineDeclarationNamesAreAttached() {
    DelphiAst ast =
        associate(
            "unit Test;",
            "interface",
            "type",
            "  TFoo = class(TObject)",
            "  public",
            "    function GetBar: Integer;",
            "  private",
            "    property Bar: Integer read GetBar;",
            "  end;",
            "implementation",
            "end.");

    RoutineNameNode nameNode = descendant(ast, RoutineNameNode.class);

    assertThat(nameNode.getUsages()).isNotEmpty();
    assertThat(nameNode.getRoutineNameDeclaration()).isNotNull();
    assertThat(nameNode.getNameDeclarationNode().getNameDeclaration()).isNotNull();
  }

  @Test
  void testRoutineImplementationNamesAreAttached() {
    DelphiAst ast =
        associate(
            "unit Test;", //
            "interface",
            "implementation",
            "procedure Foo;",
            "begin",
            "end;",
            "end.");

    assertThat(descendant(ast, RoutineNameNode.class).getRoutineNameDeclaration()).isNotNull();
  }

  @Test
  void testTypeParameterReferencesAreAttached() {
    DelphiAst ast =
        associate(
            "unit Test;",
            "interface",
            "type",
            "  TFoo<T> = class(TObject)",
            "    procedure Bar;",
            "  end;",
            "procedure Baz<T>(Arg: T);",
            "implementation",
            "procedure TFoo<T>.Bar;",
            "begin",
            "end;",
            "procedure Baz<T>(Arg: T);",
            "begin",
            "end;",
            "end.");

    List<NameReferenceNode> typeParameterReferences =
        ast.findDescendantsOfType(RoutineImplementationNode.class).stream()
            .map(RoutineImplementationNode::getRoutineHeading)
            .flatMap(heading -> heading.findDescendantsOfType(NameReferenceNode.class).stream())
            .filter(reference -> reference.getImage().equals("T"))
            .collect(Collectors.toList());

    assertThat(typeParameterReferences)
        .hasSize(3)
        .allSatisfy(reference -> assertThat(reference.getNameOccurrence()).isNotNull());
  }

  @Test
  void testArrayAccessorImplicitOccurrencesAreAttached() {
    DelphiAst ast =
        associate(
            "unit Test;",
            "interface",
            "type",
            "  TFoo = class(TObject)",
            "    function GetBar(Index: Integer): Integer;",
            "    property Bar[Index: Integer]: Integer read GetBar; default;",
            "  end;",
            "var",
            "  GFoo: TFoo;",
            "implementation",
            "procedure Baz;",
            "begin",
            "  GFoo[0] := 0;",
            "end;",
            "end.");

    assertThat(descendant(ast, ArrayAccessorNode.class).getImplicitNameOccurrence()).isNotNull();
  }

  @Test
  void testEnumeratorOccurrencesAreAttached() {
    DelphiAst ast =
        associate(
            "unit Test;",
            "interface",
            "type",
            "  TEnumerator = class(TObject)",
            "    FCurrent: Integer;",
            "    function MoveNext: Boolean;",
            "    property Current: Integer read FCurrent;",
            "  end;",
            "  TFoo = class(TObject)",
            "    function GetEnumerator: TEnumerator;",
            "  end;",
            "var",
            "  GFoo: TFoo;",
            "implementation",
            "procedure Bar;",
            "var",
            "  Item: Integer;",
            "begin",
            "  for Item in GFoo do;",
            "end;",
            "end.");

    assertThat(descendant(ast, ForInStatementNode.class).getEnumeratorOccurrence()).isNotNull();
  }

  private static NameReferenceNode nameReference(DelphiAst ast, String image) {
    return ast.findDescendantsOfType(NameReferenceNode.class).stream()
        .filter(reference -> reference.getIdentifier().getImage().equalsIgnoreCase(image))
        .findFirst()
        .orElseThrow();
  }

  private static <T extends DelphiNode> T descendant(DelphiAst ast, Class<T> type) {
    return ast.findDescendantsOfType(type).stream().findFirst().orElseThrow();
  }

  private DelphiAst associate(String... lines) {
    try {
      Path sourceFile = tempDir.resolve("Test.pas");
      Files.writeString(sourceFile, StringUtils.join(lines, '\n'), StandardCharsets.UTF_8);

      DelphiFile file = DelphiFile.from(sourceFile.toFile(), DelphiFileUtils.mockConfig());

      SymbolTable symbolTable =
          SymbolTable.builder()
              .preprocessorFactory(
                  new DelphiPreprocessorFactory(
                      DelphiProperties.COMPILER_VERSION_DEFAULT, Platform.WINDOWS))
              .typeFactory(
                  new TypeFactoryImpl(
                      DelphiProperties.COMPILER_TOOLCHAIN_DEFAULT,
                      DelphiProperties.COMPILER_VERSION_DEFAULT))
              .standardLibraryPath(createStandardLibrary())
              .sourceFiles(List.of(sourceFile))
              .build();

      new SymbolAssociationVisitor()
          .visit(file.getAst(), new SymbolAssociationVisitor.Data(symbolTable));

      return file.getAst();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private Path createStandardLibrary() throws IOException {
    Path bds = tempDir.resolve("bds");
    Path standardLibraryPath = Files.createDirectories(bds.resolve("source"));

    Files.writeString(
        standardLibraryPath.resolve("SysInit.pas"),
        "unit SysInit;\ninterface\nimplementation\nend.");

    Files.writeString(
        standardLibraryPath.resolve("System.pas"),
        "unit System;\n"
            + "interface\n"
            + "type\n"
            + "  TObject = class\n"
            + "    constructor Create;\n"
            + "  end;\n"
            + "  IInterface = interface\n"
            + "  end;\n"
            + "  TClassHelperBase = class\n"
            + "  end;\n"
            + "  TVarRec = record\n"
            + "  end;\n"
            + "implementation\n"
            + "end.");

    Files.writeString(
        standardLibraryPath.resolve("System.SysUtils.pas"),
        "unit System.SysUtils;\n"
            + "interface\n"
            + "procedure Bar;\n"
            + "implementation\n"
            + "end.");

    return bds;
  }
}
