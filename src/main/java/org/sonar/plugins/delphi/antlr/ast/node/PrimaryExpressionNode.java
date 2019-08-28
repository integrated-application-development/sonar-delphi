package org.sonar.plugins.delphi.antlr.ast.node;

import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.CollectionType;
import org.sonar.plugins.delphi.type.Type.PointerType;
import org.sonar.plugins.delphi.type.Typed;

public final class PrimaryExpressionNode extends ExpressionNode {
  private String image;

  public PrimaryExpressionNode(Token token) {
    super(token);
  }

  public PrimaryExpressionNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public boolean isInheritedCall() {
    return jjtGetChildId(0) == DelphiLexer.INHERITED;
  }

  public boolean isBareInherited() {
    return jjtGetNumChildren() == 1 && isInheritedCall();
  }

  @Override
  public String getImage() {
    if (image == null) {
      StringBuilder imageBuilder = new StringBuilder();
      for (int i = 0; i < jjtGetNumChildren(); ++i) {
        imageBuilder.append(jjtGetChild(i).getImage());
      }
      image = imageBuilder.toString();
    }
    return image;
  }

  @Override
  @NotNull
  public Type createType() {
    Type type = DelphiType.unknownType();
    for (int i = 0; i < jjtGetNumChildren(); ++i) {
      Node child = jjtGetChild(i);
      if (child instanceof Typed) {
        type = ((Typed) child).getType();
      } else if (child instanceof ArrayAccessorNode) {
        type = handleArrayAccessor(type, (ArrayAccessorNode) child);
      } else if (child instanceof CommonDelphiNode) {
        type = handleSyntaxToken(type, child.jjtGetId());
      }
    }
    return type;
  }

  private static Type handleArrayAccessor(Type type, ArrayAccessorNode accessor) {
    int accesses = accessor.getExpressions().size();
    for (int i = 0; i < accesses; ++i) {
      if (type instanceof CollectionType) {
        type = ((CollectionType) type).elementType();
      } else {
        type = DelphiType.unknownType();
      }
    }
    return type;
  }

  private static Type handleSyntaxToken(Type type, int id) {
    switch (id) {
      case DelphiLexer.DOT:
        if (type instanceof PointerType) {
          // This caters to Delphi Extended syntax.
          // See: http://docwiki.embarcadero.com/RADStudio/Rio/en/Extended_syntax_(Delphi)
          return ((PointerType) type).dereferencedType();
        }
        return type;

      case DelphiLexer.POINTER:
        if (type instanceof PointerType) {
          return ((PointerType) type).dereferencedType();
        }
        break;

      default:
        // Do nothing
    }
    return DelphiType.unknownType();
  }
}
