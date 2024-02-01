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
package au.com.integradev.delphi.executor;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.DelphiProperties;
import au.com.integradev.delphi.compiler.Platform;
import au.com.integradev.delphi.core.Delphi;
import au.com.integradev.delphi.file.DelphiFile.DelphiInputFile;
import au.com.integradev.delphi.file.DelphiFileConfig;
import au.com.integradev.delphi.preprocessor.DelphiPreprocessorFactory;
import au.com.integradev.delphi.preprocessor.search.SearchPath;
import au.com.integradev.delphi.symbol.SymbolTable;
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import au.com.integradev.delphi.utils.DelphiUtils;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.fs.internal.DefaultTextPointer;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitNameDeclaration;

class DelphiSymbolTableExecutorTest {
  private static final String ROOT_PATH = "/au/com/integradev/delphi/symbol/";

  private DelphiInputFile mainFile;
  private SymbolTable symbolTable;
  private DelphiSymbolTableExecutor executor;
  private SensorContextTester context;
  private Set<String> unitScopeNames;
  private Map<String, String> unitAliases;
  private Path standardLibraryPath;
  private String componentKey;

  @BeforeEach
  void setup(@TempDir Path tempDir) {
    executor = new DelphiSymbolTableExecutor();
    context = SensorContextTester.create(DelphiUtils.getResource(ROOT_PATH));
    unitScopeNames = new HashSet<>();
    unitAliases = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    standardLibraryPath = createStandardLibrary(tempDir);
  }

  @Test
  void testSimpleFile() {
    execute("Simple.pas");
    verifyUsages(7, 2, reference(20, 10), reference(29, 10), reference(34, 10));
    verifyUsages(12, 3);
    verifyUsages(20, 2);
    verifyUsages(9, 14, reference(29, 22), reference(36, 1));
    verifyUsages(10, 14, reference(31, 1), reference(34, 22));
  }

  @Test
  void testSimilarParameterDeclarations() {
    execute("SimilarParameterDeclarations.pas");
    verifyUsages(6, 2, reference(14, 10), reference(19, 10));
    verifyUsages(8, 14, reference(14, 15));
    verifyUsages(8, 19);
    verifyUsages(9, 14, reference(19, 15), reference(16, 2));
    verifyUsages(9, 19);
  }

  @Test
  void testRecords() {
    execute("Records.pas");
    verifyUsages(6, 2, reference(16, 21));
    verifyUsages(7, 4, reference(31, 30));
    verifyUsages(10, 2, reference(24, 11), reference(29, 11));
    verifyUsages(12, 4, reference(26, 14));
    verifyUsages(13, 6, reference(26, 31));
    verifyUsages(16, 4, reference(31, 14));
    verifyUsages(18, 13, reference(24, 16));
    verifyUsages(19, 13, reference(29, 16));
  }

  @Test
  void testRecordVariants() {
    execute("RecordVariants.pas");
    verifyUsages(14, 10, reference(33, 2));
    verifyUsages(19, 10, reference(34, 2));
    verifyUsages(24, 10, reference(35, 2));
  }

  @Test
  void testInheritedInvocations() {
    execute("InheritedInvocations.pas");
    verifyUsages(7, 14, reference(38, 14), reference(50, 14));
    verifyUsages(
        12,
        13,
        reference(25, 16),
        reference(37, 4),
        reference(38, 4),
        reference(39, 4),
        reference(40, 4),
        reference(40, 14),
        reference(41, 4),
        reference(52, 14),
        reference(55, 14),
        reference(62, 19),
        reference(63, 14));
    verifyUsages(
        13,
        13,
        reference(30, 16),
        reference(39, 14),
        reference(41, 14),
        reference(42, 14),
        reference(49, 4),
        reference(50, 4),
        reference(51, 4),
        reference(51, 14),
        reference(52, 4),
        reference(53, 4),
        reference(53, 14),
        reference(54, 14),
        reference(62, 14),
        reference(63, 30));
    verifyUsages(18, 13, reference(35, 16), reference(56, 4));
    verifyUsages(19, 13, reference(44, 4), reference(47, 16));
    verifyUsages(20, 13, reference(57, 4), reference(60, 16));
  }

  @Test
  void testNestedRoutines() {
    execute("NestedRoutines.pas");
    verifyUsages(6, 9);
    verifyUsages(8, 11, reference(27, 12));
    verifyUsages(8, 15, reference(10, 7));
    verifyUsages(17, 11, reference(27, 16));
    verifyUsages(17, 15, reference(19, 7));
  }

  @Test
  void testNestedTypes() {
    execute("NestedTypes.pas");
    verifyUsages(7, 8, reference(20, 10), reference(21, 5));
    verifyUsages(8, 26, reference(23, 4), reference(24, 4));
  }

  @Test
  void testWeakAliasParameter() {
    execute("WeakAliasParameter.pas");
    verifyUsages(6, 2, reference(9, 14), reference(13, 25));
    verifyUsages(9, 2, reference(18, 31));
    verifyUsages(11, 2, reference(18, 10));
    verifyUsages(13, 14, reference(18, 15));
  }

  @Test
  void testArrays() {
    execute("Arrays.pas");
    verifyUsages(
        8,
        14,
        reference(30, 15),
        reference(40, 17),
        reference(41, 30),
        reference(42, 30),
        reference(43, 23),
        reference(44, 25),
        reference(45, 9),
        reference(46, 46),
        reference(47, 14),
        reference(60, 14),
        reference(68, 9));
    verifyUsages(15, 13, reference(46, 27));
    verifyUsages(24, 13, reference(44, 6));
    verifyUsages(25, 13, reference(46, 6));
    verifyUsages(37, 2, reference(40, 2));
    verifyUsages(38, 2, reference(41, 2), reference(42, 2));
  }

  @Test
  void testArrayArgument() {
    execute("ArrayArgument.pas");
    verifyUsages(7, 10, reference(14, 2));
    verifyUsages(12, 14, reference(14, 6));
  }

  @Test
  void testArrayConstantExpressions() {
    execute("ArrayConstantExpressions.pas");
    verifyUsages(11, 10, reference(23, 2), reference(24, 2));
    verifyUsages(16, 10, reference(25, 2), reference(26, 2), reference(27, 2), reference(30, 2));
    verifyUsages(36, 10, reference(46, 2), reference(47, 2), reference(48, 2), reference(49, 2));
  }

  @Test
  void testArrayConstructor() {
    execute("ArrayConstructor.pas");
    verifyUsages(10, 10, reference(17, 2), reference(27, 2));
    verifyUsages(20, 9, reference(28, 4), reference(29, 4), reference(30, 4));
  }

