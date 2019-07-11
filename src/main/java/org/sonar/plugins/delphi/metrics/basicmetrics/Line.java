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
package org.sonar.plugins.delphi.metrics.basicmetrics;

import java.util.EnumMap;
import org.apache.commons.lang.StringUtils;

/**
 * Class representing lineNumber of code. Holds data required to source code statistic generation.
 */
class Line {

  private final int lineIndex;
  private String comment;
  private StringBuilder stringLine;
  private boolean isBlank;
  private boolean isThereDoc;
  private boolean isThereLicenseHeaderComment;
  private EnumMap<Metric, Integer> measures;
  private static final String NOSONAR_TAG = "NOSONAR";

  private Line() {
    initMeasures();
    this.lineIndex = 0;
  }

  Line(int lineIndex) {
    initMeasures();
    this.lineIndex = lineIndex;
  }

  Line(String stringLine) {
    this();
    setString(new StringBuilder(stringLine));
  }

  private void initMeasures() {
    measures = new EnumMap<>(Metric.class);
    measures.put(Metric.LINES, 1);
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

  public int getInt(Metric metric) {
    return measures.getOrDefault(metric, 0);
  }

  public void setMeasure(Metric metric, int measure) {
    if (metric == Metric.LINES) {
      throw new IllegalStateException("Metric.LINES cannot be changed.");
    }

    if (metric.isAggregateIfThereIsAlreadyAValue()) {
      measure += getInt(metric);
    }
    measures.put(metric, measure);
  }

  void setComment(String comment) {
    this.comment = comment;
  }

  private void setComment(String comment, boolean isJavadoc) {
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
      isThereCodeBeforeComment = !StringUtils
          .isBlank(stringLine.substring(0, commentStartIndex - 1));
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
