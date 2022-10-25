/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.delphi.type.intrinsic;

import static org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration.compilerVariable;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_ARRAY;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_CLASS_REFERENCE;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_DYNAMIC_ARRAY;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_OBJECT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_ORDINAL;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_SET;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicType.ANSISTRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicType.BOOLEAN;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicType.BYTE;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicType.CHAR;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicType.EXTENDED;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicType.INT64;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicType.INTEGER;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicType.LONGINT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicType.NATIVEINT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicType.NATIVEUINT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicType.PANSICHAR;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicType.PCHAR;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicType.POINTER;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicType.REAL;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicType.SHORTSTRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicType.TEXT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicType.UNICODESTRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicType.VARIANT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicType.WIDESTRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicType.WORD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.sonar.plugins.delphi.symbol.SymbolicNode;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.factory.TypeFactory;

public final class IntrinsicsInjector {
  private final TypeFactory typeFactory;
  private final List<IntrinsicMethod.Builder> methods;
  private DelphiScope scope;

  public IntrinsicsInjector(TypeFactory typeFactory) {
    this.typeFactory = typeFactory;
    this.methods = new ArrayList<>();

    buildMethods();
  }

  public void inject(DelphiScope scope) {
    this.scope = scope;
    injectTypes();
    injectMethods();
    injectConstants();
  }

  private Type type(IntrinsicType type) {
    return typeFactory.getIntrinsic(type);
  }

