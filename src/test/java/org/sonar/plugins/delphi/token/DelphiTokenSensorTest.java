package org.sonar.plugins.delphi.token;

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
import java.net.URI;
import java.util.Collections;
import java.util.Objects;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.cpd.NewCpdTokens;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class DelphiTokenSensorTest {

  private static final String SIMPLE_FILE = "/org/sonar/plugins/delphi/token/Simple.pas";
  private static final String LITERALS_FILE = "/org/sonar/plugins/delphi/token/Literals.pas";
  private static final String MIXED_CASE_FILE = "/org/sonar/plugins/delphi/token/MixedCase.pas";
  private static final String ASM_FILE = "/org/sonar/plugins/delphi/token/AsmHighlighting.pas";

  private DelphiTokenSensor sensor;
  private SensorContext context;
  private DelphiProjectHelper delphiProjectHelper;
  private NewCpdTokens cpdTokens;
  private NewHighlighting highlighting;

  @Before
  public void setup() {
    cpdTokens = mock(NewCpdTokens.class);
    when(cpdTokens.onFile(any())).thenReturn(cpdTokens);

    highlighting = mock(NewHighlighting.class);
    when(highlighting.onFile(any())).thenReturn(highlighting);

    context = mock(SensorContext.class);
    when(context.newCpdTokens()).thenReturn(cpdTokens);
    when(context.newHighlighting()).thenReturn(highlighting);

    DelphiProject delphiProject = new DelphiProject("Test Project");
    delphiProject.setSourceFiles(Collections.singletonList(mock(File.class)));

    delphiProjectHelper = mock(DelphiProjectHelper.class);
    when(delphiProjectHelper.getProjects()).thenReturn(Collections.singletonList(delphiProject));

    sensor = new DelphiTokenSensor(context, delphiProjectHelper);
  }

  @Test
  public void testDescribe() {
    final SensorDescriptor mockDescriptor = mock(SensorDescriptor.class);
    when(mockDescriptor.onlyOnLanguage(anyString())).thenReturn(mockDescriptor);

    sensor.describe(mockDescriptor);

    verify(mockDescriptor).onlyOnLanguage(DelphiLanguage.KEY);
    verify(mockDescriptor).name("DelphiTokenSensor");
  }

  @Test
  public void testResultsNotSavedIOException() throws Exception {
    InputFile inputFile = mock(InputFile.class);
    when(inputFile.uri()).thenReturn(new URI("file:///C:/DOES_NOT_EXIST.pas"));

    executeSensor(inputFile);

    verify(cpdTokens, never()).save();
    verify(highlighting, never()).save();
  }

  @Test
  public void testResultsNotSavedTokenizationFailure() {
    InputFile inputFile = mock(InputFile.class);
    when(inputFile.uri()).thenReturn(DelphiUtils.getResource(SIMPLE_FILE).toURI());
    when(cpdTokens.addToken(anyInt(), anyInt(), anyInt(), anyInt(), any()))
        .thenThrow(IllegalStateException.class);

    executeSensor(inputFile);

    verify(cpdTokens, never()).save();
    verify(highlighting, never()).save();
  }

  @Test
  public void testSimpleFile() {
    processFile(SIMPLE_FILE);

    cpdTokenCount(204);
    cpdTokenCount(0, DelphiTokenSensorTest::isWhitespaceOrComment);
    highlightCount(43);
  }

  @Test
  public void testLiteralsFile() {
    processFile(LITERALS_FILE);

    cpdTokenCount(62);
    cpdTokenCount(0, DelphiTokenSensorTest::isWhitespaceOrComment);
    cpdTokenCount(3, DelphiToken.STRING_LITERAL);
    cpdTokenCount(5, DelphiToken.NUMERIC_LITERAL);
    highlightCount(20);
  }

  @Test
  public void testMixedCaseFile() {
    processFile(MIXED_CASE_FILE);

    cpdTokenCount(183);
    cpdTokenCount(0, DelphiTokenSensorTest::isWhitespaceOrComment);
    cpdTokenCount(9, "over1");
    cpdTokenCount(2, "notover");
    highlightCount(39);
  }

  @Test
  public void testAsmFile() {
    processFile(ASM_FILE);

    cpdTokenCount(275);
    cpdTokenCount(0, DelphiTokenSensorTest::isWhitespaceOrComment);
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
        || tokenString.startsWith("{")
        || tokenString.startsWith("(*");
  }

  private void processFile(String filePath) {
    InputFile inputFile = mock(InputFile.class);
    when(inputFile.uri()).thenReturn(DelphiUtils.getResource(filePath).toURI());
    executeSensor(inputFile);
  }

  private void executeSensor(InputFile inputFile) {
    when(delphiProjectHelper.getFile(any(File.class))).thenReturn(inputFile);
    sensor.execute(context);
  }
}
