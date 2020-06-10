package org.sonar.plugins.delphi.symbol.scope;

import static org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration.compilerVariable;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicBoolean.BOOLEAN;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicDecimal.EXTENDED;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.INTEGER;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.LONGINT;

import java.util.Arrays;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.plugins.delphi.symbol.QualifiedName;
import org.sonar.plugins.delphi.symbol.SymbolicNode;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicBoolean;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicDecimal;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicMethod;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicMethodData;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicPointer;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicText;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicVariant;

public class SystemScope extends AbstractFileScope {
  private TypeNameDeclaration objectDeclaration;
  private TypeNameDeclaration interfaceDeclaration;
  private TypeNameDeclaration varRecDeclaration;
  private TypeNameDeclaration classHelperBase;

  public SystemScope() {
    super("System");
    this.injectIntrinsicTypes();
    this.injectConstants();
  }

  private void injectIntrinsicTypes() {
    Arrays.stream(IntrinsicBoolean.values())
        .map(typeEnum -> typeEnum.type)
        .forEach(this::injectIntrinsicType);

    Arrays.stream(IntrinsicVariant.values())
        .map(typeEnum -> typeEnum.type)
        .forEach(this::injectIntrinsicType);

    Arrays.stream(IntrinsicMethod.values())
        .map(typeEnum -> typeEnum.data)
        .forEach(this::injectIntrinsicMethodType);

    Arrays.stream(IntrinsicInteger.values())
        .forEach(integer -> injectIntrinsicType(integer.image, integer.type));

    Arrays.stream(IntrinsicDecimal.values())
        .forEach(decimal -> injectIntrinsicType(decimal.image, decimal.type));

    Arrays.stream(IntrinsicText.values())
        .forEach(text -> injectIntrinsicType(text.image, text.type));

    Arrays.stream(IntrinsicPointer.values())
        .forEach(pointer -> injectIntrinsicType(pointer.image, pointer.type));
  }

  private void injectConstants() {
    injectConstant("CompilerVersion", EXTENDED.type);
    injectConstant("MaxInt", INTEGER.type);
    injectConstant("MaxLongInt", LONGINT.type);
    injectConstant("True", BOOLEAN.type);
    injectConstant("False", BOOLEAN.type);
  }

  private void injectIntrinsicType(Type type) {
    injectIntrinsicType(type.getImage(), type);
  }

  private void injectIntrinsicType(String image, Type type) {
    SymbolicNode node = SymbolicNode.imaginary(image, this);
    QualifiedName qualifiedName = QualifiedName.of("System", image);
    TypeNameDeclaration declaration = new TypeNameDeclaration(node, type, qualifiedName);

    this.addDeclaration(declaration);
  }

  private void injectIntrinsicMethodType(IntrinsicMethodData data) {
    SymbolicNode node = SymbolicNode.imaginary(data.getMethodName(), this);
    MethodNameDeclaration declaration = MethodNameDeclaration.create(node, data);

    this.addDeclaration(declaration);
  }

  private void injectConstant(String image, Type type) {
    this.addDeclaration(compilerVariable(image, type, this));
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