  @Test
  void testArrayOfConst() {
    execute("ArrayOfConst.pas");
    verifyUsages(7, 10, reference(14, 2), reference(15, 2), reference(16, 2));
    verifyUsages(12, 14, reference(15, 7), reference(16, 7));
  }

  @Test
  void testAnonymousMethods() {
    execute("AnonymousMethods.pas");
    verifyUsages(6, 2, reference(11, 20), reference(26, 20));
    verifyUsages(7, 2, reference(16, 20), reference(26, 47));
    verifyUsages(11, 10, reference(30, 2), reference(31, 2));
    verifyUsages(16, 10, reference(36, 2), reference(37, 2));
    verifyUsages(21, 10, reference(39, 4));
    verifyUsages(28, 2, reference(33, 13));
    verifyUsages(31, 16, reference(33, 4));
  }

  @Test
  void testUsesDeclarations() {
    execute("UsesDeclarations.pas");
    verifyUsages(1, 5, reference(23, 7));
    verifyUsages(15, 11, reference(22, 2), reference(23, 2));
    verifyUsages(6, 2, reference(24, 2));
    verifyUsages(13, 2, reference(25, 2));
  }

  @Test
  void testResultTypes() {
    execute("ResultTypes.pas");
    verifyUsages(
        6,
        2,
        reference(15, 10),
        reference(20, 19),
        reference(25, 54),
        reference(27, 12),
        reference(34, 12),
        reference(40, 44),
        reference(42, 12));
    verifyUsages(8, 14, reference(15, 15), reference(28, 9), reference(35, 9), reference(43, 9));
    verifyUsages(20, 10, reference(29, 2), reference(36, 2), reference(44, 2));
  }

  @Test
  void testPascalResultAssignments() {
    execute("PascalResultAssignments.pas");
    verifyUsages(5, 9, reference(11, 9), reference(13, 2));
    verifyUsages(6, 9, reference(16, 9), reference(21, 4), reference(25, 2));
    verifyUsages(7, 9, reference(28, 9));
    verifyUsages(30, 2, reference(32, 2));
  }

  @Test
  void testSelfTypes() {
    execute("SelfTypes.pas");
    verifyUsages(17, 10, reference(34, 2));
    verifyUsages(22, 10, reference(39, 2));
    verifyUsages(27, 10, reference(46, 2));
  }

  @Test
  void testSelfInNestedProcedures() {
    execute("SelfInNestedProcedures.pas");
    verifyUsages(17, 11, reference(34, 2));
    verifyUsages(19, 11, reference(29, 4), reference(38, 4));
  }

  @Test
  void testInitializationFinalization() {
    execute("InitializationFinalization.pas");
    verifyUsages(6, 2, reference(14, 7), reference(17, 9));
    verifyUsages(8, 14, reference(18, 6));
    verifyUsages(14, 2, reference(20, 2));
  }

  @Test
  void testMethodDeclarationTypes() {
    execute("MethodDeclarationTypes.pas");
    verifyUsages( // TypeAndFunc type
        6,
        2,
        reference(16, 31),
        reference(16, 45),
        reference(19, 39),
        reference(19, 53),
        reference(24, 23),
        reference(24, 37),
        reference(55, 30),
        reference(55, 44),
        reference(60, 48),
        reference(60, 62),
        reference(65, 72),
        reference(65, 86),
        reference(62, 7));
    verifyUsages(31, 10, reference(57, 2));
    verifyUsages(37, 10, reference(62, 2));
    verifyUsages(42, 10, reference(67, 2));
  }

  @Test
  void testRecordExpressionItems() {
    execute("RecordExpressionItems.pas");
    verifyUsages(14, 10, reference(21, 11));
  }

  @Test
  void testHardTypeCast() {
    execute("HardTypeCast.pas");
    verifyUsages(8, 4, reference(17, 12));
    verifyUsages(15, 18, reference(17, 44));
  }

  @Test
  void testHandlerProperty() {
    execute("HandlerProperty.pas");
    verifyUsages(10, 4, reference(21, 2));
    verifyUsages(19, 19, reference(21, 22));
  }

  @Test
  void testWithStatement() {
    execute("WithStatement.pas");
    verifyUsages(7, 4, reference(42, 4));
    verifyUsages(12, 4, reference(24, 4));
    verifyUsages(8, 4, reference(25, 4));
    verifyUsages(21, 2, reference(26, 4));
  }

  @Test
  void testForStatement() {
    execute("ForStatement.pas");
    verifyUsages(9, 2, reference(11, 6), reference(15, 6), reference(19, 6));
  }

  @Test
  void testBareInterfaceMethodReference() {
    execute("BareInterfaceMethodReference.pas");
    verifyUsages(5, 9, reference(15, 9));
    verifyUsages(6, 9, reference(20, 9));
    verifyUsages(10, 10, reference(17, 2));
  }

  @Test
  void testClassReferenceMethodResolution() {
    execute("classReferences/MethodResolution.pas");
    verifyUsages(9, 14, reference(18, 6));
  }

  @Test
  void testClassReferenceArgumentResolution() {
    execute("classReferences/ArgumentResolution.pas");
    verifyUsages(18, 10, reference(25, 2), reference(26, 2));
  }

  @Test
  void testClassReferenceConstructorTypeResolution() {
    execute("classReferences/ConstructorTypeResolution.pas");
    verifyUsages(15, 10, reference(22, 2));
    verifyUsages(8, 16, reference(22, 11));
  }

  @Test
  void testSimpleForwardDeclarations() {
    execute("forwardDeclarations/Simple.pas");
    verifyUsages(22, 26, reference(24, 14));
  }

  @Test
  void testInheritanceForwardDeclarations() {
    execute("forwardDeclarations/Inheritance.pas");
    verifyUsages(27, 10, reference(34, 2), reference(35, 2));
    verifyUsages(14, 15, reference(34, 11));
    verifyUsages(32, 26, reference(35, 7));
  }

  @Test
  void testImplicitForwardDeclarations() {
    execute("forwardDeclarations/ImplicitForwarding.pas");
    verifyUsages(9, 2, reference(6, 17), reference(7, 22));
  }

  @Test
  void testTypeSignaturesOfForwardDeclaration() {
    execute("forwardDeclarations/TypeSignature.pas");
    verifyUsages(19, 12, reference(24, 10), reference(37, 2));
    verifyUsages(20, 12, reference(29, 10), reference(40, 2));
  }

  @Test
  void testSimpleTypeResolution() {
    execute("typeResolution/Simple.pas");
    verifyUsages(6, 2, reference(14, 10), reference(16, 21), reference(25, 2), reference(29, 22));
    verifyUsages(8, 16, reference(25, 7), reference(26, 9));
    verifyUsages(9, 14, reference(23, 7), reference(24, 9), reference(25, 14), reference(26, 16));
    verifyUsages(12, 2, reference(21, 10), reference(29, 9));
    verifyUsages(14, 4, reference(23, 2), reference(31, 12));
    verifyUsages(15, 14, reference(21, 15));
    verifyUsages(16, 13, reference(24, 2), reference(26, 2), reference(29, 14));
  }

