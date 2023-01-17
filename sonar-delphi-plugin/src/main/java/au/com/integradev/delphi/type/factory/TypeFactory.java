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
package au.com.integradev.delphi.type.factory;

import static au.com.integradev.delphi.type.StructKind.CLASS_HELPER;
import static au.com.integradev.delphi.type.StructKind.RECORD_HELPER;

import au.com.integradev.delphi.antlr.ast.node.ClassHelperTypeNode;
import au.com.integradev.delphi.antlr.ast.node.DelphiNode;
import au.com.integradev.delphi.antlr.ast.node.GenericDefinitionNode.TypeParameter;
import au.com.integradev.delphi.antlr.ast.node.HelperTypeNode;
import au.com.integradev.delphi.antlr.ast.node.TypeDeclarationNode;
import au.com.integradev.delphi.antlr.ast.node.TypeNode;
import au.com.integradev.delphi.compiler.Architecture;
import au.com.integradev.delphi.compiler.CompilerVersion;
import au.com.integradev.delphi.compiler.Platform;
import au.com.integradev.delphi.compiler.Toolchain;
import au.com.integradev.delphi.symbol.declaration.TypedDeclaration;
import au.com.integradev.delphi.symbol.scope.DelphiScope;
import au.com.integradev.delphi.symbol.scope.FileScope;
import au.com.integradev.delphi.symbol.scope.SystemScope;
import au.com.integradev.delphi.type.ArrayOption;
import au.com.integradev.delphi.type.DelphiType;
import au.com.integradev.delphi.type.StructKind;
import au.com.integradev.delphi.type.Type;
import au.com.integradev.delphi.type.Type.AnsiStringType;
import au.com.integradev.delphi.type.Type.ArrayConstructorType;
import au.com.integradev.delphi.type.Type.CharacterType;
import au.com.integradev.delphi.type.Type.ClassReferenceType;
import au.com.integradev.delphi.type.Type.CollectionType;
import au.com.integradev.delphi.type.Type.EnumType;
import au.com.integradev.delphi.type.Type.FileType;
import au.com.integradev.delphi.type.Type.HelperType;
import au.com.integradev.delphi.type.Type.IntegerType;
import au.com.integradev.delphi.type.Type.PointerType;
import au.com.integradev.delphi.type.Type.ProceduralType;
import au.com.integradev.delphi.type.Type.ProceduralType.ProceduralKind;
import au.com.integradev.delphi.type.Type.StructType;
import au.com.integradev.delphi.type.Type.SubrangeType;
import au.com.integradev.delphi.type.Type.TypeType;
import au.com.integradev.delphi.type.Type.VariantType.VariantKind;
import au.com.integradev.delphi.type.factory.DelphiStructType.ImagePart;
import au.com.integradev.delphi.type.intrinsic.IntrinsicType;
import au.com.integradev.delphi.type.parameter.Parameter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import net.sourceforge.pmd.lang.ast.Node;
import org.jetbrains.annotations.Nullable;

public class TypeFactory {
  private static final CompilerVersion VERSION_4 = CompilerVersion.fromVersionSymbol("VER120");
  private static final CompilerVersion VERSION_2009 = CompilerVersion.fromVersionNumber("20.0");
  private static final CompilerVersion VERSION_XE8 = CompilerVersion.fromVersionNumber("29.0");
  private static final AtomicLong ANONYMOUS_STRUCT_COUNTER = new AtomicLong();

  private final Toolchain toolchain;
  private final CompilerVersion compilerVersion;

  private final EnumMap<IntrinsicType, Type> intrinsicTypes;
  private final PointerType nilPointer;
  private final FileType untypedFile;
  private final CollectionType emptySet;

  public TypeFactory(Toolchain toolchain, CompilerVersion compilerVersion) {
    this.toolchain = toolchain;
    this.compilerVersion = compilerVersion;
    this.intrinsicTypes = new EnumMap<>(IntrinsicType.class);
    this.nilPointer = pointerTo("nil", DelphiType.voidType());
    this.untypedFile = fileOf(DelphiType.untypedType());
    this.emptySet = new DelphiSetType(DelphiType.voidType());
    createIntrinsicTypes();
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
    intrinsicTypes.put(intrinsic, new DelphiBooleanType(intrinsic.fullyQualifiedName(), size));
  }

  private void addDecimal(IntrinsicType intrinsic, int size) {
    intrinsicTypes.put(intrinsic, new DelphiDecimalType(intrinsic.fullyQualifiedName(), size));
  }

  private void addInteger(IntrinsicType intrinsic, int size, boolean signed) {
    intrinsicTypes.put(
        intrinsic, new DelphiIntegerType(intrinsic.fullyQualifiedName(), size, signed));
  }

  private void addChar(IntrinsicType intrinsic, int size) {
    intrinsicTypes.put(intrinsic, new DelphiCharacterType(intrinsic.fullyQualifiedName(), size));
  }

  private void addString(IntrinsicType intrinsic, int size, IntrinsicType charType) {
    intrinsicTypes.put(
        intrinsic,
        new DelphiStringType(
            intrinsic.fullyQualifiedName(), size, (CharacterType) getIntrinsic(charType)));
  }

