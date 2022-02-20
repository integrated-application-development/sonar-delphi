package org.sonar.plugins.delphi.type.factory;

import static java.util.function.Predicate.not;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.symbol.declaration.MethodKind;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.symbol.scope.TypeScope;
import org.sonar.plugins.delphi.type.StructKind;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.StructType;
import org.sonar.plugins.delphi.type.generic.DelphiGenerifiableType;
import org.sonar.plugins.delphi.type.generic.TypeSpecializationContext;

class DelphiStructType extends DelphiGenerifiableType implements StructType {
  private final List<ImagePart> imageParts;
  private final int size;
  private DelphiScope scope;
  private Set<Type> parents;
  private StructKind kind;
  private Type superType;
  private Set<Type> typesWithImplicitConversionsToThis;
  private Set<Type> typesWithImplicitConversionsFromThis;
  private boolean isForwardType;

  DelphiStructType(
      List<ImagePart> imageParts, int size, DelphiScope scope, Set<Type> parents, StructKind kind) {
    this.imageParts = imageParts;
    this.size = size;
    this.scope = scope;
    this.kind = kind;
    setParents(parents);
  }

  private void setParents(Set<Type> parents) {
    this.parents = Set.copyOf(parents);
    if (isInterface()) {
      this.superType = Iterables.getFirst(parents, unknownType());
    } else {
      this.superType =
          this.parents.stream()
              .filter(DelphiStructType.class::isInstance)
              .filter(not(Type::isInterface))
              .findFirst()
              .orElse(unknownType());
    }
  }

  @Override
  public String getImage() {
    return imageParts.stream().map(ImagePart::toString).collect(Collectors.joining("."));
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isSubTypeOf(String image) {
    for (Type parent : parents) {
      if (parent.is(image) || parent.isSubTypeOf(image)) {
        return true;
      }
    }
    return false;
  }

  @NotNull
  @Override
  public Type superType() {
    return superType;
  }

  @Override
  public Set<Type> parents() {
    return parents;
  }

  @Override
  public boolean isClass() {
    return kind == StructKind.CLASS;
  }

  @Override
  public boolean isInterface() {
    return kind == StructKind.INTERFACE;
  }

  @Override
  public boolean isRecord() {
    return kind == StructKind.RECORD;
  }

  @Override
  public final boolean isStruct() {
    return true;
  }

  @Override
  @NotNull
  public DelphiScope typeScope() {
    return scope;
  }

  @Override
  public StructKind kind() {
    return kind;
  }

  @Override
  public boolean isForwardType() {
    return isForwardType;
  }

  @Override
  public void setFullType(StructType fullType) {
    this.scope = fullType.typeScope();
    this.parents = ImmutableSet.copyOf(fullType.parents());
    this.kind = fullType.kind();
    this.superType = fullType.superType();
    this.isForwardType = true;
  }

  @Override
  public Set<Type> typesWithImplicitConversionsFromThis() {
    if (typesWithImplicitConversionsFromThis == null) {
      indexImplicitConversions();
    }

    return typesWithImplicitConversionsFromThis;
  }

  @Override
  public Set<Type> typesWithImplicitConversionsToThis() {
    if (typesWithImplicitConversionsToThis == null) {
      indexImplicitConversions();
    }
    return typesWithImplicitConversionsToThis;
  }

  private void indexImplicitConversions() {
    ImmutableSet.Builder<Type> fromBuilder = ImmutableSet.builder();
    ImmutableSet.Builder<Type> toBuilder = ImmutableSet.builder();

    for (MethodNameDeclaration method : scope.getMethodDeclarations()) {
      if (method.getMethodKind() == MethodKind.OPERATOR
          && method.getName().equalsIgnoreCase("Implicit")
          && method.getParametersCount() == 1) {
        Type returnType = method.getReturnType();
        Type parameterType = method.getParameter(0).getType();
        if (returnType.is(this)) {
          toBuilder.add(parameterType);
        } else if (parameterType.is(this)) {
          fromBuilder.add(returnType);
        }
      }
    }

    typesWithImplicitConversionsToThis = toBuilder.build();
    typesWithImplicitConversionsFromThis = fromBuilder.build();
  }

  @Override
  public Set<NameDeclaration> findDefaultArrayProperties() {
    Set<NameDeclaration> result = new HashSet<>();
    findDefaultArrayProperties(this, result);
    return result;
  }

  @Override
  public boolean canBeSpecialized(TypeSpecializationContext context) {
    for (ImagePart part : imageParts) {
      for (Type parameter : part.getTypeParameters()) {
        if (parameter.isTypeParameter() && context.getArgument(parameter) != null) {
          return true;
        }

        if (parameter.canBeSpecialized(context)) {
          return true;
        }
      }
    }

    return parents.stream()
        .filter(DelphiStructType.class::isInstance)
        .map(DelphiStructType.class::cast)
        .anyMatch(parent -> parent.canBeSpecialized(context));
  }

  @Override
  public DelphiGenerifiableType doSpecialization(TypeSpecializationContext context) {
    return new DelphiStructType(
        imageParts.stream()
            .map(imagePart -> imagePart.specialize(context))
            .collect(Collectors.toUnmodifiableList()),
        size,
        scope,
        parents,
        kind);
  }

  @Override
  protected final void doAfterSpecialization(TypeSpecializationContext context) {
    this.setParents(
        parents.stream()
            .map(parent -> parent.specialize(context))
            .collect(Collectors.toUnmodifiableSet()));
    this.scope = TypeScope.specializedScope(scope, this, context);
  }

  private static void findDefaultArrayProperties(StructType type, Set<NameDeclaration> result) {
    type.typeScope().getPropertyDeclarations().stream()
        .filter(PropertyNameDeclaration::isArrayProperty)
        .filter(PropertyNameDeclaration::isDefaultProperty)
        .filter(
            declaration ->
                result.stream()
                    .map(PropertyNameDeclaration.class::cast)
                    .noneMatch(property -> property.hasSameParameterTypes(declaration)))
        .forEach(result::add);

    Type superType = type.superType();
    if (superType.isStruct()) {
      findDefaultArrayProperties((StructType) superType, result);
    }
  }

  static final class ImagePart {
    private final String name;
    private final List<Type> typeParameters;

    ImagePart(String name) {
      this(name, Collections.emptyList());
    }

    ImagePart(String name, List<Type> typeParameters) {
      this.name = name;
      this.typeParameters = typeParameters;
    }

    public List<Type> getTypeParameters() {
      return typeParameters;
    }

    private ImagePart specialize(TypeSpecializationContext context) {
      if (typeParameters.isEmpty()) {
        return this;
      }
      return new ImagePart(
          name,
          typeParameters.stream()
              .map(type -> type.specialize(context))
              .collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public String toString() {
      if (!typeParameters.isEmpty()) {
        return name
            + "<"
            + typeParameters.stream().map(Type::getImage).collect(Collectors.joining(","))
            + ">";
      }
      return name;
    }
  }
}
