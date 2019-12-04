package org.sonar.plugins.delphi.executor;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.token.DelphiToken;
import org.sonar.plugins.delphi.file.DelphiFile.DelphiInputFile;

public class DelphiHighlightExecutor extends DelphiTokenExecutor {
  private NewHighlighting highlighter;
  private boolean insideAsmBlock;

  @Override
  public void onFile(SensorContext context, DelphiInputFile delphiFile) {
    highlighter = context.newHighlighting().onFile(delphiFile.getInputFile());
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
        token.getBeginLine(),
        token.getBeginColumn(),
        token.getEndLine(),
        token.getEndColumn(),
        highlightType);
  }

  private boolean shouldSkip(DelphiToken token) {
    int type = token.getType();

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

  @Override
  public void save() {
    highlighter.save();
  }
}
