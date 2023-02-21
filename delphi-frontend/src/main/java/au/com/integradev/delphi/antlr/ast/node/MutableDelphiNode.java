package au.com.integradev.delphi.antlr.ast.node;

import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;

public interface MutableDelphiNode extends DelphiNode {
  void jjtSetFirstToken(DelphiToken token);

  void jjtSetLastToken(DelphiToken token);

  void jjtSetParent(DelphiNode parent);

  void setScope(DelphiScope scope);
}
