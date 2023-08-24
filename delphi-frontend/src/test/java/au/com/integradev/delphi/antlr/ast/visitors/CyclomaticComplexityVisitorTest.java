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
package au.com.integradev.delphi.antlr.ast.visitors;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.integradev.delphi.antlr.ast.visitors.CyclomaticComplexityVisitor.Data;
import au.com.integradev.delphi.file.DelphiFile;
import au.com.integradev.delphi.utils.files.DelphiFileUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;

class CyclomaticComplexityVisitorTest {
  private CyclomaticComplexityVisitor visitor;

  @TempDir Path tempDir;

  @BeforeEach
  void setup() {
    visitor = new CyclomaticComplexityVisitor();
  }

  @Test
  void testEmptyMethod() {
    assertThat(
            getComplexity(
                "function Foo: Integer;\n" // 1
                    + "begin\n"
                    + "end;\n"))
        .isEqualTo(1);
  }

  @Test
  void testSimpleMethod() {
    assertThat(
            getComplexity(
                "function Foo: Integer;\n" // 1
                    + "begin\n"
                    + "  if X then begin\n" // 2
                    + "    Bar;\n"
                    + "  end;\n"
                    + "end;\n"))
        .isEqualTo(2);
  }

  @Test
  void testComplexMethod() {
    assertThat(
            getComplexity(
                "function Foo: Integer;\n" // 1
                    + "begin\n"
                    + "  if X then begin\n" // 2
                    + "    Bar;\n"
                    + "  end;\n"
                    + "  if X and Y then MyProcedure;\n" // 3 4
                    + "  if X or Y then \n" // 5 6
                    + "    Bar\n"
                    + "  else\n"
                    + "    Baz(1, 2, 3);\n"
                    + "  if X then begin\n" // 7
                    + "    Bar\n"
                    + "  end;\n"
                    + "  case MyProperty of\n"
                    + "    1: begin\n" // 8
                    + "       Bar;\n"
                    + "    end;\n"
                    + "    2: Bar;\n" // 9
                    + "    3: Bar\n" // 10
                    + "  end;\n"
                    + "  repeat\n" // 11
                    + "    Bar;\n"
                    + "    Baz(3, 2, 1);\n"
                    + "    Continue;\n"
                    + "  until ConditionMet;\n"
                    + "  asm\n"
                    + "    push eax\n"
                    + "  end;\n"
                    + "  try\n"
                    + "    Bar;\n"
                    + "    Xyzzy\n"
                    + "  except\n"
                    + "    on E : MyException do;\n"
                    + "    on Exception do begin\n"
                    + "      HandleException;\n"
                    + "    end;\n"
                    + "  end;\n"
                    + "  try\n"
                    + "    Xyzzy\n"
                    + "  finally\n"
                    + "  end;\n"
                    + "  while MyCondition do\n" // 12
                    + "    Break;\n"
                    + "  if X then begin\n" // 13
                    + "    Bar\n"
                    + "  end;\n"
                    + "  FAnonymous := procedure\n" // 14
                    + "    begin\n"
                    + "      Bar;\n"
                    + "    end;\n"
                    + "  for Index := 0 to 10 do begin\n" // 15
                    + "    Bar;\n"
                    + "  end;\n"
                    + "  Exit;\n"
                    + "end;\n"))
        .isEqualTo(15);
  }

  private int getComplexity(String function) {
    Path path = tempDir.resolve("SourceFile.pas");
    try {
      Files.writeString(path, "unit SourceFile;\ninterface\nimplementation\n" + function + "end.");
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    DelphiFile delphiFile = DelphiFile.from(path.toFile(), DelphiFileUtils.mockConfig());
    DelphiAst ast = delphiFile.getAst();
    return visitor.visit(ast, new Data()).getComplexity();
  }
}
