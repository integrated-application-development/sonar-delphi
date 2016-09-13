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

import org.junit.Before;
import org.junit.Test;
import org.sonar.squidbridge.measures.Metric;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LineTest {

  private boolean isEndOfLine(char nextChar) {
    return (nextChar == '\n') || (nextChar == '\r');
  }

  private void runHandler(LineContextHandler handler, Line line) {
    char at = ' ';
    int index = 0;
    StringBuilder pendingLine = new StringBuilder();
    boolean isMatch = false;
    do {
      at = line.getString().charAt(index++);

      // EOF
      if (at == '\0') {
        handler.matchWithEndOfLine(line, pendingLine);
        break;
      } else {
        pendingLine.append(at);
      }

      // EOL
      if (isEndOfLine(at)) {
        handler.matchWithEndOfLine(line, pendingLine);
        pendingLine = new StringBuilder();
        continue;
      }

      if (!isMatch) {
        isMatch = handler.matchToBegin(line, pendingLine);
      } else if (handler.matchToEnd(line, pendingLine)) {
        break; // when handler succesfully parsed, end parsing
      }

    } while (index < line.getString().length());
  }

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testMultiLineCommentHandler() {
    // test lines
    Line line4 = new Line("(* comment *)\n\0");
    Line line5 = new Line("(* comment \n comment \n commment *)\0");
    Line line6 = new Line("/* comment */\n\0");
    Line line7 = new Line("{ comment \n comment \n commment }\0");
    Line line8 = new Line("{** function documentation 1 blah blah }\n\0");

    // handlers
    LineContextHandler multiLineHandler1 = new DelphiCommentHandler("(*", "*)", false);
    LineContextHandler multiLineHandler2 = new DelphiCommentHandler("{", "}", false);
    LineContextHandler docHandler = new DelphiCommentHandler("{", "}", true);

    // run
    runHandler(multiLineHandler1, line4);
    runHandler(multiLineHandler1, line5);
    runHandler(multiLineHandler2, line6);
    runHandler(multiLineHandler2, line7);
    runHandler(docHandler, line8);

    // asserts
    assertThat(line4.isThereComment(), is(true));
    assertThat(line5.isThereComment(), is(true));
    assertThat(line6.isThereComment(), is(false));
    assertThat(line7.isThereComment(), is(true));
    assertThat(line7.getInt(Metric.PUBLIC_DOC_API), is(0));
    assertThat(line8.isThereComment(), is(true));
    assertThat(line8.getInt(Metric.PUBLIC_DOC_API), is(1));
  }



  @Test
  public void testSingleLineCommentHandler() {
    // test lines
    Line line1 = new Line("//test\0");
    Line line2 = new Line("//test longer comment\0");
    Line line3 = new Line("codeLine; //comment int i = 5;\0");

    // handlers
    LineContextHandler singleLineHandler = new SingleLineCommentHandler("//", "*//");

    // run
    runHandler(singleLineHandler, line1);
    runHandler(singleLineHandler, line2);
    runHandler(singleLineHandler, line3);

    // asserts
    assertThat(line1.isThereComment(), is(true));
    assertThat(line2.isThereComment(), is(true));
    assertThat(line3.isThereComment(), is(true));
  }

}
