package org.sonar.plugins.communitydelphi.api.ast;

import au.com.integradev.delphi.antlr.ast.token.DelphiToken;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;

public interface MutableDelphiNode extends DelphiNode {
  void jjtSetFirstToken(DelphiToken token);

  void jjtSetLastToken(DelphiToken token);

  void jjtSetParent(DelphiNode parent);

  void setScope(DelphiScope scope);
}
