package org.sonar.plugins.delphi.symbol.declaration;

import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.symbol.SymbolicNode;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.type.TypeSpecializationContext;

public interface DelphiNameDeclaration extends NameDeclaration, Comparable<DelphiNameDeclaration> {

  @Override
  SymbolicNode getNode();

  @Override
  DelphiScope getScope();

  /**
   * If applicable, returns a new declaration with any relevant generic types specialized. Also
   * attaches a reference to the original generic declaration.
   *
   * @param context information about the type arguments and parameters
   * @return specialized declaration
   */
  DelphiNameDeclaration specialize(TypeSpecializationContext context);

  boolean isSpecializedDeclaration();

  DelphiNameDeclaration getGenericDeclaration();

  void setGenericDeclaration(DelphiNameDeclaration genericDeclaration);

  @Nullable
  DelphiNameDeclaration getForwardDeclaration();

  void setForwardDeclaration(DelphiNameDeclaration declaration);

  void setIsForwardDeclaration();

  boolean isForwardDeclaration();
}
