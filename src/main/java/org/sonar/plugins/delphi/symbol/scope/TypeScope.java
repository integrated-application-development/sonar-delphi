package org.sonar.plugins.delphi.symbol.scope;

import static org.sonar.plugins.delphi.symbol.scope.UnknownScope.unknownScope;
import static org.sonar.plugins.delphi.type.DelphiType.unknownType;

import java.util.Set;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.type.DelphiGenerifiableType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.HelperType;
import org.sonar.plugins.delphi.type.Type.ScopedType;
import org.sonar.plugins.delphi.type.TypeSpecializationContext;
import org.sonar.plugins.delphi.type.Typed;

public class TypeScope extends AbstractDelphiScope implements Typed {
  private Type type = unknownType();
  private DelphiScope superTypeScope = unknownScope();

  public void setType(Type type) {
    this.type = type;

    Type superType = type.superType();
    if (superType instanceof ScopedType) {
      this.superTypeScope = ((ScopedType) superType).typeScope();
    }
  }

  public DelphiScope getSuperTypeScope() {
    return superTypeScope;
  }

  @NotNull
  @Override
  public Type getType() {
    return type;
  }

  @Nullable
  @Override
  protected DelphiScope overloadSearchScope() {
    // A helper type doesn't search for overloads in its ancestors.
    // Instead, it searches for overloads in the extended type.
    // See: https://wiki.freepascal.org/Helper_types#Method_hiding
    if (type.isHelper()) {
      HelperType helperType = ((HelperType) type);
      Type extendedType = helperType.extendedType();
      if (extendedType instanceof ScopedType) {
        return ((ScopedType) extendedType).typeScope();
      }
      return null;
    }
    return superTypeScope;
  }

  @Override
  public String toString() {
    return type.getImage() + " <TypeScope>";
  }

  public static TypeScope specializedScope(
      DelphiScope scope,
      DelphiGenerifiableType specializedType,
      TypeSpecializationContext context) {
    SpecializedTypeScope result = new SpecializedTypeScope(scope, context);
    result.setType(specializedType);
    return result;
  }

  /**
   * Specialized type scopes just wrap a generic type's "real" scope. Name occurrences of
   * specialized declarations are forwarded to their generic declarations in the real scope.
   */
  private static class SpecializedTypeScope extends TypeScope {
    private final DelphiScope genericScope;

    private SpecializedTypeScope(DelphiScope scope, TypeSpecializationContext context) {
      this.genericScope = scope;
      scope.getAllDeclarations().stream()
          .map(DelphiNameDeclaration.class::cast)
          .map(declaration -> declaration.specialize(context))
          .forEach(super::addDeclaration);
    }

    @Override
    public void addDeclaration(NameDeclaration declaration) {
      throw new UnsupportedOperationException("Can't add declarations to a specialized type scope");
    }

    @Override
    public Set<NameDeclaration> addNameOccurrence(@NotNull NameOccurrence occurrence) {
      return genericScope.addNameOccurrence(occurrence);
    }
  }
}
