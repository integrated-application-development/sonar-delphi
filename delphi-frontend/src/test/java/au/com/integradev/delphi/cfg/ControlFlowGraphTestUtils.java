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
package au.com.integradev.delphi.cfg;

import au.com.integradev.delphi.DelphiProperties;
import au.com.integradev.delphi.antlr.ast.visitors.SymbolAssociationVisitor;
import au.com.integradev.delphi.cfg.api.ControlFlowGraph;
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
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.communitydelphi.api.ast.RoutineImplementationNode;

public class ControlFlowGraphTestUtils {
  private static final Logger LOG = LoggerFactory.getLogger(ControlFlowGraphTestUtils.class);

  public static ControlFlowGraph buildCfg(String... input) {
    return buildCfg(Collections.emptyMap(), input);
  }

  public static ControlFlowGraph buildCfg(List<String> variables, String... input) {
    return buildCfg(Map.of("var", variables), input);
  }

  public static ControlFlowGraph buildCfg(Map<String, List<String>> sections, String... input) {
    StringBuilder content = new StringBuilder();
    content
        .append("unit Test;\n")
        .append("interface\n")
        .append("uses System.SysUtils;\n")
        .append("implementation\n")
        .append("function TestFunc: Integer;\n");
    for (Map.Entry<String, List<String>> section : sections.entrySet()) {
      if (!section.getKey().isEmpty()) {
        content.append(section.getKey()).append("\n");
      }
      for (String declaration : section.getValue()) {
        content.append("  ").append(declaration).append(";\n");
      }
    }
    content.append("begin\n");
    for (String line : input) {
      content.append("  ").append(line).append("\n");
    }
    content.append("end;\n").append("end.");
    return buildCfgFromUnit(content.toString(), "TestFunc");
  }

  public static ControlFlowGraph buildCfgFromUnit(String unit, String routineName) {
    try {
      var tempFile = File.createTempFile("CfgTest-", ".pas");
      tempFile.deleteOnExit();

      LOG.info("Test file:");
      LOG.info(unit);
      Files.write(tempFile.toPath(), unit.getBytes(StandardCharsets.UTF_8));

      DelphiFileConfig config = DelphiFileUtils.mockConfig();
      var file = DelphiFile.from(tempFile, config);

      Path standardLibraryPath = createStandardLibrary();
      SymbolTable symbolTable =
          SymbolTable.builder()
              .preprocessorFactory(
                  new DelphiPreprocessorFactory(
                      DelphiProperties.COMPILER_VERSION_DEFAULT, Platform.WINDOWS))
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

      var routineImplementation =
          file.getAst().findDescendantsOfType(RoutineImplementationNode.class).stream()
              .filter(
                  impl ->
                      Objects.equals(impl.getNameReferenceNode().fullyQualifiedName(), routineName))
              .findFirst()
              .orElseThrow();

      var cfg = ControlFlowGraphFactory.create(routineImplementation);
      LOG.info(ControlFlowGraphDebug.toString(cfg));

      return cfg;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
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
}
