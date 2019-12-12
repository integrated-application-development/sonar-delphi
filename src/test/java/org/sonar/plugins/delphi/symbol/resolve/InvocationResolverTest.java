package org.sonar.plugins.delphi.symbol.resolve;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.delphi.symbol.scope.UnknownScope.unknownScope;
import static org.sonar.plugins.delphi.type.DelphiCollectionType.dynamicArray;
import static org.sonar.plugins.delphi.type.DelphiCollectionType.openArray;
import static org.sonar.plugins.delphi.type.DelphiFileType.fileOf;
import static org.sonar.plugins.delphi.type.DelphiFileType.untypedFile;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.BooleanType.BOOLEAN;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.DecimalType.CURRENCY;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.DecimalType.DOUBLE;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.DecimalType.EXTENDED;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.DecimalType.REAL;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.DecimalType.SINGLE;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.IntegerType.BYTE;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.IntegerType.CARDINAL;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.IntegerType.INT64;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.IntegerType.INTEGER;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.IntegerType.LONGINT;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.IntegerType.SHORTINT;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.IntegerType.SMALLINT;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.IntegerType.UINT64;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.IntegerType.WORD;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.TextType.ANSISTRING;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.TextType.CHAR;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.TextType.SHORTSTRING;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.TextType.UNICODESTRING;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.TextType.WIDESTRING;
import static org.sonar.plugins.delphi.type.DelphiPointerType.pointerTo;
import static org.sonar.plugins.delphi.type.DelphiPointerType.untypedPointer;
import static org.sonar.plugins.delphi.type.DelphiType.unknownType;
import static org.sonar.plugins.delphi.type.DelphiType.untypedType;
import static org.sonar.plugins.delphi.type.DelphiVariantType.variant;
import static org.sonar.plugins.delphi.type.StructKind.CLASS;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameter;
import org.sonar.plugins.delphi.symbol.declaration.ParameterDeclaration;
import org.sonar.plugins.delphi.type.DelphiStructType;
import org.sonar.plugins.delphi.type.Type;

public class InvocationResolverTest {
  private Set<InvocationCandidate> resolved;

  private void assertResolved(Type argumentType, Type winnerType, Type loserType) {
    InvocationCandidate winner = mockCandidate(winnerType, false);
    InvocationCandidate loser = mockCandidate(loserType, false);

    runResolver(argumentType, List.of(winner, loser));
    assertThat(resolved).containsOnly(winner);
  }

  private void assertResolvedVar(Type argumentType, Type winnerType) {
    runResolver(argumentType, Collections.singletonList(mockCandidate(winnerType, true)));
  }

  private void assertAmbiguous(Type argumentType, Type... candidates) {
    runResolver(
        argumentType,
        Arrays.stream(candidates)
            .map(type -> mockCandidate(type, false))
            .collect(Collectors.toList()));

    assertThat(resolved).hasSizeGreaterThan(1);
  }

  private void assertIncompatible(Type argumentType, Type... candidates) {
    runResolver(
        argumentType,
        Arrays.stream(candidates)
            .map(type -> mockCandidate(type, false))
            .collect(Collectors.toList()));

    assertThat(resolved).isEmpty();
  }

  private void runResolver(Type argumentType, List<InvocationCandidate> candidates) {
    InvocationResolver resolver = new InvocationResolver();
    InvocationArgument argument = mock(InvocationArgument.class);
    when(argument.getType()).thenReturn(argumentType);

    resolver.addArgument(argument);
    candidates.forEach(resolver::addCandidate);
    resolver.processCandidates();

    resolved = resolver.chooseBest();
  }

  private static InvocationCandidate mockCandidate(Type parameterType, boolean var) {
    Invocable invocable = mock(Invocable.class);
    FormalParameter formalParameter = mock(FormalParameter.class);
    when(formalParameter.getType()).thenReturn(parameterType);
    if (var) {
      when(formalParameter.isVar()).thenReturn(true);
    }
    ParameterDeclaration parameter = new ParameterDeclaration(formalParameter);
    when(invocable.getParameter(anyInt())).thenReturn(parameter);
    when(invocable.getParameters()).thenReturn(Collections.singletonList(parameter));
    when(invocable.getParametersCount()).thenReturn(1);
    when(invocable.getRequiredParametersCount()).thenReturn(1);
    return new InvocationCandidate(invocable);
  }

  @Test
  public void testIntegerTypes() {
    assertResolved(INTEGER.type, LONGINT.type, BYTE.type);
    assertResolved(INTEGER.type, LONGINT.type, INT64.type);
    assertResolved(INTEGER.type, INT64.type, UINT64.type);
    assertResolved(INTEGER.type, WORD.type, SHORTINT.type);
    assertResolved(INTEGER.type, WORD.type, BYTE.type);
    assertResolved(BYTE.type, INTEGER.type, DOUBLE.type);
  }

  @Test
  public void testFloatingPointTypes() {
    assertResolved(EXTENDED.type, DOUBLE.type, SINGLE.type);
    assertResolved(EXTENDED.type, REAL.type, SINGLE.type);
    assertResolved(DOUBLE.type, EXTENDED.type, SINGLE.type);
    assertResolved(REAL.type, EXTENDED.type, SINGLE.type);
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
    assertResolved(variant(), variant(), INTEGER.type);
    assertResolved(variant(), INTEGER.type, unknownType());
    assertResolved(variant(), SINGLE.type, INT64.type);
    assertResolved(variant(), DOUBLE.type, INT64.type);
    assertResolved(variant(), CURRENCY.type, INT64.type);
    assertResolved(variant(), EXTENDED.type, INT64.type);
    assertResolved(variant(), LONGINT.type, INT64.type);
    assertResolved(variant(), CARDINAL.type, INT64.type);
    assertResolved(variant(), SMALLINT.type, LONGINT.type);
    assertResolved(variant(), WORD.type, LONGINT.type);
    assertResolved(variant(), SHORTINT.type, LONGINT.type);
    assertResolved(variant(), BYTE.type, LONGINT.type);
    assertResolved(variant(), BOOLEAN.type, CHAR.type);
    assertResolved(variant(), untypedType(), CHAR.type);
    assertResolved(variant(), BOOLEAN.type, ANSISTRING.type);
    assertResolved(variant(), WIDESTRING.type, CHAR.type);
    assertResolved(variant(), UNICODESTRING.type, CHAR.type);
    assertResolved(variant(), ANSISTRING.type, CHAR.type);
    assertResolved(variant(), SHORTSTRING.type, CHAR.type);
    assertResolved(variant(), WIDESTRING.type, UNICODESTRING.type);
    assertResolved(variant(), UNICODESTRING.type, ANSISTRING.type);
    assertResolved(variant(), ANSISTRING.type, SHORTSTRING.type);

    assertAmbiguous(variant(), SMALLINT.type, WORD.type);
    assertIncompatible(variant(), unknownType());
  }
}