  private void buildMethods() {
    method("Abs").param(type(REAL)).returns(type(REAL));
    method("Abs").param(type(INTEGER)).returns(type(INTEGER));
    method("Abs").param(type(INT64)).returns(type(INT64));
    method("Addr").varParam(typeFactory.untypedType()).returns(typeFactory.untypedPointer());
    method("Append").varParam(type(TEXT)).returns(type(INTEGER));
    method("Assert").varParam(type(BOOLEAN)).param(type(UNICODESTRING)).required(1);
    method("Assign")
        .varParam(typeFactory.untypedFile())
        .param(type(UNICODESTRING))
        .param(type(WORD))
        .required(2);
    method("Assigned").varParam(typeFactory.untypedType()).returns(type(BOOLEAN));
    method("AssignFile")
        .varParam(typeFactory.untypedFile())
        .param(type(UNICODESTRING))
        .param(type(WORD))
        .required(2);
    method("AtomicCmpExchange")
        .varParam(typeFactory.untypedType())
        .param(type(INTEGER))
        .param(type(INTEGER))
        .outParam(type(BOOLEAN))
        .required(3)
        .returns(type(INTEGER));
    method("AtomicCmpExchange")
        .varParam(typeFactory.untypedType())
        .param(typeFactory.untypedType())
        .param(typeFactory.untypedType())
        .outParam(type(BOOLEAN))
        .required(3)
        .returns(typeFactory.untypedPointer());
    method("AtomicDecrement")
        .varParam(typeFactory.untypedType())
        .param(typeFactory.untypedType())
        .required(1)
        .returns(type(INTEGER));
    method("AtomicExchange")
        .varParam(typeFactory.untypedType())
        .param(type(INTEGER))
        .returns(type(INTEGER));
    method("AtomicExchange")
        .varParam(typeFactory.untypedType())
        .param(typeFactory.untypedPointer())
        .returns(typeFactory.untypedPointer());
    method("AtomicIncrement")
        .varParam(typeFactory.untypedType())
        .param(typeFactory.untypedType())
        .required(1)
        .returns(type(INTEGER));
    method("BlockRead")
        .varParam(typeFactory.untypedFile())
        .varParam(typeFactory.untypedType())
        .param(type(INTEGER))
        .varParam(type(INTEGER))
        .required(3)
        .returns(type(INTEGER));
    method("BlockWrite")
        .varParam(typeFactory.untypedFile())
        .constParam(typeFactory.untypedType())
        .param(type(INTEGER))
        .varParam(type(INTEGER))
        .required(3)
        .returns(type(INTEGER));
    method("Break");
    method("BuiltInArcTan").param(type(REAL)).returns(type(EXTENDED));
    method("BuiltInArcTan2").param(type(REAL)).param(type(REAL)).returns(type(EXTENDED));
    method("BuiltInCos").param(type(REAL)).returns(type(EXTENDED));
    method("BuiltInLn").param(type(REAL)).returns(type(EXTENDED));
    method("BuiltInLnXPlus1").param(type(REAL)).returns(type(EXTENDED));
    method("BuiltInLog10").param(type(REAL)).returns(type(EXTENDED));
    method("BuiltInLog2").param(type(REAL)).returns(type(EXTENDED));
    method("BuiltInSin").param(type(REAL)).returns(type(EXTENDED));
    method("BuiltInSqrt").param(type(REAL)).returns(type(EXTENDED));
    method("BuiltInTan").param(type(REAL)).returns(type(EXTENDED));
    method("Chr").param(type(BYTE)).returns(type(CHAR));
    method("Close").varParam(typeFactory.untypedFile()).returns(type(INTEGER));
    method("CloseFile").varParam(typeFactory.untypedFile());
    method("Concat")
        .param(type(UNICODESTRING))
        .param(type(UNICODESTRING))
        .variadic(type(UNICODESTRING))
        .returns(type(UNICODESTRING));
    method("Concat")
        .param(ANY_DYNAMIC_ARRAY)
        .param(ANY_DYNAMIC_ARRAY)
        .variadic(ANY_DYNAMIC_ARRAY)
        .returns(ANY_DYNAMIC_ARRAY);
    method("Continue");
    method("Copy")
        .param(type(UNICODESTRING))
        .param(type(INTEGER))
        .param(type(INTEGER))
        .returns(type(UNICODESTRING));
    method("Copy")
        .param(ANY_DYNAMIC_ARRAY)
        .param(type(INTEGER))
        .param(type(INTEGER))
        .required(1)
        .returns(type(UNICODESTRING));
    method("Dec").varParam(ANY_ORDINAL).param(type(INTEGER)).required(1).returns(type(INTEGER));
    method("Default").param(ANY_CLASS_REFERENCE).returns(IntrinsicReturnType.classReferenceValue());
    method("Delete").varParam(type(UNICODESTRING)).param(type(INTEGER)).param(type(INTEGER));
    method("Delete").varParam(ANY_DYNAMIC_ARRAY).param(type(INTEGER)).param(type(INTEGER));
    method("Dispose").varParam(typeFactory.untypedPointer());
    method("Eof").varParam(typeFactory.untypedFile()).required(0).returns(type(BOOLEAN));
    method("Eoln").varParam(typeFactory.untypedFile()).required(0).returns(type(BOOLEAN));
    method("Erase").varParam(typeFactory.untypedFile());
    method("Exclude").varParam(ANY_SET).param(ANY_ORDINAL);
    method("Exit").varParam(typeFactory.untypedType()).required(0);
    method("Fail");
    method("FilePos").varParam(typeFactory.untypedFile()).returns(type(INTEGER));
    method("FileSize").varParam(typeFactory.untypedFile()).returns(type(INTEGER));
    method("FillChar").varParam(typeFactory.untypedType()).param(type(INTEGER)).param(ANY_ORDINAL);
    method("Finalize").varParam(typeFactory.untypedType()).param(type(NATIVEUINT)).required(1);
    method("FreeMem").varParam(typeFactory.untypedPointer()).param(type(INTEGER)).required(1);
    method("GetDir").param(type(BYTE)).varParam(type(UNICODESTRING));
    method("GetMem").varParam(typeFactory.untypedPointer()).param(type(INTEGER));
    method("Halt").param(type(INTEGER)).required(0);
    method("HasWeakRef").param(ANY_CLASS_REFERENCE).returns(type(BOOLEAN));
    method("Hi").param(type(INTEGER)).returns(type(BYTE));
    method("High")
        .varParam(typeFactory.untypedType())
        .returns(IntrinsicReturnType.high(typeFactory));
    method("Inc").varParam(ANY_ORDINAL).param(type(INTEGER)).required(1);
    method("Include").varParam(ANY_SET).param(ANY_ORDINAL);
    method("Initialize").varParam(typeFactory.untypedType()).param(type(NATIVEINT)).required(1);
    method("Insert").param(type(UNICODESTRING)).varParam(type(UNICODESTRING)).param(type(INTEGER));
    method("Insert").param(ANY_DYNAMIC_ARRAY).varParam(ANY_DYNAMIC_ARRAY).param(type(INTEGER));
    method("IsConstValue").param(typeFactory.untypedType()).returns(type(BOOLEAN));
    method("IsManagedType").param(ANY_CLASS_REFERENCE).returns(type(BOOLEAN));
    method("Length").param(type(UNICODESTRING)).returns(type(INTEGER));
    method("Length").param(ANY_ARRAY).returns(type(INTEGER));
    method("Lo").param(type(INTEGER)).returns(type(BYTE));
    method("Low").varParam(typeFactory.untypedType()).returns(IntrinsicReturnType.low(typeFactory));
    method("MemoryBarrier");
    method("MulDivInt64")
        .param(type(INT64))
        .param(type(INT64))
        .param(type(INT64))
        .outParam(type(INT64))
        .required(3)
        .returns(type(INT64));
    method("New").varParam(typeFactory.untypedPointer());
    method("Odd").param(type(INTEGER)).returns(type(BOOLEAN));
    method("Ord").param(ANY_ORDINAL).returns(type(BYTE));
    method("Pi").returns(type(EXTENDED));
    method("Pred").param(ANY_ORDINAL).returns(type(INTEGER));
    method("Ptr").param(type(INTEGER)).returns(typeFactory.untypedPointer());
    method("Read")
        .varParam(typeFactory.untypedFile())
        .param(typeFactory.untypedType())
        .variadic(typeFactory.untypedType());
    method("ReadLn").varParam(typeFactory.untypedFile()).variadic(typeFactory.untypedType());
    method("ReallocMem").varParam(typeFactory.untypedPointer()).param(type(INTEGER));
    method("Rename").varParam(typeFactory.untypedFile()).param(type(UNICODESTRING));
    method("Reset").varParam(typeFactory.untypedFile()).param(type(INTEGER)).required(1);
    method("Rewrite").varParam(typeFactory.untypedFile()).param(type(INTEGER)).required(1);
    method("Round")
        .param(typeFactory.untypedType())
        .returns(IntrinsicReturnType.round(typeFactory));
    method("RunError").param(type(BYTE)).required(0);
    method("Seek").varParam(typeFactory.untypedFile()).param(type(INTEGER));
    method("SeekEof").varParam(type(TEXT)).required(0).returns(type(BOOLEAN));
    method("SeekEoln").varParam(type(TEXT)).required(0).returns(type(BOOLEAN));
    method("SetLength").varParam(type(UNICODESTRING)).param(type(INTEGER));
    method("SetLength").varParam(type(ANSISTRING)).param(type(INTEGER));
    method("SetLength").varParam(ANY_DYNAMIC_ARRAY).param(type(INTEGER)).variadic(type(INTEGER));
    method("SetString").varParam(type(SHORTSTRING)).param(type(PANSICHAR)).param(type(INTEGER));
    method("SetString").varParam(type(ANSISTRING)).param(type(PANSICHAR)).param(type(INTEGER));
    method("SetString").varParam(type(WIDESTRING)).param(type(PCHAR)).param(type(INTEGER));
    method("SetString").varParam(type(UNICODESTRING)).param(type(PCHAR)).param(type(INTEGER));
    method("SetTextBuf")
        .varParam(type(TEXT))
        .varParam(typeFactory.untypedType())
        .param(type(INTEGER))
        .required(2);
    method("SizeOf").param(typeFactory.untypedType()).returns(type(INTEGER));
    method("Slice").varParam(ANY_ARRAY).param(type(INTEGER)).returns(typeFactory.untypedPointer());
    method("Sqr").param(type(EXTENDED)).returns(type(EXTENDED));
    method("Sqr").param(type(INTEGER)).returns(type(INTEGER));
    method("Sqr").param(type(REAL)).returns(type(INT64));
    method("Str").constParam(typeFactory.untypedType()).varParam(type(UNICODESTRING));
    method("Succ").param(ANY_ORDINAL).returns(type(INTEGER));
    method("Swap").param(type(INTEGER)).returns(type(INTEGER));
    method("Trunc")
        .param(typeFactory.untypedType())
        .returns(IntrinsicReturnType.trunc(typeFactory));
    method("Truncate").varParam(typeFactory.untypedFile());
    method("TypeHandle").param(typeFactory.untypedType()).returns(typeFactory.untypedPointer());
    method("TypeInfo").param(typeFactory.untypedType()).returns(typeFactory.untypedPointer());
    method("TypeOf").param(ANY_OBJECT).returns(typeFactory.untypedPointer());
    method("Val")
        .param(type(UNICODESTRING))
        .varParam(typeFactory.untypedType())
        .varParam(type(INTEGER));
    method("VarArrayRedim").varParam(type(VARIANT)).param(type(INTEGER));
    method("VarCast").varParam(type(VARIANT)).param(type(VARIANT)).param(type(INTEGER));
    method("VarClear").varParam(type(VARIANT));
    method("VarCopy").varParam(type(VARIANT)).param(type(VARIANT));
    method("Write")
        .varParam(typeFactory.untypedFile())
        .param(typeFactory.untypedType())
        .variadic(typeFactory.untypedType());
    method("Write").varParam(typeFactory.untypedType()).variadic(typeFactory.untypedType());
    method("WriteLn").varParam(typeFactory.untypedFile()).variadic(typeFactory.untypedType());
    method("WriteLn").variadic(typeFactory.untypedType());
  }

