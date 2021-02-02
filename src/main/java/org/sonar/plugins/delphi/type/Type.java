package org.sonar.plugins.delphi.type;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.type.generic.TypeSpecializationContext;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicType;

public interface Type {

  /**
   * Image which describes this type
   *
   * @return The image that describes this type
   */
  String getImage();

  /**
   * Returns the type that this inherits from.
   *
   * <p>For an interface type, this will return the parent interface.
   *
   * <p>For any other type, this will return a concrete parent type, and never an interface.
   *
   * @return The type this inherits from. UnknownType if there is no such type.
   */
  @NotNull
  Type superType();

  /**
   * Returns all types from the ancestor list. (This will also return interfaces.)
   *
   * @return The types that this inherits from.
   */
  Set<Type> parents();

  /**
   * The size of this integer type
   *
   * @return Size
   */
  int size();

  /**
   * Returns whether this type can be specialized.
   *
   * <p>NOTE: When specializing a type, this method does not need to be called. If a Type cannot be
   * specialized, then <code>Type.specialize</code> will return <code>this</code>.
   *
   * @param context a specialization context, contains information to help specialize the type
   * @return true if this type can be specialized
   */
  boolean canBeSpecialized(TypeSpecializationContext context);

  /**
   * If this is a generic type, then we can specialize the type with some given type parameters and
   * arguments.
   *
   * @param context a specialization context, contains information to help specialize the type
   * @return the specialized type, or <code>this</code> if generic specialization wasn't possible
   */
  Type specialize(TypeSpecializationContext context);

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
   * Check whether a type is one of the intrinsic types
   *
   * @param intrinsic Intrinsic type to compare against
   * @return true if the type is the specified intrinsic
   */
  boolean is(IntrinsicType intrinsic);

  /**
   * Check whether a type is a subtype of another.
   *
   * @param image Type image of a potential superType
   * @return true if the specified type image is a superType of this type
   */
  boolean isSubTypeOf(String image);

  /**
   * Check whether a type is a subtype of another.
   *
   * @param superType instance of a potential superType.
   * @return true if the specified type type is a superType of this type
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
   * Check if this type is a subrange type
   *
   * @return true if the type is a subrange type
   */
  boolean isSubrange();

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
   * Check if this type is a string type (ShortString, WideString, UnicodeString, etc...)
   *
   * @return true if the type is a string type
   */
  boolean isString();

  /**
   * Check if this type is an AnsiString
   *
   * @return true if the type is an AnsiString
   */
  boolean isAnsiString();

  /**
   * Check if this type is a char type (Char, WideChar, etc...)
   *
   * @return true if the type is a char type
   */
  boolean isChar();

  /**
   * Check if this type is a boolean type (Boolean, ByteBool, WordBool, etc...)
   *
   * @return true if the type is a boolean type
   */
  boolean isBoolean();

  /**
   * Check if this type is a struct type (object, class, record, etc...)
   *
   * @return true if the type is a struct type
   */
  boolean isStruct();

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
   * Check if this type is an array of const
   *
   * @return true if the type is an array of const
   */
  boolean isArrayOfConst();

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

  /**
   * Check if this type is a 'type type'
   *
   * @return true if the type is a 'type type'
   */
  boolean isTypeType();

  /**
   * Check if this type is an array constructor
   *
   * @return true if the type is an array constructor
   */
  boolean isArrayConstructor();

  /**
   * Check if this type is a helper type
   *
   * @return true if this is a class helper or record helper
   */
  boolean isHelper();

  /**
   * Check if this type is a type parameter type
   *
   * <p>This isn't a "real" type. It's an intermediary type that exists before a generic type/method
   * has been specialized.
   *
   * @return true if this is a type parameter type
   */
  boolean isTypeParameter();

  interface CollectionType extends Type {
    /**
     * The type that this is a collection of
     *
     * @return Element type
     */
    @NotNull
    Type elementType();
  }

  interface ArrayConstructorType extends Type {
    /**
     * The types of the elements passed in to this array constructor
     *
     * @return Element types
     */
    List<Type> elementTypes();

    /**
     * Returns whether the array constructor is empty
     *
     * @return true if the array constructor has no elements
     */
    default boolean isEmpty() {
      return elementTypes().isEmpty();
    }
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

  interface StructType extends ScopedType {
    /**
     * The kind of struct that this type is
     *
     * @return Struct kind
     */
    StructKind kind();

    /**
     * Whether this is a forward type
     *
     * @return true if this is a forward type
     */
    boolean isForwardType();

    /**
     * Adds the full type declaration's information. Also marks this StructType instance as a
     * forward type.
     *
     * @param fullType Type representing the full type declaration
     */
    void setFullType(StructType fullType);

