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
package au.com.integradev.delphi.type.factory;

import static org.sonar.plugins.communitydelphi.api.type.StructKind.CLASS_HELPER;
import static org.sonar.plugins.communitydelphi.api.type.StructKind.RECORD_HELPER;

import au.com.integradev.delphi.compiler.Architecture;
import au.com.integradev.delphi.compiler.CompilerVersion;
import au.com.integradev.delphi.compiler.Platform;
import au.com.integradev.delphi.compiler.Toolchain;
import au.com.integradev.delphi.type.factory.StructTypeImpl.ImagePart;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.ast.AttributeListNode;
import org.sonar.plugins.communitydelphi.api.ast.ClassHelperTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.GenericDefinitionNode.TypeParameter;
import org.sonar.plugins.communitydelphi.api.ast.HelperTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineDirective;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypedDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.FileScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.SystemScope;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Parameter;
import org.sonar.plugins.communitydelphi.api.type.StructKind;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.AliasType;
import org.sonar.plugins.communitydelphi.api.type.Type.AnsiStringType;
import org.sonar.plugins.communitydelphi.api.type.Type.ArrayConstructorType;
import org.sonar.plugins.communitydelphi.api.type.Type.CharacterType;
import org.sonar.plugins.communitydelphi.api.type.Type.ClassReferenceType;
import org.sonar.plugins.communitydelphi.api.type.Type.CollectionType;
import org.sonar.plugins.communitydelphi.api.type.Type.EnumType;
import org.sonar.plugins.communitydelphi.api.type.Type.FileType;
import org.sonar.plugins.communitydelphi.api.type.Type.HelperType;
import org.sonar.plugins.communitydelphi.api.type.Type.IntegerSubrangeType;
import org.sonar.plugins.communitydelphi.api.type.Type.IntegerType;
import org.sonar.plugins.communitydelphi.api.type.Type.PointerType;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType.ProceduralKind;
import org.sonar.plugins.communitydelphi.api.type.Type.StructType;
import org.sonar.plugins.communitydelphi.api.type.Type.SubrangeType;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

public class TypeFactoryImpl implements TypeFactory {
  private static final CompilerVersion VERSION_4 = CompilerVersion.fromVersionSymbol("VER120");
  private static final CompilerVersion VERSION_2009 = CompilerVersion.fromVersionNumber("20.0");
  private static final CompilerVersion VERSION_XE8 = CompilerVersion.fromVersionNumber("29.0");
  private static final CompilerVersion VERSION_ATHENS = CompilerVersion.fromVersionNumber("36.0");
  private static final AtomicLong ANONYMOUS_STRUCT_COUNTER = new AtomicLong();

  private final Toolchain toolchain;
  private final CompilerVersion compilerVersion;

  private final TypeAliasGenerator typeAliasGenerator;
  private final EnumMap<IntrinsicType, Type> intrinsicTypes;
  private final IntegerSubrangeType anonymousUInt15;
  private final IntegerSubrangeType anonymousUInt31;
  private final PointerType nilPointer;
  private final FileType untypedFile;
  private final CollectionType emptySet;

  public TypeFactoryImpl(Toolchain toolchain, CompilerVersion compilerVersion) {
    this.toolchain = toolchain;
    this.compilerVersion = compilerVersion;
    this.typeAliasGenerator = new TypeAliasGenerator();
    this.intrinsicTypes = new EnumMap<>(IntrinsicType.class);
    this.nilPointer = pointerTo("nil", TypeFactory.voidType());
    this.untypedFile = fileOf(TypeFactory.untypedType());
    this.emptySet = new SetTypeImpl(TypeFactory.voidType());

    createIntrinsicTypes();

    this.anonymousUInt15 =
        subrange(
            ":AnonymousUInt15",
            BigInteger.ZERO,
            ((IntegerType) getIntrinsic(IntrinsicType.SMALLINT)).max());

    this.anonymousUInt31 =
        subrange(
            ":AnonymousUInt31",
            BigInteger.ZERO,
            ((IntegerType) getIntrinsic(IntrinsicType.INTEGER)).max());
  }

  private boolean isReal48Bit() {
    // See: http://www.ebob42.eu/delphi4/language.htm
    return compilerVersion.compareTo(VERSION_4) < 0;
  }

