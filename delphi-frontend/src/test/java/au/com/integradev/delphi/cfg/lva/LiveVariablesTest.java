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

import static au.com.integradev.delphi.cfg.ControlFlowGraphTestUtils.buildCfgFromUnit;
import static au.com.integradev.delphi.cfg.ControlFlowGraphTestUtils.buildUnit;
import static au.com.integradev.delphi.cfg.ControlFlowGraphTestUtils.stringFromLines;
import static org.assertj.core.api.Assertions.*;

import au.com.integradev.delphi.cfg.ControlFlowGraphTestUtils;
import au.com.integradev.delphi.cfg.ControlFlowGraphUtils;
import au.com.integradev.delphi.cfg.api.Block;
import au.com.integradev.delphi.cfg.api.ControlFlowGraph;
import au.com.integradev.delphi.cfg.api.Terminus;
import au.com.integradev.delphi.file.DelphiFile;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.plugins.communitydelphi.api.ast.AnonymousMethodNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;

class LiveVariablesTest {
  private static final String END_BLOCK = "if False then;";

  private static Set<String> getLiveInputs(LiveVariables liveVariables, Block block) {
    return liveVariables.getBlockInputs(block).stream()
        .map(LiveVariable::getNameDeclaration)
        .map(NameDeclaration::getName)
        .collect(Collectors.toSet());
  }

  private static Set<String> getLiveOutputs(LiveVariables liveVariables, Block block) {
    return liveVariables.getBlockOutputs(block).stream()
        .map(LiveVariable::getNameDeclaration)
        .map(NameDeclaration::getName)
        .collect(Collectors.toSet());
  }

  @Test
  void testEmptyCfg() {
    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfg("");
    LiveVariables liveVariables = LiveVariables.analyze(cfg);
    assertThat(cfg.getBlocks()).hasSize(1);
    Block block = cfg.getEntryBlock();
    assertThat(liveVariables.getBlockInputs(block)).isEmpty();
    assertThat(liveVariables.getBlockOutputs(block)).isEmpty();
  }

  @Test
  void testSimpleLife() {
    final ControlFlowGraph cfg =
        ControlFlowGraphTestUtils.buildCfg("var I := 1;", END_BLOCK, "Foo(I);");
    LiveVariables liveVariables = LiveVariables.analyze(cfg);
    Block declarationBlock = cfg.getEntryBlock();
    Block invocationBlock = cfg.getEntryBlock().getSuccessors().iterator().next();
    assertThat(getLiveInputs(liveVariables, declarationBlock)).isEmpty();
    assertThat(getLiveOutputs(liveVariables, declarationBlock)).containsExactlyInAnyOrder("I");

    assertThat(getLiveInputs(liveVariables, invocationBlock)).containsExactlyInAnyOrder("I");
    assertThat(getLiveOutputs(liveVariables, invocationBlock)).isEmpty();
  }

  @Test
  void testSimpleDeath() {
    final ControlFlowGraph cfg =
        ControlFlowGraphTestUtils.buildCfg("var I := 1;", END_BLOCK, "I := 2;");
    LiveVariables liveVariables = LiveVariables.analyze(cfg);
    Block declarationBlock = cfg.getEntryBlock();
    Block assignmentBlock = cfg.getEntryBlock().getSuccessors().iterator().next();
    assertThat(getLiveInputs(liveVariables, declarationBlock)).isEmpty();
    assertThat(getLiveOutputs(liveVariables, declarationBlock)).isEmpty();

    assertThat(getLiveInputs(liveVariables, assignmentBlock)).isEmpty();
    assertThat(getLiveOutputs(liveVariables, assignmentBlock)).isEmpty();
  }

  @Test
  void testLivenessUnaffected() {
    // V not in GEN[N], V not in KILL[N]
    final ControlFlowGraph cfg =
        ControlFlowGraphTestUtils.buildCfg(
            List.of("V, A, B, C, D: Integer"), //
            "A := B + C;",
            "B := C * D;",
            END_BLOCK,
            "Exit(V);");
    LiveVariables liveVariables = LiveVariables.analyze(cfg);
    Block declarationBlock = cfg.getEntryBlock();
    assertThat(getLiveInputs(liveVariables, declarationBlock)).contains("V");
    assertThat(getLiveOutputs(liveVariables, declarationBlock)).contains("V");
  }

