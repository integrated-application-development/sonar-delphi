/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package org.sonar.plugins.communitydelphi.api.check;

import com.google.common.base.Preconditions;
import java.io.Serializable;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;

public abstract class FilePosition implements Serializable {
  public static final int UNDEFINED_LINE = 0;
  public static final int UNDEFINED_COLUMN = -1;

  public static FilePosition atLineLevel(int line) {
    return new LineLevelPosition(line, line);
  }

  public static FilePosition atLineLevel(int beginLine, int endLine) {
    return new LineLevelPosition(beginLine, endLine);
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

    private final int beginLine;
    private final int beginColumn;
    private final int endLine;
    private final int endColumn;

    PrecisePosition(int beginLine, int beginColumn, int endLine, int endColumn) {
      Preconditions.checkArgument(beginLine != UNDEFINED_LINE);
      Preconditions.checkArgument(beginColumn != UNDEFINED_COLUMN);
      Preconditions.checkArgument(endLine != UNDEFINED_LINE);
      Preconditions.checkArgument(endColumn != UNDEFINED_COLUMN);
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

  private static class LineLevelPosition extends FilePosition {

    private final int beginLine;
    private final int endLine;

    LineLevelPosition(int beginLine, int endLine) {
      Preconditions.checkArgument(beginLine != UNDEFINED_LINE);
      Preconditions.checkArgument(endLine != UNDEFINED_LINE);
      this.beginLine = beginLine;
      this.endLine = endLine;
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
      return UNDEFINED_COLUMN;
    }

    @Override
    public int getEndColumn() {
      return UNDEFINED_COLUMN;
    }
  }
}