  private boolean isLong64Bit() {
    // See: http://bit.ly/long-on-different-platforms
    return compilerVersion.compareTo(VERSION_XE8) >= 0
        && toolchain.architecture == Architecture.X64
        && toolchain.platform != Platform.WINDOWS;
  }

  private boolean isNativeIntWeakAlias() {
    // See: https://docwiki.embarcadero.com/Libraries/Athens/en/System.NativeInt
    return compilerVersion.compareTo(VERSION_ATHENS) >= 0;
  }

  private boolean isStringUnicode() {
    // See: http://bit.ly/new-string-type-unicodestring
    return compilerVersion.compareTo(VERSION_2009) >= 0;
  }

  private int sizeByArchitecture(int x86, int x64) {
    if (toolchain.architecture == Architecture.X86) {
      return x86;
    } else {
      return x64;
    }
  }

  private int extendedSize() {
    // See: http://bit.ly/extended-on-different-platforms
    switch (toolchain) {
      case DCCOSX:
      case DCCIOS32:
      case DCCLINUX64:
        return 16;
      case DCC32:
        return 10;
      default:
        return 8;
    }
  }

  private int nativeIntegerSize() {
    if (compilerVersion.compareTo(VERSION_2009) < 0) {
      // See: https://stackoverflow.com/questions/7630781/delphi-2007-and-xe2-using-nativeint
      return 8;
    }
    return sizeByArchitecture(4, 8);
  }

  private int pointerSize() {
    return sizeByArchitecture(4, 8);
  }

  private int proceduralSize(ProceduralKind kind) {
    int result = pointerSize();
    if (kind != ProceduralKind.PROCEDURE) {
      result *= 2;
    }
    return result;
  }

  private void addBoolean(IntrinsicType intrinsic, int size) {
    intrinsicTypes.put(intrinsic, new BooleanTypeImpl(intrinsic.fullyQualifiedName(), size));
  }

  private void addReal(IntrinsicType intrinsic, int size) {
    intrinsicTypes.put(intrinsic, new RealTypeImpl(intrinsic.fullyQualifiedName(), size));
  }

  private void addInteger(IntrinsicType intrinsic, int size, boolean signed) {
    intrinsicTypes.put(
        intrinsic, new IntegerTypeImpl(intrinsic.fullyQualifiedName(), size, signed));
  }

  private void addChar(IntrinsicType intrinsic, int size) {
    intrinsicTypes.put(intrinsic, new CharacterTypeImpl(intrinsic.fullyQualifiedName(), size));
  }

  private void addString(IntrinsicType intrinsic, int size, IntrinsicType charType) {
    intrinsicTypes.put(
        intrinsic,
        new StringTypeImpl(
            intrinsic.fullyQualifiedName(), size, (CharacterType) getIntrinsic(charType)));
  }

  private void addPointer(
      IntrinsicType intrinsic, Type dereferenced, int size, boolean pointerMath) {
    intrinsicTypes.put(
        intrinsic,
        new PointerTypeImpl(intrinsic.fullyQualifiedName(), dereferenced, size, pointerMath));
  }

  private void addVariant(IntrinsicType intrinsic, int size, boolean ole) {
    intrinsicTypes.put(intrinsic, new VariantTypeImpl(intrinsic.fullyQualifiedName(), size, ole));
  }

  private void addWeakAlias(IntrinsicType intrinsic, IntrinsicType aliased) {
    intrinsicTypes.put(intrinsic, weakAlias(intrinsic.fullyQualifiedName(), getIntrinsic(aliased)));
  }

