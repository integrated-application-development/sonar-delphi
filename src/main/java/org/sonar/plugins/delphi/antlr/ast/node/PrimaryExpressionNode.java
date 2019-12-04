package org.sonar.plugins.delphi.antlr.ast.node;

import static org.sonar.plugins.delphi.type.DelphiType.unknownType;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.ANSICHAR;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.WIDECHAR;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.node.MethodHeadingNode.MethodKind;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
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
    Type type = unknownType();
    for (int i = 0; i < jjtGetNumChildren(); ++i) {
      Node child = jjtGetChild(i);
      if (isConstructor(child)) {
        continue;
      }

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

  private static boolean isConstructor(Node node) {
    if (node instanceof NameReferenceNode) {
      DelphiNameDeclaration declaration = ((NameReferenceNode) node).getNameDeclaration();
      return declaration instanceof MethodNameDeclaration
          && ((MethodNameDeclaration) declaration).getMethodKind() == MethodKind.CONSTRUCTOR;
    }
    return false;
  }

  private static Type handleArrayAccessor(Type type, ArrayAccessorNode accessor) {
    int accesses = accessor.getExpressions().size();
    for (int i = 0; i < accesses; ++i) {
      if (accessor.getImplicitNameOccurrence() != null) {
        type = handleNameOccurrence(accessor.getImplicitNameOccurrence());
      } else if (type instanceof CollectionType) {
        type = ((CollectionType) type).elementType();
      } else if (type.isNarrowString()) {
        type = ANSICHAR.type;
      } else if (type.isWideString()) {
        type = WIDECHAR.type;
      } else {
        type = unknownType();
      }
    }
    return type;
  }

  private static Type handleNameOccurrence(DelphiNameOccurrence occurrence) {
    NameDeclaration declaration = occurrence.getNameDeclaration();
    if (declaration instanceof Typed) {
      return ((Typed) declaration).getType();
    }
    return unknownType();
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
    return unknownType();
  }
}
