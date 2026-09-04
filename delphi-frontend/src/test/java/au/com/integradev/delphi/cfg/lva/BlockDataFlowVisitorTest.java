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

package au.com.integradev.delphi.cfg.lva;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.integradev.delphi.cfg.ControlFlowGraphTestUtils;
import au.com.integradev.delphi.cfg.api.Block;
import au.com.integradev.delphi.cfg.api.Branch;
import au.com.integradev.delphi.cfg.api.ControlFlowGraph;
import au.com.integradev.delphi.cfg.api.UnknownException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.plugins.communitydelphi.api.ast.SimpleNameDeclarationNode;

class BlockDataFlowVisitorTest {

  private static BlockDataFlowVisitor getTestVisitor(
      List<LiveVariable> assignments, List<LiveVariable> references) {
    return new BlockDataFlowVisitor()
        .withOnAssign(assignments::add)
        .withOnReference(references::add);
  }

  private static Stream<String> toIdentifiers(List<LiveVariable> variables) {
    return variables.stream().map(liveVariable -> liveVariable.getNameDeclaration().getName());
  }

  @Test
  void testIfStatementReference() {
    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfg("var A;", "if A then;");
    Block target = cfg.getEntryBlock();
    List<LiveVariable> assignments = new ArrayList<>();
    List<LiveVariable> references = new ArrayList<>();
    getTestVisitor(assignments, references).visit(target);

    assertThat(toIdentifiers(assignments)).isEmpty();
    assertThat(toIdentifiers(references)).containsExactly("A");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "var A := 1;",
        "var A: Integer := 1;",
        "const A = 1;",
        "const A: Integer = 1;",
      })
  void testInlineDeclarationStatement(String statement) {
    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfg(statement);
    Block target = cfg.getEntryBlock();
    List<LiveVariable> assignments = new ArrayList<>();
    List<LiveVariable> references = new ArrayList<>();
    getTestVisitor(assignments, references).visit(target);

    assertThat(toIdentifiers(assignments)).containsExactly("A");
    assertThat(toIdentifiers(references)).isEmpty();
  }

  @Test
  void testCaseStatementReference() {
    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfg("var A;", "case A of", "end;");
    Block target = cfg.getEntryBlock();
    List<LiveVariable> assignments = new ArrayList<>();
    List<LiveVariable> references = new ArrayList<>();
    getTestVisitor(assignments, references).visit(target);

    assertThat(toIdentifiers(assignments)).isEmpty();
    assertThat(toIdentifiers(references)).containsExactly("A");
  }

  @Test
  void testRepeatStatementReference() {
    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfg("var A;", "repeat", "until A;");
    Block target = cfg.getEntryBlock().getSuccessors().iterator().next();
    List<LiveVariable> assignments = new ArrayList<>();
    List<LiveVariable> references = new ArrayList<>();
    getTestVisitor(assignments, references).visit(target);

    assertThat(toIdentifiers(assignments)).isEmpty();
    assertThat(toIdentifiers(references)).containsExactly("A");
  }

  @Test
  void testWhileStatementReference() {
    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfg("var A;", "while A do;");
    Block target = cfg.getEntryBlock().getSuccessors().iterator().next();
    List<LiveVariable> assignments = new ArrayList<>();
    List<LiveVariable> references = new ArrayList<>();
    getTestVisitor(assignments, references).visit(target);

    assertThat(toIdentifiers(assignments)).isEmpty();
    assertThat(toIdentifiers(references)).containsExactly("A");
  }

  private static Stream<Arguments> forInArguments() {
    return Stream.of(
        Arguments.of("var A, B; for A in B do;"), Arguments.of("var B; for var A in B do;"));
  }

  private static Stream<Arguments> forToArguments() {
    return Stream.of(
        Arguments.of("var A, B, C; for A := B to C do;"),
        Arguments.of("var A, B, C; for A := B downto C do;"),
        Arguments.of("var B, C; for var A := B to C do;"),
        Arguments.of("var B, C; for var A := B downto C do;"));
  }

  private static Stream<Arguments> forArguments() {
    return Stream.concat(forInArguments(), forToArguments());
  }

  @ParameterizedTest
  @MethodSource("forArguments")
  void testForStatementVariable(String statement) {
    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfg(statement);
    Block target =
        cfg.getBlocks().stream().filter(Branch.class::isInstance).findFirst().orElseThrow();
    List<LiveVariable> assignments = new ArrayList<>();
    List<LiveVariable> references = new ArrayList<>();
    getTestVisitor(assignments, references).visit(target);

    assertThat(toIdentifiers(assignments)).containsExactly("A");
    // It is implicitly referenced to check the end of the loop
    assertThat(toIdentifiers(references)).containsExactly("A");
  }

  @ParameterizedTest
  @MethodSource("forArguments")
  void testForStatementEnumerator(String statement) {
    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfg(statement);
    Block target = cfg.getEntryBlock().getSuccessors().iterator().next();
    List<LiveVariable> assignments = new ArrayList<>();
    List<LiveVariable> references = new ArrayList<>();
    getTestVisitor(assignments, references).visit(target);

    assertThat(toIdentifiers(assignments)).isEmpty();
    assertThat(toIdentifiers(references)).containsExactly("B");
  }

  @ParameterizedTest
  @MethodSource("forToArguments")
  void testForToStatementRangeEnd(String statement) {
    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfg(statement);
    Block target =
        cfg.getEntryBlock().getSuccessors().iterator().next().getSuccessors().iterator().next();
    List<LiveVariable> assignments = new ArrayList<>();
    List<LiveVariable> references = new ArrayList<>();
    getTestVisitor(assignments, references).visit(target);

    assertThat(toIdentifiers(assignments)).isEmpty();
    assertThat(toIdentifiers(references)).containsExactly("C");
  }

  @Test
  void testWithStatementReference() {
    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfg("var A;", "with A do;");
    Block target = cfg.getEntryBlock().getSuccessors().iterator().next();
    List<LiveVariable> assignments = new ArrayList<>();
    List<LiveVariable> references = new ArrayList<>();
    getTestVisitor(assignments, references).visit(target);

    assertThat(toIdentifiers(assignments)).isEmpty();
    assertThat(toIdentifiers(references)).containsExactly("A");
  }

  @Test
  void testExceptHandlerDeclaration() {
    final ControlFlowGraph cfg =
        ControlFlowGraphTestUtils.buildCfg("try", "except", "  on E: Exception do;", "end;");
    Block target =
        cfg.getBlocks().stream()
            .filter(
                block ->
                    block.getElements().stream()
                        .anyMatch(SimpleNameDeclarationNode.class::isInstance))
            .findFirst()
            .orElseThrow();
    List<LiveVariable> assignments = new ArrayList<>();
    List<LiveVariable> references = new ArrayList<>();
    getTestVisitor(assignments, references).visit(target);

    assertThat(toIdentifiers(assignments)).containsExactly("E");
    assertThat(toIdentifiers(references)).isEmpty();
  }

  @Test
  void testRaiseStatementReference() {
    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfg("var A;", "raise A;");
    Block target = cfg.getEntryBlock();
    List<LiveVariable> assignments = new ArrayList<>();
    List<LiveVariable> references = new ArrayList<>();
    getTestVisitor(assignments, references).visit(target);

    assertThat(toIdentifiers(assignments)).isEmpty();
    assertThat(toIdentifiers(references)).containsExactly("A");
  }

  @Test
  void testRaiseAtStatementReference() {
    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfg("var A, B;", "raise A at B;");
    Block target = cfg.getEntryBlock();
    List<LiveVariable> assignments = new ArrayList<>();
    List<LiveVariable> references = new ArrayList<>();
    getTestVisitor(assignments, references).visit(target);

    assertThat(toIdentifiers(assignments)).isEmpty();
    assertThat(toIdentifiers(references)).containsExactly("B", "A");
  }

  @Test
  void testLabelsAreIgnored() {
    final ControlFlowGraph cfg =
        ControlFlowGraphTestUtils.buildCfg(Map.of("label", List.of("L")), "L:");
    Block target = cfg.getEntryBlock();
    List<LiveVariable> assignments = new ArrayList<>();
    List<LiveVariable> references = new ArrayList<>();
    getTestVisitor(assignments, references).visit(target);

    assertThat(toIdentifiers(assignments)).isEmpty();
    assertThat(toIdentifiers(references)).isEmpty();
  }

  @Test
  void testAssignmentStatement() {
    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfg("var A, B;", "A := B;");
    Block target = cfg.getEntryBlock();
    List<LiveVariable> assignments = new ArrayList<>();
    List<LiveVariable> references = new ArrayList<>();
    getTestVisitor(assignments, references).visit(target);

    assertThat(toIdentifiers(assignments)).containsExactly("A");
    assertThat(toIdentifiers(references)).containsExactly("B");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "A.C := B;",
        "A[0] := B;",
      })
  void testAssignmentReferenceStatement(String statement) {
    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfg("var A, B;", statement);
    Block target = cfg.getEntryBlock();
    List<LiveVariable> assignments = new ArrayList<>();
    List<LiveVariable> references = new ArrayList<>();
    getTestVisitor(assignments, references).visit(target);

    assertThat(toIdentifiers(assignments)).isEmpty();
    assertThat(toIdentifiers(references)).containsExactly("B", "A");
  }

  @Test
  void testRoutineStatement() {
    final ControlFlowGraph cfg =
        ControlFlowGraphTestUtils.buildCfg(
            Map.of("", List.of("procedure Foo(Variable: Integer); begin end")),
            "var A: Integer;",
            "Foo(A);");
    Block target = cfg.getEntryBlock();
    List<LiveVariable> assignments = new ArrayList<>();
    List<LiveVariable> references = new ArrayList<>();
    getTestVisitor(assignments, references).visit(target);

    assertThat(toIdentifiers(assignments)).isEmpty();
    assertThat(toIdentifiers(references)).containsExactly("A", "Foo");
  }

  @Test
  void testInvocableStatement() {
    final ControlFlowGraph cfg =
        ControlFlowGraphTestUtils.buildCfg(
            Map.of(
                "",
                List.of("procedure Foo; begin end"),
                "var",
                List.of("Proc: procedure(A: Integer) of object", "A: Integer")),
            "Proc(A);");
    Block target = cfg.getEntryBlock();
    List<LiveVariable> assignments = new ArrayList<>();
    List<LiveVariable> references = new ArrayList<>();
    getTestVisitor(assignments, references).visit(target);

    assertThat(toIdentifiers(assignments)).isEmpty();
    assertThat(toIdentifiers(references)).containsExactly("A", "Proc");
  }

  @Test
  void testAnonymousRoutineReference() {
    final ControlFlowGraph cfg =
        ControlFlowGraphTestUtils.buildCfg(
            "var A: Integer;", "var B := procedure begin Writeln(A); end;");
    Block target = cfg.getEntryBlock();
    List<LiveVariable> assignments = new ArrayList<>();
    List<LiveVariable> references = new ArrayList<>();
    getTestVisitor(assignments, references).visit(target);

    assertThat(toIdentifiers(assignments)).containsExactly("B");
    assertThat(toIdentifiers(references)).containsExactly("A");
  }

  @Test
  void testAnonymousRoutineOnlyReferenceExternalVariables() {
    final ControlFlowGraph cfg =
        ControlFlowGraphTestUtils.buildCfg(
            "var External: Integer;",
            "var B :=",
            "    procedure(",
            "        Param: Integer;",
            "        out OutParam: Integer;",
            "        var VarParam: Integer;",
            "        PParam: Pointer",
            "    )",
            "    var",
            "      AnonLocal: Integer;",
            "    begin",
            "      var AnonInline := 1;",
            "      Writeln(External);",
            "      Writeln(AnonInline);",
            "      Writeln(AnonLocal);",
            "      Writeln(Param);",
            "      Writeln(OutParam);",
            "      Writeln(VarParam);",
            "      Writeln(PParam);",
            "    end;");
    Block target = cfg.getEntryBlock();
    List<LiveVariable> assignments = new ArrayList<>();
    List<LiveVariable> references = new ArrayList<>();
    getTestVisitor(assignments, references).visit(target);

    assertThat(toIdentifiers(assignments)).containsExactly("B");
    assertThat(toIdentifiers(references)).containsExactly("External");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "IntVar",
        "ImpVar",
        "Field",
        "Param",
        "VarParam",
        "OutParam",
        "PointerParam",
        "SafeProp",
        "UnsafeProp1",
        "UnsafeProp2",
        "UnsafeProp3",
        "Local",
        "Self",
        "Result",
        "Test",
      })
  void testNonInlineVariableReference(String variable) {
    String unit =
        ControlFlowGraphTestUtils.stringFromLines(
            "unit Test;",
            "interface",
            "var",
            "  IntVar: Integer;",
            "implementation",
            "var",
            "  ImpVar: Integer;",
            "type",
            "  TParent = class",
            "    ParentField: Integer;",
            "  end;",
            "  TTest = class(TParent)",
            "    Field: Integer;",
            "    function GetField: Integer;",
            "    procedure SetField(Val: Integer): Integer;",
            "    function Test(",
            "      Param: Integer;",
            "      var VarParam: Integer;",
            "      out OutParam: Integer;",
            "      PointerParam: PInteger",
            "    ): Integer;",
            "    property SafeProp: Integer read Field write Field;",
            "    property UnsafeProp1: Integer read GetField write Field;",
            "    property UnsafeProp2: Integer read Field write SetField;",
            "    property UnsafeProp3: Integer read GetField write SetField;",
            "  end;",
            "function TTest.Test(",
            "  Param: Integer;",
            "  var VarParam: Integer;",
            "  out OutParam: Integer;",
            "  PointerParam: PInteger",
            "): Integer;",
            "var",
            "  Local: Integer;",
            "begin",
            "  " + variable + " := " + variable + ";",
            "end;",
            "end.");
    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfgFromUnit(unit, "TTest.Test");
    Block target = cfg.getEntryBlock();
    List<LiveVariable> assignments = new ArrayList<>();
    List<LiveVariable> references = new ArrayList<>();
    getTestVisitor(assignments, references).visit(target);

    assertThat(toIdentifiers(assignments)).containsExactly(variable);
    assertThat(toIdentifiers(references)).containsExactly(variable);
  }

  @Test
  void testInvalidAssignment() {
    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfg("1 := 1;");
    Block target = cfg.getEntryBlock();
    List<LiveVariable> assignments = new ArrayList<>();
    List<LiveVariable> references = new ArrayList<>();
    getTestVisitor(assignments, references).visit(target);

    assertThat(toIdentifiers(assignments)).isEmpty();
    assertThat(toIdentifiers(references)).isEmpty();
  }

  @Test
  void testBareRaise() {
    final ControlFlowGraph cfg =
        ControlFlowGraphTestUtils.buildCfg(
            "try", "except", "  on E: Exception do begin raise; end;", "end;");
    Block target =
        cfg.getBlocks().stream()
            .filter(UnknownException.class::isInstance)
            .findFirst()
            .orElseThrow();
    List<LiveVariable> assignments = new ArrayList<>();
    List<LiveVariable> references = new ArrayList<>();
    getTestVisitor(assignments, references).visit(target);

    assertThat(toIdentifiers(assignments)).containsExactly("E");
    assertThat(toIdentifiers(references)).containsExactly("E");
  }

  @Test
  void testBareInherited() {
    String unit =
        ControlFlowGraphTestUtils.stringFromLines(
            "unit Test;",
            "interface",
            "implementation",
            "type",
            "  TParent = class",
            "    procedure Test(Param: Integer);",
            "  end;",
            "  TTest = class(TParent)",
            "    procedure Test(Param: Integer);",
            "  end;",
            "procedure TTest.Test(Param: Integer);",
            "begin",
            "  inherited;",
            "end;",
            "end.");

    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfgFromUnit(unit, "TTest.Test");
    Block target = cfg.getEntryBlock();
    List<LiveVariable> assignments = new ArrayList<>();
    List<LiveVariable> references = new ArrayList<>();
    getTestVisitor(assignments, references).visit(target);

    assertThat(toIdentifiers(assignments)).isEmpty();
    assertThat(toIdentifiers(references)).containsExactly("Param");
  }

  @Test
  void testInherited() {
    String unit =
        ControlFlowGraphTestUtils.stringFromLines(
            "unit Test;",
            "interface",
            "implementation",
            "type",
            "  TParent = class",
            "    procedure Test(Param: Integer);",
            "  end;",
            "  TTest = class(TParent)",
            "    procedure Test(Param: Integer);",
            "  end;",
            "procedure TTest.Test(Param: Integer);",
            "begin",
            "  inherited Test(Param);",
            "end;",
            "end.");

    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfgFromUnit(unit, "TTest.Test");
    Block target = cfg.getEntryBlock();
    List<LiveVariable> assignments = new ArrayList<>();
    List<LiveVariable> references = new ArrayList<>();
    getTestVisitor(assignments, references).visit(target);

    assertThat(toIdentifiers(assignments)).isEmpty();
    assertThat(toIdentifiers(references)).containsExactly("Test", "Param");
  }

  @Test
  void testSelfFieldReference() {
    String unit =
        ControlFlowGraphTestUtils.stringFromLines(
            "unit Test;",
            "interface",
            "implementation",
            "type",
            "  TTest = class(TParent)",
            "    Field: Integer;",
            "    procedure Test;",
            "  end;",
            "procedure TTest.Test;",
            "begin",
            "  Writeln(Self.Field);",
            "end;",
            "end.");

    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfgFromUnit(unit, "TTest.Test");
    Block target = cfg.getEntryBlock();
    List<LiveVariable> assignments = new ArrayList<>();
    List<LiveVariable> references = new ArrayList<>();
    getTestVisitor(assignments, references).visit(target);

    assertThat(toIdentifiers(assignments)).isEmpty();
    assertThat(toIdentifiers(references)).containsExactly("Field", "WriteLn");
  }
}
