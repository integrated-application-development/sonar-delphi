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
package au.com.integradev.delphi.file;

import au.com.integradev.delphi.preprocessor.CompilerSwitchRegistry;
import java.io.File;
import java.util.List;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

class DefaultDelphiFile implements DelphiFile {
  private File sourceCodeFile;
  private List<String> sourceCodeLines;
  private DelphiAst ast;
  private List<DelphiToken> tokens;
  private List<DelphiToken> comments;
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
  public List<String> getSourceCodeFilesLines() {
    return sourceCodeLines;
  }

  @Override
  public DelphiAst getAst() {
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
    this.sourceCodeLines = List.copyOf(sourceCodeLines);
  }

  void setAst(DelphiAst ast) {
    this.ast = ast;
  }

  void setTokens(List<DelphiToken> tokens) {
    this.tokens = List.copyOf(tokens);
  }

  void setComments(List<DelphiToken> comments) {
    this.comments = List.copyOf(comments);
  }

  void setCompilerSwitchRegistry(CompilerSwitchRegistry switchRegistry) {
    this.switchRegistry = switchRegistry;
  }

  void setTypeFactory(TypeFactory typeFactory) {
    this.typeFactory = typeFactory;
  }
}
