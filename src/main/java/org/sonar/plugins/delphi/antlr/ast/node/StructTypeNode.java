package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.List;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.type.Type;

public abstract class StructTypeNode extends TypeNode {
  protected StructTypeNode(Token token) {
    super(token);
  }

  public List<VisibilitySectionNode> getVisibilitySections() {
    return findChildrenOfType(VisibilitySectionNode.class);
  }

  @NotNull
  @Override
  public Type createType() {
    return getTypeFactory().struct(this);
  }
}
