package org.sonar.plugins.delphi.utils.types;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.delphi.symbol.scope.DelphiScope.unknownScope;
import static org.sonar.plugins.delphi.type.DelphiType.unknownType;

import org.sonar.plugins.delphi.type.StructKind;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.StructType;
import org.sonar.plugins.delphi.type.parameter.Parameter;

public final class TypeMocker {
  private TypeMocker() {
    // Utility class
  }

  public static StructType struct(String image, StructKind kind) {
    return struct(image, kind, unknownType());
  }

  public static StructType struct(String image, StructKind kind, Type superType) {
    StructType type = mock(StructType.class);
    when(type.typeScope()).thenReturn(unknownScope());
    when(type.isStruct()).thenReturn(true);
    when(type.isInterface()).thenReturn(kind == StructKind.INTERFACE);
    when(type.isRecord()).thenReturn(kind == StructKind.RECORD);
    when(type.getImage()).thenReturn(image);
    when(type.is(anyString()))
        .thenAnswer(invocation -> image.equalsIgnoreCase(invocation.getArgument(0)));
    when(type.is(any(Type.class)))
        .thenAnswer(arguments -> type.is(((Type) arguments.getArgument(0)).getImage()));
    when(type.kind()).thenReturn(kind);
    when(type.isSubTypeOf(anyString()))
        .thenAnswer(
            invocation -> {
              String other = invocation.getArgument(0);
              return type.is(other) || superType.is(other);
            });
    when(type.isSubTypeOf(any(Type.class)))
        .thenAnswer(invocation -> type.isSubTypeOf(((Type) invocation.getArgument(0)).getImage()));
    when(type.superType()).thenReturn(superType);
    return type;
  }

  public static Parameter parameter(Type type) {
    Parameter parameter = mock(Parameter.class);
    when(parameter.getImage()).thenReturn("_");
    when(parameter.getType()).thenReturn(type);
    return parameter;
  }
}