  @Test
  void testCharTypeResolution() {
    execute("typeResolution/Chars.pas");
    verifyUsages(7, 9, reference(22, 2));
    verifyUsages(12, 9, reference(23, 2));
  }

  @Test
  void testCastTypeResolution() {
    execute("typeResolution/Casts.pas");
    verifyUsages(8, 14, reference(15, 12), reference(16, 16));
  }

  @Test
  void testConstructorTypeResolution() {
    execute("typeResolution/Constructors.pas");
    verifyUsages(8, 14, reference(23, 14));
    verifyUsages(13, 14, reference(24, 14));
  }

  @Test
  void testConstructorInvokedOnInstanceTypeResolution() {
    execute("typeResolution/ConstructorInvokedOnInstance.pas");
    verifyUsages(12, 10, reference(20, 2), reference(23, 2), reference(27, 2));
  }

  @Test
  void testEnumsTypeResolution() {
    execute("typeResolution/Enums.pas");
    verifyUsages(18, 9, reference(28, 2), reference(29, 2), reference(30, 2));
  }

  @Test
  void testPointersTypeResolution() {
    execute("typeResolution/Pointers.pas");
    verifyUsages(7, 10, reference(16, 2), reference(17, 2), reference(18, 2), reference(19, 2));
  }

  @Test
  void testSubRangeHostTypeResolution() {
    execute("typeResolution/SubRangeHostType.pas");
    verifyUsages(12, 10, reference(19, 2), reference(20, 2), reference(21, 2));
  }

  @Test
  void testIntegersTypeInference() {
    execute("typeInference/Integers.pas");
    verifyUsages(10, 10, reference(63, 2));
    verifyUsages(15, 10, reference(70, 2), reference(77, 2));
    verifyUsages(20, 10, reference(71, 2), reference(78, 2));
    verifyUsages(25, 10, reference(65, 2), reference(66, 2), reference(72, 2), reference(73, 2));
    verifyUsages(30, 10, reference(64, 2), reference(68, 2), reference(75, 2));
    verifyUsages(35, 10, reference(67, 2), reference(69, 2), reference(74, 2), reference(76, 2));
  }

  @Test
  void testCharsTypeInference() {
    execute("typeInference/Chars.pas");
    verifyUsages(15, 10, reference(47, 2), reference(49, 2), reference(50, 2));
    verifyUsages(20, 10, reference(48, 2));
    verifyUsages(30, 10, reference(52, 2), reference(53, 2));
  }

  @Test
  void testLowHighIntrinsics() {
    execute("intrinsics/LowHighIntrinsics.pas");
    verifyUsages(
        12,
        10,
        reference(29, 2),
        reference(30, 2),
        reference(31, 2),
        reference(32, 2),
        reference(33, 2),
        reference(34, 2),
        reference(35, 2),
        reference(36, 2));
    verifyUsages(17, 10, reference(37, 2), reference(38, 2));
    verifyUsages(22, 10, reference(39, 2), reference(40, 2));
  }

  @Test
  void testDefaultIntrinsic() {
    execute("intrinsics/DefaultIntrinsic.pas");
    verifyUsages(7, 10, reference(29, 2));
    verifyUsages(12, 10, reference(30, 2));
    verifyUsages(17, 10, reference(31, 2));
    verifyUsages(22, 10, reference(32, 2));
  }

  @Test
  void testBinaryOperatorIntrinsics() {
    execute("operators/BinaryOperatorIntrinsics.pas");
    verifyUsages(
        10,
        10,
        reference(46, 2),
        reference(47, 2),
        reference(48, 2),
        reference(51, 2),
        reference(52, 2),
        reference(53, 2),
        reference(54, 2),
        reference(55, 2),
        reference(56, 2),
        reference(57, 2),
        reference(115, 2));
    verifyUsages(
        15,
        10,
        reference(60, 2),
        reference(61, 2),
        reference(62, 2),
        reference(63, 2),
        reference(64, 2),
        reference(68, 2),
        reference(69, 2),
        reference(77, 2),
        reference(78, 2),
        reference(79, 2),
        reference(80, 2),
        reference(81, 2),
        reference(133, 2));
    verifyUsages(
        20,
        10,
        reference(65, 2),
        reference(66, 2),
        reference(67, 2),
        reference(70, 2),
        reference(71, 2),
        reference(72, 2),
        reference(73, 2),
        reference(74, 2),
        reference(82, 2),
        reference(83, 2),
        reference(84, 2),
        reference(85, 2),
        reference(86, 2),
        reference(87, 2),
        reference(88, 2),
        reference(89, 2),
        reference(90, 2),
        reference(91, 2));
    verifyUsages(
        25,
        10,
        reference(92, 2),
        reference(93, 2),
        reference(94, 2),
        reference(95, 2),
        reference(96, 2),
        reference(97, 2),
        reference(98, 2),
        reference(99, 2),
        reference(100, 2),
        reference(101, 2),
        reference(102, 2),
        reference(103, 2),
        reference(104, 2),
        reference(105, 2),
        reference(106, 2),
        reference(107, 2),
        reference(108, 2),
        reference(134, 2),
        reference(135, 2),
        reference(136, 2));
    verifyUsages(30, 10, reference(111, 2), reference(112, 2));
    verifyUsages(35, 10, reference(116, 2), reference(117, 2), reference(118, 2));
  }

  @Test
  void testBinaryOperatorOverloads() {
    execute("operators/BinaryOperatorOverloads.pas");
    verifyUsages(64, 10, reference(77, 2), reference(80, 2), reference(83, 2), reference(86, 2));
    verifyUsages(69, 10, reference(89, 2), reference(92, 2));
  }

  @Test
  void testUnaryOperatorIntrinsics() {
    execute("operators/UnaryOperatorIntrinsics.pas");
    verifyUsages(10, 10, reference(40, 2));
    verifyUsages(15, 10, reference(43, 2), reference(47, 2), reference(48, 2));
    verifyUsages(20, 10, reference(44, 2), reference(49, 2), reference(50, 2));
    verifyUsages(25, 10, reference(51, 2), reference(52, 2));
  }

  @Test
  void testUnaryOperatorOverloads() {
    execute("operators/UnaryOperatorOverloads.pas");
    verifyUsages(32, 10, reference(49, 2));
    verifyUsages(37, 10, reference(50, 2));
    verifyUsages(42, 10, reference(51, 2));
  }

  @Test
  void testImplicitOperator() {
    execute("operators/ImplicitOperator.pas");
    verifyUsages(23, 10, reference(35, 2));
  }

