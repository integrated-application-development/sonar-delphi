package org.sonar.plugins.delphi.type.generic;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.GenerifiableDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypedDeclaration;
import org.sonar.plugins.delphi.type.Type;

public final class TypeSpecializationContext {
  private static final Comparator<Type> COMPARATOR = Comparator.comparing(Type::getImage);
  private final Map<Type, Type> argumentsByParameter;

  public TypeSpecializationContext(NameDeclaration declaration, List<Type> typeArguments) {
    argumentsByParameter = new TreeMap<>(COMPARATOR);

    if (!(declaration instanceof GenerifiableDeclaration)) {
      return;
    }

    List<Type> typeParameters =
        ((GenerifiableDeclaration) declaration)
            .getTypeParameters().stream()
                .map(TypedDeclaration::getType)
                .collect(Collectors.toList());

    if (typeParameters.size() != typeArguments.size()) {
      return;
    }

    for (int i = 0; i < typeParameters.size(); ++i) {
      Type parameter = typeParameters.get(i);
      Type argument = typeArguments.get(i);
      argumentsByParameter.put(parameter, argument);
    }
  }

  @Nullable
  public Type getArgument(Type parameter) {
    return argumentsByParameter.get(parameter);
  }

  public boolean hasSignatureMismatch() {
    return argumentsByParameter.isEmpty();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TypeSpecializationContext that = (TypeSpecializationContext) o;
    return argumentsByParameter.equals(that.argumentsByParameter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(argumentsByParameter);
  }
}
