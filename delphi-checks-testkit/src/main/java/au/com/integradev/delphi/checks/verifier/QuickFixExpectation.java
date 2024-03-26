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
package au.com.integradev.delphi.checks.verifier;

import java.util.Collections;
import java.util.List;

class QuickFixExpectation {
  private final List<TextEditExpectation> expectedTextEdits;
  private final String fixId;

  public QuickFixExpectation(String fixId, List<TextEditExpectation> expectedTextEdits) {
    this.fixId = fixId;
    this.expectedTextEdits = expectedTextEdits;
  }

  public List<TextEditExpectation> getExpectedTextEdits() {
    return Collections.unmodifiableList(expectedTextEdits);
  }

  public String getFixId() {
    return fixId;
  }
}