  @Test
  void testRoundTruncOperators() {
    execute("operators/RoundTruncOperators.pas");
    verifyUsages(23, 10, reference(40, 2), reference(41, 2));
    verifyUsages(28, 10, reference(42, 2));
    verifyUsages(33, 10, reference(43, 2));
  }

  @Test
  void testImplicitConversionTo() {
    execute("operators/ImplicitConversionTo.pas");
    verifyUsages(27, 10, reference(34, 2));
  }

  @Test
  void testImplicitConversionFrom() {
    execute("operators/ImplicitConversionFrom.pas");
    verifyUsages(33, 10, reference(40, 2));
  }

  @Test
  void testOperatorsAreNotCallable() {
    execute("operators/NotCallable.pas");
    verifyUsages(7, 20, reference(15, 7));
  }

  @Test
  void testPointerMathOperators() {
    execute("operators/PointerMath.pas");
    verifyUsages(
        15,
        10,
        reference(49, 2),
        reference(60, 2),
        reference(61, 2),
        reference(72, 2),
        reference(73, 2),
        reference(50, 2),
        reference(51, 2),
        reference(62, 2),
        reference(63, 2),
        reference(74, 2),
        reference(75, 2));
    verifyUsages(
        20,
        10,
        reference(42, 2),
        reference(43, 2),
        reference(44, 2),
        reference(45, 2),
        reference(46, 2),
        reference(47, 2),
        reference(48, 2));
    verifyUsages(
        25,
        10,
        reference(53, 2),
        reference(54, 2),
        reference(55, 2),
        reference(56, 2),
        reference(57, 2),
        reference(58, 2),
        reference(59, 2));
    verifyUsages(
        30,
        10,
        reference(65, 2),
        reference(66, 2),
        reference(67, 2),
        reference(68, 2),
        reference(69, 2),
        reference(70, 2),
        reference(71, 2));
  }

  @Test
  void testVariantOperators() {
    execute("operators/VariantOperators.pas");
    verifyUsages(
        8,
        10,
        reference(41, 2),
        reference(42, 2),
        reference(43, 2),
        reference(44, 2),
        reference(45, 2),
        reference(46, 2),
        reference(47, 2),
        reference(48, 2),
        reference(49, 2),
        reference(50, 2),
        reference(51, 2),
        reference(52, 2),
        reference(53, 2),
        reference(54, 2),
        reference(55, 2),
        reference(56, 2),
        reference(57, 2),
        reference(58, 2),
        reference(59, 2),
        reference(60, 2),
        reference(61, 2),
        reference(62, 2),
        reference(63, 2),
        reference(64, 2),
        reference(65, 2),
        reference(66, 2),
        reference(67, 2),
        reference(68, 2));
    verifyUsages(23, 10, reference(70, 2), reference(71, 2));
    verifyUsages(
        28,
        10,
        reference(73, 2),
        reference(74, 2),
        reference(75, 2),
        reference(76, 2),
        reference(77, 2),
        reference(78, 2),
        reference(79, 2),
        reference(80, 2),
        reference(81, 2),
        reference(82, 2),
        reference(83, 2),
        reference(84, 2));
  }

  @Test
  void testSimpleProperties() {
    execute("properties/Simple.pas");
    verifyUsages(
        6,
        2,
        reference(14, 10),
        reference(16, 26),
        reference(17, 21),
        reference(18, 24),
        reference(19, 23),
        reference(20, 26),
        reference(25, 27),
        reference(30, 22),
        reference(37, 7),
        reference(39, 19),
        reference(42, 18),
        reference(45, 21));
    verifyUsages(8, 16, reference(39, 24), reference(42, 23), reference(45, 26));
    verifyUsages(9, 14, reference(40, 16), reference(43, 15), reference(46, 18));
    verifyUsages(12, 2, reference(25, 10), reference(30, 9), reference(35, 20));
    verifyUsages(
        14,
        4,
        reference(19, 33),
        reference(19, 44),
        reference(20, 36),
        reference(20, 47),
        reference(27, 2),
        reference(32, 12));
    verifyUsages(16, 14, reference(18, 47), reference(25, 15));
    verifyUsages(17, 13, reference(18, 34), reference(30, 14));
    verifyUsages(18, 13, reference(39, 6), reference(40, 6));
    verifyUsages(19, 13, reference(42, 6), reference(43, 6));
    verifyUsages(20, 13, reference(45, 6), reference(46, 6));
  }

  @Test
  void testOverrideProperties() {
    execute("properties/OverrideProperties.pas");
    verifyUsages(8, 14, reference(29, 10), reference(30, 10), reference(31, 13), reference(32, 13));
    verifyUsages(16, 13, reference(29, 6), reference(31, 6));
    verifyUsages(21, 13, reference(30, 6), reference(32, 6));
  }

  @Test
  void testProceduralProperties() {
    execute("properties/ProceduralProperties.pas");
    verifyUsages(12, 13, reference(19, 6));
    verifyUsages(17, 26, reference(19, 14));
  }

  @Test
  void testHiddenDefaultProperties() {
    execute("properties/HiddenDefaultProperties.pas");
    verifyUsages(11, 14, reference(27, 25));
  }

  @Test
  void testSimpleOverloads() {
    execute("overloads/Simple.pas");
    verifyUsages(8, 10, reference(14, 10), reference(35, 2));
    verifyUsages(9, 10, reference(19, 10), reference(36, 2));
    verifyUsages(10, 10, reference(24, 10), reference(37, 2), reference(38, 2));
  }

  @Test
  void testStrongAliasOverloads() {
    execute("overloads/StrongAlias.pas");
    verifyUsages(6, 2, reference(15, 19), reference(23, 10));
    verifyUsages(10, 10, reference(25, 2));
    verifyUsages(15, 10, reference(26, 2));
  }

  @Test
  void testNestedExpressions() {
    execute("overloads/NestedExpressions.pas");
    verifyUsages(7, 2, reference(21, 9), reference(38, 12), reference(41, 14));
    verifyUsages(8, 13, reference(21, 25), reference(42, 15));
    verifyUsages(11, 10, reference(26, 10), reference(42, 2));
    verifyUsages(12, 10, reference(31, 10), reference(43, 2), reference(44, 2));
    verifyUsages(16, 9, reference(43, 6), reference(44, 9));
  }

  @Test
  void testAmbiguousMethodReferences() {
    execute("overloads/AmbiguousMethodReferences.pas");
    verifyUsages(6, 2, reference(8, 19), reference(18, 19), reference(30, 11));
    verifyUsages(8, 10, reference(18, 10), reference(33, 2), reference(34, 2));
    verifyUsages(9, 10, reference(23, 10), reference(35, 2), reference(36, 2));
    verifyUsages(13, 9, reference(34, 6));
  }

