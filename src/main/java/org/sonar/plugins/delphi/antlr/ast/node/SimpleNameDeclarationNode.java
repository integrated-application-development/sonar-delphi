package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.Map;
import java.util.Objects;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class SimpleNameDeclarationNode extends NameDeclarationNode {
  private static final Map<Class<?>, DeclarationKind> PARENT_NODE_KIND_MAP =
      Map.of(
          ConstDeclarationNode.class, DeclarationKind.CONST,
          ConstStatementNode.class, DeclarationKind.INLINE_CONST,
          EnumElementNode.class, DeclarationKind.ENUM_ELEMENT,
          ExceptItemNode.class, DeclarationKind.EXCEPT_ITEM,
          ForLoopVarDeclarationNode.class, DeclarationKind.LOOP_VAR,
          MethodNameNode.class, DeclarationKind.METHOD,
          PropertyNode.class, DeclarationKind.PROPERTY,
          RecordVariantTagNode.class, DeclarationKind.RECORD_VARIANT_TAG,
          TypeDeclarationNode.class, DeclarationKind.TYPE,
          TypeParameterNode.class, DeclarationKind.TYPE_PARAMETER);

  private static final Map<Class<?>, DeclarationKind> GRANDPARENT_NODE_KIND_MAP =
      Map.of(
          VarDeclarationNode.class, DeclarationKind.VAR,
          FieldDeclarationNode.class, DeclarationKind.FIELD,
          FormalParameterNode.class, DeclarationKind.PARAMETER,
          VarStatementNode.class, DeclarationKind.INLINE_VAR);

  private String image;

  public SimpleNameDeclarationNode(Token token) {
    super(token);
  }

  public SimpleNameDeclarationNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public String getImage() {
    if (image == null) {
      GenericDefinitionNode genericDefinition = getGenericDefinition();
      StringBuilder builder = new StringBuilder();
      builder.append(getIdentifier().getImage());
      if (genericDefinition != null) {
        builder.append(genericDefinition.getImage());
      }
      image = builder.toString();
    }
    return image;
  }

  public IdentifierNode getIdentifier() {
    return (IdentifierNode) jjtGetChild(0);
  }

  @NotNull
  @Override
  public DeclarationKind getKind() {
    DeclarationKind kind = PARENT_NODE_KIND_MAP.get(parent.getClass());
    if (kind == null) {
      kind = GRANDPARENT_NODE_KIND_MAP.get(getNthParent(2).getClass());
    }
    return Objects.requireNonNull(kind, "Unhandled DeclarationKind");
  }
}
