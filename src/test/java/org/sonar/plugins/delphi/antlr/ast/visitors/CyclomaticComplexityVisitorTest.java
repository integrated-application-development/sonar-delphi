package org.sonar.plugins.delphi.antlr.ast.visitors;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.visitors.CyclomaticComplexityVisitor.Data;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class CyclomaticComplexityVisitorTest {
  private CyclomaticComplexityVisitor visitor;

  @Before
  public void setup() {
    visitor = new CyclomaticComplexityVisitor();
  }

  @Test
  public void testEmptyMethod() {
    DelphiAST ast =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;") // 1
            .appendImpl("begin")
            .appendImpl("end;")
            .parse();

    assertThat(getComplexity(ast)).isEqualTo(1);
  }

  @Test
  public void testSimpleMethod() {
    DelphiAST ast =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;") // 1
            .appendImpl("begin")
            .appendImpl("  if X then begin") // 2
            .appendImpl("    Bar;")
            .appendImpl("  end;")
            .appendImpl("end;")
            .parse();

    assertThat(getComplexity(ast)).isEqualTo(2);
  }

  @Test
  public void testComplexMethod() {
    DelphiAST ast =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;") // 1
            .appendImpl("begin")
            .appendImpl("  if X then begin") // 2
            .appendImpl("    Bar;")
            .appendImpl("  end;")
            .appendImpl("  if X and Y then MyProcedure;") // 3 4
            .appendImpl("  if X or Y then ") // 5 6
            .appendImpl("    Bar")
            .appendImpl("  else")
            .appendImpl("    Baz(1, 2, 3);")
            .appendImpl("  if X then begin") // 7
            .appendImpl("    Bar")
            .appendImpl("  end;")
            .appendImpl("  case MyProperty of")
            .appendImpl("    1: begin") // 8
            .appendImpl("       Bar;")
            .appendImpl("    end;")
            .appendImpl("    2: Bar;") // 9
            .appendImpl("    3: Bar") // 10
            .appendImpl("  end;")
            .appendImpl("  repeat") // 11
            .appendImpl("    Bar;")
            .appendImpl("    Baz(3, 2, 1);")
            .appendImpl("    Continue;")
            .appendImpl("  until ConditionMet;")
            .appendImpl("  asm")
            .appendImpl("    push eax")
            .appendImpl("  end;")
            .appendImpl("  try")
            .appendImpl("    Bar;")
            .appendImpl("    Xyzzy")
            .appendImpl("  except")
            .appendImpl("    on E : MyException do;")
            .appendImpl("    on Exception do begin")
            .appendImpl("      HandleException;")
            .appendImpl("    end;")
            .appendImpl("  end;")
            .appendImpl("  try")
            .appendImpl("    Xyzzy")
            .appendImpl("  finally")
            .appendImpl("  end;")
            .appendImpl("  while MyCondition do") // 12
            .appendImpl("    Break;")
            .appendImpl("  if X then begin") // 13
            .appendImpl("    Bar")
            .appendImpl("  end;")
            .appendImpl("  FAnonymous := procedure") // 14
            .appendImpl("    begin")
            .appendImpl("      Bar;")
            .appendImpl("    end;")
            .appendImpl("  for Index := 0 to 10 do begin") // 15
            .appendImpl("    Bar;")
            .appendImpl("  end;")
            .appendImpl("  Exit;")
            .appendImpl("end;")
            .parse();

    assertThat(getComplexity(ast)).isEqualTo(15);
  }

  private int getComplexity(DelphiAST ast) {
    return visitor.visit(ast, new Data()).getComplexity();
  }
}
