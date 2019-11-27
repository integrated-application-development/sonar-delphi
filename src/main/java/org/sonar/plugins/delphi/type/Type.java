package org.sonar.plugins.delphi.type;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.symbol.DelphiScope;

public interface Type {

  /**
   * Image which describes this type
   *
   * @return The image that describes this type
   */
  String getImage();

  /**
   * Returns the type that this inherits from. Note that this will never return an interface.
   *
   * @return The type this inherits from
   */
  Type superType();

  /**
   * Check whether a type is the one designated by the qualified name.
   *
   * @param image Type image to check
   * @return true if the type is the one looked for
   */
  boolean is(String image);

  /**
   * Check whether a type is equivalent to another
   *
   * @param type Type to compare against
   * @return true if the types are equivalent
   */
  boolean is(Type type);

  /**
   * Check whether a type is a subtype of another.
   *
   * @param image Type image of a potential superType
   * @return true if types are equivalent or if the one passed in parameter is in the hierarchy.
   */
  boolean isSubTypeOf(String image);

  /**
   * Check whether a type is a subtype of another.
   *
   * @param superType instance of a potential superType.
   * @return true if types are equivalent or if the one passed in parameter is in the hierarchy.
   */
  boolean isSubTypeOf(Type superType);

  /**
   * Check if this type is untyped.
   *
   * @see <a href="http://pages.cs.wisc.edu/~rkennedy/untyped">What is an untyped parameter?</a>
   * @return true if type is untyped
   */
  boolean isUntyped();

  /**
   * Check if this type is unresolved. This happens when the type declaration cannot be found, but
   * we have the image.
   *
   * @return true if type has not been resolved
   */
  boolean isUnresolved();

  /**
   * Check if this type is unknown. This happens when we can't find any information for a type, not
   * even its image.
   *
   * @return true if type is unknown
   */
  boolean isUnknown();

  /**
   * Check if this type is void. An example of this would be the "return type" of a procedure.
   *
   * @return true if the type is void
   */
  boolean isVoid();

  /**
   * Check if this type is an interface type
   *
   * @return true if the type is an interface type
   */
  boolean isInterface();

  /**
   * Check if this type is a record type
   *
   * @return true if the type is a record type
   */
  boolean isRecord();

  /**
   * Check if this type is an enumeration type
   *
   * @return true if the type is an enumeration type
   */
  boolean isEnum();

  /**
   * Check if this type is an integer type
   *
   * @return true if the type is an integer type
   */
  boolean isInteger();

  /**
   * Check if this type is a decimal type
   *
   * @return true if the type is a decimal type
   */
  boolean isDecimal();

  /**
   * Check if this type is a text type (Char, ShortString, String, etc...)
   *
   * @return true if the type is a text type
   */
  boolean isText();

  /**
   * Check if this type is a string type (ShortString, String, etc...)
   *
   * @return true if the type is a string type
   */
  boolean isString();

  /**
   * Check if this type is a char type (Char, WideChar, etc...)
   *
   * @return true if the type is a char type
   */
  boolean isChar();

  /**
   * Check if this type is a narrow char type (AnsiChar)
   *
   * @return true if the type is a narrow char type
   */
  boolean isNarrowChar();

  /**
   * Check if this type is a wide char type (Char or WideChar)
   *
   * @return true if the type is a wide char type
   */
  boolean isWideChar();

  /**
   * Check if this type is a boolean type (Boolean, ByteBool, WordBool, etc...)
   *
   * @return true if the type is a boolean type
   */
  boolean isBoolean();

  /**
   * Check if this type is an object type (object, class, record, etc...)
   *
   * @return true if the type is an object type
   */
  boolean isObject();

  /**
   * Check if this type is a file type
   *
   * @return true if the type is a file type
   */
  boolean isFile();

  /**
   * Check if this type is an array
   *
   * @return true if the type is an array
   */
  boolean isArray();

  /**
   * Check if this type is a fixed-size array
   *
   * @return true if the type is a fixed array
   */
  boolean isFixedArray();

  /**
   * Check if this type is a dynamic array
   *
   * @return true if the type is a dynamic array
   */
  boolean isDynamicArray();

  /**
   * Check if this type is an open array
   *
   * @return true if the type is an open array
   */
  boolean isOpenArray();

  /**
   * Check if this type is a pointer
   *
   * @return true if the type is a pointer
   */
  boolean isPointer();

  /**
   * Check if this type is a set
   *
   * @return true if the type is a set
   */
  boolean isSet();

  /**
   * Check if this type is a procedural type
   *
   * @return true if the type is a procedural type
   */
  boolean isProcedural();

  /**
   * Check if this type is a method type
   *
   * @return true if the type is a method type
   */
  boolean isMethod();

  /**
   * Check if this type is a class reference
   *
   * @return true if the type is a class reference
   */
  boolean isClassReference();

  /**
   * Check if this type is a variant
   *
   * @return true if the type is a variant
   */
  boolean isVariant();

  interface CollectionType extends Type {
    /**
     * The type that is is a collection of
     *
     * @return Element type
     */
    @NotNull
    Type elementType();
  }

  interface ScopedType extends Type {
    /**
     * The scope of this type's implementation.
     *
     * @return Type scope
     */
    @NotNull
    DelphiScope typeScope();
  }

  interface HelperType extends Type {
    /**
     * The type that this is a helper for.
     *
     * @return Helper type
     */
    @NotNull
    Type helperType();
  }

  interface PointerType extends Type {
    /**
     * The type which this type dereferences to
     *
     * @return Dereferenced type
     */
    @NotNull
    Type dereferencedType();

    /**
     * Check if this pointer is a nil literal
     *
     * @return true if this pointer is a nil literal
     */
    boolean isNilPointer();

    /**
     * Check if this pointer is untyped
     *
     * @return true if this pointer is untyped
     */
    boolean isUntypedPointer();
  }

  interface ProceduralType extends Type {
    /**
     * The type that this method returns
     *
     * @return Return type
     */
    Type returnType();

    /**
     * The types of the parameters that this method expects
     *
     * @return Expected types of parameters
     */
    List<Type> parameterTypes();
  }

  interface FileType extends Type {
    /**
     * The type that this file is comprised of
     *
     * @return File type
     */
    Type fileType();
  }

  interface EnumType extends Type {
    /**
     * The base type that this is an enumeration of
     *
     * @return Base type
     */
    @Nullable
    Type baseType();
  }

  interface ClassReferenceType extends Type {
    /**
     * The class type that this references
     *
     * @return Class type
     */
    ScopedType classType();
  }

  interface VariantType extends Type {
    enum VariantKind {
      OLE_VARIANT,
      NORMAL_VARIANT
    }

    VariantKind kind();
  }
}
