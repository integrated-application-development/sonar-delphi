package org.sonar.plugins.delphi.symbol;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.type.Type;

public class DelphiNameOccurrence implements NameOccurrence {
  private final SymbolicNode location;
  private DelphiNameDeclaration declaration;
  private DelphiNameOccurrence qualifiedName;
  private String image;
  private boolean isExplicitInvocation;
  private boolean isMethodReference;
  private boolean isGeneric;
  private List<Type> typeParameters = Collections.emptyList();

  public DelphiNameOccurrence(DelphiNode concreteNode, String imageOverride) {
    this(concreteNode);
    this.image = imageOverride;
  }

  public DelphiNameOccurrence(DelphiNode concreteNode) {
    this(new SymbolicNode(concreteNode));
  }

  public DelphiNameOccurrence(SymbolicNode symbolicNode) {
    this.location = symbolicNode;
  }

  @Override
  public SymbolicNode getLocation() {
    return location;
  }

  @Override
  public String getImage() {
    if (image == null) {
      return location.getImage();
    }
    return image;
  }

  public void setNameWhichThisQualifies(DelphiNameOccurrence qualifiedName) {
    this.qualifiedName = qualifiedName;
  }

  public DelphiNameOccurrence getNameForWhichThisIsAQualifier() {
    return qualifiedName;
  }

  public void setNameDeclaration(DelphiNameDeclaration declaration) {
    this.declaration = declaration;
  }

  public DelphiNameDeclaration getNameDeclaration() {
    return declaration;
  }

  public boolean isPartOfQualifiedName() {
    return qualifiedName != null;
  }

  public void setIsExplicitInvocation(boolean isExplicitInvocation) {
    this.isExplicitInvocation = isExplicitInvocation;
  }

  public boolean isExplicitInvocation() {
    return isExplicitInvocation;
  }

  public void setIsMethodReference() {
    this.isMethodReference = true;
  }

  public boolean isMethodReference() {
    return isMethodReference;
  }

  public void setIsGeneric() {
    this.isGeneric = true;
  }

  public boolean isGeneric() {
    return isGeneric;
  }

  public void setTypeArguments(List<Type> typeParameters) {
    this.typeParameters = typeParameters;
  }

  public List<Type> getTypeArguments() {
    return typeParameters;
  }

  public boolean isSelf() {
    return "Self".equals(getImage());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DelphiNameOccurrence)) {
      return false;
    }
    DelphiNameOccurrence that = (DelphiNameOccurrence) o;
    return isExplicitInvocation == that.isExplicitInvocation
        && isMethodReference == that.isMethodReference
        && isGeneric == that.isGeneric
        && location.equals(that.location)
        && Objects.equals(declaration, that.declaration)
        && Objects.equals(qualifiedName, that.qualifiedName)
        && image.equals(that.image)
        && typeParameters.equals(that.typeParameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        location,
        declaration,
        qualifiedName,
        image,
        isExplicitInvocation,
        isMethodReference,
        isGeneric,
        typeParameters);
  }

  @Override
  public String toString() {
    return getImage()
        + " ["
        + location.getBeginLine()
        + ","
        + location.getBeginColumn()
        + "] "
        + "<"
        + location.getUnitName()
        + "> ";
  }
}