  private IntrinsicMethod.Builder method(String name) {
    IntrinsicMethod.Builder builder = IntrinsicMethod.builder(name);
    methods.add(builder);
    return builder;
  }

  private void injectTypes() {
    Arrays.stream(IntrinsicType.values()).forEach(this::injectType);
  }

  private void injectType(IntrinsicType intrinsic) {
    SymbolicNode node = SymbolicNode.imaginary(intrinsic.simpleName(), scope);
    Type type = typeFactory.getIntrinsic(intrinsic);
    TypeNameDeclaration declaration =
        new TypeNameDeclaration(node, type, intrinsic.fullyQualifiedName());

    scope.addDeclaration(declaration);
  }

  private void injectMethods() {
    methods.forEach(this::injectMethod);
  }

  private void injectMethod(IntrinsicMethod.Builder builder) {
    IntrinsicMethod method = builder.build();
    SymbolicNode node = SymbolicNode.imaginary(method.simpleName(), scope);
    MethodNameDeclaration declaration = MethodNameDeclaration.create(node, method, typeFactory);

    scope.addDeclaration(declaration);
  }

  private void injectConstants() {
    injectConstant("CompilerVersion", EXTENDED);
    injectConstant("MaxInt", INTEGER);
    injectConstant("MaxLongInt", LONGINT);
    injectConstant("True", BOOLEAN);
    injectConstant("False", BOOLEAN);
    injectConstant("ReturnAddress", POINTER);
    injectConstant("AddressOfReturnAddress", POINTER);
  }

  private void injectConstant(String image, IntrinsicType intrinsic) {
    scope.addDeclaration(compilerVariable(image, type(intrinsic), scope));
  }
}
