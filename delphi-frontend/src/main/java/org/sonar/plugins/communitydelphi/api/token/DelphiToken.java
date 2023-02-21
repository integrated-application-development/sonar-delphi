package org.sonar.plugins.communitydelphi.api.token;

import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;

public interface DelphiToken {

  String getImage();

  int getBeginLine();

  int getBeginColumn();

  int getEndLine();

  int getEndColumn();

  boolean isEof();

  boolean isImaginary();

  boolean isWhitespace();

  boolean isComment();

  boolean isCompilerDirective();

  boolean isNil();

  int getIndex();

  int getType();

  String getNormalizedImage();

  @Nullable
  TypeOfText getHighlightingType();
}
