package org.sonar.plugins.delphi.type;

import static java.util.function.Predicate.not;

import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import net.sourceforge.pmd.lang.ast.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.antlr.ast.node.InterfaceTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeNode;
import org.sonar.plugins.delphi.symbol.DelphiScope;
import org.sonar.plugins.delphi.type.Type.ScopedType;

public class DelphiStructType extends DelphiType implements ScopedType {
  private static final AtomicLong anonymousTypeCounter = new AtomicLong();
  private final DelphiScope scope;
  private final Set<Type> parents;
  private final Type superType;
  private final boolean isInterface;

  protected DelphiStructType(
      String image, DelphiScope scope, Set<Type> parents, boolean isInterface) {
    super(image);
    this.scope = scope;
    this.parents = parents;
    this.isInterface = isInterface;
    this.superType =
        this.parents.stream()
            .filter(DelphiStructType.class::isInstance)
            .filter(not(Type::isInterface))
            .findFirst()
            .orElse(super.superType());
  }

  public static ScopedType from(TypeNode node) {
    String image;
    Node typeDecl = node.jjtGetParent();

    if (typeDecl instanceof TypeDeclarationNode) {
      image = ((TypeDeclarationNode) typeDecl).fullyQualifiedName();
    } else {
      image = "<anonymous_type_" + anonymousTypeCounter.incrementAndGet() + ">";
    }

    return from(image, node.getScope(), node.getParentTypes(), node instanceof InterfaceTypeNode);
  }

  public static ScopedType from(
      @Nullable String image, DelphiScope scope, Set<Type> parents, boolean isInterface) {
    return new DelphiStructType(image, scope, parents, isInterface);
  }

  @Override
  public boolean isSubTypeOf(Type type) {
    return isSubTypeOf(type.getImage());
  }

  @Override
  public boolean isSubTypeOf(String image) {
    return parents.stream().anyMatch(parent -> parent.getImage().equalsIgnoreCase(image));
  }

  @Override
  public Type superType() {
    return superType;
  }

  @Override
  public boolean inheritsFrom(Type other) {
    for (Type parent : parents) {
      if (parent.is(other) || parent.inheritsFrom(other)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isInterface() {
    return isInterface;
  }

  @Override
  public final boolean isObject() {
    return true;
  }

  @Override
  @NotNull
  public DelphiScope typeScope() {
    return scope;
  }
}
