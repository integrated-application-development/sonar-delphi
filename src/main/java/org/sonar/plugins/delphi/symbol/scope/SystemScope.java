package org.sonar.plugins.delphi.symbol.scope;

import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.type.factory.TypeFactory;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicsInjector;

public class SystemScope extends AbstractFileScope {
  private TypeNameDeclaration objectDeclaration;
  private TypeNameDeclaration interfaceDeclaration;
  private TypeNameDeclaration varRecDeclaration;
  private TypeNameDeclaration classHelperBase;

  public SystemScope(TypeFactory typeFactory) {
    super("System");
    injectIntrinsics(typeFactory);
  }

  private void injectIntrinsics(TypeFactory typeFactory) {
    IntrinsicsInjector injector = new IntrinsicsInjector(typeFactory);
    injector.inject(this);
  }

  @Override
  public void addDeclaration(NameDeclaration declaration) {
    if (declaration instanceof TypeNameDeclaration) {
      TypeNameDeclaration typeDeclaration = (TypeNameDeclaration) declaration;
      switch (typeDeclaration.getImage()) {
        case "TObject":
          this.objectDeclaration = typeDeclaration;
          break;

        case "IInterface":
          this.interfaceDeclaration = typeDeclaration;
          break;

        case "TVarRec":
          this.varRecDeclaration = typeDeclaration;
          break;

        case "TClassHelperBase":
          this.classHelperBase = typeDeclaration;
          break;

        default:
          // Do nothing
      }
    }

    super.addDeclaration(declaration);
  }

  public TypeNameDeclaration getTObjectDeclaration() {
    return objectDeclaration;
  }

  public TypeNameDeclaration getIInterfaceDeclaration() {
    return interfaceDeclaration;
  }

  public TypeNameDeclaration getTVarRecDeclaration() {
    return varRecDeclaration;
  }

  public TypeNameDeclaration getTClassHelperBaseDeclaration() {
    return classHelperBase;
  }

  @Override
  public SystemScope getSystemScope() {
    return this;
  }
}
