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

import org.apache.commons.lang.StringUtils;
import org.sonar.squidbridge.measures.Measurable;
import org.sonar.squidbridge.measures.Metric;


/**
 * Class representing line of code. Holds data required to source code statistic
 * generation.
 */
class Line implements Measurable<Metric> {

  private final int lineIndex;
  private int blankLine = 0;
  private int line = 1;
  private int lineOfCode = 0;
  private int commentLine = 0;
  private int headerCommentLine = 0;
  private int commentBlankLine = 0;
  private int commentedOutCodeLine = 0;
  private int documentation = 0;
  private String comment = null;
  private StringBuilder stringLine;
  private boolean isBlank;
  private boolean isThereDoc;
  private boolean isThereLicenseHeaderComment;

  private static final String NOSONAR_TAG = "NOSONAR";

  Line() {
    this.lineIndex = 0;
  }

  Line(String stringLine) {
    this();
    setString(new StringBuilder(stringLine));
  }

  Line(int lineIndex, StringBuilder stringLine) {
    this(lineIndex);
    setString(stringLine);
  }

  Line(int lineIndex) {
    this.lineIndex = lineIndex;
  }

  final void setString(StringBuilder stringLine) {
    this.stringLine = stringLine;
    isBlank = isBlankLine();
  }

  private boolean isBlankLine() {
    for (int i = 0; i < stringLine.length(); i++) {
      if (!Character.isWhitespace(stringLine.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public double getDouble(Metric metric) {
    return getInt(metric);
  }


  /**
   * {@inheritDoc}
   */

  @Override
  public int getInt(Metric metric) {
    switch (metric) {
      case BLANK_LINES:
        return blankLine;
      case LINES:
        return line;
      case LINES_OF_CODE:
        return lineOfCode;
      case COMMENT_LINES:
        return commentLine;
      case COMMENTED_OUT_CODE_LINES:
        return commentedOutCodeLine;
      case COMMENT_BLANK_LINES:
        return commentBlankLine;
      case HEADER_COMMENT_LINES:
        return headerCommentLine;
      case PUBLIC_DOC_API:
        return documentation;
      default:
        throw new IllegalStateException("MetricDef " + metric.getName() + " is not available on Line object.");
    }
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void setMeasure(Metric metric, double measure) {
    setMeasure(metric, (int) measure);
  }

  /**
   * {@inheritDoc}
   */

  public void setMeasure(Metric metric, int measure) {
    switch (metric) {
      case BLANK_LINES:
        blankLine = measure;
        break;
      case LINES_OF_CODE:
        lineOfCode = measure;
        break;
      case COMMENT_LINES:
        commentLine = measure;
        break;
      case COMMENTED_OUT_CODE_LINES:
        commentedOutCodeLine = measure;
        break;
      case COMMENT_BLANK_LINES:
        commentBlankLine = measure;
        break;
      case HEADER_COMMENT_LINES:
        headerCommentLine = measure;
        break;
      case PUBLIC_DOC_API:
        documentation = measure;
        break;
      case LINES:
        throw new IllegalStateException(
          "MetricDef LINES always equals 1 on a Line and you are not permitted to change this value.");
      default:
        throw new IllegalStateException("MetricDef " + metric.getName() + " is not suitable for Line object.");
    }
  }

  void setComment(String comment) {
    this.comment = comment;
  }

  void setComment(String comment, boolean isJavadoc) {
    setComment(comment);
    this.isThereDoc = isJavadoc;
  }

  void setComment(String comment, boolean isJavadoc, boolean isLicenseHeader) {
    setComment(comment, isJavadoc);
    this.isThereLicenseHeaderComment = isLicenseHeader;
  }

  String getString() {
    return stringLine.toString();
  }

  boolean isBlank() {
    return !isThereComment() && isBlank;
  }

  public boolean isThereJavaDoc() {
    return isThereDoc;
  }

  boolean isThereCode() {
    if (!isBlank() && !isThereComment()) {
      return true;
    }
      return isThereComment() && isThereCodeBeforeOrAfterComment();
  }

  private boolean isThereCodeBeforeOrAfterComment() {
    if (!isThereComment()) {
      throw new IllegalStateException("You can't call this method when there isn't any comment");
    }

    if (stringLine == null) {
      // when {comment} //comment this is empty, so perform this check
      return false;
    }

    boolean isThereCodeBeforeComment = false;
    boolean isThereCodeAfterComment = false;
    int commentStartIndex = stringLine.indexOf(comment);
    int commentEndIndex = commentStartIndex + comment.length() - 1;
    if (commentStartIndex > 0) {
      isThereCodeBeforeComment = !StringUtils.isBlank(stringLine.substring(0, commentStartIndex - 1));
    }
    if (commentEndIndex > 0 && commentEndIndex != stringLine.length() - 1) {
      isThereCodeAfterComment = !StringUtils.isBlank(stringLine.substring(commentEndIndex + 1));
    }
    return isThereCodeBeforeComment || isThereCodeAfterComment;
  }

  boolean isThereComment() {
    return comment != null;
  }

  String getComment() {
    return comment;
  }

  boolean isThereBlankComment() {
    if (isThereComment()) {
      for (int i = 0; i < comment.length(); i++) {
        char character = comment.charAt(i);
        if (!Character.isWhitespace(character) && character != '*' && character != '/') {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  boolean isThereDoc() {
    return isThereDoc;
  }

  boolean isThereLicenseHeaderComment() {
    return isThereLicenseHeaderComment;
  }

  boolean isThereNoSonarTag() {
    return isThereComment() && comment.contains(NOSONAR_TAG);
  }

  int getLineIndex() {
    return lineIndex;
  }

  void deleteLineContent() {
    comment = null;
    stringLine = null;
  }
}
