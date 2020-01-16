package org.sonar.plugins.delphi.pmd;

import com.google.common.base.Preconditions;
import org.sonar.plugins.delphi.antlr.ast.DelphiToken;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;

public abstract class FilePosition {
  public static final int UNDEFINED_LINE = 0;
  public static final int UNDEFINED_COLUMN = -1;

  public static FilePosition atFileLevel() {
    return new FileLevelPosition();
  }

  public static FilePosition from(int beginLine, int beginColumn, int endLine, int endColumn) {
    return new PrecisePosition(beginLine, beginColumn, endLine, endColumn);
  }

  public static FilePosition from(DelphiNode node) {
    return from(node.getBeginLine(), node.getBeginColumn(), node.getEndLine(), node.getEndColumn());
  }

  public static FilePosition from(DelphiToken token) {
    return from(
        token.getBeginLine(), token.getBeginColumn(), token.getEndLine(), token.getEndColumn());
  }

  public abstract int getBeginLine();

  public abstract int getEndLine();

  public abstract int getBeginColumn();

  public abstract int getEndColumn();

  private static class PrecisePosition extends FilePosition {

    private int beginLine;
    private int beginColumn;
    private int endLine;
    private int endColumn;

    PrecisePosition(int beginLine, int beginColumn, int endLine, int endColumn) {
      Preconditions.checkArgument(beginLine != UNDEFINED_LINE, "Imaginary token!");
      Preconditions.checkArgument(beginColumn != UNDEFINED_COLUMN, "Imaginary token!");
      Preconditions.checkArgument(endLine != UNDEFINED_LINE, "Imaginary token!");
      Preconditions.checkArgument(endColumn != UNDEFINED_COLUMN, "Imaginary token!");
      this.beginLine = beginLine;
      this.beginColumn = beginColumn;
      this.endLine = endLine;
      this.endColumn = endColumn;
    }

    @Override
    public int getBeginLine() {
      return beginLine;
    }

    @Override
    public int getEndLine() {
      return endLine;
    }

    @Override
    public int getBeginColumn() {
      return beginColumn;
    }

    @Override
    public int getEndColumn() {
      return endColumn;
    }
  }

  private static class FileLevelPosition extends FilePosition {
    @Override
    public int getBeginLine() {
      return UNDEFINED_LINE;
    }

    @Override
    public int getEndLine() {
      return UNDEFINED_LINE;
    }

    @Override
    public int getBeginColumn() {
      return UNDEFINED_COLUMN;
    }

    @Override
    public int getEndColumn() {
      return UNDEFINED_COLUMN;
    }
  }
}
