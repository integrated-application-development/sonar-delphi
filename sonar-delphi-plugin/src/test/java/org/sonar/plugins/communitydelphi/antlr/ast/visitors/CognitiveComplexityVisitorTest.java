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
package org.sonar.plugins.communitydelphi.antlr.ast.visitors;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.antlr.ast.DelphiAST;
import org.sonar.plugins.communitydelphi.antlr.ast.visitors.CognitiveComplexityVisitor.Data;
import org.sonar.plugins.communitydelphi.utils.builders.DelphiTestUnitBuilder;

class CognitiveComplexityVisitorTest {
  private CognitiveComplexityVisitor visitor;

  @BeforeEach
  void setup() {
    visitor = new CognitiveComplexityVisitor();
  }

  @Test
  void testSimpleMethod() {
    DelphiAST ast =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;")
            .appendImpl("begin")
            .appendImpl("  if Foo then Bar;") // <1> 1
            .appendImpl("end;")
            .parse();

    assertThat(getComplexity(ast)).isEqualTo(1);
  }

  @Test
  void testTooComplexMethod() {
    DelphiAST ast =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;")
            .appendImpl("begin")
            .appendImpl("  if SomeCondition then begin") // <1> 1
            .appendImpl("    while SomeCondition do begin") // <3> 2 [+1 for nesting]
            .appendImpl("      SomeCondition := False;")
            .appendImpl("      Break;")
            .appendImpl("    end;")
            .appendImpl("    if not SomeCondition then begin") // <5> 2 [+1 for nesting]
            .appendImpl("      try")
            .appendImpl("        repeat") // <8> 3 [+2 for nesting]
            .appendImpl("          if SomeCondition then begin") // <12> 4 [+3 for nesting]
            .appendImpl("            // Do nothing")
            .appendImpl("          end;")
            .appendImpl("        until SomeCondition;")
            .appendImpl("      except")
            .appendImpl("        on E : Exception do begin") // <15> 3 [+2 for nesting]
            .appendImpl("          for Index := 10 downto 0 do begin") // <19> 4 [+3 for nesting]
            .appendImpl("             Log('Self destruct in: ' + Index);")
            .appendImpl("          end;")
            .appendImpl("        end;")
            .appendImpl("      end;")
            .appendImpl("    end;")
            .appendImpl("  end")
            .appendImpl("  else if SomeOtherCondition then begin") // <20> 1
            .appendImpl("    case MyEnum of") // <22> 2 [+1 for nesting]
            .appendImpl("      someEnum: DoSomething;")
            .appendImpl("      someOtherEnum: DoSomethingElse;")
            .appendImpl("    else")
            .appendImpl("      DoThirdThing;")
            .appendImpl("    end;")
            .appendImpl("  end")
            .appendImpl("  else begin") // <23> 1
            .appendImpl("    MyAnonymousProc := procedure")
            .appendImpl("    begin")
            .appendImpl("      if SpookyError then begin") // <26> 3 [+2 for nesting]
            .appendImpl("        raise SpookyErrorException.Create('A spooky error has occurred');")
            .appendImpl("      end;")
            .appendImpl("      if Nevermind then;") // <29> 3 [+2 for nesting]
            .appendImpl("    end;")
            .appendImpl("  end;")
            .appendImpl("  Result := 42;")
            .appendImpl("end;")
            .parse();

    assertThat(getComplexity(ast)).isEqualTo(29);
  }

  @Test
  void testSimpleBinaryExpression() {
    DelphiAST ast =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;")
            .appendImpl("begin")
            .appendImpl("  Result := Foo and Bar and Baz;") // 1
            .appendImpl("end;")
            .parse();

    assertThat(getComplexity(ast)).isEqualTo(1);
  }

  @Test
  void testComplexBinaryExpression() {
    DelphiAST ast =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;")
            .appendImpl("begin")
            .appendImpl("  Result := ((Foo and (Bar and Baz)) and") // 1
            .appendImpl("    ((X or (Y)) or Z)) or") // 2
            .appendImpl("    (A and ") // 3
            .appendImpl("      (B or C));") // 4
            .appendImpl("end;")
            .parse();

    assertThat(getComplexity(ast)).isEqualTo(4);
  }

  private int getComplexity(DelphiAST ast) {
    return visitor.visit(ast, new Data()).getComplexity();
  }
}
