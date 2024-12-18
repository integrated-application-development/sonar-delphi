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

import java.util.List;
import java.util.stream.Collectors;

class IssueExpectation {
  private final int beginLine;
  private final List<List<Integer>> flowLines;

  public IssueExpectation(int beginLine, List<List<Integer>> flowLines) {
    this.beginLine = beginLine;
    this.flowLines = flowLines;
  }

  public int getBeginLine() {
    return beginLine;
  }

  public List<List<Integer>> getFlowLines() {
    return flowLines;
  }

  @Override
  public String toString() {
    return "{"
        + beginLine
        + " "
        + flowLines.stream()
            .map(
                list ->
                    "("
                        + list.stream().map(Object::toString).collect(Collectors.joining(", "))
                        + ")")
            .collect(Collectors.joining(" "))
        + "}";
  }
}
