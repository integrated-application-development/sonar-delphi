package org.sonar.plugins.delphi.type;

import static java.util.function.Predicate.not;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeNode;
import org.sonar.plugins.delphi.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.symbol.scope.FileScope;
import org.sonar.plugins.delphi.symbol.scope.SystemScope;
import org.sonar.plugins.delphi.type.Type.StructType;

public class DelphiStructType extends DelphiType implements StructType {
  private static final AtomicLong ANONYMOUS_TYPE_COUNTER = new AtomicLong();

  private DelphiScope scope;
  private ImmutableSet<Type> parents;
  private StructKind kind;
  private Type superType;
  private boolean isForwardType;

  protected DelphiStructType(String image, DelphiScope scope, Set<Type> parents, StructKind kind) {
    super(image);
    this.scope = scope;
    this.parents = ImmutableSet.copyOf(parents);
    this.kind = kind;
    this.superType =
        this.parents.stream()
            .filter(DelphiStructType.class::isInstance)
            .filter(not(Type::isInterface))
            .findFirst()
            .orElse(super.superType());
  }

  public static StructType from(TypeNode node) {
    String image;
    Node typeDecl = node.jjtGetParent();

    if (typeDecl instanceof TypeDeclarationNode) {
      image = ((TypeDeclarationNode) typeDecl).fullyQualifiedName();
    } else {
      image = "<anonymous_type_" + ANONYMOUS_TYPE_COUNTER.incrementAndGet() + ">";
    }

    StructKind kind = StructKind.fromNode(node);
    Set<Type> ancestors = getAncestors(image, node, kind);
    return from(image, node.getScope(), ancestors, kind);
  }

  public static StructType from(
      @Nullable String image, DelphiScope scope, Set<Type> parents, StructKind kind) {
    return new DelphiStructType(image, scope, parents, kind);
  }

  private static Set<Type> getAncestors(String image, TypeNode node, StructKind kind) {
    Set<Type> parents = node.getParentTypes();
    DelphiScope scope = node.getASTTree().getScope();
    if (parents.isEmpty()) {
      switch (kind) {
        case CLASS:
          if (!"System.TObject".equals(image)) {
            parents = getTObjectAncestor(scope);
          }
          break;

        case INTERFACE:
          if (!"System.IInterface".equals(image)) {
            parents = getIInterfaceAncestor(scope);
          }
          break;

        default:
          // Do nothing
      }
    }
    return parents;
  }

  private static Set<Type> getTObjectAncestor(DelphiScope scope) {
    SystemScope systemScope = getSystemScope(scope);
    if (systemScope != null) {
      return Collections.singleton(systemScope.getTObjectDeclaration().getType());
    }
    return Collections.emptySet();
  }

  private static Set<Type> getIInterfaceAncestor(DelphiScope scope) {
    SystemScope systemScope = getSystemScope(scope);
    if (systemScope != null) {
      return Collections.singleton(systemScope.getIInterfaceDeclaration().getType());
    }
    return Collections.emptySet();
  }

  @Nullable
  private static SystemScope getSystemScope(DelphiScope scope) {
    FileScope unitScope = scope.getEnclosingScope(FileScope.class);
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
}