  private void addPointer(
      IntrinsicType intrinsic, Type dereferenced, int size, boolean pointerMath) {
    intrinsicTypes.put(
        intrinsic,
        new DelphiPointerType(intrinsic.fullyQualifiedName(), dereferenced, size, pointerMath));
  }

  private void addVariant(IntrinsicType intrinsic, int size, VariantKind kind) {
    intrinsicTypes.put(
        intrinsic, new DelphiVariantType(intrinsic.fullyQualifiedName(), size, kind));
  }

  private void addAlias(IntrinsicType alias, IntrinsicType concrete) {
    intrinsicTypes.put(alias, getIntrinsic(concrete));
  }

  private void createIntrinsicTypes() {
    addBoolean(IntrinsicType.BOOLEAN, 1);
    addBoolean(IntrinsicType.BYTEBOOL, 1);
    addBoolean(IntrinsicType.WORDBOOL, 2);
    addBoolean(IntrinsicType.LONGBOOL, 4);

    addDecimal(IntrinsicType.SINGLE, 4);
    addDecimal(IntrinsicType.DOUBLE, 8);
    addDecimal(IntrinsicType.REAL48, 6);
    addDecimal(IntrinsicType.COMP, 8);
    addDecimal(IntrinsicType.CURRENCY, 8);
    addDecimal(IntrinsicType.EXTENDED, extendedSize());

    if (isReal48Bit()) {
      addAlias(IntrinsicType.REAL, IntrinsicType.REAL48);
    } else {
      addAlias(IntrinsicType.REAL, IntrinsicType.DOUBLE);
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
      addAlias(IntrinsicType.LONGINT, IntrinsicType.INT64);
      addAlias(IntrinsicType.LONGWORD, IntrinsicType.UINT64);
    } else {
      addAlias(IntrinsicType.LONGINT, IntrinsicType.INTEGER);
      addAlias(IntrinsicType.LONGWORD, IntrinsicType.CARDINAL);
    }

    addInteger(IntrinsicType.NATIVEINT, nativeIntegerSize(), true);
    addInteger(IntrinsicType.NATIVEUINT, nativeIntegerSize(), false);

    addChar(IntrinsicType.ANSICHAR, 1);
    addChar(IntrinsicType.WIDECHAR, 2);

    intrinsicTypes.put(
        IntrinsicType.ANSISTRING,
        new DelphiAnsiStringType(
            pointerSize(), (CharacterType) getIntrinsic(IntrinsicType.ANSICHAR), 0));

    addString(IntrinsicType.WIDESTRING, pointerSize(), IntrinsicType.WIDECHAR);
    addString(IntrinsicType.UNICODESTRING, pointerSize(), IntrinsicType.WIDECHAR);
    addString(IntrinsicType.SHORTSTRING, 256, IntrinsicType.ANSICHAR);

    if (isStringUnicode()) {
      addAlias(IntrinsicType.STRING, IntrinsicType.UNICODESTRING);
      addAlias(IntrinsicType.CHAR, IntrinsicType.WIDECHAR);
    } else {
      addAlias(IntrinsicType.STRING, IntrinsicType.ANSISTRING);
      addAlias(IntrinsicType.CHAR, IntrinsicType.ANSICHAR);
    }

    addPointer(IntrinsicType.POINTER, DelphiType.untypedType(), pointerSize(), false);
    addPointer(IntrinsicType.PWIDECHAR, getIntrinsic(IntrinsicType.WIDECHAR), pointerSize(), true);
    addPointer(IntrinsicType.PANSICHAR, getIntrinsic(IntrinsicType.ANSICHAR), pointerSize(), true);

    if (isStringUnicode()) {
      addAlias(IntrinsicType.PCHAR, IntrinsicType.PWIDECHAR);
    } else {
      addAlias(IntrinsicType.PCHAR, IntrinsicType.PANSICHAR);
    }

    int variantSize = sizeByArchitecture(16, 24);
    addVariant(IntrinsicType.VARIANT, variantSize, VariantKind.NORMAL_VARIANT);
    addVariant(IntrinsicType.OLEVARIANT, variantSize, VariantKind.OLE_VARIANT);

    intrinsicTypes.put(
        IntrinsicType.TEXT,
        new DelphiFileType(DelphiType.untypedType(), sizeByArchitecture(730, 754)) {
          @Override
          public String getImage() {
            return IntrinsicType.TEXT.fullyQualifiedName();
          }
        });
    addAlias(IntrinsicType.TEXTFILE, IntrinsicType.TEXT);
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

  private ProceduralType createProcedural(
      ProceduralKind kind, List<Parameter> parameters, Type returnType) {
    return createProcedural(kind, parameters, returnType, false);
  }

  private ProceduralType createProcedural(
      ProceduralKind kind, List<Parameter> parameters, Type returnType, boolean variadic) {
    return new DelphiProceduralType(proceduralSize(kind), kind, parameters, returnType, variadic);
  }

  public Type untypedType() {
    return DelphiType.untypedType();
  }

  public Type getIntrinsic(IntrinsicType intrinsic) {
    return intrinsicTypes.get(intrinsic);
  }

  public AnsiStringType ansiString(int codePage) {
    if (codePage == 0) {
      return (AnsiStringType) getIntrinsic(IntrinsicType.ANSISTRING);
    }

    return new DelphiAnsiStringType(
        pointerSize(), (CharacterType) getIntrinsic(IntrinsicType.ANSICHAR), codePage);
  }

  public CollectionType array(@Nullable String image, Type elementType, Set<ArrayOption> options) {
    return new DelphiArrayType(image, pointerSize(), elementType, options);
  }

  public CollectionType multiDimensionalArray(
      @Nullable String image, Type elementType, int indices, Set<ArrayOption> options) {
    CollectionType type = array(null, elementType, options);
    for (int i = 1; i < indices; ++i) {
      type = array(((i == indices - 1) ? image : null), type, Set.of(ArrayOption.FIXED));
    }
    return type;
  }

  public ArrayConstructorType arrayConstructor(List<Type> types) {
    return new DelphiArrayConstructorType(types);
  }

  public CollectionType set(Type type) {
    return new DelphiSetType(type);
  }

  public CollectionType emptySet() {
    return emptySet;
  }

  public EnumType enumeration(String image, DelphiScope scope) {
    return new DelphiEnumerationType(image, scope);
  }

  public SubrangeType subRange(String image, Type type) {
    return new DelphiSubrangeType(image, type);
  }

  public PointerType pointerTo(@Nullable String image, Type type) {
    return new DelphiPointerType(image, type, pointerSize(), false);
  }

  public PointerType untypedPointer() {
    return (PointerType) getIntrinsic(IntrinsicType.POINTER);
  }

  public PointerType nilPointer() {
    return nilPointer;
  }

  public FileType fileOf(Type type) {
    return new DelphiFileType(type, sizeByArchitecture(592, 616));
  }

  public FileType untypedFile() {
    return untypedFile;
  }

  public ClassReferenceType classOf(@Nullable String image, Type type) {
    return new DelphiClassReferenceType(image, type, pointerSize());
  }

  public ProceduralType procedure(List<Parameter> parameters, Type returnType) {
    return createProcedural(ProceduralKind.PROCEDURE, parameters, returnType);
  }

  public ProceduralType ofObject(List<Parameter> parameters, Type returnType) {
    return createProcedural(ProceduralKind.PROCEDURE_OF_OBJECT, parameters, returnType);
  }

  public ProceduralType reference(List<Parameter> parameters, Type returnType) {
    return createProcedural(ProceduralKind.REFERENCE, parameters, returnType);
  }

  public ProceduralType anonymous(List<Parameter> parameters, Type returnType) {
    return createProcedural(ProceduralKind.ANONYMOUS, parameters, returnType);
  }

  public ProceduralType method(List<Parameter> parameters, Type returnType) {
    return createProcedural(ProceduralKind.METHOD, parameters, returnType);
  }

  public ProceduralType method(List<Parameter> parameters, Type returnType, boolean variadic) {
    return createProcedural(ProceduralKind.METHOD, parameters, returnType, variadic);
  }

  public TypeType typeType(String image, Type type) {
    return new DelphiTypeType(image, type);
  }

  public StructType struct(TypeNode node) {
    List<ImagePart> imageParts = new ArrayList<>();
    ImagePart unitPart = new ImagePart(node.findUnitName());
    imageParts.add(unitPart);

    Set<Type> ancestors = Collections.emptySet();
    StructKind kind = StructKind.fromNode(node);
    Node parent = node.jjtGetParent();

    if (parent instanceof TypeDeclarationNode) {
      TypeDeclarationNode typeDeclaration = (TypeDeclarationNode) parent;
      ancestors = getAncestors(typeDeclaration, kind);
      imageParts.addAll(createImageParts(typeDeclaration));
    } else {
      String anonymousImage = "<anonymous_type_" + ANONYMOUS_STRUCT_COUNTER.incrementAndGet() + ">";
      imageParts.add(new ImagePart(anonymousImage));
    }

    return new DelphiStructType(imageParts, pointerSize(), node.getScope(), ancestors, kind);
  }

  public HelperType helper(HelperTypeNode node) {
    TypeDeclarationNode declaration = (TypeDeclarationNode) node.jjtGetParent();
    StructKind kind = (node instanceof ClassHelperTypeNode) ? CLASS_HELPER : RECORD_HELPER;

    return new DelphiHelperType(
        createImageParts(declaration),
        pointerSize(),
        node.getScope(),
        getAncestors(declaration, kind),
        node.getFor().getType(),
        kind);
  }

  public IntegerType integerFromLiteralValue(BigInteger value) {
    return intrinsicTypes.values().stream()
        .filter(IntegerType.class::isInstance)
        .map(IntegerType.class::cast)
        .filter(type -> type.min().compareTo(value) <= 0 && type.max().compareTo(value) >= 0)
        .findFirst()
        .orElseThrow(IllegalStateException::new);
  }
}
