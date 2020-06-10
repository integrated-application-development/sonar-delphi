package org.sonar.plugins.delphi.type;

import static java.util.function.Predicate.not;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.GenericDefinitionNode.TypeParameter;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeNode;
import org.sonar.plugins.delphi.symbol.declaration.MethodKind;
import org.sonar.plugins.delphi.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypedDeclaration;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.symbol.scope.FileScope;
import org.sonar.plugins.delphi.symbol.scope.SystemScope;
import org.sonar.plugins.delphi.symbol.scope.TypeScope;
import org.sonar.plugins.delphi.type.Type.StructType;
import org.sonar.plugins.delphi.type.generic.DelphiGenerifiableType;
import org.sonar.plugins.delphi.type.generic.TypeSpecializationContext;

public class DelphiStructType extends DelphiGenerifiableType implements StructType {
  private static final AtomicLong ANONYMOUS_TYPE_COUNTER = new AtomicLong();

  private final List<ImagePart> imageParts;
  private DelphiScope scope;
  private Set<Type> parents;
  private StructKind kind;
  private Type superType;
  private boolean isForwardType;

  protected DelphiStructType(
      List<ImagePart> imageParts, DelphiScope scope, Set<Type> parents, StructKind kind) {
    super(createImage(imageParts));
    this.imageParts = imageParts;
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

  public static StructType from(TypeNode node) {
    List<ImagePart> imageParts = new ArrayList<>();
    ImagePart unitPart = new ImagePart(node.findUnitName());
    imageParts.add(unitPart);

    Set<Type> ancestors = Collections.emptySet();
    StructKind kind = StructKind.fromNode(node);
    Node parent = node.jjtGetParent();

    if (parent instanceof TypeDeclarationNode) {
      TypeDeclarationNode typeDeclaration = (TypeDeclarationNode) parent;
      ancestors = getAncestors(typeDeclaration, kind);
      imageParts.addAll(createImageParts(typeDeclaration));
    } else {
      String anonymousImage = "<anonymous_type_" + ANONYMOUS_TYPE_COUNTER.incrementAndGet() + ">";
      imageParts.add(new ImagePart(anonymousImage));
    }

    return new DelphiStructType(imageParts, node.getScope(), ancestors, kind);
  }

  protected static List<ImagePart> createImageParts(TypeDeclarationNode declaration) {
    List<ImagePart> result = new ArrayList<>();
    for (TypeDeclarationNode outer : declaration.getOuterTypeDeclarationNodes()) {
      result.add(createImagePart(outer));
    }
    result.add(createImagePart(declaration));
    return result;
  }

  private static ImagePart createImagePart(TypeDeclarationNode declaration) {
    List<Type> typeParameters =
        declaration.getTypeNameNode().getTypeParameters().stream()
            .map(TypeParameter::getType)
            .collect(Collectors.toUnmodifiableList());
    return new ImagePart(declaration.simpleName(), typeParameters);
  }

  private static String createImage(List<ImagePart> imageParts) {
    return imageParts.stream().map(ImagePart::toString).collect(Collectors.joining("."));
  }

  protected static Set<Type> getAncestors(TypeDeclarationNode typeDeclaration, StructKind kind) {
    Set<Type> parents = typeDeclaration.getTypeNode().getParentTypes();
    if (parents.isEmpty()) {
      parents = getDefaultAncestors(typeDeclaration, kind);
    }
    return parents;
  }

  private static Set<Type> getDefaultAncestors(TypeDeclarationNode node, StructKind kind) {
    SystemScope systemScope = getSystemScope(node);
    String image = node.fullyQualifiedName();

    if (systemScope != null) {
      TypedDeclaration defaultAncestor = null;

      switch (kind) {
        case CLASS:
          if (!"System.TObject".equals(image)) {
            defaultAncestor = systemScope.getTObjectDeclaration();
          }
          break;

        case INTERFACE:
          if (!"System.IInterface".equals(image)) {
            defaultAncestor = systemScope.getIInterfaceDeclaration();
          }
          break;

        case CLASS_HELPER:
          defaultAncestor = systemScope.getTClassHelperBaseDeclaration();
          break;

        default:
          // Do nothing
      }

      if (defaultAncestor != null) {
        return Set.of(defaultAncestor.getType());
      }
    }

    return Collections.emptySet();
  }

  @Nullable
  private static SystemScope getSystemScope(DelphiNode node) {
    FileScope unitScope = node.getScope().getEnclosingScope(FileScope.class);
    if (unitScope != null) {
      return unitScope.getSystemScope();
    }
    return null;
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

  protected static final class ImagePart {
    private final String name;
    private final List<Type> typeParameters;

    private ImagePart(String name) {
      this(name, Collections.emptyList());
    }

    private ImagePart(String name, List<Type> typeParameters) {
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
