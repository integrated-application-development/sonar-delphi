package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameter;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Typed;

public final class PropertyNode extends DelphiNode implements Typed, Visibility {
  private VisibilityType visibility;
  private Type type;

  public PropertyNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public VisibilityType getVisibility() {
    if (visibility == null) {
      visibility = ((VisibilitySectionNode) jjtGetParent()).getVisibility();
    }
    return visibility;
  }

  @Override
  @NotNull
  public Type getType() {
    if (type == null) {
      TypeNode typeNode = getTypeNode();
      type = (typeNode == null) ? DelphiType.unknownType() : typeNode.getType();
    }
    return type;
  }

  public NameDeclarationNode getPropertyName() {
    return (NameDeclarationNode) jjtGetChild(0);
  }

  public TypeNode getTypeNode() {
    return getFirstChildOfType(TypeNode.class);
  }

  @Nullable
  public FormalParameterListNode getParameterListNode() {
    Node node = jjtGetChild(1);
    return (node instanceof FormalParameterListNode) ? (FormalParameterListNode) node : null;
  }

  @Nullable
  public PropertyReadSpecifierNode getReadSpecifier() {
    return getFirstChildOfType(PropertyReadSpecifierNode.class);
  }

  @Nullable
  public PropertyWriteSpecifierNode getWriteSpecifier() {
    return getFirstChildOfType(PropertyWriteSpecifierNode.class);
  }

  public List<FormalParameter> getParameters() {
    FormalParameterListNode paramList = getParameterListNode();
    return (paramList == null) ? Collections.emptyList() : paramList.getParameters();
  }

  public List<Type> getParameterTypes() {
    FormalParameterListNode paramList = getParameterListNode();
    return (paramList == null) ? Collections.emptyList() : paramList.getParameterTypes();
  }

  public boolean isClassProperty() {
    return getFirstChildWithId(DelphiLexer.CLASS) != null;
  }

  public boolean isDefaultProperty() {
    return getFirstChildWithId(DelphiLexer.DEFAULT) != null;
  }
}
