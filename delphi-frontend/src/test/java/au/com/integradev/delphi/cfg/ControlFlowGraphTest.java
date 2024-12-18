/*
 * Sonar Delphi Plugin
 * Copyright (C) 2024 Integrated Application Development
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
package au.com.integradev.delphi.cfg;

import static au.com.integradev.delphi.cfg.checker.BlockChecker.block;
import static au.com.integradev.delphi.cfg.checker.BlockChecker.terminator;
import static au.com.integradev.delphi.cfg.checker.ElementChecker.element;
import static au.com.integradev.delphi.cfg.checker.GraphChecker.checker;
import static org.assertj.core.api.Assertions.*;

import au.com.integradev.delphi.DelphiProperties;
import au.com.integradev.delphi.antlr.ast.visitors.SymbolAssociationVisitor;
import au.com.integradev.delphi.cfg.api.Block;
import au.com.integradev.delphi.cfg.api.ControlFlowGraph;
import au.com.integradev.delphi.cfg.block.TerminatorKind;
import au.com.integradev.delphi.cfg.checker.GraphChecker;
import au.com.integradev.delphi.cfg.checker.StatementTerminator;
import au.com.integradev.delphi.compiler.Platform;
import au.com.integradev.delphi.file.DelphiFile;
import au.com.integradev.delphi.file.DelphiFileConfig;
import au.com.integradev.delphi.preprocessor.DelphiPreprocessorFactory;
import au.com.integradev.delphi.symbol.SymbolTable;
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import au.com.integradev.delphi.utils.files.DelphiFileUtils;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.communitydelphi.api.ast.BinaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.CaseStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.CommonDelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ForInStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ForToStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.GotoStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.IfStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.IntegerLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.NilLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.RaiseStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.RepeatStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.TryStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.UnaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.WhileStatementNode;
import org.sonar.plugins.communitydelphi.api.operator.BinaryOperator;
import org.sonar.plugins.communitydelphi.api.operator.UnaryOperator;

class ControlFlowGraphTest {
  private static final Logger LOG = LoggerFactory.getLogger(ControlFlowGraphTest.class);

  private ControlFlowGraph buildCFG(String input) {
    return buildCFG(Collections.emptyMap(), input);
  }

  private ControlFlowGraph buildCFG(List<String> variables, String input) {
    return buildCFG(Map.of("var", variables), input);
  }

  private ControlFlowGraph buildCFG(Map<String, List<String>> sections, String input) {
    try {
      var tempFile = File.createTempFile("CFGTest-", ".pas");
      tempFile.deleteOnExit();

      StringBuilder content = new StringBuilder();
      content
          .append("unit Test;\n")
          .append("interface\n")
          .append("uses System.SysUtils;\n")
          .append("implementation\n")
          .append("procedure TestProc;\n");
      for (Entry<String, List<String>> section : sections.entrySet()) {
        if (!section.getKey().isEmpty()) {
          content.append(section.getKey()).append("\n");
        }
        for (String declaration : section.getValue()) {
          content.append("  ").append(declaration).append(";\n");
        }
      }
      content.append("begin\n").append(input).append("\nend;\n").append("end.");

      LOG.info("Test file:");
      LOG.info(content.toString());
      Files.write(tempFile.toPath(), content.toString().getBytes(StandardCharsets.UTF_8));

      DelphiFileConfig config = DelphiFileUtils.mockConfig();
      var file = DelphiFile.from(tempFile, config);

      Path standardLibraryPath = createStandardLibrary();
      SymbolTable symbolTable =
          SymbolTable.builder()
              .preprocessorFactory(new DelphiPreprocessorFactory(Platform.WINDOWS))
              .typeFactory(
                  new TypeFactoryImpl(
                      DelphiProperties.COMPILER_TOOLCHAIN_DEFAULT,
                      DelphiProperties.COMPILER_VERSION_DEFAULT))
              .standardLibraryPath(standardLibraryPath)
              .sourceFiles(List.of(file.getSourceCodeFile().toPath()))
              .build();

      FileUtils.deleteQuietly(standardLibraryPath.toFile());

      new SymbolAssociationVisitor()
          .visit(file.getAst(), new SymbolAssociationVisitor.Data(symbolTable));

      var statementList =
          file.getAst().findDescendantsOfType(RoutineImplementationNode.class).stream()
              .filter(impl -> impl.getRoutineBody() != null)
              .map(impl -> impl.getRoutineBody().getStatementBlock().getStatementList())
              .findFirst()
              .orElseThrow();

      var cfg = ControlFlowGraphFactory.create(statementList);
      LOG.info(ControlFlowGraphDebug.toString(cfg));

      return cfg;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void test(String input, GraphChecker checker) {
    test(Collections.emptyMap(), input, checker);
  }

  private void test(List<String> variables, String input, GraphChecker checker) {
    checker.check(buildCFG(variables, input));
  }

  private void test(Map<String, List<String>> sections, String input, GraphChecker checker) {
    checker.check(buildCFG(sections, input));
  }

  private static Path createStandardLibrary() {
    try {
      Path bds = Files.createTempDirectory("bds");

      var hook = new Thread(() -> FileUtils.deleteQuietly(bds.toFile()));
      Runtime.getRuntime().addShutdownHook(hook);

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
              + "type\n"
              + "  Exception = class\n"
              + "    constructor Create(Message: String);\n"
              + "  end;\n"
              + "  EAbort = class(Exception);\n"
              + "implementation\n"
              + "end.");

      return bds;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private Consumer<DelphiNode> binaryOpTest(BinaryOperator operator) {
    return node -> {
      assertThat(node).as("binary expression expected").isInstanceOf(BinaryExpressionNode.class);
      assertThat(((BinaryExpressionNode) node).getOperator())
          .as(operator + " operator expected")
          .isEqualTo(operator);
    };
  }

  private Consumer<DelphiNode> unaryOpTest(UnaryOperator operator) {
    return node -> {
      assertThat(node).as("unary expression expected").isInstanceOf(UnaryExpressionNode.class);
      assertThat(((UnaryExpressionNode) node).getOperator())
          .as(operator + " operator expected")
          .isEqualTo(operator);
    };
  }

  @Test
  void testEmptyCFG() {
    final ControlFlowGraph cfg = buildCFG("");
    checker().check(cfg);
    assertThat(cfg.getEntryBlock().getSuccessors()).as("entry is an exit").isEmpty();
  }

  @Test
  void testSimplestCFG() {
    final ControlFlowGraph cfg = buildCFG("Foo;");
    checker(block(element(NameReferenceNode.class, "Foo")).succeedsTo(0)).check(cfg);
    Block entry = cfg.getEntryBlock();
    assertThat(entry.getSuccessors()).as("1st block is not an exit").isNotEmpty();
    assertThat(entry.getSuccessors()).as("number of succeedsTo").hasSize(1);
    Block exit = entry.getSuccessors().iterator().next();
    assertThat(exit.getSuccessors()).as("2nd block is an exit").isEmpty();
  }

  @Test
  void testIfThen() {
    test(
        "if A then Foo;",
        checker(
            block(element(NameReferenceNode.class, "A"))
                .branchesTo(1, 0)
                .withTerminator(IfStatementNode.class),
            block(element(NameReferenceNode.class, "Foo")).succeedsTo(0)));
  }

  @Test
  void testIfThenElse() {
    test(
        "if A then Foo else Bar;",
        checker(
            block(element(NameReferenceNode.class, "A"))
                .branchesTo(2, 1)
                .withTerminator(IfStatementNode.class),
            block(element(NameReferenceNode.class, "Foo")).succeedsTo(0),
            block(element(NameReferenceNode.class, "Bar")).succeedsTo(0)));
  }

  @Test
  void testIfThenElseIf() {
    test(
        "if A then Foo else if B then Bar;",
        checker(
            block(element(NameReferenceNode.class, "A"))
                .branchesTo(3, 2)
                .withTerminator(IfStatementNode.class),
            block(element(NameReferenceNode.class, "Foo")).succeedsTo(0),
            block(element(NameReferenceNode.class, "B"))
                .branchesTo(1, 0)
                .withTerminator(IfStatementNode.class),
            block(element(NameReferenceNode.class, "Bar")).succeedsTo(0)));
  }

  @Test
  void testIfOr() {
    test(
        "if A or B then Foo;",
        checker(
            block(element(NameReferenceNode.class, "A"))
                .branchesTo(1, 2)
                .withTerminator(BinaryExpressionNode.class)
                .withTerminatorNodeCheck(binaryOpTest(BinaryOperator.OR)),
            block(element(NameReferenceNode.class, "B"))
                .branchesTo(1, 0)
                .withTerminator(IfStatementNode.class),
            block(element(NameReferenceNode.class, "Foo")).succeedsTo(0)));
  }

  @Test
  void testIfAnd() {
    test(
        "if A and B then Foo;",
        checker(
            block(element(NameReferenceNode.class, "A"))
                .branchesTo(2, 0)
                .withTerminator(BinaryExpressionNode.class)
                .withTerminatorNodeCheck(binaryOpTest(BinaryOperator.AND)),
            block(element(NameReferenceNode.class, "B"))
                .branchesTo(1, 0)
                .withTerminator(IfStatementNode.class),
            block(element(NameReferenceNode.class, "Foo")).succeedsTo(0)));
  }

  @Test
  void testLocalVarDeclaration() {
    test(
        "var Bar: TObject;",
        checker(block(element(NameDeclarationNode.class, "Bar")).succeedsTo(0)));
  }

  @Test
  void testLocalVarListDeclaration() {
    test(
        "var Foo,Bar: TObject;",
        checker(
            block(
                    element(NameDeclarationNode.class, "Foo"),
                    element(NameDeclarationNode.class, "Bar"))
                .succeedsTo(0)));
  }

  @Test
  void testLocalVarValueDeclaration() {
    test(
        "var Bar: TObject := nil;",
        checker(
            block(element(NilLiteralNode.class), element(NameDeclarationNode.class, "Bar"))
                .succeedsTo(0)));
  }

  @Test
  void testUntypedConstDeclaration() {
    test(
        "const Foo = 0;",
        checker(
            block(element(IntegerLiteralNode.class), element(NameDeclarationNode.class, "Foo"))
                .succeedsTo(0)));
  }

  @Test
  void testTypedConstDeclaration() {
    test(
        "const Foo: Integer = 0;",
        checker(
            block(element(IntegerLiteralNode.class), element(NameDeclarationNode.class, "Foo"))
                .succeedsTo(0)));
  }

  @Test
  void testCaseStatement1Arm() {
    test(
        "case Foo of Bar: Bar1; end;",
        checker(
            block(element(NameReferenceNode.class, "Bar1")).succeedsTo(0),
            block(element(NameReferenceNode.class, "Foo"), element(NameReferenceNode.class, "Bar"))
                .succeedsToCases(0, 2)
                .withTerminator(CaseStatementNode.class)));
  }

  @Test
  void testCaseStatement2Arm() {
    test(
        "case Foo of Bar: Bar1; Baz: Baz1; end;",
        checker(
            block(element(NameReferenceNode.class, "Bar1")).succeedsTo(0),
            block(element(NameReferenceNode.class, "Baz1")).succeedsTo(0),
            block(
                    element(NameReferenceNode.class, "Foo"),
                    element(NameReferenceNode.class, "Bar"),
                    element(NameReferenceNode.class, "Baz"))
                .withTerminator(CaseStatementNode.class)
                .succeedsToCases(0, 2, 3)));
  }

  @Test
  void testCaseStatementElse() {
    test(
        "case Foo of Bar: Bar1; Baz: Baz1; else Flarp; end;",
        checker(
            block(element(NameReferenceNode.class, "Bar1")).succeedsTo(0),
            block(element(NameReferenceNode.class, "Baz1")).succeedsTo(0),
            block(element(NameReferenceNode.class, "Flarp")).succeedsTo(0),
            block(
                    element(NameReferenceNode.class, "Foo"),
                    element(NameReferenceNode.class, "Bar"),
                    element(NameReferenceNode.class, "Baz"))
                .withTerminator(CaseStatementNode.class)
                .succeedsToCases(2, 3, 4)));
  }

  @Test
  void testRepeat() {
    test(
        "repeat Bar; until Foo;",
        checker(
            block(element(NameReferenceNode.class, "Bar")).succeedsTo(1),
            block(element(NameReferenceNode.class, "Foo"))
                .branchesTo(0, 2)
                .withTerminator(RepeatStatementNode.class)));
  }

  @Test
  void testRepeatContinue() {
    test(
        "repeat Continue; Bar; until Foo;",
        checker(
            terminator(StatementTerminator.CONTINUE).jumpsTo(1, 2),
            block(element(NameReferenceNode.class, "Bar")).succeedsTo(1),
            block(element(NameReferenceNode.class, "Foo"))
                .branchesTo(0, 3)
                .withTerminator(RepeatStatementNode.class)));
  }

  @Test
  void testRepeatBreak() {
    test(
        "repeat Bar; break; until Foo;",
        checker(
            block(element(NameReferenceNode.class, "Bar"))
                .jumpsTo(0, 1)
                .withTerminator(StatementTerminator.BREAK),
            block(element(NameReferenceNode.class, "Foo"))
                .branchesTo(0, 2)
                .withTerminator(RepeatStatementNode.class)));
  }

  @Test
  void testWhile() {
    test(
        "while Foo do Bar;",
        checker(
            block(element(NameReferenceNode.class, "Foo"))
                .branchesTo(1, 0)
                .withTerminator(WhileStatementNode.class),
            block(element(NameReferenceNode.class, "Bar")).succeedsTo(2)));
  }

  @Test
  void testWhileContinue() {
    test(
        "while Foo do begin Continue; Bar; end;",
        checker(
            block(element(NameReferenceNode.class, "Foo"))
                .branchesTo(2, 0)
                .withTerminator(WhileStatementNode.class),
            terminator(StatementTerminator.CONTINUE).jumpsTo(3, 1),
            block(element(NameReferenceNode.class, "Bar")).succeedsTo(3)));
  }

  @Test
  void testWhileBreak() {
    test(
        "while Foo do begin Bar; break; end;",
        checker(
            block(element(NameReferenceNode.class, "Foo"))
                .branchesTo(1, 0)
                .withTerminator(WhileStatementNode.class),
            block(element(NameReferenceNode.class, "Bar"))
                .jumpsTo(0, 2)
                .withTerminator(StatementTerminator.BREAK)));
  }

  @Test
  void testForToVarDecl() {
    test(
        "for var I := Foo to Bar do Baz;",
        checker(
            block(element(NameReferenceNode.class, "Foo")).succeedsTo(3),
            block(element(NameReferenceNode.class, "Bar")).succeedsTo(1),
            block(element(NameReferenceNode.class, "Baz")).succeedsTo(1),
            block(element(NameDeclarationNode.class, "I"))
                .branchesTo(2, 0)
                .withTerminator(ForToStatementNode.class)));
  }

  @Test
  void testForToVarRef() {
    test(
        "for I := Foo to Bar do Baz;",
        checker(
            block(element(NameReferenceNode.class, "Foo")).succeedsTo(3),
            block(element(NameReferenceNode.class, "Bar")).succeedsTo(1),
            block(element(NameReferenceNode.class, "Baz")).succeedsTo(1),
            block(element(NameReferenceNode.class, "I"))
                .branchesTo(2, 0)
                .withTerminator(ForToStatementNode.class)));
  }

  @Test
  void testForToBreak() {
    test(
        "for I := Foo to Bar do Break;",
        checker(
            block(element(NameReferenceNode.class, "Foo")).succeedsTo(3),
            block(element(NameReferenceNode.class, "Bar")).succeedsTo(1),
            terminator(StatementTerminator.BREAK).jumpsTo(0, 1),
            block(element(NameReferenceNode.class, "I"))
                .branchesTo(2, 0)
                .withTerminator(ForToStatementNode.class)));
  }

  @Test
  void testForToConditionalBreak() {
    test(
        "for I := Foo to Bar do if I = 1 then Break;",
        checker(
            block(element(NameReferenceNode.class, "Foo")).succeedsTo(4),
            block(element(NameReferenceNode.class, "Bar")).succeedsTo(1),
            block(
                    element(NameReferenceNode.class, "I"),
                    element(IntegerLiteralNode.class, "1"),
                    element(BinaryExpressionNode.class)
                        .withCheck(binaryOpTest(BinaryOperator.EQUAL)))
                .branchesTo(2, 1)
                .withTerminator(IfStatementNode.class),
            terminator(StatementTerminator.BREAK).jumpsTo(0, 1),
            block(element(NameReferenceNode.class, "I"))
                .branchesTo(3, 0)
                .withTerminator(ForToStatementNode.class)));
  }

  @Test
  void testForToContinue() {
    test(
        "for I := Foo to Bar do Continue;",
        checker(
            block(element(NameReferenceNode.class, "Foo")).succeedsTo(3),
            block(element(NameReferenceNode.class, "Bar")).succeedsTo(1),
            terminator(StatementTerminator.CONTINUE).jumpsTo(1, 1),
            block(element(NameReferenceNode.class, "I"))
                .branchesTo(2, 0)
                .withTerminator(ForToStatementNode.class)));
  }

  @Test
  void testForToConditionalContinue() {
    test(
        "for I := Foo to Bar do if I = 1 then Continue;",
        checker(
            block(element(NameReferenceNode.class, "Foo")).succeedsTo(4),
            block(element(NameReferenceNode.class, "Bar")).succeedsTo(1),
            block(
                    element(NameReferenceNode.class, "I"),
                    element(IntegerLiteralNode.class, "1"),
                    element(BinaryExpressionNode.class)
                        .withCheck(binaryOpTest(BinaryOperator.EQUAL)))
                .branchesTo(2, 1)
                .withTerminator(IfStatementNode.class),
            terminator(StatementTerminator.CONTINUE).jumpsTo(1, 1),
            block(element(NameReferenceNode.class, "I"))
                .branchesTo(3, 0)
                .withTerminator(ForToStatementNode.class)));
  }

  @Test
  void testForInVarDecl() {
    test(
        "for var I in List do Foo;",
        checker(
            block(element(NameReferenceNode.class, "List")).succeedsTo(1),
            block(element(NameReferenceNode.class, "Foo")).succeedsTo(1),
            block(element(NameDeclarationNode.class, "I"))
                .branchesTo(2, 0)
                .withTerminator(ForInStatementNode.class)));
  }

  @Test
  void testForInVarRef() {
    test(
        "for I in List do Foo;",
        checker(
            block(element(NameReferenceNode.class, "List")).succeedsTo(1),
            block(element(NameReferenceNode.class, "Foo")).succeedsTo(1),
            block(element(NameReferenceNode.class, "I"))
                .branchesTo(2, 0)
                .withTerminator(ForInStatementNode.class)));
  }

  @Test
  void testForInBreak() {
    test(
        "for I in List do Break;",
        checker(
            block(element(NameReferenceNode.class, "List")).succeedsTo(1),
            terminator(StatementTerminator.BREAK).jumpsTo(0, 1),
            block(element(NameReferenceNode.class, "I"))
                .branchesTo(2, 0)
                .withTerminator(ForInStatementNode.class)));
  }

  @Test
  void testForInConditionalBreak() {
    test(
        "for I in List do if I = 1 then Break;",
        checker(
            block(element(NameReferenceNode.class, "List")).succeedsTo(1),
            block(
                    element(NameReferenceNode.class, "I"),
                    element(IntegerLiteralNode.class, "1"),
                    element(BinaryExpressionNode.class)
                        .withCheck(binaryOpTest(BinaryOperator.EQUAL)))
                .branchesTo(2, 1)
                .withTerminator(IfStatementNode.class),
            terminator(StatementTerminator.BREAK).jumpsTo(0, 1),
            block(element(NameReferenceNode.class, "I"))
                .branchesTo(3, 0)
                .withTerminator(ForInStatementNode.class)));
  }

  @Test
  void testForInContinue() {
    test(
        "for I in List do Continue;",
        checker(
            block(element(NameReferenceNode.class, "List")).succeedsTo(1),
            terminator(StatementTerminator.CONTINUE).jumpsTo(1, 1),
            block(element(NameReferenceNode.class, "I"))
                .branchesTo(2, 0)
                .withTerminator(ForInStatementNode.class)));
  }

  @Test
  void testForInConditionalContinue() {
    test(
        "for I in List do if I = 1 then Continue;",
        checker(
            block(element(NameReferenceNode.class, "List")).succeedsTo(1),
            block(
                    element(NameReferenceNode.class, "I"),
                    element(IntegerLiteralNode.class, "1"),
                    element(BinaryExpressionNode.class)
                        .withCheck(binaryOpTest(BinaryOperator.EQUAL)))
                .branchesTo(2, 1)
                .withTerminator(IfStatementNode.class),
            terminator(StatementTerminator.CONTINUE).jumpsTo(1, 1),
            block(element(NameReferenceNode.class, "I"))
                .branchesTo(3, 0)
                .withTerminator(ForInStatementNode.class)));
  }

  @Test
  void testWith() {
    test(
        "with TObject.Create do Foo;",
        checker(
            block(element(NameReferenceNode.class, "TObject.Create")).succeedsTo(1),
            block(element(NameReferenceNode.class, "Foo")).succeedsTo(0)));
  }

  @Test
  void testTryFinallyNoRaise() {
    test(
        Map.of("", List.of("procedure Foo; begin end")),
        "try Foo finally Bar end;",
        checker(
            block(element(TryStatementNode.class)).succeedsTo(2),
            block(element(NameReferenceNode.class, "Foo")).succeedsToWithExceptions(1, 1),
            block(element(NameReferenceNode.class, "Bar")).succeedsToWithExit(0, 0)));
  }

  @Test
  void testTryFinallyRaise() {
    test(
        "try raise E finally Bar end;",
        checker(
            block(element(TryStatementNode.class)).succeedsTo(2),
            block(element(NameReferenceNode.class, "E"))
                .withTerminator(RaiseStatementNode.class, TerminatorKind.RAISE)
                .jumpsTo(1, 1),
            block(element(NameReferenceNode.class, "Bar")).succeedsToWithExit(0, 0)));
  }

  @Test
  void testTryFinallyExit() {
    test(
        "try Exit finally Bar end;",
        checker(
            block(element(TryStatementNode.class)).succeedsTo(2),
            terminator(StatementTerminator.EXIT).jumpsTo(1, 1),
            block(element(NameReferenceNode.class, "Bar")).succeedsToWithExit(0, 0)));
  }

  @Test
  void testTryFinallyBreak() {
    test(
        "while True do try Break finally Bar end;",
        checker(
            block(element(NameReferenceNode.class, "True"))
                .branchesTo(3, 0)
                .withTerminator(WhileStatementNode.class),
            block(element(TryStatementNode.class)).succeedsTo(2),
            terminator(StatementTerminator.BREAK).jumpsTo(1, 1),
            block(element(NameReferenceNode.class, "Bar")).succeedsToWithExit(4, 0)));
  }

  @Test
  void testTryFinallyContinue() {
    test(
        "while True do try Continue finally Bar end;",
        checker(
            block(element(NameReferenceNode.class, "True"))
                .branchesTo(3, 0)
                .withTerminator(WhileStatementNode.class),
            block(element(TryStatementNode.class)).succeedsTo(2),
            terminator(StatementTerminator.CONTINUE).jumpsTo(1, 1),
            block(element(NameReferenceNode.class, "Bar")).succeedsToWithExit(4, 0)));
  }

  @Test
  void testTryFinallyHalt() {
    test(
        "try Halt finally Bar end;",
        checker(
            block(element(TryStatementNode.class)).succeedsTo(2),
            terminator(StatementTerminator.HALT).isSink(),
            block(element(NameReferenceNode.class, "Bar")).succeedsToWithExit(0, 0)));
  }

  @Test
  void testTryContinueFinallyInLoop() {
    test(
        "for var A := 1 to 4 do begin"
            + "  try"
            + "    Continue;"
            + "  finally"
            + "    Bar;"
            + "  end;"
            + "end;",
        checker(
            block(element(IntegerLiteralNode.class)).succeedsTo(5),
            block(element(IntegerLiteralNode.class)).succeedsTo(1),
            block(element(TryStatementNode.class)).succeedsTo(3),
            terminator(StatementTerminator.CONTINUE).jumpsTo(2, 2),
            block(element(NameReferenceNode.class, "Bar")).succeedsToWithExit(1, 0),
            block(element(NameDeclarationNode.class, "A"))
                .branchesTo(4, 0)
                .withTerminator(ForToStatementNode.class)));
  }

  @Test
  void testTryBreakFinallyInLoop() {
    test(
        "for var A := 1 to 4 do begin"
            + "  try"
            + "    Break;"
            + "  finally"
            + "    Bar;"
            + "  end;"
            + "end;",
        checker(
            block(element(IntegerLiteralNode.class)).succeedsTo(5),
            block(element(IntegerLiteralNode.class)).succeedsTo(1),
            block(element(TryStatementNode.class)).succeedsTo(3),
            terminator(StatementTerminator.BREAK).jumpsTo(2, 2),
            block(element(NameReferenceNode.class, "Bar")).succeedsToWithExit(1, 0),
            block(element(NameDeclarationNode.class, "A"))
                .branchesTo(4, 0)
                .withTerminator(ForToStatementNode.class)));
  }

  @Test
  void testTryExitFinallyInLoop() {
    test(
        "for var A := 1 to 4 do begin"
            + "  try"
            + "    Exit;"
            + "  finally"
            + "    Bar;"
            + "  end;"
            + "end;",
        checker(
            block(element(IntegerLiteralNode.class)).succeedsTo(5),
            block(element(IntegerLiteralNode.class)).succeedsTo(1),
            block(element(TryStatementNode.class)).succeedsTo(3),
            terminator(StatementTerminator.EXIT).jumpsTo(2, 2),
            block(element(NameReferenceNode.class, "Bar")).succeedsToWithExit(1, 0),
            block(element(NameDeclarationNode.class, "A"))
                .branchesTo(4, 0)
                .withTerminator(ForToStatementNode.class)));
  }

  @Test
  void testTryHaltFinallyInLoop() {
    test(
        "for var A := 1 to 4 do begin"
            + "  try"
            + "    Halt;"
            + "  finally"
            + "    Bar;"
            + "  end;"
            + "end;",
        checker(
            block(element(IntegerLiteralNode.class)).succeedsTo(5),
            block(element(IntegerLiteralNode.class)).succeedsTo(1),
            block(element(TryStatementNode.class)).succeedsTo(3),
            terminator(StatementTerminator.HALT).isSink(),
            block(element(NameReferenceNode.class, "Bar")).succeedsToWithExit(1, 0),
            block(element(NameDeclarationNode.class, "A"))
                .branchesTo(4, 0)
                .withTerminator(ForToStatementNode.class)));
  }

  @Test
  void testTryExceptNoRaise() {
    test(
        Map.of("", List.of("procedure Foo; begin end", "procedure Bar; begin end")),
        "try Foo finally Bar end;",
        checker(
            block(element(TryStatementNode.class)).succeedsTo(2),
            block(element(NameReferenceNode.class, "Foo")).succeedsToWithExceptions(1, 1),
            block(element(NameReferenceNode.class, "Bar")).succeedsToWithExit(0, 0)));
  }

  @Test
  void testTryBareExceptNoRaise() {
    test(
        Map.of("", List.of("procedure Foo; begin end")),
        "try Foo; except Bar; end;",
        checker(
            block(element(TryStatementNode.class)).succeedsTo(2),
            block(element(NameReferenceNode.class, "Foo")).succeedsToWithExceptions(0, 1),
            block(element(NameReferenceNode.class, "Bar")).succeedsTo(0)));
  }

  @Test
  void testTryExceptOnNoRaise() {
    test(
        Map.of("", List.of("procedure Foo; begin end")),
        "try\n"
            + "  Foo;\n"
            + "except\n"
            + "  on E: EAbort do Bar;\n"
            + "  on E: Exception do Baz;\n"
            + "end;",
        checker(
            block(element(TryStatementNode.class)).succeedsTo(3),
            block(element(NameReferenceNode.class, "Foo")).succeedsToWithExceptions(0, 0, 1, 2),
            block(element(NameDeclarationNode.class, "E"), element(NameReferenceNode.class, "Bar"))
                .succeedsTo(0),
            block(element(NameDeclarationNode.class, "E"), element(NameReferenceNode.class, "Baz"))
                .succeedsTo(0)));
  }

  @Test
  void testTryExceptRaise1stCatch() {
    test(
        Map.of(
            "",
            List.of(
                "procedure Foo; begin end",
                "procedure Bar; begin end",
                "procedure Baz; begin end")),
        "try\n"
            + "  raise EAbort.Create('');\n"
            + "except\n"
            + "  on E: EAbort do Bar;\n"
            + "  on E: Exception do Baz;\n"
            + "end;",
        checker(
            block(element(TryStatementNode.class)).succeedsTo(4),
            block(element(NameReferenceNode.class, "EAbort.Create"))
                .succeedsToWithExceptions(3, 0, 1, 2),
            terminator(RaiseStatementNode.class, TerminatorKind.RAISE).jumpsTo(2, 0),
            block(element(NameDeclarationNode.class, "E"), element(NameReferenceNode.class, "Bar"))
                .succeedsTo(0),
            block(element(NameDeclarationNode.class, "E"), element(NameReferenceNode.class, "Baz"))
                .succeedsTo(0)));
  }

  @Test
  void testTryExceptRaise2ndCatch() {
    test(
        Map.of(
            "",
            List.of(
                "procedure Foo; begin end",
                "procedure Bar; begin end",
                "procedure Baz; begin end")),
        "try\n"
            + "  raise Exception.Create('');\n"
            + "except\n"
            + "  on E: EAbort do Bar;\n"
            + "  on E: Exception do Baz;\n"
            + "end;",
        checker(
            block(element(TryStatementNode.class)).succeedsTo(4),
            block(element(NameReferenceNode.class, "Exception.Create"))
                .succeedsToWithExceptions(3, 0, 1, 2),
            terminator(RaiseStatementNode.class, TerminatorKind.RAISE).jumpsTo(1, 0),
            block(element(NameDeclarationNode.class, "E"), element(NameReferenceNode.class, "Bar"))
                .succeedsTo(0),
            block(element(NameDeclarationNode.class, "E"), element(NameReferenceNode.class, "Baz"))
                .succeedsTo(0)));
  }

  @Test
  void testTryBareExceptRaise() {
    test(
        Map.of("", List.of("procedure Bar; begin end")),
        "try raise Exception.Create(''); except Bar; end;",
        checker(
            block(element(TryStatementNode.class)).succeedsTo(3),
            block(element(NameReferenceNode.class, "Exception.Create"))
                .succeedsToWithExceptions(2, 1),
            terminator(RaiseStatementNode.class, TerminatorKind.RAISE).jumpsTo(1, 0),
            block(element(NameReferenceNode.class, "Bar")).succeedsTo(0)));
  }

  @Test
  void testTryExceptElseRaise() {
    test(
        Map.of(
            "",
            List.of(
                "procedure Foo; begin end",
                "procedure Bar; begin end",
                "procedure Baz; begin end")),
        "try\n"
            + "  raise TObject.Create;\n"
            + "except\n"
            + "  on E: EAbort do Bar;\n"
            + "  on E: Exception do Baz;\n"
            + "else\n"
            + "  Flarp;\n"
            + "end;",
        checker(
            block(element(TryStatementNode.class)).succeedsTo(5),
            block(element(NameReferenceNode.class, "TObject.Create"))
                .succeedsToWithExceptions(4, 1, 2, 3),
            terminator(RaiseStatementNode.class, TerminatorKind.RAISE).jumpsTo(1, 0),
            block(element(NameDeclarationNode.class, "E"), element(NameReferenceNode.class, "Bar"))
                .succeedsTo(0),
            block(element(NameDeclarationNode.class, "E"), element(NameReferenceNode.class, "Baz"))
                .succeedsTo(0),
            block(element(NameReferenceNode.class, "Flarp")).succeedsTo(0)));
  }

  @Test
  void testNestedTryExceptFinally() {
    test(
        Map.of(
            "",
            List.of(
                "procedure Foo; begin end",
                "procedure Bar; begin end",
                "procedure Baz; begin end")),
        "try\n"
            + "  try\n"
            + "    Foo\n"
            + "  except\n"
            + "    on E: Exception do Bar\n"
            + "  end\n"
            + "finally\n"
            + "  Baz\n"
            + "end;",
        checker(
            block(element(TryStatementNode.class)).succeedsTo(4),
            block(element(TryStatementNode.class)).succeedsTo(3),
            block(element(NameReferenceNode.class, "Foo")).succeedsToWithExceptions(1, 1, 2),
            block(element(NameDeclarationNode.class, "E"), element(NameReferenceNode.class, "Bar"))
                .succeedsToWithExceptions(1, 1),
            block(element(NameReferenceNode.class, "Baz")).succeedsToWithExit(0, 0)));
  }

  @Test
  void testNestedTryFinallyExcept() {
    test(
        Map.of(
            "",
            List.of(
                "procedure Foo; begin end",
                "procedure Bar; begin end",
                "procedure Baz; begin end")),
        "try try Foo finally Bar end except on E: Exception do Baz end;",
        checker(
            block(element(TryStatementNode.class)).succeedsTo(4),
            block(element(TryStatementNode.class)).succeedsTo(3),
            block(element(NameReferenceNode.class, "Foo")).succeedsToWithExceptions(2, 2),
            block(element(NameReferenceNode.class, "Bar")).succeedsToWithExceptions(0, 0, 1),
            block(element(NameDeclarationNode.class, "E"), element(NameReferenceNode.class, "Baz"))
                .succeedsTo(0)));
  }

  @Test
  void testTryBareExcept() {
    test(
        Map.of("", List.of("procedure Foo; begin end")),
        "try raise Exception.Create('') except Foo end;",
        checker(
            block(element(TryStatementNode.class)).succeedsTo(3),
            block(element(NameReferenceNode.class, "Exception.Create"))
                .succeedsToWithExceptions(2, 1),
            terminator(RaiseStatementNode.class, TerminatorKind.RAISE).jumpsTo(1, 0),
            block(element(NameReferenceNode.class, "Foo")).succeedsTo(0)));
  }

  @Test
  void testTryBareExceptReRaise() {
    test(
        Map.of("", List.of("procedure Foo; begin end")),
        "try Foo except raise; end;",
        checker(
            block(element(TryStatementNode.class)).succeedsTo(2),
            block(element(NameReferenceNode.class, "Foo")).succeedsToWithExceptions(0, 1),
            block(element(RaiseStatementNode.class)).succeedsToWithExceptions(0)));
  }

  @Test
  void testTryExceptReRaise() {
    test(
        Map.of("", List.of("procedure Foo; begin end")),
        "try Foo except on E: Exception do raise; end;",
        checker(
            block(element(TryStatementNode.class)).succeedsTo(2),
            block(element(NameReferenceNode.class, "Foo")).succeedsToWithExceptions(0, 0, 1),
            block(element(NameDeclarationNode.class, "E"), element(RaiseStatementNode.class))
                .succeedsToWithExceptions(0)));
  }

  @Test
  void testCompoundStatement() {
    test(
        "begin Foo; end; begin Bar; end;",
        checker(
            block(element(NameReferenceNode.class, "Foo"), element(NameReferenceNode.class, "Bar"))
                .succeedsTo(0)));
  }

  @Test
  void testAssignmentAnd() {
    test(
        List.of("Foo: Boolean", "A: Boolean", "B: Boolean"),
        "Foo := A and B;",
        checker(
            block(element(NameReferenceNode.class, "A"))
                .branchesTo(2, 1)
                .withTerminator(BinaryExpressionNode.class),
            block(element(NameReferenceNode.class, "B")).succeedsTo(1),
            block(element(NameReferenceNode.class, "Foo")).succeedsTo(0)));
  }

  @Test
  void testAssignmentPlus() {
    test(
        List.of("Foo: Integer", "A: Integer", "B: Integer"),
        "Foo := A + B;",
        checker(
            block(
                    element(NameReferenceNode.class, "A"),
                    element(NameReferenceNode.class, "B"),
                    element(BinaryExpressionNode.class),
                    element(NameReferenceNode.class, "Foo"))
                .succeedsTo(0)));
  }

  @Test
  void testAssignmentMultipleBinary() {
    test(
        List.of("Foo: Integer", "A: Integer", "B: Integer", "C: Integer", "D: Integer"),
        "Foo := A + B / C * D;",
        checker(
            block(
                    element(NameReferenceNode.class, "A"),
                    element(NameReferenceNode.class, "B"),
                    element(NameReferenceNode.class, "C"),
                    element(BinaryExpressionNode.class)
                        .withCheck(binaryOpTest(BinaryOperator.DIVIDE)),
                    element(NameReferenceNode.class, "D"),
                    element(BinaryExpressionNode.class)
                        .withCheck(binaryOpTest(BinaryOperator.MULTIPLY)),
                    element(BinaryExpressionNode.class).withCheck(binaryOpTest(BinaryOperator.ADD)),
                    element(NameReferenceNode.class, "Foo"))
                .succeedsTo(0)));
  }

  @Test
  void testExitStatement() {
    test(
        List.of("Foo: TObject"),
        "if Foo = nil then Exit;",
        checker(
            block(
                    element(NameReferenceNode.class, "Foo"),
                    element(NilLiteralNode.class),
                    element(BinaryExpressionNode.class)
                        .withCheck(binaryOpTest(BinaryOperator.EQUAL)))
                .branchesTo(1, 0)
                .withTerminator(IfStatementNode.class),
            terminator(StatementTerminator.EXIT).jumpsTo(0, 0)));
  }

  @Test
  void testExitValueStatement() {
    test(
        List.of("Foo: TObject", "Bar: TObject"),
        "if Foo = nil then Exit(Bar);",
        checker(
            block(
                    element(NameReferenceNode.class, "Foo"),
                    element(NilLiteralNode.class),
                    element(BinaryExpressionNode.class)
                        .withCheck(binaryOpTest(BinaryOperator.EQUAL)))
                .branchesTo(1, 0)
                .withTerminator(IfStatementNode.class),
            block(element(NameReferenceNode.class, "Bar"))
                .withTerminator(StatementTerminator.EXIT)
                .jumpsTo(0, 0)));
  }

  @Test
  void testExitCascadedOr() {
    test(
        List.of("A, B, C: Boolean"),
        "Exit(A or B or C);",
        checker(
            block(element(NameReferenceNode.class, "A"))
                .branchesTo(3, 4)
                .withTerminator(BinaryExpressionNode.class)
                .withTerminatorNodeCheck(binaryOpTest(BinaryOperator.OR)),
            block(element(NameReferenceNode.class, "B")).succeedsTo(3),
            terminator(BinaryExpressionNode.class)
                .withTerminatorNodeCheck(binaryOpTest(BinaryOperator.OR))
                .branchesTo(1, 2),
            block(element(NameReferenceNode.class, "C")).succeedsTo(1),
            terminator(StatementTerminator.EXIT).jumpsTo(0, 0)));
  }

  @Test
  void testExitCascadedAnd() {
    test(
        List.of("A, B, C: Boolean"),
        "Exit(A and B and C);",
        checker(
            block(element(NameReferenceNode.class, "A"))
                .withTerminator(BinaryExpressionNode.class)
                .withTerminatorNodeCheck(binaryOpTest(BinaryOperator.AND))
                .branchesTo(4, 3),
            block(element(NameReferenceNode.class, "B")).succeedsTo(3),
            terminator(BinaryExpressionNode.class)
                .withTerminatorNodeCheck(binaryOpTest(BinaryOperator.AND))
                .branchesTo(2, 1),
            block(element(NameReferenceNode.class, "C")).succeedsTo(1),
            terminator(StatementTerminator.EXIT).jumpsTo(0, 0)));
  }

  @Test
  void testExitComplexBoolean() {
    test(
        List.of("Bool, A, B: Boolean"),
        "Exit((not Bool and A) or (Bool and B));",
        checker(
            block(
                    element(NameReferenceNode.class, "Bool"),
                    element(UnaryExpressionNode.class).withCheck(unaryOpTest(UnaryOperator.NOT)))
                .branchesTo(5, 4)
                .withTerminator(BinaryExpressionNode.class)
                .withTerminatorNodeCheck(binaryOpTest(BinaryOperator.AND)),
            block(element(NameReferenceNode.class, "A")).succeedsTo(4),
            terminator(BinaryExpressionNode.class)
                .withTerminatorNodeCheck(binaryOpTest(BinaryOperator.OR))
                .branchesTo(1, 3),
            block(element(NameReferenceNode.class, "Bool"))
                .withTerminator(BinaryExpressionNode.class)
                .withTerminatorNodeCheck(binaryOpTest(BinaryOperator.AND))
                .branchesTo(2, 1),
            block(element(NameReferenceNode.class, "B")).succeedsTo(1),
            terminator(StatementTerminator.EXIT).jumpsTo(0, 0)));
  }

  @Test
  void testBareInherited() {
    test("inherited;", checker(block(element(CommonDelphiNode.class, "inherited")).succeedsTo(0)));
  }

  @Test
  void testNamedInheritedNoArgs() {
    test(
        "inherited Foo;",
        checker(
            block(
                    element(CommonDelphiNode.class, "inherited"),
                    element(NameReferenceNode.class, "Foo"))
                .succeedsTo(0)));
  }

  @Test
  void testInherited() {
    test(
        "inherited Foo(A, B, C);",
        checker(
            block(
                    element(CommonDelphiNode.class, "inherited"),
                    element(NameReferenceNode.class, "Foo"),
                    element(NameReferenceNode.class, "A"),
                    element(NameReferenceNode.class, "B"),
                    element(NameReferenceNode.class, "C"))
                .succeedsTo(0)));
  }

  @Test
  void testSucceedingLabelGoto() {
    test(
        Map.of("label", List.of("A")),
        "if B then goto A; A:",
        checker(
            block(element(NameReferenceNode.class, "B"))
                .branchesTo(1, 0)
                .withTerminator(IfStatementNode.class),
            terminator(GotoStatementNode.class, TerminatorKind.GOTO).jumpsTo(0, 0)));
  }

  @Test
  void testPrecedingLabelGoto() {
    test(
        Map.of("label", List.of("A")),
        "A: if B then goto A;",
        checker(
            block(element(NameReferenceNode.class, "B"))
                .branchesTo(1, 0)
                .withTerminator(IfStatementNode.class),
            block(element(NameReferenceNode.class, "A"))
                .withTerminator(GotoStatementNode.class, TerminatorKind.GOTO)
                .jumpsTo(2, 0)));
  }

  @Test
  void testLabelSeparatesBlock() {
    test(
        Map.of("label", List.of("A")),
        "Foo; A: Bar; if B then goto A;",
        checker(
            block(element(NameReferenceNode.class, "Foo")).succeedsTo(2),
            block(element(NameReferenceNode.class, "Bar"), element(NameReferenceNode.class, "B"))
                .branchesTo(1, 0)
                .withTerminator(IfStatementNode.class),
            block(element(NameReferenceNode.class, "A"))
                .withTerminator(GotoStatementNode.class, TerminatorKind.GOTO)
                .jumpsTo(2, 0)));
  }
}
