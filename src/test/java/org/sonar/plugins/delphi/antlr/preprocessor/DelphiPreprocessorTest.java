package org.sonar.plugins.delphi.antlr.preprocessor;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.antlr.runtime.BufferedTokenStream;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.DelphiParser;
import org.sonar.plugins.delphi.antlr.LowercaseFileStream;
import org.sonar.plugins.delphi.antlr.ast.DelphiTreeAdaptor;
import org.sonar.plugins.delphi.file.DelphiFile;
import org.sonar.plugins.delphi.file.DelphiFileConfig;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class DelphiPreprocessorTest {
  private static final String BASE_DIR = "/org/sonar/plugins/delphi/preprocessor/";

  @Test
  public void testUndefineDirectives() {
    executeWithDefines("UndefineDirectives.pas", "FAIL_IF_DEFINED");
  }

  @Test
  public void testNestedDirectives() {
    executeWithDefines("NestedDirectives.pas", "FAIL_IF_DEFINED", "NESTED");
  }

  @Test
  public void testSameFolderIncludeDirectives() {
    execute("includeTest/SameFolder.pas");
  }

  @Test
  public void testBacktrackIncludeDirectives() {
    execute("includeTest/Backtrack.pas");
  }

  @Test
  public void testSearchPathIncludeDirectives() {
    executeWithSearchPath("includeTest/SearchPath.pas", "includes");
  }

  @Test
  public void testNestedIncludeDirectives() {
    executeWithSearchPath("includeTest/NestedSearchPath.pas", "includes", "nestedIncludes");
  }

  @Test
  public void testSameNameBacktrackIncludeDirectives() {
    execute("includeTest/SameNameBacktrack.pas");
  }

  @Test
  public void testBadIncludeTokenShouldNotThrowException() {
    execute("includeTest/BadIncludeToken.pas");
  }

  @Test
  public void testNonexistentIncludeShouldNotThrowException() {
    executeWithSearchPath("includeTest/IncludeDoesNotExist.pas", "includes");
  }

  @Test
  public void testSelfReferencingIncludeShouldNotThrowException() {
    executeWithSearchPath("includeTest/SelfReferencingInclude.pas");
  }

  @Test
  public void testCallingProcessTwiceShouldThrowException() throws Exception {
    String filePath =
        DelphiUtils.getResource(BASE_DIR + "includeTest/SameNameBacktrack.pas").getAbsolutePath();
    DelphiFileConfig config = DelphiFile.createConfig(UTF_8.name(), emptyList(), emptySet());
    LowercaseFileStream fileStream = new LowercaseFileStream(filePath, config.getEncoding());

    DelphiLexer lexer = new DelphiLexer(fileStream);
    DelphiPreprocessor preprocessor = new DelphiPreprocessor(lexer, config);
    preprocessor.process();

    assertThatThrownBy(preprocessor::process).isInstanceOf(IllegalStateException.class);
  }

  private static void executeWithDefines(String filename, String... defines) {
    Set<String> defineSet = new HashSet<>(Arrays.asList(defines));
    execute(filename, DelphiFile.createConfig(UTF_8.name(), emptyList(), defineSet));
  }

  private static void executeWithSearchPath(String filename, String... directories) {
    List<Path> searchPath =
        Arrays.stream(directories)
            .map(dir -> DelphiUtils.getResource(BASE_DIR + dir).toPath())
            .collect(Collectors.toList());

    execute(filename, DelphiFile.createConfig(UTF_8.name(), searchPath, emptySet()));
  }

  private static void execute(String filename) {
    execute(filename, DelphiFile.createConfig(UTF_8.name()));
  }

  private static void execute(String filename, DelphiFileConfig config) {
    try {
      String filePath = DelphiUtils.getResource(BASE_DIR + filename).getAbsolutePath();
      LowercaseFileStream fileStream = new LowercaseFileStream(filePath, config.getEncoding());

      DelphiLexer lexer = new DelphiLexer(fileStream);
      DelphiPreprocessor preprocessor = new DelphiPreprocessor(lexer, config);
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
