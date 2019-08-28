package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.ArrayList;
import java.util.List;
import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.symbol.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.symbol.Qualifiable;
import org.sonar.plugins.delphi.symbol.QualifiedName;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Typed;

public final class NameReferenceNode extends DelphiNode implements Qualifiable, Typed {
  private DelphiNameDeclaration declaration;
  private DelphiNameOccurrence occurrence;
  private List<NameReferenceNode> names;
  private QualifiedName qualifiedName;

  public NameReferenceNode(Token token) {
    super(token);
  }

  public NameReferenceNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public List<NameReferenceNode> flatten() {
    if (names == null) {
      names = new ArrayList<>();
      NameReferenceNode name = this;
      while (name != null) {
        names.add(name);
        name = name.nextName();
      }
    }
    return names;
  }

  public IdentifierNode getIdentifier() {
    return (IdentifierNode) jjtGetChild(0);
  }

  @Override
  public QualifiedName getQualifiedName() {
    if (qualifiedName == null) {
      List<String> nameParts = new ArrayList<>();
      StringBuilder builder = new StringBuilder();

      for (NameReferenceNode part : flatten()) {
        builder.append(part.getIdentifier().getImage());

        GenericDefinitionNode generic = part.getGenericDefinition();
        if (generic != null) {
          builder.append(generic.getImage());
        }

        nameParts.add(builder.toString());
        builder.setLength(0);
      }

      qualifiedName = new QualifiedName(nameParts);
    }
    return qualifiedName;
  }

  public GenericDefinitionNode getGenericDefinition() {
    Node generic = jjtGetChild(1);
    return (generic instanceof GenericDefinitionNode) ? (GenericDefinitionNode) generic : null;
  }

  public NameReferenceNode nextName() {
    Node child = jjtGetChild(jjtGetNumChildren() - 1);
    return (child instanceof NameReferenceNode) ? (NameReferenceNode) child : null;
  }

  @Override
  public String getImage() {
    return fullyQualifiedName();
  }

  public void setNameOccurrence(DelphiNameOccurrence occurrence) {
    this.occurrence = occurrence;
  }

  public DelphiNameOccurrence getNameOccurrence() {
    return occurrence;
  }

  public DelphiNameDeclaration getNameDeclaration() {
    if (declaration == null && getNameOccurrence() != null) {
      declaration = getNameOccurrence().getNameDeclaration();
    }
    return declaration;
  }

  @Override
  @NotNull
  public Type getType() {
    if (getNameDeclaration() instanceof Typed) {
      return ((Typed) getNameDeclaration()).getType();
    } else {
      return DelphiType.unknownType();
    }
  }

  public NameReferenceNode getLastName() {
    List<NameReferenceNode> flatNames = flatten();
    return flatNames.get(flatNames.size() - 1);
  }
}
