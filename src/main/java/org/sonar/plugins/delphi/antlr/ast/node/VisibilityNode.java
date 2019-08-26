package org.sonar.plugins.delphi.antlr.ast.node;

import static org.sonar.plugins.delphi.antlr.ast.node.Visibility.VisibilityType.PRIVATE;
import static org.sonar.plugins.delphi.antlr.ast.node.Visibility.VisibilityType.PROTECTED;
import static org.sonar.plugins.delphi.antlr.ast.node.Visibility.VisibilityType.PUBLIC;
import static org.sonar.plugins.delphi.antlr.ast.node.Visibility.VisibilityType.PUBLISHED;
import static org.sonar.plugins.delphi.antlr.ast.node.Visibility.VisibilityType.STRICT_PRIVATE;
import static org.sonar.plugins.delphi.antlr.ast.node.Visibility.VisibilityType.STRICT_PROTECTED;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.DelphiParser;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class VisibilityNode extends DelphiNode implements Visibility {
  public VisibilityNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public VisibilityType getVisibility() {
    switch (jjtGetId()) {
      case DelphiLexer.PUBLISHED:
        return PUBLISHED;
      case DelphiLexer.PUBLIC:
        return PUBLIC;
      case DelphiLexer.PROTECTED:
        return jjtGetChildId(0) == DelphiLexer.STRICT ? STRICT_PROTECTED : PROTECTED;
      case DelphiLexer.PRIVATE:
        return jjtGetChildId(0) == DelphiLexer.STRICT ? STRICT_PRIVATE : PRIVATE;
      default:
        throw new AssertionError(
            "Visibility node has unexpected token type: " + DelphiParser.tokenNames[jjtGetId()]);
    }
  }
}