  private void createIntrinsicTypes() {
    addBoolean(IntrinsicType.BOOLEAN, 1);
    addBoolean(IntrinsicType.BYTEBOOL, 1);
    addBoolean(IntrinsicType.WORDBOOL, 2);
    addBoolean(IntrinsicType.LONGBOOL, 4);

    addReal(IntrinsicType.SINGLE, 4);
    addReal(IntrinsicType.DOUBLE, 8);
    addReal(IntrinsicType.REAL48, 6);
    addReal(IntrinsicType.COMP, 8);
    addReal(IntrinsicType.CURRENCY, 8);
    addReal(IntrinsicType.EXTENDED, extendedSize());

    if (isReal48Bit()) {
      addWeakAlias(IntrinsicType.REAL, IntrinsicType.REAL48);
    } else {
      addWeakAlias(IntrinsicType.REAL, IntrinsicType.DOUBLE);
    }

    addInteger(IntrinsicType.SHORTINT, 1, true);
    addInteger(IntrinsicType.BYTE, 1, false);
    addInteger(IntrinsicType.SMALLINT, 2, true);
    addInteger(IntrinsicType.WORD, 2, false);
    addInteger(IntrinsicType.INTEGER, 4, true);
    addInteger(IntrinsicType.CARDINAL, 4, false);
    addInteger(IntrinsicType.INT64, 8, true);
    addInteger(IntrinsicType.UINT64, 8, false);

    if (isLong64Bit()) {
      addWeakAlias(IntrinsicType.LONGINT, IntrinsicType.INT64);
      addWeakAlias(IntrinsicType.LONGWORD, IntrinsicType.UINT64);
    } else {
      addWeakAlias(IntrinsicType.LONGINT, IntrinsicType.INTEGER);
      addWeakAlias(IntrinsicType.LONGWORD, IntrinsicType.CARDINAL);
    }

    if (isNativeIntWeakAlias()) {
      if (toolchain.architecture == Architecture.X64) {
        addWeakAlias(IntrinsicType.NATIVEINT, IntrinsicType.INT64);
        addWeakAlias(IntrinsicType.NATIVEUINT, IntrinsicType.UINT64);
      } else {
        addWeakAlias(IntrinsicType.NATIVEINT, IntrinsicType.INTEGER);
        addWeakAlias(IntrinsicType.NATIVEUINT, IntrinsicType.CARDINAL);
      }
    } else {
      addInteger(IntrinsicType.NATIVEINT, nativeIntegerSize(), true);
      addInteger(IntrinsicType.NATIVEUINT, nativeIntegerSize(), false);
    }

    addChar(IntrinsicType.ANSICHAR, 1);
    addChar(IntrinsicType.WIDECHAR, 2);

    intrinsicTypes.put(
        IntrinsicType.ANSISTRING,
        new AnsiStringTypeImpl(
            pointerSize(), (CharacterType) getIntrinsic(IntrinsicType.ANSICHAR), 0));

    addString(IntrinsicType.WIDESTRING, pointerSize(), IntrinsicType.WIDECHAR);
    addString(IntrinsicType.UNICODESTRING, pointerSize(), IntrinsicType.WIDECHAR);
    addString(IntrinsicType.SHORTSTRING, 256, IntrinsicType.ANSICHAR);

    if (isStringUnicode()) {
      addWeakAlias(IntrinsicType.STRING, IntrinsicType.UNICODESTRING);
      addWeakAlias(IntrinsicType.CHAR, IntrinsicType.WIDECHAR);
    } else {
      addWeakAlias(IntrinsicType.STRING, IntrinsicType.ANSISTRING);
      addWeakAlias(IntrinsicType.CHAR, IntrinsicType.ANSICHAR);
    }

    addPointer(IntrinsicType.POINTER, TypeFactory.untypedType(), pointerSize(), false);
    addPointer(IntrinsicType.PWIDECHAR, getIntrinsic(IntrinsicType.WIDECHAR), pointerSize(), true);
    addPointer(IntrinsicType.PANSICHAR, getIntrinsic(IntrinsicType.ANSICHAR), pointerSize(), true);

    if (isStringUnicode()) {
      addWeakAlias(IntrinsicType.PCHAR, IntrinsicType.PWIDECHAR);
    } else {
      addWeakAlias(IntrinsicType.PCHAR, IntrinsicType.PANSICHAR);
    }

    int variantSize = sizeByArchitecture(16, 24);
    addVariant(IntrinsicType.VARIANT, variantSize, false);
    addVariant(IntrinsicType.OLEVARIANT, variantSize, true);

    intrinsicTypes.put(
        IntrinsicType.TEXT,
        new FileTypeImpl(TypeFactory.untypedType(), sizeByArchitecture(730, 754)) {
          @Override
          public String getImage() {
            return IntrinsicType.TEXT.fullyQualifiedName();
          }
        });
    addWeakAlias(IntrinsicType.TEXTFILE, IntrinsicType.TEXT);
  }

