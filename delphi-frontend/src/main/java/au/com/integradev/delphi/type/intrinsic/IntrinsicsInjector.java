/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package au.com.integradev.delphi.type.intrinsic;

import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_32_BIT_INTEGER;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_ARRAY;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_CLASS_REFERENCE;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_FILE;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_OBJECT;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_ORDINAL;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_POINTER;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_SET;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_STRING;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_TEXT_FILE;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_TYPED_POINTER;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_VARIANT;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.LIKE_DYNAMIC_ARRAY;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.ANSISTRING;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.BOOLEAN;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.BYTE;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.CHAR;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.EXTENDED;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.INT64;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.INTEGER;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.LONGINT;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.NATIVEINT;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.NATIVEUINT;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.PANSICHAR;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.POINTER;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.PWIDECHAR;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.REAL;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.SHORTSTRING;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.UNICODESTRING;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.WORD;

import au.com.integradev.delphi.symbol.SymbolicNode;
import au.com.integradev.delphi.symbol.declaration.RoutineNameDeclarationImpl;
import au.com.integradev.delphi.symbol.declaration.TypeNameDeclarationImpl;
import au.com.integradev.delphi.symbol.declaration.VariableNameDeclarationImpl;
import au.com.integradev.delphi.symbol.scope.DelphiScopeImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

public final class IntrinsicsInjector {
  private final TypeFactory typeFactory;
  private final List<IntrinsicRoutine.Builder> routines;
  private DelphiScopeImpl scope;

  public IntrinsicsInjector(TypeFactory typeFactory) {
    this.typeFactory = typeFactory;
    this.routines = new ArrayList<>();

    buildRoutines();
  }

  public void inject(DelphiScope scope) {
    this.scope = (DelphiScopeImpl) scope;
    injectTypes();
    injectRoutines();
    injectConstants();
  }

  private Type type(IntrinsicType type) {
    return typeFactory.getIntrinsic(type);
  }

