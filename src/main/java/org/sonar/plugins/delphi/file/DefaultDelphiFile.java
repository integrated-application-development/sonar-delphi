package org.sonar.plugins.delphi.file;

import com.google.common.base.Preconditions;
import java.io.File;
import java.util.List;
import java.util.Set;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.token.DelphiToken;
import org.sonar.plugins.delphi.preprocessor.CompilerSwitchRegistry;
import org.sonar.plugins.delphi.type.factory.TypeFactory;

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
  public String getSourceCodeLine(int index) {
    Preconditions.checkPositionIndex(--index, sourceCodeLines.size());
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
