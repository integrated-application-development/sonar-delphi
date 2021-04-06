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
    method("Abs").parameters(type(REAL)).returns(type(REAL));
    method("Abs").parameters(type(INTEGER)).returns(type(INTEGER));
    method("Abs").parameters(type(INT64)).returns(type(INT64));
    method("Addr").parameters(typeFactory.untypedType()).returns(typeFactory.untypedPointer());
    method("Append").parameters(type(TEXT)).returns(type(INTEGER));
    method("Assert").parameters(type(BOOLEAN), type(UNICODESTRING)).required(1);
    method("Assign")
        .parameters(typeFactory.untypedFile(), type(UNICODESTRING), type(WORD))
        .required(2);
    method("Assigned").parameters(typeFactory.untypedType()).returns(type(BOOLEAN));
    method("AssignFile")
        .parameters(typeFactory.untypedFile(), type(UNICODESTRING), type(WORD))
        .required(2);
    method("AtomicCmpExchange")
        .parameters(typeFactory.untypedType(), type(INTEGER), type(INTEGER), type(BOOLEAN))
        .required(3)
        .returns(type(INTEGER));
    method("AtomicCmpExchange")
        .parameters(
            typeFactory.untypedType(),
            typeFactory.untypedPointer(),
            typeFactory.untypedPointer(),
            type(BOOLEAN))
        .required(3)
        .returns(typeFactory.untypedPointer());
    method("AtomicDecrement")
        .parameters(typeFactory.untypedType(), typeFactory.untypedType())
        .required(1)
        .returns(type(INTEGER));
    method("AtomicExchange")
        .parameters(typeFactory.untypedType(), type(INTEGER))
        .returns(type(INTEGER));
    method("AtomicExchange")
        .parameters(typeFactory.untypedType(), typeFactory.untypedPointer())
        .returns(typeFactory.untypedPointer());
    method("AtomicIncrement")
        .parameters(typeFactory.untypedType(), typeFactory.untypedType())
        .required(1)
        .returns(type(INTEGER));
    method("BlockRead")
        .parameters(
            typeFactory.untypedFile(), typeFactory.untypedType(), type(INTEGER), type(INTEGER))
        .required(3)
        .returns(type(INTEGER));
    method("BlockWrite")
        .parameters(
            typeFactory.untypedFile(), typeFactory.untypedType(), type(INTEGER), type(INTEGER))
        .required(3)
        .returns(type(INTEGER));
    method("Break");
    method("BuiltInArcTan").parameters(type(REAL)).returns(type(EXTENDED));
    method("BuiltInArcTan2").parameters(type(REAL), type(REAL)).returns(type(EXTENDED));
    method("BuiltInCos").parameters(type(REAL)).returns(type(EXTENDED));
    method("BuiltInLn").parameters(type(REAL)).returns(type(EXTENDED));
    method("BuiltInLnXPlus1").parameters(type(REAL)).returns(type(EXTENDED));
    method("BuiltInLog10").parameters(type(REAL)).returns(type(EXTENDED));
    method("BuiltInLog2").parameters(type(REAL)).returns(type(EXTENDED));
    method("BuiltInSin").parameters(type(REAL)).returns(type(EXTENDED));
    method("BuiltInSqrt").parameters(type(REAL)).returns(type(EXTENDED));
    method("BuiltInTan").parameters(type(REAL)).returns(type(EXTENDED));
    method("Chr").parameters(type(BYTE)).returns(type(CHAR));
    method("Close").parameters(typeFactory.untypedFile()).returns(type(INTEGER));
    method("CloseFile").parameters(typeFactory.untypedFile());
    method("Concat")
        .parameters(type(UNICODESTRING), type(UNICODESTRING))
        .variadic(type(UNICODESTRING))
        .returns(type(UNICODESTRING));
    method("Concat")
        .parameters(ANY_DYNAMIC_ARRAY, ANY_DYNAMIC_ARRAY)
        .variadic(ANY_DYNAMIC_ARRAY)
        .returns(ANY_DYNAMIC_ARRAY);
    method("Continue");
    method("Copy")
        .parameters(type(UNICODESTRING), type(INTEGER), type(INTEGER))
        .returns(type(UNICODESTRING));
    method("Copy")
        .parameters(ANY_DYNAMIC_ARRAY, type(INTEGER), type(INTEGER))
        .required(1)
        .returns(type(UNICODESTRING));
    method("Dec").parameters(ANY_ORDINAL, type(INTEGER)).required(1).returns(type(INTEGER));
    method("Default")
        .parameters(ANY_CLASS_REFERENCE)
        .returns(IntrinsicReturnType.classReferenceValue());
    method("Delete").parameters(type(UNICODESTRING), type(INTEGER), type(INTEGER));
    method("Delete").parameters(ANY_DYNAMIC_ARRAY, type(INTEGER), type(INTEGER));
    method("Dispose").parameters(typeFactory.untypedPointer());
    method("Eof").parameters(typeFactory.untypedFile()).required(0).returns(type(BOOLEAN));
    method("Eoln").parameters(typeFactory.untypedFile()).required(0).returns(type(BOOLEAN));
    method("Erase").parameters(typeFactory.untypedFile());
    method("Exclude").parameters(ANY_SET, ANY_ORDINAL);
    method("Exit").parameters(typeFactory.untypedType()).required(0);
    method("Fail");
    method("FilePos").parameters(typeFactory.untypedFile()).returns(type(INTEGER));
    method("FileSize").parameters(typeFactory.untypedFile()).returns(type(INTEGER));
    method("FillChar").parameters(typeFactory.untypedType(), type(INTEGER), ANY_ORDINAL);
    method("Finalize").parameters(typeFactory.untypedType(), type(NATIVEUINT)).required(1);
    method("FreeMem").parameters(typeFactory.untypedPointer(), type(INTEGER)).required(1);
    method("GetDir").parameters(type(BYTE), type(UNICODESTRING));
    method("GetMem").parameters(typeFactory.untypedPointer(), type(INTEGER));
    method("Halt").parameters(type(INTEGER)).required(0);
    method("HasWeakRef").parameters(ANY_CLASS_REFERENCE).returns(type(BOOLEAN));
    method("Hi").parameters(type(INTEGER)).returns(type(BYTE));
    method("High")
        .parameters(typeFactory.untypedType())
        .returns(IntrinsicReturnType.high(typeFactory));
    method("Inc").parameters(ANY_ORDINAL, type(INTEGER)).required(1);
    method("Include").parameters(ANY_SET, ANY_ORDINAL);
    method("Initialize").parameters(typeFactory.untypedType(), type(NATIVEINT)).required(1);
    method("Insert").parameters(type(UNICODESTRING), type(UNICODESTRING), type(INTEGER));
    method("Insert").parameters(ANY_DYNAMIC_ARRAY, ANY_DYNAMIC_ARRAY, type(INTEGER));
    method("IsConstValue").parameters(typeFactory.untypedType()).returns(type(BOOLEAN));
    method("IsManagedType").parameters(ANY_CLASS_REFERENCE).returns(type(BOOLEAN));
    method("Length").parameters(type(UNICODESTRING)).returns(type(INTEGER));
    method("Length").parameters(ANY_ARRAY).returns(type(INTEGER));
    method("Lo").parameters(type(INTEGER)).returns(type(BYTE));
    method("Low")
        .parameters(typeFactory.untypedType())
        .returns(IntrinsicReturnType.low(typeFactory));
    method("MemoryBarrier");
    method("MulDivInt64")
        .parameters(type(INT64), type(INT64), type(INT64), type(INT64))
        .required(3)
        .returns(type(INT64));
    method("New").parameters(typeFactory.untypedPointer());
    method("Odd").parameters(type(INTEGER)).returns(type(BOOLEAN));
    method("Ord").parameters(ANY_ORDINAL).returns(type(BYTE));
    method("Pi").returns(type(EXTENDED));
    method("Pred").parameters(ANY_ORDINAL).returns(type(INTEGER));
    method("Ptr").parameters(type(INTEGER)).returns(typeFactory.untypedPointer());
    method("Read")
        .parameters(typeFactory.untypedFile(), typeFactory.untypedType())
        .variadic(typeFactory.untypedType());
    method("ReadLn").parameters(typeFactory.untypedFile()).variadic(typeFactory.untypedType());
    method("ReallocMem").parameters(typeFactory.untypedPointer(), type(INTEGER));
    method("Rename").parameters(typeFactory.untypedFile(), type(UNICODESTRING));
    method("Reset").parameters(typeFactory.untypedFile(), type(INTEGER)).required(1);
    method("Rewrite").parameters(typeFactory.untypedFile(), type(INTEGER)).required(1);
    method("Round").parameters(type(REAL)).returns(type(INT64));
    method("RunError").parameters(type(BYTE)).required(0);
    method("Seek").parameters(typeFactory.untypedFile(), type(INTEGER));
    method("SeekEof").parameters(type(TEXT)).required(0).returns(type(BOOLEAN));
    method("SeekEoln").parameters(type(TEXT)).required(0).returns(type(BOOLEAN));
    method("SetLength").parameters(type(UNICODESTRING), type(INTEGER));
    method("SetLength").parameters(ANY_DYNAMIC_ARRAY, type(INTEGER)).variadic(type(INTEGER));
    method("SetString").parameters(type(SHORTSTRING), type(PANSICHAR), type(INTEGER));
    method("SetString").parameters(type(ANSISTRING), type(PANSICHAR), type(INTEGER));
    method("SetString").parameters(type(WIDESTRING), type(PCHAR), type(INTEGER));
    method("SetString").parameters(type(UNICODESTRING), type(PCHAR), type(INTEGER));
    method("SetTextBuf")
        .parameters(type(TEXT), typeFactory.untypedType(), type(INTEGER))
        .required(2);
    method("SizeOf").parameters(typeFactory.untypedType()).returns(type(INTEGER));
    method("Slice").parameters(ANY_ARRAY, type(INTEGER)).returns(typeFactory.untypedPointer());
    method("Sqr").parameters(type(EXTENDED)).returns(type(EXTENDED));
    method("Sqr").parameters(type(INTEGER)).returns(type(INTEGER));
    method("Sqr").parameters(type(REAL)).returns(type(INT64));
    method("Str").parameters(typeFactory.untypedType(), type(UNICODESTRING));
    method("Succ").parameters(ANY_ORDINAL).returns(type(INTEGER));
    method("Swap").parameters(type(INTEGER)).returns(type(INTEGER));
    method("Trunc").parameters(type(REAL)).returns(type(INT64));
    method("Truncate").parameters(typeFactory.untypedFile());
    method("TypeHandle")
        .parameters(typeFactory.untypedType())
        .returns(typeFactory.untypedPointer());
    method("TypeInfo").parameters(typeFactory.untypedType()).returns(typeFactory.untypedPointer());
    method("TypeOf").parameters(ANY_OBJECT).returns(typeFactory.untypedPointer());
    method("Val").parameters(type(UNICODESTRING), typeFactory.untypedType(), type(INTEGER));
    method("VarArrayRedim").parameters(type(VARIANT), type(INTEGER));
    method("VarCast").parameters(type(VARIANT), type(VARIANT), type(INTEGER));
    method("VarClear").parameters(type(VARIANT));
    method("VarCopy").parameters(type(VARIANT), type(VARIANT));
    method("Write")
        .parameters(typeFactory.untypedFile(), typeFactory.untypedType())
        .variadic(typeFactory.untypedType());
    method("Write").parameters(typeFactory.untypedType()).variadic(typeFactory.untypedType());
    method("WriteLn").parameters(typeFactory.untypedFile()).variadic(typeFactory.untypedType());
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
