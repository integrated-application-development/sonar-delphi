package org.sonar.plugins.delphi.executor;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.intThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Range;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.cpd.NewCpdTokens;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.plugins.delphi.antlr.ast.token.DelphiToken;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.file.DelphiFile;
import org.sonar.plugins.delphi.file.DelphiFile.DelphiInputFile;
import org.sonar.plugins.delphi.symbol.SymbolTable;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class DelphiTokenExecutorTest {
  private static final String ROOT_DIR_NAME = "/org/sonar/plugins/delphi/token";
  private static final File ROOT_DIR = DelphiUtils.getResource(ROOT_DIR_NAME);

  private static final String SIMPLE_FILE = "/org/sonar/plugins/delphi/token/Simple.pas";
  private static final String LITERALS_FILE = "/org/sonar/plugins/delphi/token/Literals.pas";
  private static final String MIXED_CASE_FILE = "/org/sonar/plugins/delphi/token/MixedCase.pas";
  private static final String ASM_FILE = "/org/sonar/plugins/delphi/token/AsmHighlighting.pas";

  private DelphiMasterExecutor executor;
  private DelphiHighlightExecutor highlightExecutor;
  private DelphiCpdExecutor cpdExecutor;

  private ExecutorContext context;
  private NewCpdTokens cpdTokens;
  private NewHighlighting highlighting;

  @Before
  public void setup() {
    cpdTokens = mock(NewCpdTokens.class);
    when(cpdTokens.onFile(any())).thenReturn(cpdTokens);

    highlighting = mock(NewHighlighting.class);
    when(highlighting.onFile(any())).thenReturn(highlighting);

    SensorContext sensorContext = mock(SensorContext.class);
    when(sensorContext.newCpdTokens()).thenReturn(cpdTokens);
    when(sensorContext.newHighlighting()).thenReturn(highlighting);

    context = new ExecutorContext(sensorContext, mock(SymbolTable.class));

    highlightExecutor = new DelphiHighlightExecutor();
    cpdExecutor = new DelphiCpdExecutor();
    executor = new DelphiMasterExecutor(highlightExecutor, cpdExecutor);
  }

  @Test
  public void testResultsNotSavedTokenizationFailure() {
    when(cpdTokens.addToken(anyInt(), anyInt(), anyInt(), anyInt(), any()))
        .thenThrow(IllegalStateException.class);

    when(highlighting.highlight(anyInt(), anyInt(), anyInt(), anyInt(), any()))
        .thenThrow(IllegalStateException.class);

    execute(makeDelphiFile(SIMPLE_FILE));

    verify(cpdTokens, never()).save();
    verify(highlighting, never()).save();
  }

  @Test
  public void testSimpleFile() {
    execute(makeDelphiFile(SIMPLE_FILE));

    cpdTokenCount(205);
    cpdTokenCount(0, DelphiTokenExecutorTest::isWhitespaceOrComment);
    highlightCount(41);
  }

  @Test
  public void testLiteralsFile() {
    execute(makeDelphiFile(LITERALS_FILE));

    cpdTokenCount(62);
    cpdTokenCount(0, DelphiTokenExecutorTest::isWhitespaceOrComment);
    cpdTokenCount(3, DelphiToken.STRING_LITERAL);
    cpdTokenCount(5, DelphiToken.NUMERIC_LITERAL);
    highlightCount(20);
  }

  @Test
  public void testMixedCaseFile() {
    execute(makeDelphiFile(MIXED_CASE_FILE));

    cpdTokenCount(184);
    cpdTokenCount(0, DelphiTokenExecutorTest::isWhitespaceOrComment);
    cpdTokenCount(9, "over1");
    cpdTokenCount(2, "notover");
    highlightCount(39);
  }

  @Test
  public void testAsmFile() {
    execute(makeDelphiFile(ASM_FILE));

    cpdTokenCount(275);
    cpdTokenCount(0, DelphiTokenExecutorTest::isWhitespaceOrComment);
    highlightCount(41);

    // shl
    highlightExistsOnLine(17);

    // shr
    highlightExistsOnLine(20);

    // xor
    highlightExistsOnLine(23);

    // and
    highlightExistsOnLine(26);

    // asm
    highlightExistsOnLine(29);
    highlightExistsOnLine(47);
    highlightExistsOnLine(65);

    // asm end
    highlightExistsOnLine(43);
    highlightExistsOnLine(61);
    highlightExistsOnLine(79);

    // No highlighting inside asm blocks
    highlightCountInLineRange(0, Range.open(29, 43));
    highlightCountInLineRange(0, Range.open(47, 61));

    // Except for comments!
    highlightCountInLineRange(7, Range.open(65, 79));
  }

  private void cpdTokenCount(int count) {
    verify(cpdTokens, times(count)).addToken(anyInt(), anyInt(), anyInt(), anyInt(), anyString());
  }

  private void cpdTokenCount(int count, String image) {
    verify(cpdTokens, times(count)).addToken(anyInt(), anyInt(), anyInt(), anyInt(), eq(image));
  }

  @SuppressWarnings("SameParameterValue")
  private void cpdTokenCount(int count, ArgumentMatcher<String> matcher) {
    verify(cpdTokens, times(count))
        .addToken(anyInt(), anyInt(), anyInt(), anyInt(), argThat(matcher));
  }

  private void highlightCount(int count) {
    verify(highlighting, times(count)).highlight(anyInt(), anyInt(), anyInt(), anyInt(), any());
  }

  private void highlightExistsOnLine(int line) {
    verify(highlighting, atLeastOnce()).highlight(eq(line), anyInt(), anyInt(), anyInt(), any());
  }

  private void highlightCountInLineRange(int count, Range<Integer> lineRange) {
    Objects.requireNonNull(lineRange);
    verify(highlighting, times(count))
        .highlight(intThat(lineRange::contains), anyInt(), anyInt(), anyInt(), any());
  }

  private static boolean isWhitespaceOrComment(String tokenString) {
    return tokenString.isBlank()
        || tokenString.startsWith("//")
        || tokenString.matches("(?s)\\{[^$].*")
        || tokenString.matches("(?s)\\(\\*[^$].*");
  }

  private DelphiInputFile makeDelphiFile(String filePath) {
    try {
      File srcFile = DelphiUtils.getResource(filePath);

      InputFile inputFile =
          TestInputFileBuilder.create("ROOT_KEY_CHANGE_AT_SONARAPI_5", ROOT_DIR, srcFile)
              .setModuleBaseDir(ROOT_DIR.toPath())
              .setContents(DelphiUtils.readFileContent(srcFile, UTF_8.name()))
              .setLanguage(DelphiLanguage.KEY)
              .setType(Type.MAIN)
              .build();

      return DelphiInputFile.from(inputFile, DelphiFile.createConfig(UTF_8.name()));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void execute(DelphiInputFile delphiFile) {
    executor.execute(context, delphiFile);
    cpdExecutor.complete();
    highlightExecutor.complete();
  }
}
