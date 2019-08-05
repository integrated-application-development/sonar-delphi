package org.sonar.plugins.delphi.token;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.util.Set;
import org.antlr.runtime.Token;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.antlr.filestream.LowercaseFileStream;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class DelphiTokenSensor implements Sensor {

  private static final Logger LOG = Loggers.get(DelphiTokenSensor.class);
  private static final Set<DelphiTokenHandler> HANDLERS =
      Set.of(new DelphiCpdHandler(), new DelphiHighlightHandler());

  private SensorContext context;
  private DelphiProjectHelper delphiProjectHelper;

  public DelphiTokenSensor(SensorContext context, DelphiProjectHelper delphiProjectHelper) {
    this.context = context;
    this.delphiProjectHelper = delphiProjectHelper;
  }

  /** {@inheritDoc} */
  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.name("DelphiTokenSensor");
    descriptor.onlyOnLanguage(DelphiLanguage.KEY);
  }

  /** {@inheritDoc} */
  @Override
  public void execute(@NonNull SensorContext context) {
    for (DelphiProject project : delphiProjectHelper.getProjects()) {
      project
          .getSourceFiles()
          .stream()
          .map(file -> delphiProjectHelper.getFile(file))
          .forEach(this::doExecute);
    }
  }

  private void doExecute(InputFile inputFile) {
    try {
      onFile(context, inputFile);
      processTokens(inputFile);
      saveResults();
    } catch (IOException e) {
      LOG.error("IO Exception on: {}", inputFile.toString(), e);
    } catch (IllegalStateException e) {
      LOG.error("Tokenization failure on: {}", inputFile.toString(), e);
    }
  }

  private void processTokens(InputFile inputFile) throws IOException {
    String fileName = DelphiUtils.uriToAbsolutePath(inputFile.uri());

    DelphiLexer lexer = new DelphiLexer(new LowercaseFileStream(fileName, UTF_8.name()));
    Token token = lexer.nextToken();

    while (token.getType() != Token.EOF) {
      Token nextToken = lexer.nextToken();
      int startLine = token.getLine();
      int startPos = token.getCharPositionInLine();
      int endLine = nextToken.getLine();
      int endPos = nextToken.getCharPositionInLine();

      DelphiToken delphiToken = new DelphiToken(token, startLine, startPos, endLine, endPos);
      handleToken(delphiToken);

      token = nextToken;
    }
  }

  private void onFile(SensorContext context, InputFile inputFile) {
    for (DelphiTokenHandler handler : HANDLERS) {
      handler.onFile(context, inputFile);
    }
  }

  private void handleToken(DelphiToken delphiToken) {
    for (DelphiTokenHandler handler : HANDLERS) {
      handler.handleToken(delphiToken);
    }
  }

  private void saveResults() {
    for (DelphiTokenHandler handler : HANDLERS) {
      handler.saveResults();
    }
  }
}
