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

import au.com.integradev.delphi.file.DelphiFile.DelphiInputFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;

class Expectations {
  private static final Pattern NONCOMPLIANT_PATTERN =
      Pattern.compile("(?i)^//\\s*Noncompliant(?:@([-+]\\d+))?\\b(.+)?");

  private static final Pattern FLOW_PATTERN = Pattern.compile("\\s*\\(([^)]*)\\)");
  private static final Pattern FLOW_LINE_OFFSET_PATTERN =
      Pattern.compile("\\s*,?\\s*([+-]?\\d+)\\b");

  private static final Pattern QUICK_FIX_RANGE_PATTERN =
      Pattern.compile("^([+-]\\d+):(\\d*) to ([+-]\\d+):(\\d*)$");

  private static final Pattern QUICK_FIX_PATTERN =
      Pattern.compile("(?i)^//\\s*Fix\\s*(\\w+)?@\\s*\\[([^]]*)]\\s*(?:<<(.*?)>>)?");

  private final List<IssueExpectation> expectedIssues;
  private final List<QuickFixExpectation> expectedQuickFixes;

  private Expectations(List<IssueExpectation> issues, List<QuickFixExpectation> quickFixes) {
    this.expectedIssues = issues;
    this.expectedQuickFixes = quickFixes;
  }

  public List<IssueExpectation> issues() {
    return Collections.unmodifiableList(expectedIssues);
  }

  public List<QuickFixExpectation> quickFixes() {
    return Collections.unmodifiableList(expectedQuickFixes);
  }

  public static Expectations fromComments(DelphiInputFile delphiFile) {
    return parse(delphiFile.getComments());
  }

  private static Expectations parse(List<DelphiToken> comments) {
    List<MatchResultOnLine> noncompliantComments = new ArrayList<>();
    List<MatchResultOnLine> fixComments = new ArrayList<>();

    for (DelphiToken comment : comments) {
      var matcher = NONCOMPLIANT_PATTERN.matcher(comment.getImage());
      if (matcher.matches()) {
        noncompliantComments.add(
            new MatchResultOnLine(matcher.toMatchResult(), comment.getBeginLine()));
      } else {
        matcher = QUICK_FIX_PATTERN.matcher(comment.getImage());
        if (matcher.matches()) {
          fixComments.add(new MatchResultOnLine(matcher.toMatchResult(), comment.getBeginLine()));
        }
      }
    }

    List<IssueExpectation> issues =
        noncompliantComments.stream()
            .map(r -> parseNoncompliantComment(r.getLine(), r.getMatchResult()))
            .collect(Collectors.toList());

    List<QuickFixExpectation> quickFixes =
        fixComments.stream()
            .map(r -> parseFixComment(r.getLine(), r.getMatchResult()))
            .collect(Collectors.groupingBy(TextEditExpectation::getFixId))
            .entrySet()
            .stream()
            .map(entry -> new QuickFixExpectation(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());

    return new Expectations(issues, quickFixes);
  }

  private static TextEditExpectation parseFixComment(int offset, MatchResult matchResult) {
    String fixId = matchResult.group(1);
    String rangeStr = matchResult.group(2);
    String replacementStr = matchResult.group(3);

    if (replacementStr == null) {
      throw new AssertionError(
          String.format(
              "Replacement text must be specified for quick fix edit on line %d", offset));
    }

    var rangeMatcher = QUICK_FIX_RANGE_PATTERN.matcher(rangeStr);

    if (!rangeMatcher.matches()) {
      throw new AssertionError(
          String.format("Invalid range '%s' for quick fix edit on line %d", rangeStr, offset));
    }

    String beginLineStr = rangeMatcher.group(1);
    String beginColumnStr = rangeMatcher.group(2);
    String endLineStr = rangeMatcher.group(3);
    String endColumnStr = rangeMatcher.group(4);

    int beginLine = beginLineStr != null ? Integer.parseInt(beginLineStr) : 0;
    int endLine = endLineStr != null ? Integer.parseInt(endLineStr) : 0;

    return new TextEditExpectation(
        fixId == null ? "(unnamed)" : fixId,
        replacementStr.replace("\\n", "\n").replace("\\r", "\r"),
        beginLine + offset,
        endLine + offset,
        Integer.parseInt(beginColumnStr),
        Integer.parseInt(endColumnStr));
  }

  private static IssueExpectation parseNoncompliantComment(int beginLine, MatchResult matchResult) {
    String offset = matchResult.group(1);
    String flows = matchResult.group(2);

    int lineOffset = parseIssueOffset(offset);
    List<List<Integer>> flowLines = parseFlows(flows);

    return new IssueExpectation(beginLine + lineOffset, flowLines);
  }

  private static int parseIssueOffset(String offset) {
    if (offset == null) {
      return 0;
    }

    try {
      return Integer.parseInt(offset);
    } catch (NumberFormatException e) {
      throw new AssertionError(
          String.format(
              "Failed to parse 'Noncompliant' comment line offset '%s' as an integer.", offset));
    }
  }

  private static List<List<Integer>> parseFlows(String flows) {
    List<List<Integer>> flowLines = new ArrayList<>();
    if (flows == null) {
      return flowLines;
    }
    Matcher flowMatcher = FLOW_PATTERN.matcher(flows);
    while (flowMatcher.find()) {
      String flow = flowMatcher.group(1);
      List<Integer> lines = parseFlowLines(flow);
      if (!lines.isEmpty()) {
        flowLines.add(lines);
      }
    }
    flowLines.sort(Comparator.comparing(list -> list.get(0)));
    return flowLines;
  }

  private static List<Integer> parseFlowLines(String flow) {
    List<Integer> lines = new ArrayList<>();
    if (flow == null) {
      return lines;
    }
    Matcher lineMatcher = FLOW_LINE_OFFSET_PATTERN.matcher(flow);
    while (lineMatcher.find()) {
      String flowLineOffset = lineMatcher.group(1);
      try {
        lines.add(Integer.parseInt(flowLineOffset));
      } catch (NumberFormatException e) {
        throw new AssertionError(
            String.format(
                "Failed to parse 'Noncompliant' flow line offset '%s' as an integer.",
                flowLineOffset));
      }
    }
    return lines;
  }

  private static class MatchResultOnLine {
    private final MatchResult matchResult;
    private final int line;

    public MatchResultOnLine(MatchResult result, int line) {
      this.matchResult = result;
      this.line = line;
    }

    public MatchResult getMatchResult() {
      return matchResult;
    }

    public int getLine() {
      return line;
    }
  }
}
