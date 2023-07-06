package au.com.integradev.delphi.utils;

import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;

public class IndentationUtils {
  private IndentationUtils() {
    // Utility class
  }

  /**
   * Gets the leading whitespace of a source code line.
   *
   * @param node Any node starting on the indented line.
   * @return a string containing the leading whitespace.
   */
  public static String getLineIndentation(DelphiNode node) {
    return getLineIndentation(
        node.getAst().getDelphiFile().getSourceCodeFilesLines().get(node.getBeginLine() - 1));
  }

  /**
   * Gets the leading whitespace of a string.
   *
   * @param line the string to search.
   * @return a string containing the leading whitespace.
   */
  private static String getLineIndentation(String line) {
    for (int i = 0; i < line.length(); i++) {
      if (line.charAt(i) != '\t' && line.charAt(i) != ' ') {
        return line.substring(0, i);
      }
    }

    return line;
  }
}
