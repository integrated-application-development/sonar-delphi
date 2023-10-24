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
package org.sonar.plugins.communitydelphi.api.reporting;

import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuickFix {
  private final String description;
  private final List<QuickFixEdit> edits;

  private QuickFix(String description, List<QuickFixEdit> edits) {
    this.description = description;
    this.edits = edits;
  }

  public String getDescription() {
    return description;
  }

  public List<QuickFixEdit> getEdits() {
    return Collections.unmodifiableList(edits);
  }

  public QuickFix withEdit(QuickFixEdit edit) {
    edits.add(edit);
    return this;
  }

  public QuickFix withEdits(QuickFixEdit... edits) {
    return withEdits(List.of(edits));
  }

  public QuickFix withEdits(List<QuickFixEdit> edit) {
    edits.addAll(edit);
    return this;
  }

  public static QuickFix newFix(String description) {
    return new QuickFix(description, new ArrayList<>());
  }

  @FormatMethod
  public static QuickFix newFix(@FormatString String description, Object... args) {
    return newFix(String.format(description, args));
  }
}
