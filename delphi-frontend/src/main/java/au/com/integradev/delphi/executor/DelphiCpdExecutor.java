/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
import org.sonar.api.batch.sensor.cpd.NewCpdTokens;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;

public class DelphiCpdExecutor extends DelphiTokenExecutor {
  static final String STRING_LITERAL = "STRING_LITERAL";
  static final String NUMERIC_LITERAL = "NUMERIC_LITERAL";

  private NewCpdTokens cpdTokens;

  @Override
  public void onFile(SensorContext context, DelphiInputFile delphiFile) {
    cpdTokens = context.newCpdTokens().onFile(delphiFile.getInputFile());
  }

  @Override
  public void handleToken(DelphiToken token) {
    if (token.isWhitespace() || token.isComment()) {
      return;
    }

    cpdTokens.addToken(
        token.getBeginLine(),
        token.getBeginColumn(),
        token.getEndLine(),
        token.getEndColumn(),
        getNormalizedImage(token));
  }

  @Override
  public void save() {
    cpdTokens.save();
  }

  private static String getNormalizedImage(DelphiToken token) {
    if (token.getType() == DelphiTokenType.QUOTED_STRING) {
      return STRING_LITERAL;
    }

    if (isNumericLiteral(token)) {
      return NUMERIC_LITERAL;
    }

    return token.getImage().toLowerCase();
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
}
