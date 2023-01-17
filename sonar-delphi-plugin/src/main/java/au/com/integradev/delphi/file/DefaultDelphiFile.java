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
package au.com.integradev.delphi.file;

import au.com.integradev.delphi.antlr.ast.DelphiAST;
import au.com.integradev.delphi.antlr.ast.token.DelphiToken;
import au.com.integradev.delphi.preprocessor.CompilerSwitchRegistry;
import au.com.integradev.delphi.type.factory.TypeFactory;
import com.google.common.base.Preconditions;
import java.io.File;
import java.util.List;
import java.util.Set;

class DefaultDelphiFile implements DelphiFile {
  private File sourceCodeFile;
  private List<String> sourceCodeLines;
  private DelphiAST ast;
  private List<DelphiToken> tokens;
  private List<DelphiToken> comments;
  private Set<Integer> suppressions;
  private CompilerSwitchRegistry switchRegistry;
  private TypeFactory typeFactory;

  DefaultDelphiFile() {
    // package-private constructor
  }

  @Override
  public File getSourceCodeFile() {
    return sourceCodeFile;
  }

  @Override
  public String getSourceCodeLine(int line) {
    int index = line - 1;
    Preconditions.checkPositionIndex(index, sourceCodeLines.size());
    return sourceCodeLines.get(index);
  }

  @Override
  public DelphiAST getAst() {
    return ast;
  }

  @Override
  public List<DelphiToken> getTokens() {
    return tokens;
  }

  @Override
  public List<DelphiToken> getComments() {
    return comments;
  }

  @Override
  public Set<Integer> getSuppressions() {
    return suppressions;
  }

  @Override
  public CompilerSwitchRegistry getCompilerSwitchRegistry() {
    return switchRegistry;
  }

  @Override
  public TypeFactory getTypeFactory() {
    return typeFactory;
  }

  void setSourceCodeFile(File sourceCodeFile) {
    this.sourceCodeFile = sourceCodeFile;
  }

  void setSourceCodeLines(List<String> sourceCodeLines) {
    this.sourceCodeLines = sourceCodeLines;
  }

  void setAst(DelphiAST ast) {
    this.ast = ast;
  }

  void setTokens(List<DelphiToken> tokens) {
    this.tokens = tokens;
  }

  void setComments(List<DelphiToken> comments) {
    this.comments = comments;
  }

  void setSuppressions(Set<Integer> suppressions) {
    this.suppressions = suppressions;
  }

  void setCompilerSwitchRegistry(CompilerSwitchRegistry switchRegistry) {
    this.switchRegistry = switchRegistry;
  }

  void setTypeFactory(TypeFactory typeFactory) {
    this.typeFactory = typeFactory;
  }
}
