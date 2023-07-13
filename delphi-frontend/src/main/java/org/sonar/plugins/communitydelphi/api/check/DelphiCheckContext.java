package org.sonar.plugins.communitydelphi.api.check;

import au.com.integradev.delphi.preprocessor.CompilerSwitchRegistry;
import au.com.integradev.delphi.reporting.DelphiIssueBuilder;
import java.util.List;
import java.util.Objects;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.directive.CompilerDirectiveParser;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

/** Context injected in check classes and used to report issues. */
public interface DelphiCheckContext {
  /**
   * Returns the parsed ast of the current file.
   *
   * @return parsed ast of the current file
   */
  DelphiAst getAst();

  /**
   * Returns the raw tokens of the current file.
   *
   * @return list of file tokens
   */
  List<DelphiToken> getTokens();

  /**
   * Returns the lines of the currently analyzed file.
   *
   * @return list of file lines
   */
  List<String> getFileLines();

  /**
   * Returns the compiler switch registry
   *
   * @return compiler switch registry
   */
  CompilerSwitchRegistry getCompilerSwitchRegistry();

  /**
   * Returns a compiler directive parser
   *
   * @return compiler directive parser
   */
  CompilerDirectiveParser getCompilerDirectiveParser();

  /**
   * Returns the type factory
   *
   * @return type factory
   */
  TypeFactory getTypeFactory();

  /**
   * Creates and returns a new issue builder
   *
   * @return issue builder
   */
  DelphiIssueBuilder newIssue();

  final class Location {
    private final String message;
    private final FilePosition filePosition;

    public Location(String message, DelphiNode node) {
      this.message = message;
      this.filePosition = FilePosition.from(node);
    }

    public Location(String message, FilePosition filePosition) {
      this.message = message;
      this.filePosition = filePosition;
    }

    public String getMessage() {
      return message;
    }

    public FilePosition getFilePosition() {
      return filePosition;
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof Location)) {
        return false;
      }
      Location location = (Location) other;
      return Objects.equals(message, location.message)
          && Objects.equals(filePosition, location.filePosition);
    }

    @Override
    public int hashCode() {
      return Objects.hash(message, filePosition);
    }
  }
}