  private void buildRoutines() {
    routine("Abs").param(type(REAL)).returns(type(REAL));
    routine("Abs").param(type(INTEGER)).returns(type(INTEGER));
    routine("Abs").param(type(INT64)).returns(type(INT64));
    routine("Addr").varParam(TypeFactory.untypedType()).returns(typeFactory.untypedPointer());
    routine("Append").varParam(ANY_TEXT_FILE).returns(type(INTEGER));
    routine("Assert").param(type(BOOLEAN)).param(ANY_STRING).required(1);
    routine("Assign").varParam(ANY_FILE).param(ANY_STRING).param(type(WORD)).required(2);
    routine("Assigned").varParam(TypeFactory.untypedType()).returns(type(BOOLEAN));
    routine("AssignFile").varParam(ANY_FILE).param(ANY_STRING).param(type(WORD)).required(2);
    routine("AtomicCmpExchange")
        .varParam(TypeFactory.untypedType())
        .param(type(INTEGER))
        .param(type(INTEGER))
        .outParam(type(BOOLEAN))
        .required(3)
        .returns(type(INTEGER));
    routine("AtomicCmpExchange")
        .varParam(TypeFactory.untypedType())
        .param(TypeFactory.untypedType())
        .param(TypeFactory.untypedType())
        .outParam(type(BOOLEAN))
        .required(3)
        .returns(typeFactory.untypedPointer());
    routine("AtomicDecrement")
        .varParam(TypeFactory.untypedType())
        .param(TypeFactory.untypedType())
        .required(1)
        .returns(type(INTEGER));
    routine("AtomicExchange")
        .varParam(TypeFactory.untypedType())
        .param(type(INTEGER))
        .returns(type(INTEGER));
    routine("AtomicExchange")
        .varParam(TypeFactory.untypedType())
        .param(typeFactory.untypedPointer())
        .returns(typeFactory.untypedPointer());
    routine("AtomicIncrement")
        .varParam(TypeFactory.untypedType())
        .param(TypeFactory.untypedType())
        .required(1)
        .returns(type(INTEGER));
    routine("BlockRead")
        .varParam(ANY_FILE)
        .varParam(TypeFactory.untypedType())
        .param(type(INTEGER))
        .varParam(ANY_32_BIT_INTEGER)
        .required(3)
        .returns(type(INTEGER));
    routine("BlockWrite")
        .varParam(ANY_FILE)
        .constParam(TypeFactory.untypedType())
        .param(type(INTEGER))
        .varParam(ANY_32_BIT_INTEGER)
        .required(3)
        .returns(type(INTEGER));
    routine("Break");
    routine("BuiltInArcTan").param(type(REAL)).returns(type(EXTENDED));
    routine("BuiltInArcTan2").param(type(REAL)).param(type(REAL)).returns(type(EXTENDED));
    routine("BuiltInCos").param(type(REAL)).returns(type(EXTENDED));
    routine("BuiltInLn").param(type(REAL)).returns(type(EXTENDED));
    routine("BuiltInLnXPlus1").param(type(REAL)).returns(type(EXTENDED));
    routine("BuiltInLog10").param(type(REAL)).returns(type(EXTENDED));
    routine("BuiltInLog2").param(type(REAL)).returns(type(EXTENDED));
    routine("BuiltInSin").param(type(REAL)).returns(type(EXTENDED));
    routine("BuiltInSqrt").param(type(REAL)).returns(type(EXTENDED));
    routine("BuiltInTan").param(type(REAL)).returns(type(EXTENDED));
    routine("Chr").param(type(BYTE)).returns(type(CHAR));
    routine("Close").varParam(ANY_FILE).returns(type(INTEGER));
    routine("CloseFile").varParam(ANY_FILE);
    routine("Concat")
        .param(LIKE_DYNAMIC_ARRAY)
        .param(LIKE_DYNAMIC_ARRAY)
        .variadic(LIKE_DYNAMIC_ARRAY)
        .returns(IntrinsicReturnType.concat(typeFactory));
    routine("Continue");
    routine("Copy")
        .param(type(PANSICHAR))
        .param(type(INTEGER))
        .param(type(INTEGER))
        .returns(IntrinsicReturnType.copy(typeFactory));
    routine("Copy")
        .param(type(PWIDECHAR))
        .param(type(INTEGER))
        .param(type(INTEGER))
        .returns(IntrinsicReturnType.copy(typeFactory));
    routine("Copy")
        .param(LIKE_DYNAMIC_ARRAY)
        .param(type(INTEGER))
        .param(type(INTEGER))
        .required(1)
        .returns(IntrinsicReturnType.copy(typeFactory));
    routine("Dec").varParam(ANY_ORDINAL).param(type(INTEGER)).required(1);
    routine("Dec").varParam(ANY_TYPED_POINTER).param(type(INTEGER)).required(1);
    routine("Default")
        .param(ANY_CLASS_REFERENCE)
        .returns(IntrinsicReturnType.classReferenceValue());
    routine("Delete").varParam(LIKE_DYNAMIC_ARRAY).param(type(INTEGER)).param(type(INTEGER));
    routine("Dispose").varParam(ANY_POINTER);
    routine("Eof").varParam(ANY_FILE).required(0).returns(type(BOOLEAN));
    routine("Eoln").varParam(ANY_FILE).required(0).returns(type(BOOLEAN));
    routine("Erase").varParam(ANY_FILE);
    routine("Exclude").varParam(ANY_SET).param(ANY_ORDINAL);
    routine("Exit").varParam(TypeFactory.untypedType()).required(0);
    routine("Fail");
    routine("FilePos").varParam(ANY_FILE).returns(type(INTEGER));
    routine("FileSize").varParam(ANY_FILE).returns(type(INTEGER));
    routine("FillChar").varParam(TypeFactory.untypedType()).param(type(INTEGER)).param(ANY_ORDINAL);
    routine("Finalize").varParam(TypeFactory.untypedType()).param(type(NATIVEUINT)).required(1);
    routine("FreeMem").varParam(ANY_POINTER).param(type(INTEGER)).required(1);
    routine("GetDir").param(type(BYTE)).varParam(ANY_STRING);
    routine("GetMem").varParam(ANY_POINTER).param(type(INTEGER));
    routine("Halt").param(type(INTEGER)).required(0);
    routine("HasWeakRef").param(ANY_CLASS_REFERENCE).returns(type(BOOLEAN));
    routine("Hi").param(type(INTEGER)).returns(type(BYTE));
    routine("High")
        .varParam(TypeFactory.untypedType())
        .returns(IntrinsicReturnType.high(typeFactory));
    routine("Inc").varParam(ANY_ORDINAL).param(type(INTEGER)).required(1);
    routine("Inc").varParam(ANY_TYPED_POINTER).param(type(INTEGER)).required(1);
    routine("Include").varParam(ANY_SET).param(ANY_ORDINAL);
    routine("Initialize").varParam(TypeFactory.untypedType()).param(type(NATIVEINT)).required(1);
    routine("Insert").param(LIKE_DYNAMIC_ARRAY).varParam(LIKE_DYNAMIC_ARRAY).param(type(INTEGER));
    routine("IsConstValue").param(TypeFactory.untypedType()).returns(type(BOOLEAN));
    routine("IsManagedType").param(ANY_CLASS_REFERENCE).returns(type(BOOLEAN));
    routine("Length").param(type(SHORTSTRING)).returns(type(BYTE));
    routine("Length").param(type(ANSISTRING)).returns(type(INTEGER));
    routine("Length").param(type(UNICODESTRING)).returns(type(INTEGER));
    routine("Length").param(ANY_ARRAY).returns(type(INTEGER));
    routine("Lo").param(type(INTEGER)).returns(type(BYTE));
    routine("Low")
        .varParam(TypeFactory.untypedType())
        .returns(IntrinsicReturnType.low(typeFactory));
    routine("MemoryBarrier");
    routine("MulDivInt64")
        .param(type(INT64))
        .param(type(INT64))
        .param(type(INT64))
        .outParam(type(INT64))
        .required(3)
        .returns(type(INT64));
    routine("New").varParam(ANY_POINTER);
    routine("Odd").param(type(INTEGER)).returns(type(BOOLEAN));
    routine("Ord").param(ANY_ORDINAL).returns(type(BYTE));
    routine("Pi").returns(type(EXTENDED));
    routine("Pred").param(ANY_ORDINAL).returns(type(INTEGER));
    routine("Ptr").param(type(INTEGER)).returns(typeFactory.untypedPointer());
    routine("Read")
        .varParam(ANY_FILE)
        .param(TypeFactory.untypedType())
        .variadic(TypeFactory.untypedType());
    routine("ReadLn").varParam(ANY_FILE).variadic(TypeFactory.untypedType());
    routine("ReallocMem").varParam(ANY_POINTER).param(type(INTEGER));
    routine("Rename").varParam(ANY_FILE).param(ANY_STRING);
    routine("Reset").varParam(ANY_FILE).param(type(INTEGER)).required(1);
    routine("Rewrite").varParam(ANY_FILE).param(type(INTEGER)).required(1);
    routine("Round")
        .param(TypeFactory.untypedType())
        .returns(IntrinsicReturnType.round(typeFactory));
    routine("RunError").param(type(BYTE)).required(0);
    routine("Seek").varParam(ANY_FILE).param(type(INTEGER));
    routine("SeekEof").varParam(ANY_TEXT_FILE).required(0).returns(type(BOOLEAN));
    routine("SeekEoln").varParam(ANY_TEXT_FILE).required(0).returns(type(BOOLEAN));
    routine("SetLength").varParam(LIKE_DYNAMIC_ARRAY).param(type(INTEGER)).variadic(type(INTEGER));
    routine("SetString").varParam(ANY_STRING).param(type(PANSICHAR)).param(type(INTEGER));
    routine("SetString").varParam(ANY_STRING).param(type(PWIDECHAR)).param(type(INTEGER));
    routine("SetTextBuf")
        .varParam(ANY_TEXT_FILE)
        .varParam(TypeFactory.untypedType())
        .param(type(INTEGER))
        .required(2);
    routine("SizeOf").param(TypeFactory.untypedType()).returns(type(INTEGER));
    routine("Slice")
        .varParam(ANY_ARRAY)
        .param(type(INTEGER))
        .returns(IntrinsicReturnType.slice(typeFactory));
    routine("Sqr").param(type(EXTENDED)).returns(type(EXTENDED));
    routine("Sqr").param(type(INTEGER)).returns(type(INTEGER));
    routine("Sqr").param(type(INT64)).returns(type(INT64));
    routine("Str").constParam(TypeFactory.untypedType()).varParam(ANY_STRING);
    routine("Succ").param(ANY_ORDINAL).returns(IntrinsicReturnType.argumentByIndex(0));
    routine("Swap").param(type(INTEGER)).returns(type(INTEGER));
    routine("Trunc")
        .param(TypeFactory.untypedType())
        .returns(IntrinsicReturnType.trunc(typeFactory));
    routine("Truncate").varParam(ANY_FILE);
    routine("TypeHandle").param(TypeFactory.untypedType()).returns(typeFactory.untypedPointer());
    routine("TypeInfo").param(TypeFactory.untypedType()).returns(typeFactory.untypedPointer());
    routine("TypeOf").param(ANY_OBJECT).returns(typeFactory.untypedPointer());
    routine("Val")
        .param(ANY_STRING)
        .varParam(TypeFactory.untypedType())
        .varParam(ANY_32_BIT_INTEGER);
    routine("VarArrayRedim").varParam(ANY_VARIANT).param(type(INTEGER));
    routine("VarCast").varParam(ANY_VARIANT).param(ANY_VARIANT).param(type(INTEGER));
    routine("VarClear").varParam(ANY_VARIANT);
    routine("VarCopy").varParam(ANY_VARIANT).param(ANY_VARIANT);
    routine("Write")
        .varParam(ANY_FILE)
        .param(TypeFactory.untypedType())
        .variadic(TypeFactory.untypedType());
    routine("Write").varParam(TypeFactory.untypedType()).variadic(TypeFactory.untypedType());
    routine("WriteLn").varParam(ANY_FILE).variadic(TypeFactory.untypedType());
    routine("WriteLn").variadic(TypeFactory.untypedType());
  }

  private IntrinsicRoutine.Builder routine(String name) {
    IntrinsicRoutine.Builder builder = IntrinsicRoutine.builder(name);
    routines.add(builder);
    return builder;
  }

  private void injectTypes() {
    Arrays.stream(IntrinsicType.values()).forEach(this::injectType);
  }

  private void injectType(IntrinsicType intrinsic) {
    SymbolicNode node = SymbolicNode.imaginary(intrinsic.simpleName(), scope);
    Type type = typeFactory.getIntrinsic(intrinsic);
    TypeNameDeclaration declaration =
        new TypeNameDeclarationImpl(node, type, intrinsic.fullyQualifiedName());

    scope.addDeclaration(declaration);
  }

  private void injectRoutines() {
    routines.forEach(this::injectRoutine);
  }

  private void injectRoutine(IntrinsicRoutine.Builder builder) {
    IntrinsicRoutine routine = builder.build();
    SymbolicNode node = SymbolicNode.imaginary(routine.simpleName(), scope);
    RoutineNameDeclaration declaration =
        RoutineNameDeclarationImpl.create(node, routine, typeFactory);

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
    var declaration = VariableNameDeclarationImpl.constant(image, type(intrinsic), scope);
    scope.addDeclaration(declaration);
  }
}
