package org.sonar.plugins.delphi.token;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

public class DelphiHighlightHandler implements DelphiTokenHandler {
  private NewHighlighting highlighter;
  private boolean insideAsmBlock;

  @Override
  public void onFile(SensorContext context, InputFile inputFile) {
    highlighter = context.newHighlighting().onFile(inputFile);
    insideAsmBlock = false;
  }

  @Override
  public void handleToken(DelphiToken token) {
    if (shouldSkip(token)) {
      return;
    }

    TypeOfText highlightType = token.getHighlightingType();
    if (highlightType == null) {
      return;
    }

    highlighter.highlight(
        token.getStartLine(),
        token.getStartColumn(),
        token.getEndLine(),
        token.getEndColumn(),
        highlightType);
  }

  @Override
  public void saveResults() {
    highlighter.save();
  }

  private boolean shouldSkip(DelphiToken token) {
    int type = token.getToken().getType();

    if (type == DelphiLexer.ASM) {
      // We still want to highlight the asm keyword
      insideAsmBlock = true;
      return false;
    }

    if (insideAsmBlock) {
      insideAsmBlock = (type != DelphiLexer.END);
    }

    return insideAsmBlock && !token.isComment();
  }
}