  @Test
  void testProceduralVariables() {
    execute("overloads/ProceduralVariables.pas");
    verifyUsages(5, 10, reference(10, 10), reference(25, 20));
    verifyUsages(6, 10, reference(15, 10), reference(26, 19));
    verifyUsages(22, 2, reference(25, 2), reference(28, 2));
    verifyUsages(23, 2, reference(26, 2), reference(29, 2));
  }

  @Test
  void testCharInSet() {
    execute("overloads/CharInSet.pas");
    verifyUsages(13, 13, reference(25, 22));
    verifyUsages(18, 10, reference(25, 12));
    verifyUsages(23, 19, reference(25, 36));
  }

  @Test
  void testImportedOverloads() {
    execute("overloads/Imports.pas", "overloads/imports/");
    verifyUsages(13, 10, reference(30, 2), reference(31, 2));
    verifyUsages(24, 2, reference(28, 6));
    verifyUsages(25, 2, reference(29, 6));
    verifyUsages(26, 2, reference(30, 6));
  }

  @Test
  void testDisambiguationOfOverloadsByDistanceFromCallSite() {
    execute("overloads/Distance.pas", "overloads/imports");
    verifyUsages(8, 10, reference(53, 2));
    verifyUsages(12, 16, reference(56, 14));
    verifyUsages(13, 14, reference(58, 6), reference(64, 2));
    verifyUsages(17, 16, reference(55, 14));
    verifyUsages(18, 14, reference(57, 6), reference(63, 2));
    verifyUsages(49, 15, reference(54, 6), reference(56, 21), reference(58, 10));
  }

  @Test
  void testOverriddenOverloads() {
    execute("overloads/Overrides.pas", "overloads/imports");
    verifyUsages(31, 2, reference(35, 10));
    verifyUsages(32, 2, reference(36, 10));
    verifyUsages(33, 2, reference(37, 10));
  }

  @Test
  void testAmpersands() {
    execute("Ampersands.pas");
    verifyUsages(7, 11, reference(17, 2), reference(18, 2));
    verifyUsages(11, 11, reference(19, 2));
  }

  @Test
  void testRegularMethodPreferredOverImplicitSpecializations() {
    execute("generics/RegularMethodPreferredOverImplicitSpecialization.pas");
    verifyUsages(10, 20, reference(10, 26));
    verifyUsages(10, 15, reference(20, 2));
    verifyUsages(11, 15, reference(19, 2));
  }

  @Test
  void testGenericArrayAssignmentCompatibility() {
    execute("generics/ArrayAssignmentCompatibility.pas");
    verifyUsages(12, 15, reference(25, 2));
    verifyUsages(13, 15, reference(26, 2));
    verifyUsages(14, 15, reference(27, 2));
    verifyUsages(15, 15, reference(28, 2));
  }

  @Test
  void testStructAssignmentCompatibility() {
    execute("generics/StructAssignmentCompatibility.pas");
    verifyUsages(14, 15, reference(27, 2));
    verifyUsages(15, 15, reference(28, 2));
    verifyUsages(16, 15, reference(29, 2));
    verifyUsages(17, 15, reference(30, 2));
  }

  @Test
  void testGenericMethodInterfaceNameResolution() {
    execute("generics/MethodInterfaceNameResolution.pas");
    verifyUsages(6, 2, reference(16, 10));
    verifyUsages(7, 15, reference(16, 18));
    verifyUsages(10, 2, reference(21, 10));
    verifyUsages(11, 15, reference(21, 15));
  }

  @Test
  void testGenericSameNameTypes() {
    execute("generics/SameNameType.pas");
    verifyUsages(6, 2, reference(18, 15));
    verifyUsages(7, 2, reference(19, 16));
    verifyUsages(8, 2, reference(20, 18));
  }

  @Test
  void testGenericParameterizedMethods() {
    execute("generics/ParameterizedMethod.pas");
    verifyUsages(
        11,
        14,
        reference(16, 15),
        reference(29, 2),
        reference(30, 2),
        reference(31, 2),
        reference(32, 2));
  }

  @Test
  void testGenericImplicitSpecializations() {
    execute("generics/ImplicitSpecialization.pas");
    verifyUsages(14, 14, reference(19, 20), reference(26, 2), reference(27, 2), reference(28, 2));
  }

  @Test
  void testGenericConstraints() {
    execute("generics/Constraint.pas");
    verifyUsages(11, 14, reference(20, 25), reference(53, 8));
  }

  @Test
  void testGenericTypeParameterNameDeclarations() {
    execute("generics/TypeParameterNameDeclaration.pas");
    verifyUsages(
        6,
        7,
        reference(8, 22),
        reference(9, 19),
        reference(10, 18),
        reference(13, 49),
        reference(13, 53),
        reference(15, 45),
        reference(17, 31),
        reference(22, 14),
        reference(22, 35),
        reference(22, 39));
    verifyUsages(12, 13, reference(12, 27));
    verifyUsages(13, 22, reference(13, 41));
    verifyUsages(14, 11, reference(15, 31), reference(15, 54), reference(15, 58));
  }

  @Test
  void testGenericTypeParameterConflicts() {
    execute("generics/TypeParameterNameConflict.pas");
    verifyUsages(7, 14, reference(25, 8));
    verifyUsages(11, 14, reference(26, 6), reference(27, 9));
    verifyUsages(14, 11, reference(15, 11), reference(21, 19));
    verifyUsages(
        16, 19, reference(16, 33), reference(21, 27), reference(21, 35), reference(23, 10));
  }

  @Test
  void testPropertySpecialization() {
    execute("generics/PropertySpecialization.pas");
    verifyUsages(13, 10, reference(20, 2));
  }

  @Test
  void testIncludes() {
    execute("includes/Includes.pas");
    Collection<TextRange> references = context.referencesForSymbolAt(componentKey, 5, 0);
    assertThat(references).isNull();
  }

  @Test
  void testSimpleAttribute() {
    execute("attributes/SimpleAttribute.pas");
    verifyUsages(6, 2, reference(9, 3));
  }

  @Test
  void testGroupedAttributes() {
    execute("attributes/GroupedAttributes.pas");
    verifyUsages(6, 2, reference(12, 3));
    verifyUsages(9, 2, reference(12, 8));
  }

  @Test
  void testSuffixedAttribute() {
    execute("attributes/SuffixedAttribute.pas");
    verifyUsages(6, 2, reference(9, 3));
  }

  @Test
  void testLayeredAttribute() {
    execute("attributes/LayeredAttribute.pas");
    verifyUsages(8, 4, reference(12, 14));
  }

  @Test
  void testLayeredSuffixedAttribute() {
    execute("attributes/LayeredSuffixedAttribute.pas");
    verifyUsages(8, 4, reference(12, 14));
  }