  private static List<ImagePart> createImageParts(TypeDeclarationNode declaration) {
    List<ImagePart> result = new ArrayList<>();
    for (TypeDeclarationNode outer : declaration.getOuterTypeDeclarationNodes()) {
      result.add(createImagePart(outer));
    }
    result.add(createImagePart(declaration));
    return result;
  }

  private static ImagePart createImagePart(TypeDeclarationNode declaration) {
    List<Type> typeParameters =
        declaration.getTypeNameNode().getTypeParameters().stream()
            .map(TypeParameter::getType)
            .collect(Collectors.toUnmodifiableList());
    return new ImagePart(declaration.simpleName(), typeParameters);
  }

  private static Set<Type> getAncestors(TypeDeclarationNode typeDeclaration, StructKind kind) {
    Set<Type> parents = typeDeclaration.getTypeNode().getParentTypes();
    if (parents.isEmpty()) {
      parents = getDefaultAncestors(typeDeclaration, kind);
    }
    return parents;
  }

  private static Set<Type> getDefaultAncestors(TypeDeclarationNode node, StructKind kind) {
    SystemScope systemScope = getSystemScope(node);
    String image = node.fullyQualifiedName();

    if (systemScope != null) {
      TypedDeclaration defaultAncestor = null;

      switch (kind) {
        case CLASS:
          if (!"System.TObject".equals(image)) {
            defaultAncestor = systemScope.getTObjectDeclaration();
          }
          break;

        case INTERFACE:
          if (!"System.IInterface".equals(image)) {
            defaultAncestor = systemScope.getIInterfaceDeclaration();
          }
          break;

        case CLASS_HELPER:
          defaultAncestor = systemScope.getTClassHelperBaseDeclaration();
          break;

        default:
          // Do nothing
      }

      if (defaultAncestor != null) {
        return Set.of(defaultAncestor.getType());
      }
    }

    return Collections.emptySet();
  }

  @Nullable
  private static SystemScope getSystemScope(DelphiNode node) {
    FileScope unitScope = node.getScope().getEnclosingScope(FileScope.class);
    if (unitScope != null) {
      return unitScope.getSystemScope();
    }
    return null;
  }

  @Override
  public Type getIntrinsic(IntrinsicType intrinsic) {
    return intrinsicTypes.get(intrinsic);
  }

  @Override
  public AnsiStringType ansiString(int codePage) {
    if (codePage == 0) {
      return (AnsiStringType) getIntrinsic(IntrinsicType.ANSISTRING);
    }

    return new AnsiStringTypeImpl(
        pointerSize(), (CharacterType) getIntrinsic(IntrinsicType.ANSICHAR), codePage);
  }

  public CollectionType array(@Nullable String image, Type elementType, Set<ArrayOption> options) {
    return new ArrayTypeImpl(image, pointerSize(), elementType, options);
  }

  public CollectionType multiDimensionalArray(
      @Nullable String image, Type elementType, int indices, Set<ArrayOption> options) {
    CollectionType type = array(null, elementType, options);
    for (int i = 1; i < indices; ++i) {
      type = array(((i == indices - 1) ? image : null), type, Set.of(ArrayOption.FIXED));
    }
    return type;
  }

  @Override
  public ArrayConstructorType arrayConstructor(List<Type> types) {
    return new ArrayConstructorTypeImpl(types);
  }

  @Override
  public CollectionType set(Type type) {
    return new SetTypeImpl(type);
  }

  @Override
  public CollectionType emptySet() {
    return emptySet;
  }

  public EnumType enumeration(String image, DelphiScope scope) {
    return new EnumTypeImpl(image, scope);
  }

  @Override
  public SubrangeType subrange(String image, Type type) {
    if (type.isInteger()) {
      var integerType = (IntegerType) type;
      return subrange(image, integerType.min(), integerType.max());
    }
    return new SubrangeTypeImpl(image, type);
  }

