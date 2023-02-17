package org.sonar.plugins.communitydelphi.api.ast;

import java.math.BigInteger;
import org.sonar.plugins.communitydelphi.api.type.Typed;

public interface LiteralNode extends DelphiNode, Typed {
  boolean isTextLiteral();

  boolean isNilLiteral();

  boolean isIntegerLiteral();

  boolean isHexadecimalLiteral();

  boolean isBinaryLiteral();

  boolean isDecimalLiteral();

  String getValueAsString();

  int getValueAsInt();

  BigInteger getValueAsBigInteger();

  double getValueAsDouble();
}
