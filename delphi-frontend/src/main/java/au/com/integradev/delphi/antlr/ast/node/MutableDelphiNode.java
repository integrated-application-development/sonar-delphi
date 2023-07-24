package au.com.integradev.delphi.antlr.ast.node;

import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;

public interface MutableDelphiNode extends DelphiNode {
  void setFirstToken(DelphiToken token);

  void setLastToken(DelphiToken token);

  void setParent(DelphiNode parent);

  void addChild(DelphiNode child);

  void setChildIndex(int index);

  void setScope(DelphiScope scope);
}
