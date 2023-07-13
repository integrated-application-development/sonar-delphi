package org.sonar.plugins.communitydelphi.api.type;

import au.com.integradev.delphi.type.factory.UnknownTypeImpl;
import au.com.integradev.delphi.type.factory.UntypedTypeImpl;
import au.com.integradev.delphi.type.factory.VoidTypeImpl;
import java.math.BigInteger;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.communitydelphi.api.type.Type.AnsiStringType;
import org.sonar.plugins.communitydelphi.api.type.Type.ArrayConstructorType;
import org.sonar.plugins.communitydelphi.api.type.Type.ClassReferenceType;
import org.sonar.plugins.communitydelphi.api.type.Type.CollectionType;
import org.sonar.plugins.communitydelphi.api.type.Type.FileType;
import org.sonar.plugins.communitydelphi.api.type.Type.IntegerType;
import org.sonar.plugins.communitydelphi.api.type.Type.PointerType;
import org.sonar.plugins.communitydelphi.api.type.Type.SubrangeType;
import org.sonar.plugins.communitydelphi.api.type.Type.TypeType;

public interface TypeFactory {

  static Type unknownType() {
    return UnknownTypeImpl.instance();
  }

  static Type untypedType() {
    return UntypedTypeImpl.instance();
  }

  static Type voidType() {
    return VoidTypeImpl.instance();
  }

  Type getIntrinsic(IntrinsicType intrinsic);

  AnsiStringType ansiString(int codePage);

  ArrayConstructorType arrayConstructor(List<Type> types);

  CollectionType set(Type type);

  CollectionType emptySet();

  SubrangeType subRange(String image, Type type);

  PointerType pointerTo(@Nullable String image, Type type);

  PointerType untypedPointer();

  PointerType nilPointer();

  FileType fileOf(Type type);

  FileType untypedFile();

  ClassReferenceType classOf(@Nullable String image, Type type);

  TypeType typeType(String image, Type type);

  IntegerType integerFromLiteralValue(BigInteger value);
}
