package org.sonar.plugins.delphi.antlr.ast.node;

import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Iterables.getLast;

import java.util.List;
import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.DelphiEnumerationType;
import org.sonar.plugins.delphi.type.Type;

public final class EnumTypeNode extends TypeNode {
  public EnumTypeNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public List<EnumElementNode> getElements() {
    return findChildrenOfType(EnumElementNode.class);
  }

  @Override
  @NotNull
  public Type createType() {
    Node parent = jjtGetParent();
    String image;

    if (parent instanceof TypeDeclarationNode) {
      image = ((TypeDeclarationNode) parent).fullyQualifiedName();
    } else {
      image = makeAnonymousImage(this);
    }

    return DelphiEnumerationType.enumeration(image, getScope());
  }

  private static String makeAnonymousImage(EnumTypeNode typeNode) {
    List<EnumElementNode> elements = typeNode.getElements();
    EnumElementNode first = getFirst(elements, null);
    if (first == null) {
      return "Enumeration";
    }

    return "Enumeration(" + first.getImage() + ".." + getLast(elements).getImage();
  }
}