    /**
     * Returns a set of types that this type defines implicit conversion to
     *
     * @return set of types that this type defines implicit conversion to
     */
    Set<Type> typesWithImplicitConversionsFromThis();

    /**
     * Returns a set of types that this type defines implicit conversion from
     *
     * @return set of types that this type defines implicit conversion from
     */
    Set<Type> typesWithImplicitConversionsToThis();

    /**
     * Returns a set of all default array properties that can be called on this type.
     *
     * @return set of default array property declarations
     */
    Set<NameDeclaration> findDefaultArrayProperties();
  }

  interface HelperType extends StructType {
    /**
     * The type that this is a helper for.
     *
     * @return the type that this is a helper for
     */
    @NotNull
    Type extendedType();
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

    /**
     * Sets the dereferenced type. Used for type completion at the end of a type section.
     *
     * @param type The dereferenced type
     */
    void setDereferencedType(Type type);

    /** Flag this pointer type as allowing */
    void setAllowsPointerMath();

    /**
     * Check if this pointer type was declared with {$POINTERMATH ON}
     *
     * @return true if this pointer type was declared with {$POINTERMATH ON}
     */
    boolean allowsPointerMath();
  }

  interface ProceduralType extends Type {
    /**
     * NOTE: The order of this enum matters. The ordinal value is used to determine which kind gets
     * preference during overload resolution.
     *
     * @see org.sonar.plugins.delphi.symbol.resolve.InvocationResolver
     */
    enum ProceduralKind {
      PROCEDURE,
      PROCEDURE_OF_OBJECT,
      REFERENCE,
      ANONYMOUS,
      METHOD
    }

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

    /**
     * The kind of procedural type that this type is
     *
     * @return Procedural kind
     */
    ProceduralKind kind();
  }

  interface FileType extends Type {
    /**
     * The type that this file is comprised of
     *
     * @return File type
     */
    Type fileType();
  }

  interface EnumType extends ScopedType {}

  interface SubrangeType extends Type {
    /**
     * The base type that this is a subrange of
     *
     * @return Base type
     */
    @NotNull
    Type hostType();
  }

  interface ClassReferenceType extends Type {
    /**
     * The class type that this references
     *
     * @return Class type
     */
    Type classType();

    /**
     * Sets the classOf type. Used for type completion at the end of a type section.
     *
     * @param type The classOf type
     */
    void setClassType(Type type);
  }

  interface TypeType extends Type {
    /**
     * The type that this type is based off of
     *
     * @return Original type
     */
    Type originalType();
  }

  interface TypeParameterType extends Type {

    /**
     * The set of constraint types for this type parameter.
     *
     * <p>For example, if we're constrained by a class type, then a generic specialization will
     * require the type argument to be assignment-compatible with that class type.
     *
     * @return list of constraint types
     * @see <a href="http://docwiki.embarcadero.com/RADStudio/Rio/en/Constraints_in_Generics">
     *     Constraints in Generics</a>
     */
    List<Type> constraints();

    /**
     * Adds information from the full type parameter declaration.
     *
     * @param fullType Type representing the full type declaration
     */
    void setFullType(TypeParameterType fullType);
  }

  interface IntegerType extends Type {
    /**
     * Minimum value that this type can hold
     *
     * @return minimum value
     */
    BigInteger min();

    /**
     * Maximum value that this type can hold
     *
     * @return maximum value
     */
    BigInteger max();

    /**
     * Returns whether the type is signed
     *
     * @return true if the type is signed
     */
    boolean isSigned();

    /**
     * Returns whether another integer type is capable of holding this type's value without losing
     * data. (Disregards signing.)
     *
     * @param other Other type
     * @return true if the other integer type has a larger size than this
     */
    boolean isWithinLimit(IntegerType other);

    /**
     * Returns whether another integer type has the same value range as this
     *
     * @param other Other type
     * @return true if the other integer type has the same size and signing as this
     */
    boolean isSameRange(IntegerType other);

    /**
     * The difference in size between another integer type and this, expressed as the distance
     * between their value ranges.
     *
     * @param other Other type
     * @return Ordinal distance between another type and this
     */
    double ordinalDistance(IntegerType other);
  }

  interface DecimalType extends Type {}

  interface BooleanType extends Type {}

  interface CharacterType extends Type {}

  interface StringType extends Type {
    /**
     * The type used for the characters in this string type
     *
     * @return the type used for the characters in this string type
     */
    CharacterType characterType();
  }

  interface AnsiStringType extends Type {
    /**
     * The ansi code page used by this AnsiString
     *
     * @return the ansi code page used by this AnsiString
     */
    int codePage();
  }

  interface VariantType {
    enum VariantKind {
      OLE_VARIANT,
      NORMAL_VARIANT
    }

    /**
     * The kind of variant that this type is
     *
     * @return Variant kind
     */
    VariantKind kind();
  }
}
