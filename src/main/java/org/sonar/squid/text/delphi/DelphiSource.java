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
import org.sonar.squidbridge.recognizer.CodeRecognizer;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class representing Delphi source code. It holds statistics for code lines.
 */
public class DelphiSource {

  private List<Line> lines = new ArrayList<Line>();
  private CodeRecognizer codeRecognizer;
  private Set<Integer> noSonarTagLines = new HashSet<Integer>();

  /**
   * Constructor. Does the line processing.
   * 
   * @param reader File to read
   * @param codeRecognizer Code recognizer class to recognize Delphi code
   */
  public DelphiSource(Reader reader, CodeRecognizer codeRecognizer) {
    this.codeRecognizer = codeRecognizer;
    DelphiLinesFactory linesFactory = new DelphiLinesFactory(reader);
    lines = linesFactory.getLines();
    processLines();
  }

  /**
   * Constructor. Does the line processing.
   * 
   * @param stringLines Code lines
   * @param codeRecognizer Code recognizer class to recognize Delphi code
   */
  public DelphiSource(String[] stringLines, CodeRecognizer codeRecognizer) {
    this(new StringArrayReader(stringLines), codeRecognizer);
  }

  /**
   * Gets the source code measure on whole file
   * 
   * @param metric Metric to get
   * @return Sum of specified metric on all lines in file
   */
  public double getMeasure(Metric metric) {
    return getMeasure(metric, 1, lines.size());
  }

  /**
   * Gets the source code measure on whole file
   * 
   * @param metric Metric to get
   * @param fromLine First line of calculations
   * @param toLine Last line of calculations
   * @return Sum of specified metric from 'fromLine' to 'toLine'
   */
  public int getMeasure(Metric metric, int fromLine, int toLine) {
    if (toLine > lines.size()) {
      throw new IllegalStateException("There are only " + lines.size()
        + " lines in the file and you're trying to reach line " + toLine);
    }
    if (fromLine < 1) {
      throw new IllegalStateException("Line index starts from 1 and not from " + fromLine);
    }

    int measure = 0;
    for (int index = fromLine; index < toLine + 1; index++) {
      measure += lines.get(index - 1).getInt(metric);
    }
    return measure;
  }

  public Set<Integer> getNoSonarTagLines() {
    return noSonarTagLines;
  }

  private void processLines() {
    for (Line line : lines) {
      computeBlankLine(line);
      computeHeaderCommentLine(line);
      computeCommentLine(line);
      computeCommentBlankLine(line);
      computeLineOfCode(line);
      computeNoSonarTag(line);
      line.deleteLineContent();
    }
  }

  private void computeNoSonarTag(Line line) {
    if (line.isThereNoSonarTag()) {
      noSonarTagLines.add(line.getLineIndex());
    }
  }

  private void computeLineOfCode(Line line) {
    if (line.isThereCode()) {
      line.setMeasure(Metric.LINES_OF_CODE, 1);
    }
  }

  private void computeHeaderCommentLine(Line line) {
    if (line.isThereComment() && !line.isThereBlankComment() && line.isThereLicenseHeaderComment()) {
      line.setMeasure(Metric.HEADER_COMMENT_LINES, 1);
    }
  }

  private void computeCommentLine(Line line) {
    if (line.isThereComment() && !line.isThereBlankComment()) {
      if (line.isThereDoc() || line.isThereLicenseHeaderComment()) {
        line.setMeasure(Metric.COMMENT_LINES, 1);
        return;
      }

      boolean isCommentedOutCode = codeRecognizer.isLineOfCode(line.getComment());
      if (!isCommentedOutCode) {
        line.setMeasure(Metric.COMMENT_LINES, 1);
      } else {
        line.setMeasure(Metric.COMMENTED_OUT_CODE_LINES, 1);
      }
    }
  }

  private void computeBlankLine(Line line) {
    if (line.isBlank()) {
      line.setMeasure(Metric.BLANK_LINES, 1);
    }
  }

  private void computeCommentBlankLine(Line line) {
    if (line.isThereBlankComment()) {
      line.setMeasure(Metric.COMMENT_BLANK_LINES, 1);
    }
  }
}
