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
package au.com.integradev.delphi.preprocessor;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import au.com.integradev.delphi.antlr.DelphiFileStream;
import au.com.integradev.delphi.antlr.DelphiLexer;
import au.com.integradev.delphi.antlr.DelphiParser;
import au.com.integradev.delphi.antlr.ast.DelphiTreeAdaptor;
import au.com.integradev.delphi.compiler.Platform;
import au.com.integradev.delphi.file.DelphiFile;
import au.com.integradev.delphi.file.DelphiFileConfig;
import au.com.integradev.delphi.preprocessor.search.SearchPath;
import au.com.integradev.delphi.type.factory.TypeFactory;
import au.com.integradev.delphi.utils.DelphiUtils;
import au.com.integradev.delphi.utils.files.DelphiFileUtils;
import au.com.integradev.delphi.utils.types.TypeFactoryUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;
import org.antlr.runtime.BufferedTokenStream;
import org.junit.jupiter.api.Test;

class DelphiPreprocessorTest {
  private static final String BASE_DIR = "/au/com/integradev/delphi/preprocessor/";

  @Test
  void testUndefineDirectives() {
    executeWithDefines("UndefineDirectives.pas", "FAIL_IF_DEFINED");
  }

  @Test
  void testNestedDirectives() {
    executeWithDefines("NestedDirectives.pas", "FAIL_IF_DEFINED", "NESTED");
  }

  @Test
  void testSizeOfIntrinsic() {
    execute("SizeOfIntrinsic.pas");
  }

  @Test
  void testSameFolderIncludeDirectives() {
    execute("includeTest/SameFolder.pas");
  }

  @Test
  void testBacktrackIncludeDirectives() {
    execute("includeTest/Backtrack.pas");
  }

  @Test
  void testSearchPathIncludeDirectives() {
    executeWithSearchPath("includeTest/SearchPath.pas", "includes");
  }

  @Test
  void testNestedIncludeDirectives() {
    executeWithSearchPath("includeTest/NestedSearchPath.pas", "includes", "nestedIncludes");
  }

  @Test
  void testSameNameBacktrackIncludeDirectives() {
    execute("includeTest/SameNameBacktrack.pas");
  }

  @Test
  void testBadIncludeTokenShouldNotThrowException() {
    execute("includeTest/BadIncludeToken.pas");
  }

  @Test
  void testNonexistentIncludeShouldNotThrowException() {
    executeWithSearchPath("includeTest/IncludeDoesNotExist.pas", "includes");
  }

  @Test
  void testSelfReferencingIncludeShouldNotThrowException() {
    execute("includeTest/SelfReferencingInclude.pas");
  }

  @Test
  void testCallingProcessTwiceShouldThrowException() throws Exception {
    String filePath =
        DelphiUtils.getResource(BASE_DIR + "includeTest/SameNameBacktrack.pas").getAbsolutePath();
    DelphiFileConfig config = DelphiFileUtils.mockConfig();
    DelphiFileStream fileStream = new DelphiFileStream(filePath, config.getEncoding());

    DelphiLexer lexer = new DelphiLexer(fileStream);
    DelphiPreprocessor preprocessor = new DelphiPreprocessor(lexer, config, Platform.WINDOWS);
    preprocessor.process();

    assertThatThrownBy(preprocessor::process).isInstanceOf(IllegalStateException.class);
  }

  private static void executeWithDefines(String filename, String... defines) {
    DelphiFileConfig config =
        DelphiFile.createConfig(
            UTF_8.name(),
            new DelphiPreprocessorFactory(Platform.WINDOWS),
            TypeFactoryUtils.defaultFactory(),
            SearchPath.create(Collections.emptyList()),
            new HashSet<>(Arrays.asList(defines)));
    execute(filename, config);
  }

  private static void executeWithSearchPath(String filename, String... directories) {
    TypeFactory typeFactory = TypeFactoryUtils.defaultFactory();
    SearchPath searchPath =
        SearchPath.create(
            Arrays.stream(directories)
                .map(dir -> DelphiUtils.getResource(BASE_DIR + dir).toPath())
                .collect(Collectors.toList()));

    execute(
        filename,
        DelphiFile.createConfig(
            UTF_8.name(),
            new DelphiPreprocessorFactory(Platform.WINDOWS),
            typeFactory,
            searchPath,
            emptySet()));
  }

  private static void execute(String filename) {
    execute(filename, DelphiFileUtils.mockConfig());
  }

  private static void execute(String filename, DelphiFileConfig config) {
    try {
      String filePath = DelphiUtils.getResource(BASE_DIR + filename).getAbsolutePath();
      DelphiFileStream fileStream = new DelphiFileStream(filePath, config.getEncoding());

      DelphiLexer lexer = new DelphiLexer(fileStream);
      DelphiPreprocessor preprocessor =
          config.getPreprocessorFactory().createPreprocessor(lexer, config);
      preprocessor.process();
      BufferedTokenStream tokenStream = preprocessor.getTokenStream();

      DelphiParser parser = new DelphiParser(tokenStream);
      parser.setTreeAdaptor(new DelphiTreeAdaptor());
      parser.file();
    } catch (Exception e) {
      throw new AssertionError("Expected file to parse successfully after preprocessing.", e);
    }
  }
}
