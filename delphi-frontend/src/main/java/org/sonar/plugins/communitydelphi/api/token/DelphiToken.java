package org.sonar.plugins.communitydelphi.api.token;

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
}
