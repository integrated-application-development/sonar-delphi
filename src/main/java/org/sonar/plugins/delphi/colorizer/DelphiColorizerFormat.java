/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
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
package org.sonar.plugins.delphi.colorizer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.sonar.api.web.CodeColorizerFormat;
import org.sonar.colorizer.CDocTokenizer;
import org.sonar.colorizer.KeywordsTokenizer;
import org.sonar.colorizer.MultilinesDocTokenizer;
import org.sonar.colorizer.StringTokenizer;
import org.sonar.colorizer.Tokenizer;
import org.sonar.plugins.delphi.core.DelphiLanguage;

/**
 * Class used to colorize DelphiLanguage source code in Sonar window.
 */
public class DelphiColorizerFormat extends CodeColorizerFormat {

  private static final String SPAN_GREEN = "<span style='color:green; font-style: italic;'>";
  private static final String SPAN_RED = "<span style='color:red; font-style: italic;'>";
  private static final String SPAN_END = "</span>";

  /**
   * ctor
   */
  public DelphiColorizerFormat() {
    super(DelphiLanguage.KEY);
  }

  @Override
  public List<Tokenizer> getTokenizers() {
    return Collections.unmodifiableList(Arrays.asList(new StringTokenizer(SPAN_RED, SPAN_END), new CDocTokenizer(SPAN_GREEN, SPAN_END),
        new CustomTokenizer("{", "}", SPAN_GREEN, SPAN_END), new CustomTokenizer2("(*", "*)", SPAN_GREEN, SPAN_END), new KeywordsTokenizer(
            "<span class=\"k\">", SPAN_END, DelphiKeywords.get())));
  }
}

/**
 * Custom tokenizer
 */
class CustomTokenizer extends MultilinesDocTokenizer {

  /**
   * ctor
   */
  public CustomTokenizer(String start, String end, String tagBefore, String tagAfter) {
    super(start, end, tagBefore, tagAfter);
  }
}

// has no sense, very redundant, but works (with one class in list, code was formatted in a wrong way)
class CustomTokenizer2 extends MultilinesDocTokenizer {

  /**
   * ctor
   */
  public CustomTokenizer2(String start, String end, String tagBefore, String tagAfter) {
    super(start, end, tagBefore, tagAfter);
  }
}