  @Test
  void testMultipleSimpleAttributes() {
    execute("attributes/MultipleSimpleAttributes.pas");
    verifyUsages(11, 4, reference(14, 5));
  }

  @Test
  void testHigherScopeSuffixedAttribute() {
    execute("attributes/HigherScopeSuffixedAttribute.pas");
    verifyUsages(6, 2, reference(14, 5));
  }

  @Test
  void testHigherScopeUnsuffixedAttribute() {
    execute("attributes/HigherScopeUnsuffixedAttribute.pas");
    verifyUsages(11, 4, reference(14, 5));
  }

  @Test
  void testAttributeWithConstructor() {
    execute("attributes/AttributeWithConstructor.pas");
    verifyUsages(7, 16); // The constructor reference is implicit
  }

  @Test
  void testAttributeWithMultipleConstructors() {
    execute("attributes/AttributeWithMultipleConstructors.pas");
    verifyUsages(7, 16); // The constructor reference is implicit
    verifyUsages(8, 16); // The constructor reference is implicit
    verifyUsages(9, 16); // The constructor reference is implicit
    verifyUsages(10, 16); // The constructor reference is implicit
  }

  @Test
  void testSimpleMethodResolutionClause() {
    execute("methodResolutionClauses/Simple.pas");
    verifyUsages(7, 14, reference(12, 26));
    verifyUsages(11, 14, reference(12, 42));
  }

  @Test
  void testMethodResolutionClauseWithOverloadedImplementation() {
    execute("methodResolutionClauses/OverloadedImplementation.pas");
    verifyUsages(7, 14, reference(13, 26));
    verifyUsages(11, 14, reference(13, 42));
    verifyUsages(12, 14);
  }

  @Test
  void testMethodResolutionClauseWithOverloadedInterfaceAndImplementation() {
    execute("methodResolutionClauses/OverloadedInterfaceAndImplementation.pas");
    verifyUsages(7, 14, reference(14, 26));
    verifyUsages(12, 14, reference(14, 42));
    verifyUsages(13, 14);
  }

  @Test
  void testVarShadowingImport() {
    execute("shadowedImports/VarShadowingImport.pas", "shadowedImports");
    verifyUsages(10, 15, reference(19, 15));
  }

  @Test
  void testTypeShadowingImport() {
    execute("shadowedImports/TypeShadowingImport.pas", "shadowedImports");
    verifyUsages(10, 21, reference(16, 15));
  }

  @Test
  void testConstShadowingImport() {
    execute("shadowedImports/ConstShadowingImport.pas", "shadowedImports");
    verifyUsages(10, 15, reference(19, 15));
  }

  @Test
  void testRoutineShadowingImport() {
    execute("shadowedImports/RoutineShadowingImport.pas", "shadowedImports");
    verifyUsages(10, 15, reference(23, 15));
  }

  @Test
  void testShadowedImplementationImport() {
    execute("shadowedImports/ShadowedImplementationImport.pas", "shadowedImports");
    verifyUsages(12, 21, reference(16, 15));
  }

  @Test
  void testUnshadowedImplementationImport() {
    execute("shadowedImports/UnshadowedImplementationImport.pas", "shadowedImports");
    verifyUsages(13, 6, reference(16, 2));
  }

  @Test
  void testInterfaceImportShadowedLater() {
    execute("shadowedImports/InterfaceImportShadowedLater.pas", "shadowedImports");
    verifyUsages(5, 6, reference(7, 28));
  }

  @Test
  void testInterfaceImportShadowedInImplementation() {
    execute("shadowedImports/InterfaceImportShadowedInImplementation.pas", "shadowedImports");
    verifyUsages(5, 6, reference(7, 28));
  }

  @Test
  void testImports() {
    execute("imports/source/Unit1.pas", "imports/", "imports/source/", "imports/ignored/");
    verifyUsages(1, 5, reference(23, 2), reference(26, 18));
    verifyUsages(6, 2, reference(26, 2));
    verifyUsages(9, 2, reference(26, 24), reference(27, 12));
    verifyUsages(14, 2, reference(23, 18));
    verifyUsages(16, 10, reference(23, 8), reference(24, 2));
  }

  @Test
  void testNamespaces() {
    execute("namespaces/Namespaced.Unit1.pas", "namespaces/");

    verifyUsages(1, 5, reference(23, 2), reference(26, 18));
    verifyUsages(6, 2, reference(30, 2));
    verifyUsages(6, 9, reference(26, 2));
    verifyUsages(9, 2, reference(26, 35), reference(27, 12), reference(29, 23), reference(30, 29));
    verifyUsages(14, 2, reference(23, 29));
    verifyUsages(16, 10, reference(23, 19), reference(24, 2));
  }

  @Test
  void testUnitScopeNames() {
    unitScopeNames = Set.of("NonexistentUnitScope", "UnitScopeName", "ABCUnitScopeXYZ");

    execute("namespaces/UnitScopeNameTest.pas", "namespaces/");

    verifyUsages(1, 5, reference(23, 2), reference(26, 30));
    verifyUsages(6, 2, reference(26, 2));
    verifyUsages(9, 2, reference(26, 48), reference(27, 18));
    verifyUsages(14, 2, reference(23, 30));
    verifyUsages(16, 10, reference(23, 20), reference(24, 2));
  }

  @Test
  void testUnitAliases() {
    unitAliases.put("UnitX", "Unit2");
    unitAliases.put("UnitY", "Unit3");

    execute("unitAliases/Unit1.pas", "unitAliases/");

    verifyUsages(1, 5, reference(23, 2), reference(26, 18));
    verifyUsages(6, 2, reference(26, 2));
    verifyUsages(9, 2, reference(26, 24), reference(27, 12));
    verifyUsages(14, 2, reference(23, 18));
    verifyUsages(16, 10, reference(23, 8), reference(24, 2));
  }

  @Test
  void testUnscopedEnums() {
    execute("enums/UnscopedEnum.pas");
    verifyUsages(6, 2, reference(10, 19));
    verifyUsages(6, 9, reference(12, 11));
  }

  @Test
  void testNestedUnscopedEnums() {
    execute("enums/NestedUnscopedEnum.pas");
    verifyUsages(8, 12, reference(15, 9));
    verifyUsages(20, 16, reference(24, 9));
    verifyUsages(29, 8, reference(31, 9));
  }

  @Test
  void testScopedEnums() {
    execute("enums/ScopedEnum.pas");
    verifyUsages(8, 2);
    verifyUsages(8, 9);
  }

  @Test
  void testSimpleHelpers() {
    execute("helpers/Simple.pas");
    verifyUsages(10, 14, reference(15, 21), reference(22, 6));
  }

