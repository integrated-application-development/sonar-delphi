package au.com.integradev.delphi.utils;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;

public final class TextRangeUtils {
  private static final Logger LOG = Loggers.get(TextRangeUtils.class);

  private TextRangeUtils() {
    // utility class
  }

  public static TextRange fromFilePosition(FilePosition filePosition, InputFile inputFile) {
    TextRange result = null;

    if (filePosition.getBeginLine() != FilePosition.UNDEFINED_LINE) {
      if (filePosition.getBeginColumn() != FilePosition.UNDEFINED_COLUMN) {
        result = calculateAccurateRange(filePosition, inputFile);
      }

      if (result == null) {
        result = calculateLineRange(filePosition, inputFile);
      }
    }

    return result;
  }

  private static TextRange calculateAccurateRange(FilePosition filePosition, InputFile inputFile) {
    int beginLine = filePosition.getBeginLine();
    int endLine = filePosition.getEndLine();
    int beginColumn = filePosition.getBeginColumn();
    int endColumn = filePosition.getEndColumn();

    try {
      return inputFile.newRange(beginLine, beginColumn, endLine, endColumn);
    } catch (IllegalArgumentException e) {
      LOG.debug(
          "file: {} beginLine: {} beginCol: {} endLine: {} endCol: {}",
          inputFile,
          beginLine,
          beginColumn,
          endLine,
          endColumn);
      LOG.debug("Error while creating issue highlighting text range: ", e);
    }

    return null;
  }

  private static TextRange calculateLineRange(FilePosition filePosition, InputFile inputFile) {
    final int startLine = calculateSafeBeginLine(filePosition);
    final int endLine = calculateSafeEndLine(filePosition);
    final TextPointer startPointer = inputFile.selectLine(startLine).start();
    final TextPointer endPointer = inputFile.selectLine(endLine).end();

    return inputFile.newRange(startPointer, endPointer);
  }

  private static int calculateSafeEndLine(FilePosition filePosition) {
    return Math.max(filePosition.getBeginLine(), filePosition.getEndLine());
  }

  private static int calculateSafeBeginLine(FilePosition filePosition) {
    int minLine = Math.min(filePosition.getBeginLine(), filePosition.getEndLine());
    return minLine > 0 ? minLine : calculateSafeEndLine(filePosition);
  }
}
