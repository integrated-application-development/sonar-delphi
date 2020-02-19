package org.sonar.plugins.delphi.symbol.resolve;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.delphi.symbol.scope.UnknownScope.unknownScope;
import static org.sonar.plugins.delphi.type.DelphiArrayType.dynamicArray;
import static org.sonar.plugins.delphi.type.DelphiArrayType.openArray;
import static org.sonar.plugins.delphi.type.DelphiFileType.fileOf;
import static org.sonar.plugins.delphi.type.DelphiFileType.untypedFile;
import static org.sonar.plugins.delphi.type.DelphiPointerType.pointerTo;
import static org.sonar.plugins.delphi.type.DelphiPointerType.untypedPointer;
import static org.sonar.plugins.delphi.type.DelphiType.unknownType;
import static org.sonar.plugins.delphi.type.DelphiType.untypedType;
import static org.sonar.plugins.delphi.type.StructKind.CLASS;
import static org.sonar.plugins.delphi.type.StructKind.RECORD;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicBoolean.BOOLEAN;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicDecimal.CURRENCY;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicDecimal.DOUBLE;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicDecimal.EXTENDED;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicDecimal.REAL;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicDecimal.SINGLE;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.BYTE;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.CARDINAL;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.INT64;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.INTEGER;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.LONGINT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.LONGWORD;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.NATIVEINT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.NATIVEUINT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.SHORTINT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.SMALLINT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.UINT64;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.WORD;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.ANSISTRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.CHAR;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.SHORTSTRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.UNICODESTRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.WIDESTRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicVariant.VARIANT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameter;
import org.sonar.plugins.delphi.symbol.declaration.ParameterDeclaration;
import org.sonar.plugins.delphi.type.DelphiStructType;
import org.sonar.plugins.delphi.type.DelphiTypeType;
import org.sonar.plugins.delphi.type.Type;

public class InvocationResolverTest {
  private Set<InvocationCandidate> resolved;

  private void assertResolved(Type argumentType, Type winnerType, Type loserType) {
    assertResolved(List.of(argumentType), List.of(winnerType), List.of(loserType));
  }

  private void assertResolved(
      List<Type> argumentTypes, List<Type> winnerTypes, List<Type> loserTypes) {
    InvocationCandidate winner = mockCandidate(winnerTypes, false);
    InvocationCandidate loser = mockCandidate(loserTypes, false);

    runResolver(argumentTypes, List.of(winner, loser));
    assertThat(resolved).containsOnly(winner);
  }

  private void assertResolvedVar(Type argumentType, Type winnerType) {
    runResolver(
        List.of(argumentType), Collections.singletonList(mockCandidate(List.of(winnerType), true)));
  }

  private void assertAmbiguous(Type argumentType, Type... candidates) {
    runResolver(
        List.of(argumentType),
        Arrays.stream(candidates)
            .map(type -> mockCandidate(List.of(type), false))
            .collect(Collectors.toList()));

    assertThat(resolved).hasSizeGreaterThan(1);
  }

  private void assertIncompatible(Type argumentType, Type... candidates) {
    runResolver(
        List.of(argumentType),
        Arrays.stream(candidates)
            .map(type -> mockCandidate(List.of(type), false))
            .collect(Collectors.toList()));

    assertThat(resolved).isEmpty();
  }

  private void runResolver(List<Type> argumentTypes, List<InvocationCandidate> candidates) {
    InvocationResolver resolver = new InvocationResolver();
    for (Type argumentType : argumentTypes) {
      InvocationArgument argument = mock(InvocationArgument.class);
      when(argument.getType()).thenReturn(argumentType);
      resolver.addArgument(argument);
    }

    candidates.forEach(resolver::addCandidate);
    resolver.processCandidates();

    resolved = resolver.chooseBest();
  }

  private static InvocationCandidate mockCandidate(List<Type> parameterTypes, boolean var) {
    Invocable invocable = mock(Invocable.class);
    List<ParameterDeclaration> parameters = new ArrayList<>();

    for (Type paramType : parameterTypes) {
      FormalParameter formalParameter = mock(FormalParameter.class);
      when(formalParameter.getType()).thenReturn(paramType);
      if (var) {
        when(formalParameter.isVar()).thenReturn(true);
      }
      parameters.add(ParameterDeclaration.create(formalParameter));
    }

    when(invocable.getParameters()).thenReturn(parameters);
    when(invocable.getParameter(anyInt())).thenCallRealMethod();
    when(invocable.getParametersCount()).thenCallRealMethod();
    when(invocable.getRequiredParametersCount()).thenCallRealMethod();

    return new InvocationCandidate(invocable);
  }

  @Test
  public void testIntegerTypes() {
    assertResolved(INTEGER.type, LONGINT.type, BYTE.type);
    assertResolved(INTEGER.type, LONGINT.type, INT64.type);
    assertResolved(INTEGER.type, INT64.type, UINT64.type);
    assertResolved(INTEGER.type, WORD.type, SHORTINT.type);
    assertResolved(INTEGER.type, WORD.type, BYTE.type);
    assertResolved(SHORTINT.type, NATIVEINT.type, NATIVEUINT.type);

    Type HWND = DelphiTypeType.create("HWND", NATIVEUINT.type);
    assertResolved(
        List.of(HWND, NATIVEUINT.type, SHORTINT.type, SHORTINT.type),
        List.of(HWND, NATIVEUINT.type, NATIVEINT.type, NATIVEINT.type),
        List.of(HWND, NATIVEUINT.type, NATIVEUINT.type, NATIVEINT.type));

    assertResolved(BYTE.type, INTEGER.type, DOUBLE.type);

    assertResolved(DelphiTypeType.create("MyWord", LONGWORD.type), INTEGER.type, INT64.type);
    assertResolved(LONGWORD.type, DelphiTypeType.create("MyInt", INTEGER.type), INT64.type);
  }