  @Test
  void testClassHelperOverrides() {
    execute("helpers/ClassHelperOverride.pas");
    verifyUsages(7, 14, reference(16, 15));
    verifyUsages(11, 14, reference(21, 21), reference(28, 6));
  }

  @Test
  void testClassHelperOverloads() {
    execute("helpers/ClassHelperOverload.pas");
    verifyUsages(7, 14, reference(20, 15), reference(37, 6));
    verifyUsages(11, 14, reference(25, 25));
    verifyUsages(15, 14, reference(30, 21), reference(38, 6));
  }

  @Test
  void testClassHelperSelfValues() {
    execute("helpers/ClassHelperSelfValue.pas");
    verifyUsages(15, 10, reference(22, 2));
  }

  @Test
  void testClassHelperInheritedStatements() {
    execute("helpers/ClassHelperInheritedStatement.pas");
    verifyUsages(7, 14, reference(29, 19), reference(66, 2));
    verifyUsages(8, 14, reference(34, 19), reference(74, 2));
    verifyUsages(13, 14, reference(44, 15), reference(67, 12), reference(75, 12));
    verifyUsages(14, 14, reference(49, 15), reference(68, 12), reference(76, 12));
    verifyUsages(9, 14, reference(39, 19), reference(69, 12), reference(77, 12));
  }

  @Test
  void testClassHelperAccessingExtendedTypes() {
    execute("helpers/ClassHelperAccessingExtendedType.pas");
    verifyUsages(8, 14, reference(26, 19));
    verifyUsages(10, 14, reference(31, 19), reference(52, 2));
    verifyUsages(15, 14, reference(37, 15));
    verifyUsages(17, 14, reference(42, 15), reference(53, 2));
  }

  @Test
  void testRecordHelperLiterals() {
    execute("helpers/RecordHelperLiteral.pas");
    verifyUsages(7, 14, reference(24, 21), reference(46, 10));
    verifyUsages(11, 14, reference(29, 24), reference(47, 9));
    verifyUsages(15, 14, reference(34, 22), reference(48, 6));
    verifyUsages(19, 14, reference(39, 26), reference(49, 29));
  }

  @Test
  void testRecordHelperTypeReference() {
    execute("helpers/RecordHelperTypeReference.pas");
    verifyUsages(7, 14, reference(19, 9), reference(20, 16));
  }

  @Test
  void testRecordHelperConstants() {
    execute("helpers/RecordHelperConstant.pas");
    verifyUsages(9, 6, reference(16, 11), reference(17, 16));
    verifyUsages(14, 10, reference(16, 2), reference(17, 2));
  }

  @Test
  void testRecordHelperSelfValues() {
    execute("helpers/RecordHelperSelfValue.pas");
    verifyUsages(12, 10, reference(19, 2));
  }

  @Test
  void testHelperImports() {
    execute("helpers/imports/Unit1.pas", "helpers/imports/");
    verifyUsages(17, 10, reference(24, 2));
  }

  @Test
  void testWeakAliasHelpers() {
    execute("helpers/WeakAliasHelper.pas");
    verifyUsages(13, 14, reference(24, 21), reference(31, 6));
  }

  @Test
  void testDependencyReferencedImplicitly() {
    execute("dependencies/Implicit.pas");
    verifyDependencies("System.SysUtils");
  }

  @Test
  void testDependencyReferencedExplicitly() {
    execute("dependencies/Explicit.pas");
    verifyDependencies("System.SysUtils");
  }

  @Test
  void testDependencyForHelperReference() {
    execute("dependencies/Helper.pas");
    verifyDependencies("System.SysUtils");
  }

  @Test
  void testDependencyForComponentAncestor() {
    execute("dependencies/ComponentAncestor.pas");
    verifyDependencies("Vcl.Controls", "System.Classes");
  }

  @Test
  void testDependencyForComponentAncestorDeclaredInImplementation() {
    execute("dependencies/ComponentAncestorDeclaredInImplementation.pas");
    verifyDependencies("Vcl.Controls");
  }

  @Test
  void testDependencyForComponentAncestorWithPublishedFieldInNonNonComponentType() {
    execute("dependencies/ComponentAncestorWithPublishedFieldInNonComponentType.pas");
    verifyDependencies("Vcl.Controls");
  }

  @Test
  void testDependencyForComponentAncestorDependencyWithNonPublishedField() {
    execute("dependencies/ComponentAncestorWithNonPublishedField.pas");
    verifyDependencies("Vcl.Controls");
  }

  @Test
  void testDependencyRequiredForInlineMethodExpansion() {
    execute("dependencies/InlineMethodExpansion.pas");
    verifyDependencies("System.UITypes", "Vcl.Dialogs");
  }

  @Test
  void testDependencyRequiredForInlineMethodExpansionViaDefaultArrayProperties() {
    execute(
        "dependencies/InlineMethodExpansionViaDefaultArrayProperty.pas", "dependencies/imports/");
    verifyDependencies("UnitWithDefaultArrayPropertyBackedByInlineMethod", "System.SysUtils");
  }

  @Test
  void testDependencyShouldNotBeIntroducedForImplementationMethods() {
    execute("dependencies/ImplementationVisibility.pas", "dependencies/imports/");
    verifyDependencies("System.SysUtils");
  }

  @Test
  void testDependencyRequiredForImplicitInvocationOfGetEnumerator() {
    execute("dependencies/Enumerator.pas", "dependencies/imports/");
    verifyDependencies("UnitWithGetEnumeratorForTObject");
  }

