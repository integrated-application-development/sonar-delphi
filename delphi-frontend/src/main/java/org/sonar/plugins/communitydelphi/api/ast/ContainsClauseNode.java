package org.sonar.plugins.communitydelphi.api.ast;

import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;

public interface ContainsClauseNode extends ImportClauseNode {
  @Override
  <T> T accept(DelphiParserVisitor<T> visitor, T data);
}