  @Test
  void testBirthBeforeBlock() {
    // V in GEN[N], V not in KILL[N]
    final ControlFlowGraph cfg =
        ControlFlowGraphTestUtils.buildCfg(
            List.of("V, A, B, C, D: Integer"), //
            "Writeln(V);",
            END_BLOCK,
            "A := B + C;",
            "B := V * D;");
    LiveVariables liveVariables = LiveVariables.analyze(cfg);
    Block declarationBlock = cfg.getEntryBlock().getSuccessors().iterator().next();
    assertThat(getLiveInputs(liveVariables, declarationBlock)).contains("V");
    assertThat(getLiveOutputs(liveVariables, declarationBlock)).doesNotContain("V");
  }

  @Test
  void testDiesBeforeBlock() {
    // V not in GEN[N], V in KILL[N]
    final ControlFlowGraph cfg =
        ControlFlowGraphTestUtils.buildCfg(
            List.of("V, A, B, C, D: Integer"), //
            "V := 1;",
            END_BLOCK,
            "A := B + C;",
            "V := C * D;");
    LiveVariables liveVariables = LiveVariables.analyze(cfg);
    Block declarationBlock = cfg.getEntryBlock().getSuccessors().iterator().next();
    assertThat(getLiveInputs(liveVariables, declarationBlock)).doesNotContain("V");
    assertThat(getLiveOutputs(liveVariables, declarationBlock)).doesNotContain("V");
  }

  @Test
  void testRebirthBeforeBlock() {
    // V in GEN[N], V in KILL[N]
    final ControlFlowGraph cfg =
        ControlFlowGraphTestUtils.buildCfg(
            List.of("V, A, B, C, D: Integer"), //
            "Writeln(V);",
            END_BLOCK,
            "A := V + C;",
            "V := C * D;");
    LiveVariables liveVariables = LiveVariables.analyze(cfg);
    Block declarationBlock = cfg.getEntryBlock().getSuccessors().iterator().next();
    assertThat(getLiveInputs(liveVariables, declarationBlock)).contains("V");
    assertThat(getLiveOutputs(liveVariables, declarationBlock)).doesNotContain("V");
  }

  @Test
  void testAssignmentList() {
    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfg("var A := 1;", "A := 2;");
    LiveVariables liveVariables = LiveVariables.analyze(cfg);
    assertThat(
            liveVariables.getAllAssignments().stream()
                .map(LiveVariable::getNameDeclaration)
                .map(NameDeclaration::getName))
        .containsExactly("A", "A");
  }

  private static Stream<Arguments> getExternalVarDefinitionNames() {
    return Stream.of(
        Arguments.of("IntVar"),
        Arguments.of("ImpVar"),
        Arguments.of("ParentField"),
        Arguments.of("Field"),
        Arguments.of("VarParam"),
        Arguments.of("OutParam"),
        Arguments.of("PointerParam"),
        Arguments.of("SafeProp"));
  }

  private static Stream<Arguments> getInternalVarDefinitionNames() {
    return Stream.of(Arguments.of("Param"), Arguments.of("Local"));
  }

  private static String getAllVarDefinitionsUnit(String... routineBody) {
    StringBuilder builder = new StringBuilder();
    builder.append(
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
            "  PointerParam: Pointer",
            "): Integer;",
            "var",
            "  Local: Integer;",
            "begin"));
    for (String routineBodyLine : routineBody) {
      builder.append("  ").append(routineBodyLine).append("\n");
    }

