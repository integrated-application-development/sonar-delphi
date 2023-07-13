package org.sonar.plugins.communitydelphi.api.token;

import javax.annotation.Nullable;
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

  boolean isKeyword();

  boolean isNil();

  int getIndex();

  DelphiTokenType getType();

  String getNormalizedImage();

  @Nullable
  TypeOfText getHighlightingType();
}
