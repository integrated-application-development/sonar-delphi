package org.sonar.plugins.delphi.symbol;

import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;

public class DelphiNameOccurrence implements NameOccurrence {

  private final DelphiNode location;
  private final String image;
  private DelphiNameDeclaration declaration;
  private DelphiNameOccurrence qualifiedName;
  private boolean isExplicitInvocation;
  private boolean isInherited;

  private static final String SELF = "Self";

  public DelphiNameOccurrence(DelphiNode location, String image) {
    this.location = location;
    this.image = image;
  }

  @Override
  public DelphiNode getLocation() {
    return location;
  }

  @Override
  public String getImage() {
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

  public void setIsInherited() {
    isInherited = true;
  }

  public boolean isInherited() {
    return isInherited;
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
        + location.getClass()
        + "> ";
  }
}
