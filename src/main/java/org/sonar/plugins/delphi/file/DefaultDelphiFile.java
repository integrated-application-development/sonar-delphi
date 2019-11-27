package org.sonar.plugins.delphi.file;

import com.google.common.base.Preconditions;
import java.io.File;
import java.util.List;
import java.util.Set;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.DelphiToken;

class DefaultDelphiFile implements DelphiFile {
  private File sourceCodeFile;
  private List<String> sourceCodeLines;
  private DelphiAST ast;
  private List<DelphiToken> tokens;
  private List<DelphiToken> comments;
  private Set<Integer> suppressions;

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
}
