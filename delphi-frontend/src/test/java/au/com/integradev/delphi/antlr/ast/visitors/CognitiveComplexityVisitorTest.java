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
package au.com.integradev.delphi.antlr.ast.visitors;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.integradev.delphi.antlr.ast.DelphiAST;
import au.com.integradev.delphi.antlr.ast.visitors.CognitiveComplexityVisitor.Data;
import au.com.integradev.delphi.file.DelphiFile;
import au.com.integradev.delphi.utils.files.DelphiFileUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CognitiveComplexityVisitorTest {
  private CognitiveComplexityVisitor visitor;

  @TempDir Path tempDir;

  @BeforeEach
  void setup() {
    visitor = new CognitiveComplexityVisitor();
  }

  @Test
  void testSimpleMethod() {
    assertThat(
            getComplexity(
                "function Foo: Integer;\n"
                    + "begin\n"
                    + "  if Foo then Bar;\n" // <1> 1
                    + "end;\n"))
        .isEqualTo(1);
  }

  @Test
  void testTooComplexMethod() {
    assertThat(
            getComplexity(
                "function Foo: Integer;\n"
                    + "begin\n"
                    + "  if SomeCondition then begin\n" // <1> 1
                    + "    while SomeCondition do begin\n" // <3> 2 [+1 for nesting]
                    + "      SomeCondition := False;\n"
                    + "      Break;\n"
                    + "    end;\n"
                    + "    if not SomeCondition then begin\n" // <5> 2 [+1 for nesting]
                    + "      try\n"
                    + "        repeat\n" // <8> 3 [+2 for nesting]
                    + "          if SomeCondition then begin\n" // <12> 4 [+3 for nesting]
                    + "            // Do nothing\n"
                    + "          end;\n"
                    + "        until SomeCondition;\n"
                    + "      except\n"
                    + "        on E : Exception do begin\n" // <15> 3 [+2 for nesting]
                    + "          for Index := 10 downto 0 do begin\n" // <19> 4 [+3 for nesting]
                    + "             Log('Self destruct in: ' + Index);\n"
                    + "          end;\n"
                    + "        end;\n"
                    + "      end;\n"
                    + "    end;\n"
                    + "  end\n"
                    + "  else if SomeOtherCondition then begin\n" // <20> 1
                    + "    case MyEnum of\n" // <22> 2 [+1 for nesting]
                    + "      someEnum: DoSomething;\n"
                    + "      someOtherEnum: DoSomethingElse;\n"
                    + "    else\n"
                    + "      DoThirdThing;\n"
                    + "    end;\n"
                    + "  end\n"
                    + "  else begin\n" // <23> 1
                    + "    MyAnonymousProc := procedure\n"
                    + "    begin\n"
                    + "      if SpookyError then begin\n" // <26> 3 [+2 for nesting]
                    + "        raise SpookyErrorException.Create('A spooky error has occurred');\n"
                    + "      end;\n"
                    + "      if Nevermind then;\n" // <29> 3 [+2 for nesting]
                    + "    end;\n"
                    + "  end;\n"
                    + "  Result := 42;\n"
                    + "end;\n"))
        .isEqualTo(29);
  }

  @Test
  void testSimpleBinaryExpression() {
    assertThat(
            getComplexity(
                "function Foo: Integer;\n"
                    + "begin\n"
                    + "  Result := Foo and Bar and Baz;\n" // 1
                    + "end;\n"))
        .isEqualTo(1);
  }

  @Test
  void testComplexBinaryExpression() {
    assertThat(
            getComplexity(
                "function Foo: Integer;\n"
                    + "begin\n"
                    + "  Result := ((Foo and (Bar and Baz)) and\n" // 1
                    + "    ((X or (Y)) or Z)) or\n" // 2
                    + "    (A and\n" // 3
                    + "      (B or C));\n" // 4
                    + "end;\n"))
        .isEqualTo(4);
  }

  private int getComplexity(String function) {
    Path path = tempDir.resolve("SourceFile.pas");
    try {
      Files.writeString(path, "unit SourceFile;\ninterface\nimplementation\n" + function + "end.");
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    DelphiFile delphiFile = DelphiFile.from(path.toFile(), DelphiFileUtils.mockConfig());
    DelphiAST ast = delphiFile.getAst();
    return visitor.visit(ast, new Data()).getComplexity();
  }
}