  @Override
  public IntegerSubrangeType subrange(String image, BigInteger min, BigInteger max) {
    IntegerType lowType = integerFromLiteralValue(min);
    IntegerType highType = integerFromLiteralValue(max);

    IntegerType hostType;
    if (highType.size() > lowType.size()) {
      hostType = highType;
      if (min.compareTo(BigInteger.ZERO) < 0 && !highType.isSigned()) {
        hostType = integerFromLiteralValue(max.negate().subtract(BigInteger.ONE));
      }
    } else {
      hostType = lowType;
    }

    return new IntegerSubrangeTypeImpl(image, hostType, min, max);
  }

  @Override
  public PointerType pointerTo(@Nullable String image, Type type) {
    return new PointerTypeImpl(image, type, pointerSize(), false);
  }

  @Override
  public PointerType untypedPointer() {
    return (PointerType) getIntrinsic(IntrinsicType.POINTER);
  }

  @Override
  public PointerType nilPointer() {
    return nilPointer;
  }

  @Override
  public FileType fileOf(Type type) {
    return new FileTypeImpl(type, sizeByArchitecture(592, 616));
  }

  @Override
  public FileType untypedFile() {
    return untypedFile;
  }

  @Override
  public ClassReferenceType classOf(@Nullable String image, Type type) {
    return new ClassReferenceTypeImpl(image, type, pointerSize());
  }

  @Override
  public AliasType strongAlias(String image, Type aliased) {
    return typeAliasGenerator.generate(image, aliased, true);
  }

  @Override
  public AliasType weakAlias(String image, Type aliased) {
    return typeAliasGenerator.generate(image, aliased, false);
  }

  public ProceduralType createProcedural(
      ProceduralKind kind,
      List<Parameter> parameters,
      Type returnType,
      Set<RoutineDirective> directives) {
    return new ProceduralTypeImpl(proceduralSize(kind), kind, parameters, returnType, directives);
  }

  public StructType struct(TypeNode node) {
    List<ImagePart> imageParts = new ArrayList<>();
    ImagePart unitPart = new ImagePart(node.getUnitName());
    imageParts.add(unitPart);

    Set<Type> ancestors = Collections.emptySet();
    StructKind kind = StructKind.fromNode(node);
    Node parent = node.getParent();
    List<Type> attributeTypes = Collections.emptyList();

    if (parent instanceof TypeDeclarationNode) {
      TypeDeclarationNode typeDeclaration = (TypeDeclarationNode) parent;
      ancestors = getAncestors(typeDeclaration, kind);
      imageParts.addAll(createImageParts(typeDeclaration));
      attributeTypes = getAttributeTypes(typeDeclaration);
    } else {
      String anonymousImage = "<anonymous_type_" + ANONYMOUS_STRUCT_COUNTER.incrementAndGet() + ">";
      imageParts.add(new ImagePart(anonymousImage));
    }

    return new StructTypeImpl(
        imageParts, pointerSize(), node.getScope(), ancestors, kind, attributeTypes);
  }

  private static List<Type> getAttributeTypes(TypeDeclarationNode typeDeclaration) {
    AttributeListNode attributeList = typeDeclaration.getAttributeList();
    if (attributeList == null) {
      return Collections.emptyList();
    }

    return attributeList.getAttributeTypes();
  }

  public HelperType helper(HelperTypeNode node) {
    TypeDeclarationNode declaration = (TypeDeclarationNode) node.getParent();
    StructKind kind = (node instanceof ClassHelperTypeNode) ? CLASS_HELPER : RECORD_HELPER;

    return new HelperTypeImpl(
        createImageParts(declaration),
        pointerSize(),
        node.getScope(),
        getAncestors(declaration, kind),
        node.getFor().getType(),
        kind,
        getAttributeTypes(declaration));
  }

  public IntegerSubrangeType anonymousUInt15() {
    return anonymousUInt15;
  }

  public IntegerSubrangeType anonymousUInt31() {
    return anonymousUInt31;
  }

  @Override
  public IntegerType integerFromLiteralValue(BigInteger value) {
    return intrinsicTypes.values().stream()
        .filter(IntegerType.class::isInstance)
        .map(IntegerType.class::cast)
        .filter(type -> type.min().compareTo(value) <= 0 && type.max().compareTo(value) >= 0)
        .findFirst()
        .orElseThrow(IllegalStateException::new);
  }

  public CompilerVersion getCompilerVersion() {
    return compilerVersion;
  }
}
