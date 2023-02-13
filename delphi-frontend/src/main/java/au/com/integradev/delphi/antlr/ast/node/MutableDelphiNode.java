package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.antlr.ast.token.DelphiToken;
import au.com.integradev.delphi.symbol.scope.DelphiScope;

public interface MutableDelphiNode extends DelphiNode {
  void jjtSetFirstToken(DelphiToken token);

  void jjtSetLastToken(DelphiToken token);

  void jjtSetParent(DelphiNode parent);

  void setScope(DelphiScope scope);
}
