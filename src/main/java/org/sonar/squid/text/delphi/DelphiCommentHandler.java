/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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
package org.sonar.squid.text.delphi;

import org.sonar.squidbridge.measures.Metric;

/**
 * Class for handling custom block comments. Can also count documentation
 * comments.
 * 
 */
// TODO Rename to MultiLinesCommentHandler
public class DelphiCommentHandler extends LineContextHandler {

  private static final int MIN_CHARS_LEFT = 3;
  private StringBuilder currentLineComment;
  private boolean isFirstLineOfComment = false;
  private boolean isLicenseHeader = false;
  private boolean commentStarted = false;
  private boolean isDoc = false;
  private int start = -1;

  private String startCommentTag;
  private String endCommentTag;

  /**
   * Constructor.
   * 
   * @param start The start tag, like "{", "(*" etc
   * @param end The end tag, like "}", *)" etc
   * @param isDocumentation If this param is set, the class will look for
   *            additional start tag "**" and count comment as documentation
   */
  public DelphiCommentHandler(String start, String end, boolean isDocumentation) {
    if (start == null || end == null) {
      throw new IllegalStateException("Method DelphiCommentHandler() c-tor needs two strings!");
    }
    startCommentTag = start;
    endCommentTag = end;
    isDoc = isDocumentation;
  }

  @Override
  boolean matchToEnd(Line line, StringBuilder pendingLine) {
    if (!commentStarted) {
      throw new IllegalStateException(
        "Method doContextBegin(StringBuilder pendingLine) has not been called first (line = '"
          + pendingLine
          + "').");
    }

    currentLineComment.append(getLastCharacter(pendingLine));
    boolean match = matchEndOfString(pendingLine, endCommentTag);
    if (match
      && !(isFirstLineOfComment && pendingLine.indexOf(startCommentTag) + 1 == pendingLine
        .indexOf(endCommentTag))) {
      endOfCommentLine(line, pendingLine);
      initProperties();
      return true;
    }

    return false;
  }

  private boolean isDocumentation(StringBuilder pendingLine) {
      return isDoc && start != -1 && pendingLine.length() >= start + MIN_CHARS_LEFT
              && pendingLine.charAt(start + 1) == '*'
              && pendingLine.charAt(start + 2) == '*';
  }

  @Override
  boolean matchToBegin(Line line, StringBuilder pendingLine) {
    boolean match = matchEndOfString(pendingLine, startCommentTag);
    if (match) {
      isFirstLineOfComment = true;
      commentStarted = true;
      currentLineComment = new StringBuilder(startCommentTag);
      isLicenseHeader = (line.getLineIndex() == 1);
      start = pendingLine.length() - 1;
    }
    return match;
  }

  @Override
  boolean matchWithEndOfLine(Line line, StringBuilder pendingLine) {
    if (commentStarted) {
      endOfCommentLine(line, pendingLine);
    }
    return false;
  }

  private void endOfCommentLine(Line line, StringBuilder pendingLine) {
    line.setComment(currentLineComment.toString(), isDoc, isLicenseHeader);
    currentLineComment = new StringBuilder();
    isFirstLineOfComment = false;
    if (isDocumentation(pendingLine)) {
      line.setMeasure(Metric.PUBLIC_DOC_API, 1);
    }
  }

  private void initProperties() {
    commentStarted = false;
    isLicenseHeader = false;
    currentLineComment = new StringBuilder();
    isFirstLineOfComment = false;
  }
}
