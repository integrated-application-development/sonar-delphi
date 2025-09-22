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
package au.com.integradev.delphi.antlr.ast.visitors;

import au.com.integradev.delphi.antlr.ast.visitors.MetricsVisitor.Data;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.sonar.plugins.communitydelphi.api.ast.ClassTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.RoutineBodyNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;

/**
 * Visitor that collects various code metrics from Delphi source code.
 *
 * <p>This visitor traverses the AST and collects metrics including:
 *
 * <ul>
 *   <li>Number of classes, routines, and statements
 *   <li>Cyclomatic and cognitive complexity
 *   <li>Code and comment line counts
 * </ul>
 */
public class MetricsVisitor implements DelphiParserVisitor<Data> {
  private static final Pattern NEW_LINE_PATTERN = Pattern.compile("\r\n|\n|\r");

  /**
   * Container class for metrics collected from Delphi source code.
   *
   * <p>This class holds various metrics gathered during AST traversal, including:
   * <ul>
   *   <li>Number of classes ({@code classes})</li>
   *   <li>Number of routines ({@code routines})</li>
   *   <li>Cyclomatic complexity ({@code complexity})</li>
   *   <li>Cognitive complexity ({@code cognitiveComplexity})</li>
   *   <li>Set of code line numbers ({@code codeLines})</li>
   *   <li>Set of comment line numbers ({@code commentLines})</li>
   *   <li>Number of statements ({@code statements})</li>
   * </ul>
   * <p>These metrics are used to assess code quality and complexity.
   */
  public static class Data {
    private int classes;
    private int routines;
    private int complexity;
    private int cognitiveComplexity;
    private final Set<Integer> codeLines = new HashSet<>();
    private final Set<Integer> commentLines = new HashSet<>();
    private int statements;

    public int getClasses() {
      return classes;
    }

    public int getRoutines() {
      return routines;
    }

    public int getComplexity() {
      return complexity;
    }

    public int getCognitiveComplexity() {
      return cognitiveComplexity;
    }

    public int getCommentLines() {
      return commentLines.size();
    }

    public int getStatements() {
      return statements;
    }

    public Set<Integer> getCodeLines() {
      return codeLines;
    }
  }

  @Override
  public Data visit(DelphiAst ast, Data data) {
    calculateComplexityMetrics(ast, data);
    return DelphiParserVisitor.super.visit(ast, data);
  }

  /**
   * Calculates both cyclomatic and cognitive complexity metrics for the given AST.
   *
   * @param ast the AST to analyze
   * @param data the data container to populate with complexity metrics
   */
  private void calculateComplexityMetrics(DelphiAst ast, Data data) {
    var cyclomaticVisitor = new CyclomaticComplexityVisitor();
    var cyclomaticComplexity = cyclomaticVisitor.visit(ast, new CyclomaticComplexityVisitor.Data());
    data.complexity = cyclomaticComplexity.getComplexity();

    var cognitiveVisitor = new CognitiveComplexityVisitor();
    var cognitiveComplexity = cognitiveVisitor.visit(ast, new CognitiveComplexityVisitor.Data());
    data.cognitiveComplexity = cognitiveComplexity.getComplexity();
  }

  @Override
  public void visitToken(DelphiToken token, Data data) {
    if (token.isComment()) {
      int line = token.getBeginLine();
      String[] commentLines = NEW_LINE_PATTERN.split(token.getImage(), -1);
      for (String commentLine : commentLines) {
        if (StringUtils.isNotBlank(commentLine)) {
          data.commentLines.add(line);
        }
        ++line;
      }
    } else if (!token.isImaginary() && !token.isWhitespace() && !token.isCompilerDirective()) {
      data.codeLines.add(token.getBeginLine());
    }
  }

  @Override
  public Data visit(RoutineImplementationNode routine, Data data) {
    ++data.routines;
    return DelphiParserVisitor.super.visit(routine, data);
  }

  @Override
  public Data visit(ClassTypeNode typeDecl, Data data) {
    ++data.classes;
    return DelphiParserVisitor.super.visit(typeDecl, data);
  }

  @Override
  public Data visit(StatementNode statement, Data data) {
    if (!(statement.getParent() instanceof RoutineBodyNode)) {
      ++data.statements;
    }
    return DelphiParserVisitor.super.visit(statement, data);
  }
}
