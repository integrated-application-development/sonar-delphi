/*
 * Sonar Delphi Plugin
 * Copyright (C) 2024 Integrated Application Development
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
package au.com.integradev.delphi.reporting.edits;

import au.com.integradev.delphi.antlr.DelphiFileStream;
import au.com.integradev.delphi.antlr.ast.token.DelphiTokenImpl;
import au.com.integradev.delphi.reporting.TextRangeReplacement;
import java.util.List;
import java.util.function.Supplier;
import org.antlr.runtime.CommonToken;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFixEdit;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;

public abstract class QuickFixEditImpl implements QuickFixEdit {
  public abstract List<TextRangeReplacement> toTextEdits(
      Supplier<DelphiFileStream> fileStreamSupplier);

  private static CommonToken getAntlrToken(DelphiToken token) {
    return (CommonToken) ((DelphiTokenImpl) token).getAntlrToken();
  }

  protected static String getNodeText(DelphiNode node, DelphiFileStream fileStream) {
    int startIndex = getAntlrToken(node.getFirstToken()).getStartIndex();
    int endIndex = getAntlrToken(node.getLastToken()).getStopIndex();

    return fileStream.substring(startIndex, endIndex);
  }
}