  private void execute(String filename, String... searchPaths) {
    File baseDir = DelphiUtils.getResource(ROOT_PATH);
    File file = DelphiUtils.getResource(ROOT_PATH + filename);

    InputFile inputFile;
    try {
      inputFile =
          TestInputFileBuilder.create("moduleKey", baseDir, file)
              .setContents(FileUtils.readFileToString(file, UTF_8.name()))
              .setLanguage(Delphi.KEY)
              .setType(InputFile.Type.MAIN)
              .build();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    var preprocessorFactory = new DelphiPreprocessorFactory(Platform.WINDOWS);
    var typeFactory =
        new TypeFactoryImpl(
            DelphiProperties.COMPILER_TOOLCHAIN_DEFAULT, DelphiProperties.COMPILER_VERSION_DEFAULT);

    DelphiFileConfig fileConfig = mock(DelphiFileConfig.class);
    when(fileConfig.getEncoding()).thenReturn(StandardCharsets.UTF_8.name());
    when(fileConfig.getPreprocessorFactory()).thenReturn(preprocessorFactory);
    when(fileConfig.getTypeFactory()).thenReturn(typeFactory);
    when(fileConfig.getSearchPath()).thenReturn(SearchPath.create(Collections.emptyList()));
    when(fileConfig.getDefinitions()).thenReturn(Collections.emptySet());

    mainFile = DelphiInputFile.from(inputFile, fileConfig);
    List<Path> sourceFiles = List.of(Path.of(mainFile.getInputFile().uri()));
    SearchPath searchPath =
        SearchPath.create(
            Arrays.stream(searchPaths)
                .map(relativePath -> ROOT_PATH + relativePath)
                .map(DelphiUtils::getResource)
                .map(File::toPath)
                .collect(Collectors.toList()));

    symbolTable =
        SymbolTable.builder()
            .preprocessorFactory(preprocessorFactory)
            .typeFactory(typeFactory)
            .sourceFiles(sourceFiles)
            .searchPath(searchPath)
            .standardLibraryPath(standardLibraryPath)
            .unitScopeNames(unitScopeNames)
            .unitAliases(unitAliases)
            .build();

    ExecutorContext executorContext = new ExecutorContext(context, symbolTable);

    componentKey = mainFile.getInputFile().key();
    executor.execute(executorContext, mainFile);
  }

  private void verifyUsages(int line, int offset, TextPointer... pointers) {
    Collection<TextRange> textRanges = context.referencesForSymbolAt(componentKey, line, offset);

    assertThat(textRanges).as("Expected symbol to be created").isNotNull();

    if (pointers.length == 0) {
      assertThat(textRanges).as("Expected no symbol references to exist").isEmpty();
    } else {
      var usages = textRanges.stream().map(TextRange::start).collect(Collectors.toList());
      assertThat(usages).as("Expected symbol references to exist").isNotEmpty().contains(pointers);
    }
  }

  private static TextPointer reference(int line, int column) {
    return new DefaultTextPointer(line, column);
  }

  private void verifyDependencies(String... dependency) {
    String path = mainFile.getSourceCodeFile().getAbsolutePath();
    UnitNameDeclaration unit = symbolTable.getUnitByPath(path);

    Set<String> dependencies =
        Sets.union(unit.getInterfaceDependencies(), unit.getImplementationDependencies()).stream()
            .map(UnitNameDeclaration::getName)
            .filter(not("System"::equals))
            .filter(not(unit.getName()::equals))
            .collect(Collectors.toUnmodifiableSet());

    assertThat(dependencies).containsExactlyInAnyOrder(dependency);
  }

  private static Path createStandardLibrary(Path baseDir) {
    try {
      Path standardLibraryPath = baseDir.resolve("bds/source");

      Files.createDirectories(standardLibraryPath);

      Files.writeString(
          standardLibraryPath.resolve("SysInit.pas"),
          "unit SysInit;\n" //
              + "interface\n"
              + "implementation\n"
              + "end.");

      Files.writeString(
          standardLibraryPath.resolve("System.pas"),
          "unit System;\n"
              + "interface\n"
              + "type\n"
              + "  TObject = class\n"
              + "    constructor Create;"
              + "  end;\n"
              + "  IInterface = interface\n"
              + "  end;\n"
              + "  TClassHelperBase = class\n"
              + "  end;\n"
              + "  TVarRec = record\n"
              + "  end;\n"
              + "implementation\n"
              + "end.");

      Files.writeString(
          standardLibraryPath.resolve("System.SysUtils.pas"),
          "unit System.SysUtils;\n"
              + "interface\n"
              + "procedure FreeAndNil(var Obj); inline;\n"
              + "\n"
              + "type\n"
              + "  TStringHelper = record helper for String\n"
              + "    function IsEmpty: Boolean;"
              + "  end;"
              + "implementation\n"
              + "end.");

      Files.writeString(
          standardLibraryPath.resolve("System.Classes.pas"),
          "unit System.Classes;\n"
              + "\n"
              + "interface\n"
              + "\n"
              + "type\n"
              + "  TPersistent = class(TObject)\n"
              + "\n"
              + "  end;\n"
              + "\n"
              + "  TComponentClass = class of TComponent;\n"
              + "\n"
              + "  TComponent = class(TPersistent, IInterface)\n"
              + " \n"
              + "  end;\n"
              + "\n"
              + "implementation\n"
              + "\n"
              + "end.");

      Files.writeString(
          standardLibraryPath.resolve("System.UITypes.pas"),
          "unit System.UITypes;\n"
              + "\n"
              + "interface\n"
              + "\n"
              + "{$SCOPEDENUMS ON}\n"
              + "type\n"
              + "  TMsgDlgType = (mtWarning, mtError, mtInformation, mtConfirmation, mtCustom);\n"
              + "  TMsgDlgBtn = (mbYes, mbNo, mbOK, mbCancel, mbAbort, mbRetry, mbIgnore,\n"
              + "    mbAll, mbNoToAll, mbYesToAll, mbHelp, mbClose);\n"
              + "  TMsgDlgButtons = set of TMsgDlgBtn;\n"
              + "\n"
              + "implementation\n"
              + "\n"
              + "end.");

      Files.writeString(
          standardLibraryPath.resolve("Vcl.Controls.pas"),
          "unit Vcl.Controls;\n"
              + "\n"
              + "interface\n"
              + "\n"
              + "uses\n"
              + "  System.Classes;\n"
              + "\n"
              + "type\n"
              + "  TControl = class(TComponent)\n"
              + "  \n"
              + "  end;\n"
              + "  \n"
              + "  TWinControl = class(TControl)\n"
              + "  \n"
              + "  end;\n"
              + "\n"
              + "  TCustomControl = class(TWinControl)\n"
              + "\n"
              + "  end;\n"
              + "\n"
              + "implementation\n"
              + "\n"
              + "end.");

      Files.writeString(
          standardLibraryPath.resolve("Vcl.Dialogs.pas"),
          "unit Vcl.Dialogs;\n"
              + "\n"
              + "interface\n"
              + "\n"
              + "uses\n"
              + "  System.UITypes;\n"
              + "\n"
              + "const\n"
              + "  mtError\t= System.UITypes.TMsgDlgType.mtError;\n"
              + "  mbOK\t= System.UITypes.TMsgDlgBtn.mbOK;\n"
              + "\n"
              + "function MessageDlg(const Msg: string; DlgType: TMsgDlgType;\n"
              + "  Buttons: TMsgDlgButtons; HelpCtx: Longint): Integer; overload; inline;\n"
              + "\n"
              + "implementation\n"
              + "\n"
              + "function MessageDlg(const Msg: string; DlgType: TMsgDlgType;\n"
              + "  Buttons: TMsgDlgButtons; HelpCtx: Longint): Integer;\n"
              + "begin\n"
              + "  Result := MessageDlgPosHelp(Msg, DlgType, Buttons, HelpCtx, -1, -1, '');\n"
              + "end;\n"
              + "\n"
              + "end.");

      return standardLibraryPath;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
