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
package au.com.integradev.delphi.executor;

import au.com.integradev.delphi.file.DelphiFile.DelphiInputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;

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

    TypeOfText highlightType = getHighlightingType(token);
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

  private static TypeOfText getHighlightingType(DelphiToken token) {
    TypeOfText type = null;

    if (token.getType() == DelphiTokenType.QUOTED_STRING) {
      type = TypeOfText.STRING;
    } else if (isNumericLiteral(token)) {
      type = TypeOfText.CONSTANT;
    } else if (token.isComment()) {
      type = TypeOfText.COMMENT;
    } else if (token.isCompilerDirective()) {
      type = TypeOfText.PREPROCESS_DIRECTIVE;
    } else if (token.isKeyword()) {
      type = TypeOfText.KEYWORD;
    }

    return type;
  }

  private static boolean isNumericLiteral(DelphiToken token) {
    switch (token.getType()) {
      case INT_NUMBER:
      case REAL_NUMBER:
      case HEX_NUMBER:
      case BINARY_NUMBER:
        return true;
      default:
        return false;
    }
  }

  private boolean shouldSkip(DelphiToken token) {
    DelphiTokenType type = token.getType();

    if (type == DelphiTokenType.ASM) {
      // We still want to highlight the asm keyword
      insideAsmBlock = true;
      return false;
    }

    if (insideAsmBlock) {
      insideAsmBlock = (type != DelphiTokenType.END);
    }

    return insideAsmBlock && !token.isComment();
  }

  @Override
  public void save() {
    highlighter.save();
  }
}
