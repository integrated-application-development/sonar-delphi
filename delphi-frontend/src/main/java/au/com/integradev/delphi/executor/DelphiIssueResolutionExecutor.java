/*
 * Sonar Delphi Plugin
 * Copyright (C) 2026 Integrated Application Development
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
package au.com.integradev.delphi.executor;

import au.com.integradev.delphi.file.DelphiFile.DelphiInputFile;
import com.google.common.base.Splitter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;
import org.sonarsource.analyzer.commons.SonarResolve;
import org.sonarsource.analyzer.commons.SonarResolve.StreamingParser;
import org.sonarsource.analyzer.commons.SonarResolve.StreamingParser.State;

public class DelphiIssueResolutionExecutor implements Executor {
  private static final Logger LOG = LoggerFactory.getLogger(DelphiIssueResolutionExecutor.class);
  private static final Splitter NEW_LINE_SPLITTER = Splitter.on(Pattern.compile("\r\n?|\n"));

  @Override
  public void execute(Context context, DelphiInputFile delphiFile) {
    SensorContext sensorContext = context.sensorContext();
    InputFile inputFile = delphiFile.getInputFile();

    List<CommentLine> lineCommentRun = new ArrayList<>();

    for (DelphiToken comment : delphiFile.getComments()) {
      if (isLineComment(comment)) {
        CommentLine line =
            new CommentLine(comment.getBeginLine(), stripCommentDelimiters(comment.getImage()));
        if (!lineCommentRun.isEmpty() && shouldEndRun(lineCommentRun, line)) {
          processLines(sensorContext, inputFile, lineCommentRun);
          lineCommentRun.clear();
        }
        lineCommentRun.add(line);
      } else {
        if (!lineCommentRun.isEmpty()) {
          processLines(sensorContext, inputFile, lineCommentRun);
          lineCommentRun.clear();
        }
        processLines(sensorContext, inputFile, toCommentLines(comment));
      }
    }

    if (!lineCommentRun.isEmpty()) {
      processLines(sensorContext, inputFile, lineCommentRun);
    }
  }

  private static boolean isLineComment(DelphiToken comment) {
    return comment.getImage().startsWith("//");
  }

  private static boolean shouldEndRun(List<CommentLine> run, CommentLine nextLine) {
    CommentLine lastLine = run.get(run.size() - 1);
    return lastLine.lineNumber + 1 != nextLine.lineNumber || isDirectiveLine(nextLine.content);
  }

  private static boolean isDirectiveLine(String content) {
    return Strings.CI.startsWith(content.stripLeading(), SonarResolve.KEYWORD);
  }

  private static void processLines(
      SensorContext sensorContext, InputFile inputFile, List<CommentLine> lines) {
    for (int index = 0; index < lines.size(); index++) {
      CommentLine line = lines.get(index);
      if (!isDirectiveLine(line.content)) {
        continue;
      }

      StreamingParser parser = new StreamingParser(line.lineNumber);
      int consumedIndex = index;
      State state = parser.consumeLine(line.lineNumber, line.content);

      while (state == State.INCOMPLETE && consumedIndex + 1 < lines.size()) {
        CommentLine nextLine = lines.get(consumedIndex + 1);
        if (isDirectiveLine(nextLine.content)) {
          break;
        }
        ++consumedIndex;
        state = parser.consumeLine(nextLine.lineNumber, nextLine.content);
      }

      if (state == State.INCOMPLETE) {
        state = parser.finish();
      }

      if (state == State.COMPLETE) {
        saveResolution(sensorContext, inputFile, parser.result());
      } else if (state == State.INVALID) {
        LOG.warn(
            "Ignoring invalid {} directive in {} at line {}: {}",
            SonarResolve.KEYWORD,
            inputFile,
            line.lineNumber,
            parser.errorMessage());
      }

      index = consumedIndex;
    }
  }

  private static void saveResolution(
      SensorContext sensorContext, InputFile inputFile, SonarResolve sonarResolve) {
    sensorContext
        .newIssueResolution()
        .on(inputFile)
        .at(inputFile.selectLine(sonarResolve.targetLine()))
        .status(sonarResolve.status())
        .forRules(sonarResolve.ruleKeys())
        .comment(sonarResolve.justification())
        .save();
  }

  private static List<CommentLine> toCommentLines(DelphiToken comment) {
    int firstLine = comment.getBeginLine();
    String commentText = stripCommentDelimiters(comment.getImage());
    List<String> lines = NEW_LINE_SPLITTER.splitToList(commentText);

    return IntStream.range(0, lines.size())
        .mapToObj(index -> new CommentLine(firstLine + index, lines.get(index)))
        .collect(Collectors.toUnmodifiableList());
  }

  private static String stripCommentDelimiters(String image) {
    if (image.startsWith("//")) {
      return image.substring(2);
    }
    if (image.startsWith("(*") && image.endsWith("*)")) {
      return image.substring(2, image.length() - 2);
    }
    if (image.startsWith("{") && image.endsWith("}")) {
      return image.substring(1, image.length() - 1);
    }
    return image;
  }

  private static final class CommentLine {
    private final int lineNumber;
    private final String content;

    private CommentLine(int lineNumber, String content) {
      this.lineNumber = lineNumber;
      this.content = content;
    }
  }
}