  @Test
  public void testFloatingPointTypes() {
    assertResolved(EXTENDED.type, DOUBLE.type, SINGLE.type);
    assertResolved(EXTENDED.type, REAL.type, SINGLE.type);
    assertResolved(DOUBLE.type, EXTENDED.type, SINGLE.type);
    assertResolved(REAL.type, EXTENDED.type, SINGLE.type);
  }

  @Test
  public void testTextTypes() {
    assertResolved(
        DelphiTypeType.create("MyString", UNICODESTRING.type),
        UNICODESTRING.type,
        SHORTSTRING.type);
    assertResolved(
        UNICODESTRING.type,
        DelphiTypeType.create("MyString", UNICODESTRING.type),
        SHORTSTRING.type);
  }

  @Test
  public void testVariantTypes() {
    Type variantIncompatibleType =
        DelphiStructType.from("MyRecord", unknownScope(), emptySet(), RECORD);
    assertResolved(
        List.of(UNICODESTRING.type, variantIncompatibleType, BOOLEAN.type),
        List.of(UNICODESTRING.type, variantIncompatibleType, VARIANT.type, BOOLEAN.type),
        List.of(UNICODESTRING.type, VARIANT.type, BOOLEAN.type));
    assertResolved(
        List.of(VARIANT.type, BYTE.type),
        List.of(UNICODESTRING.type, INTEGER.type),
        List.of(ANSISTRING.type, INTEGER.type));
    assertResolved(VARIANT.type, INTEGER.type, INT64.type);
    assertResolved(VARIANT.type, SINGLE.type, DOUBLE.type);
    assertResolved(VARIANT.type, DOUBLE.type, EXTENDED.type);
  }

  @Test
  public void testInheritedTypes() {
    Type grandparent = DelphiStructType.from("Grandparent", unknownScope(), emptySet(), CLASS);
    Type parent = DelphiStructType.from("Parent", unknownScope(), singleton(grandparent), CLASS);
    Type child = DelphiStructType.from("Child", unknownScope(), singleton(parent), CLASS);

    assertResolved(child, parent, grandparent);
    assertResolved(parent, grandparent, child);
  }

  @Test
  public void testVarParameters() {
    Type openArray = openArray(null, INTEGER.type);
    Type dynamicArray = dynamicArray(null, INTEGER.type);

    assertResolvedVar(INTEGER.type, untypedType());
    assertResolvedVar(dynamicArray, openArray);
    assertResolvedVar(INTEGER.type, openArray);
    assertResolvedVar(untypedPointer(), untypedPointer());
    assertResolvedVar(pointerTo(INTEGER.type), pointerTo(SHORTINT.type));
    assertResolvedVar(fileOf(INTEGER.type), untypedFile());
  }

  @Test
  public void testSingleVariantArgument() {
    assertResolved(VARIANT.type, VARIANT.type, INTEGER.type);
    assertResolved(VARIANT.type, INTEGER.type, unknownType());
    assertResolved(VARIANT.type, SINGLE.type, INT64.type);
    assertResolved(VARIANT.type, DOUBLE.type, INT64.type);
    assertResolved(VARIANT.type, CURRENCY.type, INT64.type);
    assertResolved(VARIANT.type, EXTENDED.type, INT64.type);
    assertResolved(VARIANT.type, LONGINT.type, INT64.type);
    assertResolved(VARIANT.type, CARDINAL.type, INT64.type);
    assertResolved(VARIANT.type, SMALLINT.type, LONGINT.type);
    assertResolved(VARIANT.type, WORD.type, LONGINT.type);
    assertResolved(VARIANT.type, SHORTINT.type, LONGINT.type);
    assertResolved(VARIANT.type, BYTE.type, LONGINT.type);
    assertResolved(VARIANT.type, BOOLEAN.type, CHAR.type);
    assertResolved(VARIANT.type, untypedType(), CHAR.type);
    assertResolved(VARIANT.type, BOOLEAN.type, ANSISTRING.type);
    assertResolved(VARIANT.type, WIDESTRING.type, CHAR.type);
    assertResolved(VARIANT.type, UNICODESTRING.type, CHAR.type);
    assertResolved(VARIANT.type, ANSISTRING.type, CHAR.type);
    assertResolved(VARIANT.type, SHORTSTRING.type, CHAR.type);
    assertResolved(VARIANT.type, WIDESTRING.type, UNICODESTRING.type);
    assertResolved(VARIANT.type, UNICODESTRING.type, ANSISTRING.type);
    assertResolved(VARIANT.type, ANSISTRING.type, SHORTSTRING.type);

    assertAmbiguous(VARIANT.type, SMALLINT.type, WORD.type);
    assertIncompatible(VARIANT.type, unknownType());
  }
}
