package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.List;
import java.util.stream.Collectors;
import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class FormalParameterNode extends DelphiNode {
  private List<FormalParameter> parameters;

  public FormalParameterNode(Token token) {
    super(token);
  }

  public FormalParameterNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public List<FormalParameter> getParameters() {
    if (parameters == null) {
      IdentifierListNode identifierList = (IdentifierListNode) jjtGetChild(0);
      Node argType = identifierList.nextNode();
      String typeImage = (argType instanceof TypeNode) ? argType.getImage() : "UNTYPED";

      parameters =
          identifierList.getIdentifiers().stream()
              .map(identifier -> new FormalParameter(identifier, typeImage))
              .collect(Collectors.toList());
    }
    return parameters;
  }

  public static class FormalParameter {
    private final DelphiNode node;
    private final String typeImage;

    public FormalParameter(DelphiNode node, String typeImage) {
      this.node = node;
      this.typeImage = typeImage;
    }

    public DelphiNode getNode() {
      return node;
    }

    public String getTypeImage() {
      return typeImage;
    }

    public String getImage() {
      return node.getImage();
    }
  }
}
