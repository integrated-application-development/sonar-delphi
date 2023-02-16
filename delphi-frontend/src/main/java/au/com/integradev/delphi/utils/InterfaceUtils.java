package au.com.integradev.delphi.utils;

import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ScopedType;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;

public class InterfaceUtils {
  private InterfaceUtils() {
    // Utility class
  }

  public static boolean implementsMethodOnInterface(MethodNameDeclaration method) {
    var typeDeclaration = method.getTypeDeclaration();
    return typeDeclaration != null && hasMatchingInterfaceMethod(typeDeclaration.getType(), method);
  }

  public static Set<MethodNameDeclaration> findImplementedInterfaceMethodDeclarations(
      MethodNameDeclaration method) {
    var typeDeclaration = method.getTypeDeclaration();
    if (typeDeclaration == null) {
      return new HashSet<>();
    }

    Set<MethodNameDeclaration> implementedMethods = new HashSet<>();

    var interfaces =
        typeDeclaration.getType().parents().stream()
            .filter(Type::isInterface)
            .map(ScopedType.class::cast)
            .collect(Collectors.toUnmodifiableList());

    for (var interfaceType : interfaces) {
      interfaceType.typeScope().getMethodDeclarations().stream()
          .filter(interfaceMethod -> hasSameMethodSignature(method, interfaceMethod))
          .forEach(implementedMethods::add);
    }

    return implementedMethods;
  }

  private static boolean hasMatchingInterfaceMethod(Type type, MethodNameDeclaration method) {
    if (type.isInterface()
        && ((ScopedType) type)
            .typeScope().getMethodDeclarations().stream()
                .anyMatch(interfaceMethod -> hasSameMethodSignature(method, interfaceMethod))) {
      return true;
    }

    return type.parents().stream()
        .anyMatch(parentType -> hasMatchingInterfaceMethod(parentType, method));
  }

  private static boolean hasSameMethodSignature(
      MethodNameDeclaration thisMethod, MethodNameDeclaration overriddenMethod) {
    return thisMethod.getName().equalsIgnoreCase(overriddenMethod.getName())
        && thisMethod.hasSameParameterTypes(overriddenMethod);
  }
}
