package org.sonar.plugins.delphi.type.intrinsic;

import static org.sonar.plugins.delphi.type.DelphiFileType.untypedFile;
import static org.sonar.plugins.delphi.type.DelphiPointerType.pointerTo;
import static org.sonar.plugins.delphi.type.DelphiPointerType.untypedPointer;
import static org.sonar.plugins.delphi.type.DelphiType.untypedType;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_ARRAY;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_DYNAMIC_ARRAY;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_OBJECT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_ORDINAL;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_SET;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicBoolean.BOOLEAN;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicDecimal.EXTENDED;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicDecimal.REAL;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicFile.TEXT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.BYTE;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.INT64;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.INTEGER;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.NATIVEINT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.NATIVEUINT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.WORD;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.CHAR;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.UNICODESTRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicVariant.VARIANT;

import org.sonar.plugins.delphi.type.intrinsic.IntrinsicMethodData.IntrinsicMethodDataBuilder;

public enum IntrinsicMethod {
  ABS_REAL(method("Abs").parameters(REAL.type).returns(REAL.type)),
  ABS_INTEGER(method("Abs").parameters(INTEGER.type).returns(INTEGER.type)),
  ABS_INT64(method("Abs").parameters(INT64.type).returns(INT64.type)),
  ADDR(method("Addr").parameters(untypedType()).returns(untypedPointer())),
  APPEND(method("Append").parameters(TEXT.type).returns(INTEGER.type)),
  ASSERT(method("Assert").parameters(BOOLEAN.type).required(1).returns(UNICODESTRING.type)),
  ASSIGN(method("Assign").parameters(untypedFile(), UNICODESTRING.type, WORD.type).required(2)),
  ASSIGNED(method("Assigned").parameters(untypedType()).returns(BOOLEAN.type)),
  ASSIGN_FILE(
      method("AssignFile").parameters(untypedFile(), UNICODESTRING.type, WORD.type).required(2)),
  ATOMIC_CMP_EXCHANGE_INTEGER(
      method("AtomicCmpExchange")
          .parameters(INTEGER.type, INTEGER.type, BOOLEAN.type)
          .required(2)
          .returns(INTEGER.type)),
  ATOMIC_CMP_EXCHANGE_POINTER(
      method("AtomicCmpExchange")
          .parameters(untypedPointer(), untypedPointer(), BOOLEAN.type)
          .required(2)
          .returns(untypedPointer())),
  ATOMIC_CMP_EXCHANGE_NATIVEINT(
      method("AtomicCmpExchange")
          .parameters(NATIVEINT.type, NATIVEINT.type, BOOLEAN.type)
          .required(2)
          .returns(NATIVEINT.type)),
  ATOMIC_DECREMENT(
      method("AtomicDecrement")
          .parameters(untypedType(), untypedType())
          .required(1)
          .returns(INTEGER.type)),
  ATOMIC_EXCHANGE_INTEGER(
      method("AtomicExchange").parameters(untypedType(), INTEGER.type).returns(INTEGER.type)),
  ATOMIC_EXCHANGE_NATIVEINT(
      method("AtomicExchange").parameters(untypedType(), NATIVEINT.type).returns(INTEGER.type)),
  ATOMIC_EXCHANGE_POINTER(
      method("AtomicExchange").parameters(untypedType(), untypedPointer()).returns(INTEGER.type)),
  ATOMIC_INCREMENT(
      method("AtomicIncrement")
          .parameters(untypedType(), untypedType())
          .required(1)
          .returns(INTEGER.type)),
  BLOCK_READ(
      method("BlockRead")
          .parameters(untypedFile(), untypedType(), INTEGER.type, INTEGER.type)
          .required(3)
          .returns(INTEGER.type)),
  BLOCK_WRITE(
      method("BlockWrite")
          .parameters(untypedFile(), untypedType(), INTEGER.type, INTEGER.type)
          .required(3)
          .returns(INTEGER.type)),
  BREAK(method("Break")),
  BUILTIN_ARCTAN(method("BuiltInArcTan").parameters(REAL.type).returns(EXTENDED.type)),
  BUILTIN_ARCTAN2(method("BuiltInArcTan2").parameters(REAL.type, REAL.type).returns(EXTENDED.type)),
  BUILTIN_COS(method("BuiltInCos").parameters(REAL.type).returns(EXTENDED.type)),
  BUILTIN_LN(method("BuiltInLn").parameters(REAL.type).returns(EXTENDED.type)),
  BUILTIN_LNXPLUS1(method("BuiltInLnXPlus1").parameters(REAL.type).returns(EXTENDED.type)),
  BUILTIN_LOG10(method("BuiltInLog10").parameters(REAL.type).returns(EXTENDED.type)),
  BUILTIN_LOG2(method("BuiltInLog2").parameters(REAL.type).returns(EXTENDED.type)),
  BUILTIN_SIN(method("BuiltInSin").parameters(REAL.type).returns(EXTENDED.type)),
  BUILTIN_SQRT(method("BuiltInSqrt").parameters(REAL.type).returns(EXTENDED.type)),
  BUILTIN_TAN(method("BuiltInTan").parameters(REAL.type).returns(EXTENDED.type)),
  CHR(method("Chr").parameters(BYTE.type).returns(CHAR.type)),
  CLOSE(method("Close").parameters(untypedFile()).returns(INTEGER.type)),
  CLOSE_FILE(method("CloseFile").parameters(untypedFile())),
  CONCAT_STRING(
      method("Concat")
          .parameters(UNICODESTRING.type, UNICODESTRING.type)
          .variadic(UNICODESTRING.type)
          .returns(UNICODESTRING.type)),
  CONCAT_ARRAY(
      method("Concat")
          .parameters(ANY_DYNAMIC_ARRAY, ANY_DYNAMIC_ARRAY)
          .variadic(ANY_DYNAMIC_ARRAY)
          .returns(ANY_DYNAMIC_ARRAY)),
  CONTINUE(method("Continue")),
  COPY_STRING(
      method("Copy")
          .parameters(UNICODESTRING.type, INTEGER.type, INTEGER.type)
          .returns(UNICODESTRING.type)),
  COPY_ARRAY(
      method("Copy")
          .parameters(ANY_DYNAMIC_ARRAY, INTEGER.type, INTEGER.type)
          .returns(UNICODESTRING.type)),
  DEC(method("Dec").parameters(ANY_ORDINAL, INTEGER.type).required(1).returns(INTEGER.type)),
  DELETE_STRING(method("Delete").parameters(UNICODESTRING.type, INTEGER.type, INTEGER.type)),
  DELETE_ARRAY(method("Delete").parameters(ANY_DYNAMIC_ARRAY, INTEGER.type, INTEGER.type)),
  DISPOSE(method("Dispose").parameters(untypedPointer())),
  EOF(method("Eof").parameters(untypedFile()).required(0).returns(BOOLEAN.type)),
  EOLN(method("Eoln").parameters(untypedFile()).required(0).returns(BOOLEAN.type)),
  ERASE(method("Erase").parameters(untypedFile())),
  EXCLUDE(method("Exclude").parameters(ANY_SET, ANY_ORDINAL)),
  EXIT(method("Exit").parameters(untypedType())),
  FAIL(method("Fail")),
  FILEPOS(method("FilePos").parameters(untypedFile()).returns(INTEGER.type)),
  FILESIZE(method("FileSize").parameters(untypedFile()).returns(INTEGER.type)),
  FILLCHAR(method("FillChar").parameters(untypedType(), INTEGER.type, ANY_ORDINAL)),
  FINALIZE(method("Finalize").parameters(untypedType(), NATIVEUINT.type).required(1)),
  FLUSH(method("Flush").parameters(TEXT.type).returns(INTEGER.type)),
  FREEMEM(method("FreeMem").parameters(untypedPointer(), INTEGER.type).required(1)),
  GETDIR(method("GetDir").parameters(BYTE.type, UNICODESTRING.type)),
  GETMEM(method("GetMem").parameters(untypedPointer(), INTEGER.type)),
  HALT(method("Halt").parameters(INTEGER.type).required(0)),
  HI(method("Hi").parameters(INTEGER.type)),
  HIGH(method("High").parameters(untypedType())),
  INC(method("Inc").parameters(ANY_ORDINAL, INTEGER.type).required(1)),
  INCLUDE(method("Include").parameters(ANY_SET, ANY_ORDINAL)),
  INITIALIZE(method("Initialize").parameters(untypedType(), NATIVEINT.type).required(1)),
  INSERT_STRING(method("Insert").parameters(UNICODESTRING.type, UNICODESTRING.type, INTEGER.type)),
  INSERT_ARRAY(method("Insert").parameters(ANY_DYNAMIC_ARRAY, ANY_DYNAMIC_ARRAY, INTEGER.type)),
  LENGTH_STRING(method("Length").parameters(UNICODESTRING.type).returns(INTEGER.type)),
  LENGTH_ARRAY(method("Length").parameters(ANY_DYNAMIC_ARRAY).returns(INTEGER.type)),
  LO(method("Lo").parameters(INTEGER.type).returns(BYTE.type)),
  LOW(method("Low").parameters(untypedType()).returns(INTEGER.type)),
  MEMORY_BARRIER(method("MemoryBarrier")),
  MULDIVINT64(
      method("MulDivInt64")
          .parameters(INT64.type, INT64.type, INT64.type, INT64.type)
          .required(3)
          .returns(INT64.type)),
  NEW(method("New").parameters(untypedPointer())),
  ODD(method("Odd").parameters(INTEGER.type).returns(BOOLEAN.type)),
  ORD(method("Ord").parameters(ANY_ORDINAL).returns(BYTE.type)),
  PI(method("Pi").returns(EXTENDED.type)),
  PRED(method("Pred").parameters(ANY_ORDINAL).returns(INTEGER.type)),
  PTR(method("Ptr").parameters(INTEGER.type).returns(untypedPointer())),
  READ(method("Read").parameters(untypedFile(), untypedType()).variadic(untypedType())),
  READLN(method("ReadLn").parameters(untypedFile()).variadic(untypedType())),
  REALLOC_MEM(method("ReallocMem").parameters(untypedPointer(), INTEGER.type)),
  RENAME(method("Rename").parameters(untypedFile(), UNICODESTRING.type)),
  RESET(method("Reset").parameters(untypedFile(), INTEGER.type).required(1)),
  REWRITE(method("Rewrite").parameters(untypedFile(), INTEGER.type).required(1)),
  ROUND(method("Round").parameters(REAL.type).returns(INT64.type)),
  RUN_ERROR(method("RunError").parameters(BYTE.type).required(0)),
  SEEK(method("Seek").parameters(untypedFile(), INTEGER.type)),
  SEEK_EOF(method("SeekEof").parameters(TEXT.type).required(0).returns(BOOLEAN.type)),
  SEEK_EOLN(method("SeekEoln").parameters(TEXT.type).required(0).returns(BOOLEAN.type)),
  SET_LENGTH_STRING(method("SetLength").parameters(UNICODESTRING.type, INTEGER.type)),
  SET_LENGTH_ARRAY(method("SetLength").parameters(ANY_DYNAMIC_ARRAY, INTEGER.type)),
  SET_STRING(
      method("SetString").parameters(UNICODESTRING.type, pointerTo(CHAR.type), INTEGER.type)),
  SET_TEXT_BUF(method("SetTextBuf").parameters(TEXT.type, untypedType(), INTEGER.type).required(2)),
  SIZEOF(method("SizeOf").parameters(untypedType()).returns(INTEGER.type)),
  SLICE(method("Slice").parameters(ANY_ARRAY, INTEGER.type).returns(untypedPointer())),
  SQR_REAL(method("Sqr").parameters(EXTENDED.type).returns(EXTENDED.type)),
  SQR_INTEGER(method("Sqr").parameters(INTEGER.type).returns(INTEGER.type)),
  SQR_INT64(method("Sqr").parameters(REAL.type).returns(INT64.type)),
  STR(method("Str").parameters(untypedType(), UNICODESTRING.type)),
  SUCC(method("Succ").parameters(ANY_ORDINAL).returns(INTEGER.type)),
  SWAP(method("Swap").parameters(INTEGER.type).returns(INTEGER.type)),
  TRUNC(method("Trunc").parameters(REAL.type).returns(INT64.type)),
  TRUNCATE(method("Truncate").parameters(untypedFile())),
  TYPE_HANDLE(method("TypeHandle").parameters(untypedType()).returns(untypedPointer())),
  TYPE_INFO(method("TypeInfo").parameters(untypedType()).returns(untypedPointer())),
  TYPEOF(method("TypeOf").parameters(ANY_OBJECT).returns(untypedPointer())),
  VAL(method("Val").parameters(UNICODESTRING.type, untypedType(), INTEGER.type)),
  VAR_ARRAY_REDIM(method("VarArrayRedim").parameters(VARIANT.type, INTEGER.type)),
  VAR_CAST(method("VarCast").parameters(VARIANT.type, VARIANT.type, INTEGER.type)),
  VARCLEAR(method("VarClear").parameters(VARIANT.type)),
  VARCOPY(method("VarCopy").parameters(VARIANT.type, VARIANT.type)),
  WRITE_FILE(method("Write").parameters(untypedFile(), untypedType()).variadic(untypedType())),
  WRITE_OUTPUT(method("Write").parameters(untypedType()).variadic(untypedType())),
  WRITELN_FILE(method("WriteLn").parameters(untypedFile()).variadic(untypedType())),
  WRITELN_OUTPUT(method("WriteLn").variadic(untypedType()));

  public final IntrinsicMethodData data;

  IntrinsicMethod(IntrinsicMethodDataBuilder builder) {
    this.data = builder.build();
  }

  private static IntrinsicMethodDataBuilder method(String name) {
    return IntrinsicMethodData.builder(name);
  }
}