    builder.append("end;\n");
    builder.append("end.\n");
    return builder.toString();
  }

  @ParameterizedTest
  @MethodSource("getExternalVarDefinitionNames")
  void testExternalValuesAliveAtExit(String externalVar) {
    String unit = getAllVarDefinitionsUnit(externalVar + " := 1;");
    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfgFromUnit(unit, "TTest.Test");
    LiveVariables liveVariables = LiveVariables.analyze(cfg);
    Block block = cfg.getEntryBlock();
    assertThat(getLiveInputs(liveVariables, block)).isEmpty();
    assertThat(getLiveOutputs(liveVariables, block)).containsExactly(externalVar);

    Block exitBlock = block.getSuccessors().iterator().next();
    assertThat(getLiveInputs(liveVariables, exitBlock)).containsExactly(externalVar);
    assertThat(getLiveOutputs(liveVariables, exitBlock)).containsExactly(externalVar);
  }

  @ParameterizedTest
  @MethodSource("getExternalVarDefinitionNames")
  void testExternalValuesAliveAtExceptionalExit(String externalVar) {
    String unit = getAllVarDefinitionsUnit(externalVar + " := 1; raise " + externalVar + ";");
    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfgFromUnit(unit, "TTest.Test");
    LiveVariables liveVariables = LiveVariables.analyze(cfg);
    Block block = cfg.getEntryBlock();
    assertThat(getLiveInputs(liveVariables, block)).isEmpty();
    assertThat(getLiveOutputs(liveVariables, block)).containsExactly(externalVar);

    for (Block terminus :
        cfg.getBlocks().stream().filter(Terminus.class::isInstance).collect(Collectors.toList())) {
      assertThat(getLiveInputs(liveVariables, terminus)).containsExactly(externalVar);
      assertThat(getLiveOutputs(liveVariables, terminus)).containsExactly(externalVar);
    }
  }

  @ParameterizedTest
  @MethodSource("getInternalVarDefinitionNames")
  void testInternalValuesNotAliveAtExit(String internalVar) {
    String unit = getAllVarDefinitionsUnit(internalVar + " := 1;");
    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfgFromUnit(unit, "TTest.Test");
    LiveVariables liveVariables = LiveVariables.analyze(cfg);
    Block block = cfg.getEntryBlock();
    assertThat(getLiveInputs(liveVariables, block)).isEmpty();
    assertThat(getLiveOutputs(liveVariables, block)).isEmpty();

    Block exitBlock = block.getSuccessors().iterator().next();
    assertThat(getLiveInputs(liveVariables, exitBlock)).isEmpty();
    assertThat(getLiveOutputs(liveVariables, exitBlock)).isEmpty();
  }

  @ParameterizedTest
  @MethodSource("getInternalVarDefinitionNames")
  void testInternalValuesNotAliveAtExceptionalExit(String internalVar) {
    String unit = getAllVarDefinitionsUnit(internalVar + " := 1; raise " + internalVar + ";");
    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfgFromUnit(unit, "TTest.Test");
    LiveVariables liveVariables = LiveVariables.analyze(cfg);
    Block block = cfg.getEntryBlock();
    assertThat(getLiveInputs(liveVariables, block)).isEmpty();
    assertThat(getLiveOutputs(liveVariables, block)).isEmpty();

    for (Block terminus :
        cfg.getBlocks().stream().filter(Terminus.class::isInstance).collect(Collectors.toList())) {
      assertThat(getLiveInputs(liveVariables, terminus)).isEmpty();
      assertThat(getLiveOutputs(liveVariables, terminus)).isEmpty();
    }
  }

  @Test
  void testSubroutineUsesVariablesBeforeCall() {
    String unit =
        stringFromLines(
            "unit Test;",
            "interface",
            "implementation",
            "var Global1: Integer;",
            "var Global2: Integer;",
            "procedure ExternalRoutine; forward;",
            "function TestRoutine: Integer;",
            "var",
            "  Local1: Integer;",
            "  Local2: Integer;",
            "  procedure SubRoutine; begin Writeln(Local1); end;",
            "begin",
            "  Local1 := 1;",
            "  Local2 := 1;",
            END_BLOCK,
            "  SubRoutine;",
            "end;",
            "end.");

    final ControlFlowGraph cfg = buildCfgFromUnit(unit, "TestRoutine");

    LiveVariables liveVariables = LiveVariables.analyze(cfg);
    Block target =
        cfg.getBlocks().stream()
            .filter(
                block ->
                    block.getElements().stream()
                        .filter(NameReferenceNode.class::isInstance)
                        .map(NameReferenceNode.class::cast)
                        .map(NameReferenceNode::getNameDeclaration)
                        .anyMatch(RoutineNameDeclaration.class::isInstance))
            .findFirst()
            .orElseThrow();

    assertThat(getLiveInputs(liveVariables, target)).containsExactlyInAnyOrder("Local1");
    assertThat(getLiveOutputs(liveVariables, target)).isEmpty();
  }

  @Test
  void testExceptItemIsDeadAtExit() {
    final ControlFlowGraph cfg =
        ControlFlowGraphTestUtils.buildCfg("try", "except", "  on E: Exception do;", "end;");
    LiveVariables liveVariables = LiveVariables.analyze(cfg);
    Block target =
        cfg.getBlocks().stream()
            .filter(
                block ->
                    block.getElements().stream().anyMatch(NameDeclarationNode.class::isInstance))
            .findFirst()
            .orElseThrow();
    assertThat(liveVariables.getBlockInputs(target)).isEmpty();
    assertThat(liveVariables.getBlockOutputs(target)).isEmpty();
  }

  @Test
  void testForLoopVarIsDeadAtExit() {
    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfg("for var A in [] do;");
    LiveVariables liveVariables = LiveVariables.analyze(cfg);
    Block target =
        cfg.getBlocks().stream()
            .filter(
                block ->
                    block.getElements().stream().anyMatch(NameDeclarationNode.class::isInstance))
            .findFirst()
            .orElseThrow();
    assertThat(liveVariables.getBlockInputs(target)).isEmpty();
    assertThat(liveVariables.getBlockOutputs(target)).isEmpty();
  }

  @Test
  void testAnonymousMethodLocalVariables() {
    // Including parameters
    final ControlFlowGraph cfg =
        ControlFlowGraphTestUtils.buildCfg(
            Map.of(
                "type",
                List.of("TProc = reference to procedure(Input: Integer)"),
                "var",
                List.of("Foo: TProc", "Local: Integer")),
            "Foo :=",
            "    procedure(Input: Integer)",
            "    var",
            "      AnonLocal: Integer;",
            "    begin",
            "      WriteLn(Local);",
            "      WriteLn(AnonLocal);",
            "      WriteLn(Input);",
            "    end;");
    LiveVariables liveVariables = LiveVariables.analyze(cfg);
    Block target =
        cfg.getBlocks().stream()
            .filter(
                block ->
                    block.getElements().stream().anyMatch(AnonymousMethodNode.class::isInstance))
            .findFirst()
            .orElseThrow();
    assertThat(getLiveInputs(liveVariables, target)).containsExactly("Local");
    assertThat(liveVariables.getBlockOutputs(target)).isEmpty();
  }

  @Test
  void testAnonymousMethodLocalRoutines() {
    String unit =
        stringFromLines(
            "unit Test;",
            "interface",
            "implementation",
            "function TestRoutine: Integer;",
            "type",
            "  TProc = reference to procedure(Input: Integer);",
            "var",
            "  Foo: TProc;",
            "  Local1: Integer;",
            "  Local2: Integer;",
            "  Local3: Integer;",
            "  procedure OuterSubRoutine; begin Writeln(Local1); end;",
            "begin",
            "  Foo :=",
            "      procedure(Input: Integer)",
            "        procedure AnonymousSubRoutine; begin Writeln(Local2); end;",
            "      var",
            "        AnonLocal: Integer;",
            "      begin",
            "        WriteLn(Local);",
            "        WriteLn(AnonLocal);",
            "        WriteLn(Input);",
            "        OuterSubRoutine;",
            "        AnonymousSubRoutine;",
            "      end;",
            "end;",
            "end.");

    final ControlFlowGraph cfg = buildCfgFromUnit(unit, "TestRoutine");

    LiveVariables liveVariables = LiveVariables.analyze(cfg);
    Block target =
        cfg.getBlocks().stream()
            .filter(
                block ->
                    block.getElements().stream().anyMatch(AnonymousMethodNode.class::isInstance))
            .findFirst()
            .orElseThrow();

    assertThat(getLiveInputs(liveVariables, target)).containsExactlyInAnyOrder("Local1", "Local2");
    assertThat(getLiveOutputs(liveVariables, target)).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "begin",
        "initialization",
        "initialization finalization",
      })
  void testNonRoutineLocalVariables(String blockStart) {
    final DelphiFile file =
        buildUnit(
            "unit Test;",
            "interface",
            "implementation",
            "var",
            "  External: Integer;",
            blockStart,
            "  var Local := 1;",
            "  WriteLn(Local);",
            "  WriteLn(External);",
            "end.");

    final ControlFlowGraph cfg =
        ControlFlowGraphUtils.findContainingCFG(
            file.getAst().findDescendantsOfType(NameReferenceNode.class).stream()
                .filter(name -> name.getNameDeclaration().getName().equals("Local"))
                .findFirst()
                .orElseThrow());

    if (cfg == null) {
      fail("Unable to find CFG");
      return;
    }

    LiveVariables liveVariables = LiveVariables.analyze(cfg);
    Block target = cfg.getEntryBlock();

    assertThat(getLiveInputs(liveVariables, target)).containsExactlyInAnyOrder("External");
    assertThat(getLiveOutputs(liveVariables, target)).containsExactlyInAnyOrder("External");
  }

  @Test
  void testWhileLoop() {
    final ControlFlowGraph cfg =
        ControlFlowGraphTestUtils.buildCfg(
            "var A := 4;", // b1
            "var N := A * 2;",
            "while A > N do", // b2
            "  A := A + 1;", // b3
            "A := A + 1;" // b4
            );

    LiveVariables liveVariables = LiveVariables.analyze(cfg);
    Iterator<Block> blocks = cfg.getBlocks().iterator();

    SoftAssertions.assertSoftly(
        softly -> {
          Block b1 = blocks.next();
          Block b2 = blocks.next();
          Block b3 = blocks.next();
          Block b4 = blocks.next();
          Block exitBlock = blocks.next();
          softly.assertThat(blocks.hasNext()).isFalse();

          softly.assertThat(getLiveInputs(liveVariables, b1)).isEmpty();
          softly.assertThat(getLiveOutputs(liveVariables, b1)).containsExactlyInAnyOrder("A", "N");

          softly.assertThat(getLiveInputs(liveVariables, b2)).containsExactlyInAnyOrder("A", "N");
          softly.assertThat(getLiveOutputs(liveVariables, b2)).containsExactlyInAnyOrder("A", "N");

          softly.assertThat(getLiveInputs(liveVariables, b3)).containsExactlyInAnyOrder("A", "N");
          softly.assertThat(getLiveOutputs(liveVariables, b3)).containsExactlyInAnyOrder("A", "N");

          softly.assertThat(getLiveInputs(liveVariables, b4)).containsExactlyInAnyOrder("A");
          softly.assertThat(getLiveOutputs(liveVariables, b4)).isEmpty();

          softly.assertThat(getLiveInputs(liveVariables, exitBlock)).isEmpty();
          softly.assertThat(getLiveOutputs(liveVariables, exitBlock)).isEmpty();
        });
  }

  @Test
  void testIfBlock() {
    final ControlFlowGraph cfg =
        ControlFlowGraphTestUtils.buildCfg(
            "var A := 3;", // b1
            "var B := 5;",
            "var D := 4",
            "var X := 100;",
            "var C;",
            "if A > B then begin", // b2
            "  C := A + B;",
            "  D := 2;",
            "end;",
            "C := 4;", // b3
            "Exit(B * D * C);");

    LiveVariables liveVariables = LiveVariables.analyze(cfg);
    Iterator<Block> blocks = cfg.getBlocks().iterator();

    SoftAssertions.assertSoftly(
        softly -> {
          Block b1 = blocks.next();
          Block b2 = blocks.next();
          Block b3 = blocks.next();
          Block exitBlock = blocks.next();
          softly.assertThat(blocks.hasNext()).isFalse();

          softly.assertThat(getLiveInputs(liveVariables, b1)).isEmpty();
          softly
              .assertThat(getLiveOutputs(liveVariables, b1))
              .containsExactlyInAnyOrder("A", "B", "D");

          softly.assertThat(getLiveInputs(liveVariables, b2)).containsExactlyInAnyOrder("A", "B");
          softly.assertThat(getLiveOutputs(liveVariables, b2)).containsExactlyInAnyOrder("B", "D");

          softly.assertThat(getLiveInputs(liveVariables, b3)).containsExactlyInAnyOrder("B", "D");
          softly.assertThat(getLiveOutputs(liveVariables, b3)).isEmpty();

          softly.assertThat(getLiveOutputs(liveVariables, exitBlock)).isEmpty();
        });
  }

  @Test
  void testIndependentAssignments() {
    final ControlFlowGraph cfg =
        ControlFlowGraphTestUtils.buildCfg(
            List.of("A: Integer"),
            "if True then begin", // b1
            "  A := 1;", // b2
            "end else begin",
            "  A := 2;", // b3
            "end;",
            "Exit(A);" // b4
            );

    LiveVariables liveVariables = LiveVariables.analyze(cfg);
    Iterator<Block> blocks = cfg.getBlocks().iterator();

    SoftAssertions.assertSoftly(
        softly -> {
          Block b1 = blocks.next();
          Block b2 = blocks.next();
          Block b3 = blocks.next();
          Block b4 = blocks.next();
          Block exitBlock = blocks.next();
          softly.assertThat(blocks.hasNext()).isFalse();

          softly.assertThat(getLiveInputs(liveVariables, b1)).isEmpty();
          softly.assertThat(getLiveOutputs(liveVariables, b1)).isEmpty();

          softly.assertThat(getLiveInputs(liveVariables, b2)).isEmpty();
          softly.assertThat(getLiveOutputs(liveVariables, b2)).containsExactlyInAnyOrder("A");

          softly.assertThat(getLiveInputs(liveVariables, b3)).isEmpty();
          softly.assertThat(getLiveOutputs(liveVariables, b3)).containsExactlyInAnyOrder("A");

          softly.assertThat(getLiveInputs(liveVariables, b4)).containsExactlyInAnyOrder("A");
          softly.assertThat(getLiveOutputs(liveVariables, b4)).isEmpty();

          softly.assertThat(getLiveOutputs(liveVariables, exitBlock)).isEmpty();
        });
  }
}
