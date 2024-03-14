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
package au.com.integradev.delphi.preprocessor;

import java.util.ArrayList;
import java.util.List;

public class TextBlockLineEndingModeRegistry {
  private final TextBlockLineEndingMode initialLineEndingMode;
  private final List<LineEndingModeRegistration> registrations;

  TextBlockLineEndingModeRegistry(TextBlockLineEndingMode initialLineEndingMode) {
    this.initialLineEndingMode = initialLineEndingMode;
    this.registrations = new ArrayList<>();
  }

  void registerLineEndingMode(TextBlockLineEndingMode lineEndingMode, int startIndex) {
    registrations.add(new LineEndingModeRegistration(lineEndingMode, startIndex));
  }

  public TextBlockLineEndingMode getLineEndingMode(int tokenIndex) {
    for (int i = registrations.size() - 1; i >= 0; --i) {
      LineEndingModeRegistration registration = registrations.get(i);
      if (tokenIndex >= registration.getStartIndex()) {
        return registration.getLineEndingMode();
      }
    }
    return initialLineEndingMode;
  }

  private static final class LineEndingModeRegistration {
    private final TextBlockLineEndingMode lineEndingMode;
    private final int startIndex;

    public LineEndingModeRegistration(TextBlockLineEndingMode lineEndingMode, int startIndex) {
      this.lineEndingMode = lineEndingMode;
      this.startIndex = startIndex;
    }

    public TextBlockLineEndingMode getLineEndingMode() {
      return lineEndingMode;
    }

    public int getStartIndex() {
      return startIndex;
    }
  }
}
