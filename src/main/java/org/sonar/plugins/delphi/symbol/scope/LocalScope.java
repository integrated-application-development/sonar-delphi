package org.sonar.plugins.delphi.symbol.scope;

import com.google.common.base.Preconditions;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration;

public class LocalScope extends AbstractDelphiScope {

  @Override
  public void addDeclaration(NameDeclaration nameDecl) {
    Preconditions.checkArgument(
        nameDecl instanceof VariableNameDeclaration,
        "A LocalScope can only contain variables. Tried to add "
            + nameDecl.getClass()
            + " ("
            + nameDecl
            + ")");
    super.addDeclaration(nameDecl);
  }

  @Override
  public String toString() {
    return "LocalScope:" + glomNames(getVariableDeclarations().keySet());
  }
}
