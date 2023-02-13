package au.com.integradev.delphi.antlr.ast.node;

import java.util.List;

public interface GenericArgumentsNode extends DelphiNode {
  List<TypeNode> getTypeArguments();
}
