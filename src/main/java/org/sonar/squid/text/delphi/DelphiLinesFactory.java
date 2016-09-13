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

import org.sonar.squidbridge.api.AnalysisException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for parsing source code lines and calculating statistic for each line.
 */
public class DelphiLinesFactory {

  private final List<Line> lines = new ArrayList<Line>();
  private char lastReadCharacter;
  private StringBuilder currentStringBuilder = new StringBuilder();
  private Line currentLine;
  private static final char LF = '\n';
  private static final char CR = '\r';
  private static final int EOF = -1;
  private LineContextHandler currentHandler;
  private LineContextHandler[] handlers;

  /**
   * Constructor. Calculates line statistics
   * 
   * @param reader File to read
   */
  DelphiLinesFactory(Reader reader) {
    List<LineContextHandler> tmpHandlers = new ArrayList<LineContextHandler>();

    // //comment
    tmpHandlers.add(new SingleLineCommentHandler("//", "*//"));
    // {comment} and {** documentation **}
    tmpHandlers.add(new DelphiCommentHandler("{", "}", true));
    // (*comment*)
    tmpHandlers.add(new DelphiCommentHandler("(*", "*)", false));
    tmpHandlers.add(new LiteralValueHandler('\''));
    tmpHandlers.add(new LiteralValueHandler('"'));
    this.handlers = tmpHandlers.toArray(new LineContextHandler[tmpHandlers.size()]);
    fillLines(new BufferedReader(reader));
  }

  private void fillLines(Reader reader) {
    try {
      currentLine = new Line(1);
      int nextChar;
      do {
        nextChar = reader.read();
        if (isEndOfFile(nextChar)) {
          notifyHandlersAboutEndOfLine();
          break;
        }
        lastReadCharacter = (char) nextChar;
        if (isEndOfLine(nextChar)) {
          popOptionalRemainingEndOfLineChar(reader);
          createNewLine();
          continue;
        }
        appendToStringBuilder(nextChar);
        notifyHandlersAboutNewChar();
      } while (true);
    } catch (IOException e) {
      throw new AnalysisException("Unable to read the source code.", e);
    } catch (Exception e) {
      throw new AnalysisException("A problem was encountered when analyzing line " + lines.size() + " : '"
        + currentStringBuilder.toString() + "'", e);
    }
  }

  private void popOptionalRemainingEndOfLineChar(Reader reader) throws IOException {
    reader.mark(1);
    char nextChar = (char) reader.read();
    reader.reset();
    if (isTechnicalCharacter(nextChar) && lastReadCharacter != nextChar) {
      reader.read();
    }
  }

  private void notifyHandlersAboutNewChar() {
    if (currentHandler == null) {
      for (LineContextHandler handler : handlers) {
        if (handler.matchToBegin(currentLine, currentStringBuilder)) {
          currentHandler = handler;
          break;
        }
      }
    } else if (currentHandler.matchToEnd(currentLine, currentStringBuilder)) {
      currentHandler = null;
    }
  }

  private void notifyHandlersAboutEndOfLine() {
    if (currentHandler != null && currentHandler.matchWithEndOfLine(currentLine, currentStringBuilder)) {
      currentHandler = null;
    }
  }

  private void createNewLine() {
    notifyHandlersAboutEndOfLine();
    currentLine.setString(currentStringBuilder);
    lines.add(currentLine);
    currentLine = new Line(lines.size() + 1);
    currentStringBuilder = new StringBuilder();
  }

  private void appendToStringBuilder(int nextChar) {
    if (!isTechnicalCharacter(nextChar)) {
      currentStringBuilder.append((char) nextChar);
    }
  }

  private boolean isEndOfFile(int nextChar) {
    return nextChar == EOF && currentStringBuilder.length() == 0 && lastReadCharacter != LF;
  }

  private boolean isEndOfLine(int nextChar) {
    return nextChar == EOF || (char) nextChar == LF || (char) nextChar == CR;
  }

  private boolean isTechnicalCharacter(int nextChar) {
    return nextChar == LF || nextChar == CR || nextChar == EOF;
  }

  List<Line> getLines() {
    return lines;
  }
}
