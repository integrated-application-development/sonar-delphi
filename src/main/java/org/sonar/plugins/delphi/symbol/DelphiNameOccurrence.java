package org.sonar.plugins.delphi.symbol;

import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;

public class DelphiNameOccurrence implements NameOccurrence {

  private final SymbolicNode location;
  private DelphiNameDeclaration declaration;
  private DelphiNameOccurrence qualifiedName;
  private String image;
  private boolean isExplicitInvocation;

  private static final String SELF = "Self";

  public DelphiNameOccurrence(DelphiNode concreteNode, String imageOverride) {
    this(concreteNode);
    this.image = imageOverride;
  }

  public DelphiNameOccurrence(DelphiNode concreteNode) {
    this.location = new SymbolicNode(concreteNode, concreteNode.getScope());
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

  /**
   * Simply return true is the image is equal to reserved word 'Self'
   *
   * @return return true if image equal to 'Self'
   */
  public boolean isSelf() {
    return SELF.equals(image);
  }

  @Override
  public final boolean equals(Object o) {
    if (o instanceof DelphiNameOccurrence) {
      DelphiNameOccurrence n = (DelphiNameOccurrence) o;
      return n.getImage().equalsIgnoreCase(getImage());
    }
    return false;
  }

  @Override
  public final int hashCode() {
    return getImage().hashCode();
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
