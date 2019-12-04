package org.sonar.plugins.delphi.symbol.scope;

import java.util.Arrays;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.plugins.delphi.symbol.QualifiedName;
import org.sonar.plugins.delphi.symbol.SymbolicNode;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.type.DelphiIntrinsicType.BooleanType;
import org.sonar.plugins.delphi.type.DelphiIntrinsicType.DecimalType;
import org.sonar.plugins.delphi.type.DelphiIntrinsicType.IntegerType;
import org.sonar.plugins.delphi.type.DelphiIntrinsicType.TextType;
import org.sonar.plugins.delphi.type.Type;

public class SystemScope extends AbstractFileScope {
  private TypeNameDeclaration objectDeclaration;
  private TypeNameDeclaration interfaceDeclaration;

  public SystemScope() {
    super("System");
    this.injectIntrinsicTypes();
  }

  private void injectIntrinsicTypes() {
    Arrays.stream(IntegerType.values())
        .map(typeEnum -> typeEnum.type)
        .forEach(this::injectIntrinsicType);

    Arrays.stream(DecimalType.values())
        .map(typeEnum -> typeEnum.type)
        .forEach(this::injectIntrinsicType);

    Arrays.stream(BooleanType.values())
        .map(typeEnum -> typeEnum.type)
        .forEach(this::injectIntrinsicType);

    Arrays.stream(TextType.values())
        .map(typeEnum -> typeEnum.type)
        .forEach(this::injectIntrinsicType);
  }

  private void injectIntrinsicType(Type type) {
    SymbolicNode node = SymbolicNode.imaginary(type.getImage(), this);
    QualifiedName qualifiedName = QualifiedName.of("System", type.getImage());
    TypeNameDeclaration declaration = new TypeNameDeclaration(node, type, qualifiedName);

    this.addDeclaration(declaration);
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

  @Override
  public SystemScope getSystemScope() {
    return this;
  }
}
