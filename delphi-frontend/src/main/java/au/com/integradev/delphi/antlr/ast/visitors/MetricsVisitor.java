/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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

import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.ClassTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodBodyNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;
import au.com.integradev.delphi.antlr.ast.token.DelphiToken;
import au.com.integradev.delphi.antlr.ast.visitors.MetricsVisitor.Data;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

public class MetricsVisitor implements DelphiParserVisitor<Data> {
  public static class Data {
    private int classes;
    private int methods;
    private int complexity;
    private int cognitiveComplexity;
    private final Set<Integer> codeLines = new HashSet<>();
    private final Set<Integer> commentLines = new HashSet<>();
    private int statements;

    public int getClasses() {
      return classes;
    }

    public int getMethods() {
      return methods;
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
    var cyclomaticVisitor = new CyclomaticComplexityVisitor();
    var cyclomaticComplexity = cyclomaticVisitor.visit(ast, new CyclomaticComplexityVisitor.Data());
    data.complexity = cyclomaticComplexity.getComplexity();

    var cognitiveVisitor = new CognitiveComplexityVisitor();
    var cognitiveComplexity = cognitiveVisitor.visit(ast, new CognitiveComplexityVisitor.Data());
    data.cognitiveComplexity = cognitiveComplexity.getComplexity();

    return DelphiParserVisitor.super.visit(ast, data);
  }

  @Override
  public void visitToken(DelphiToken token, Data data) {
    if (token.isComment()) {
      int line = token.getBeginLine();
      String[] commentLines = token.getImage().split("\r\n|\n|\r", -1);
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
  public Data visit(MethodImplementationNode method, Data data) {
    ++data.methods;
    return DelphiParserVisitor.super.visit(method, data);
  }

  @Override
  public Data visit(ClassTypeNode typeDecl, Data data) {
    ++data.classes;
    return DelphiParserVisitor.super.visit(typeDecl, data);
  }

  @Override
  public Data visit(StatementNode statement, Data data) {
    if (!(statement.jjtGetParent() instanceof MethodBodyNode)) {
      ++data.statements;
    }
    return DelphiParserVisitor.super.visit(statement, data);
  }
}
