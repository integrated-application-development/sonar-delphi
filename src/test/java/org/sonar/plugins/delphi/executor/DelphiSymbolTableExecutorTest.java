package org.sonar.plugins.delphi.executor;

import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.delphi.utils.DelphiUtils.uriToAbsolutePath;

import com.google.common.collect.Sets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.fs.internal.DefaultTextPointer;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.plugins.delphi.file.DelphiFile.DelphiInputFile;
import org.sonar.plugins.delphi.project.DelphiProjectHelper;
import org.sonar.plugins.delphi.symbol.SymbolTable;
import org.sonar.plugins.delphi.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonar.plugins.delphi.utils.builders.DelphiTestFileBuilder;

public class DelphiSymbolTableExecutorTest {
  private static final String ROOT_PATH = "/org/sonar/plugins/delphi/symbol/";
  private static final String STANDARD_LIBRARY = "/org/sonar/plugins/delphi/standardLibrary";

  private DelphiInputFile mainFile;
  private SymbolTable symbolTable;
  private DelphiSymbolTableExecutor executor;
  private SensorContextTester context;
  private Set<String> unitScopeNames;
  private Map<String, String> unitAliases;
  private String componentKey;

  @Before
  public void setup() {
    executor = new DelphiSymbolTableExecutor();
    context = SensorContextTester.create(DelphiUtils.getResource(ROOT_PATH));
    unitScopeNames = new HashSet<>();
    unitAliases = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
  }

  @Test
  public void testSimpleFile() {
    execute("Simple.pas");
    verifyUsages(9, 2, reference(22, 10), reference(31, 10), reference(36, 10));
    verifyUsages(14, 3);
    verifyUsages(22, 2);
    verifyUsages(11, 14, reference(31, 22), reference(38, 1));
    verifyUsages(12, 14, reference(33, 1), reference(36, 22));
  }

  @Test
  public void testSimilarParameterDeclarations() {
    execute("SimilarParameterDeclarations.pas");
    verifyUsages(8, 2, reference(16, 10), reference(21, 10));
    verifyUsages(10, 14, reference(16, 15));
    verifyUsages(10, 19);
    verifyUsages(11, 14, reference(21, 15), reference(18, 2));
    verifyUsages(11, 19);
  }

  @Test
  public void testRecords() {
    execute("Records.pas");
    verifyUsages(8, 2, reference(18, 21));
    verifyUsages(9, 4, reference(33, 30));
    verifyUsages(12, 2, reference(26, 11), reference(31, 11));
    verifyUsages(14, 4, reference(28, 14));
    verifyUsages(15, 6, reference(28, 31));
    verifyUsages(18, 4, reference(33, 14));
    verifyUsages(20, 13, reference(26, 16));
    verifyUsages(21, 13, reference(31, 16));
  }

  @Test
  public void testInheritedInvocations() {
    execute("InheritedInvocations.pas");
    verifyUsages(9, 14, reference(40, 14), reference(52, 14));
    verifyUsages(
        14,
        13,
        reference(27, 16),
        reference(39, 4),
        reference(40, 4),
        reference(41, 4),
        reference(42, 4),
        reference(42, 14),
        reference(43, 4),
        reference(54, 14),
        reference(57, 14),
        reference(64, 19),
        reference(65, 14));
    verifyUsages(
        15,
        13,
        reference(32, 16),
        reference(41, 14),
        reference(43, 14),
        reference(44, 14),
        reference(51, 4),
        reference(52, 4),
        reference(53, 4),
        reference(53, 14),
        reference(54, 4),
        reference(55, 4),
        reference(55, 14),
        reference(56, 14),
        reference(64, 14),
        reference(65, 30));
    verifyUsages(20, 13, reference(37, 16), reference(58, 4));
    verifyUsages(21, 13, reference(46, 4), reference(49, 16));
    verifyUsages(22, 13, reference(59, 4), reference(62, 16));
  }

  @Test
  public void testNestedMethods() {
    execute("NestedMethods.pas");
    verifyUsages(8, 9);
    verifyUsages(10, 11, reference(29, 12));
    verifyUsages(10, 15, reference(12, 7));
    verifyUsages(19, 11, reference(29, 16));
    verifyUsages(19, 15, reference(21, 7));
  }

  @Test
  public void testNestedTypes() {
    execute("NestedTypes.pas");
    verifyUsages(9, 8, reference(22, 10), reference(23, 5));
    verifyUsages(10, 26, reference(25, 4), reference(26, 4));
  }

  @Test
  public void testTypeAliasParameter() {
    execute("TypeAliasParameter.pas");
    verifyUsages(8, 2, reference(11, 14), reference(15, 25));
    verifyUsages(11, 2, reference(20, 31));
    verifyUsages(13, 2, reference(20, 10));
    verifyUsages(15, 14, reference(20, 15));
  }

  @Test
  public void testArrays() {
    execute("Arrays.pas");
    verifyUsages(
        10,
        14,
        reference(32, 15),
        reference(42, 17),
        reference(43, 30),
        reference(44, 30),
        reference(45, 23),
        reference(46, 25),
        reference(47, 9),
        reference(48, 46),
        reference(49, 14));
    verifyUsages(17, 13, reference(48, 27));
    verifyUsages(26, 13, reference(46, 6));
    verifyUsages(27, 13, reference(48, 6));
    verifyUsages(39, 2, reference(42, 2));
    verifyUsages(40, 2, reference(43, 2), reference(44, 2));
  }

  @Test
  public void testArrayArgument() {
    execute("ArrayArgument.pas");
    verifyUsages(9, 10, reference(16, 2));
    verifyUsages(14, 14, reference(16, 6));
  }

  @Test
  public void testArrayConstructor() {
    execute("ArrayConstructor.pas");
    verifyUsages(12, 10, reference(19, 2));
  }

  @Test
  public void testArrayOfConst() {
    execute("ArrayOfConst.pas");
    verifyUsages(9, 10, reference(16, 2), reference(17, 2), reference(18, 2));
    verifyUsages(14, 14, reference(17, 7), reference(18, 7));
  }

  @Test
  public void testAnonymousMethods() {
    execute("AnonymousMethods.pas");
    verifyUsages(8, 2, reference(13, 20), reference(28, 20));
    verifyUsages(9, 2, reference(18, 20), reference(28, 47));
    verifyUsages(13, 10, reference(32, 2), reference(33, 2));
    verifyUsages(18, 10, reference(38, 2), reference(39, 2));
    verifyUsages(23, 10, reference(41, 4));
    verifyUsages(30, 2, reference(35, 13));
    verifyUsages(33, 16, reference(35, 4));
  }

  @Test
  public void testUsesDeclarations() {
    execute("UsesDeclarations.pas");
    verifyUsages(1, 5, reference(25, 7));
    verifyUsages(17, 11, reference(24, 2), reference(25, 2));
    verifyUsages(8, 2, reference(26, 2));
    verifyUsages(15, 2, reference(27, 2));
  }

  @Test
  public void testResultTypes() {
    execute("ResultTypes.pas");
    verifyUsages(
        8,
        2,
        reference(17, 10),
        reference(22, 19),
        reference(27, 54),
        reference(29, 12),
        reference(36, 12),
        reference(42, 44),
        reference(44, 12));
    verifyUsages(10, 14, reference(17, 15), reference(30, 9), reference(37, 9), reference(45, 9));
    verifyUsages(22, 10, reference(31, 2), reference(38, 2), reference(46, 2));
  }

  @Test
  public void testPascalResultAssignments() {
    execute("PascalResultAssignments.pas");
    verifyUsages(7, 9, reference(13, 9), reference(15, 2));
    verifyUsages(8, 9, reference(18, 9), reference(23, 4), reference(27, 2));
    verifyUsages(9, 9, reference(30, 9));
    verifyUsages(32, 2, reference(34, 2));
  }

  @Test
  public void testSelfTypes() {
    execute("SelfTypes.pas");
    verifyUsages(19, 10, reference(36, 2));
    verifyUsages(24, 10, reference(41, 2));
    verifyUsages(29, 10, reference(48, 2));
  }

  @Test
  public void testInitializationFinalization() {
    execute("InitializationFinalization.pas");
    verifyUsages(8, 2, reference(16, 7), reference(19, 9));
    verifyUsages(10, 14, reference(20, 6));
    verifyUsages(16, 2, reference(22, 2));
  }

  @Test
  public void testRecordExpressionItems() {
    execute("RecordExpressionItems.pas");
    verifyUsages(16, 10, reference(23, 11));
  }

  @Test
  public void testHardTypeCast() {
    execute("HardTypeCast.pas");
    verifyUsages(10, 4, reference(19, 12));
    verifyUsages(17, 18, reference(19, 44));
  }

  @Test
  public void testHandlerProperty() {
    execute("HandlerProperty.pas");
    verifyUsages(12, 4, reference(23, 2));
    verifyUsages(21, 19, reference(23, 22));
  }

  @Test
  public void testWithStatement() {
    execute("WithStatement.pas");
    verifyUsages(9, 4, reference(44, 4));
    verifyUsages(14, 4, reference(26, 4));
    verifyUsages(10, 4, reference(27, 4));
    verifyUsages(23, 2, reference(28, 4));
  }

  @Test
  public void testForStatement() {
    execute("ForStatement.pas");
    verifyUsages(11, 2, reference(13, 6), reference(17, 6), reference(21, 6));
  }

  @Test
  public void testBareInterfaceMethodReference() {
    execute("BareInterfaceMethodReference.pas");
    verifyUsages(7, 9, reference(17, 9));
    verifyUsages(8, 9, reference(22, 9));
    verifyUsages(12, 10, reference(19, 2));
  }

  @Test
  public void testClassReferenceMethodResolution() {
    execute("classReferences/MethodResolution.pas");
    verifyUsages(11, 14, reference(20, 6));
  }

  @Test
  public void testClassReferenceArgumentResolution() {
    execute("classReferences/ArgumentResolution.pas");
    verifyUsages(20, 10, reference(27, 2), reference(28, 2));
  }

  @Test
  public void testClassReferenceConstructorTypeResolution() {
    execute("classReferences/ConstructorTypeResolution.pas");
    verifyUsages(17, 10, reference(24, 2));
    verifyUsages(10, 16, reference(24, 11));
  }

  @Test
  public void testSimpleForwardDeclarations() {
    execute("forwardDeclarations/Simple.pas");
    verifyUsages(24, 26, reference(26, 14));
  }

  @Test
  public void testInheritanceForwardDeclarations() {
    execute("forwardDeclarations/Inheritance.pas");
    verifyUsages(29, 10, reference(36, 2), reference(37, 2));
    verifyUsages(16, 15, reference(36, 11));
    verifyUsages(34, 26, reference(37, 7));
  }

  @Test
  public void testImplicitForwardDeclarations() {
    execute("forwardDeclarations/ImplicitForwarding.pas");
    verifyUsages(11, 2, reference(8, 17), reference(9, 22));
  }

  @Test
  public void testTypeSignaturesOfForwardDeclaration() {
    execute("forwardDeclarations/TypeSignature.pas");
    verifyUsages(21, 12, reference(26, 10), reference(39, 2));
    verifyUsages(22, 12, reference(31, 10), reference(42, 2));
  }

  @Test
  public void testSimpleTypeResolution() {
    execute("typeResolution/Simple.pas");
    verifyUsages(8, 2, reference(16, 10), reference(18, 21), reference(27, 2), reference(31, 22));
    verifyUsages(10, 16, reference(27, 7), reference(28, 9));
    verifyUsages(11, 14, reference(25, 7), reference(26, 9), reference(27, 14), reference(28, 16));
    verifyUsages(14, 2, reference(23, 10), reference(31, 9));
    verifyUsages(16, 4, reference(25, 2), reference(33, 12));
    verifyUsages(17, 14, reference(23, 15));
    verifyUsages(18, 13, reference(26, 2), reference(28, 2), reference(31, 14));
  }

  @Test
  public void testCharTypeResolution() {
    execute("typeResolution/Chars.pas");
    verifyUsages(9, 9, reference(24, 2));
    verifyUsages(14, 9, reference(25, 2));
  }

  @Test
  public void testCastTypeResolution() {
    execute("typeResolution/Casts.pas");
    verifyUsages(10, 14, reference(17, 12), reference(18, 16));
  }

  @Test
  public void testConstructorTypeResolution() {
    execute("typeResolution/Constructors.pas");
    verifyUsages(10, 14, reference(25, 14));
    verifyUsages(15, 14, reference(26, 14));
  }

  @Test
  public void testEnumsTypeResolution() {
    execute("typeResolution/Enums.pas");
    verifyUsages(20, 9, reference(30, 2), reference(31, 2), reference(32, 2));
  }

  @Test
  public void testPointersTypeResolution() {
    execute("typeResolution/Pointers.pas");
    verifyUsages(9, 10, reference(18, 2), reference(19, 2), reference(20, 2), reference(21, 2));
  }

  @Test
  public void testSubRangeHostTypeResolution() {
    execute("typeResolution/SubRangeHostType.pas");
    verifyUsages(14, 10, reference(21, 2), reference(22, 2), reference(23, 2));
  }

  @Test
  public void testLowHighIntrinsics() {
    execute("intrinsics/LowHighIntrinsics.pas");
    verifyUsages(
        14,
        10,
        reference(31, 2),
        reference(32, 2),
        reference(33, 2),
        reference(34, 2),
        reference(35, 2),
        reference(36, 2),
        reference(37, 2),
        reference(38, 2));
    verifyUsages(19, 10, reference(39, 2), reference(40, 2));
    verifyUsages(24, 10, reference(41, 2), reference(42, 2));
  }

  @Test
  public void testDefaultIntrinsic() {
    execute("intrinsics/DefaultIntrinsic.pas");
    verifyUsages(9, 10, reference(31, 2));
    verifyUsages(14, 10, reference(32, 2));
    verifyUsages(19, 10, reference(33, 2));
    verifyUsages(24, 10, reference(34, 2));
  }

  @Test
  public void testBinaryOperatorIntrinsics() {
    execute("operators/BinaryOperatorIntrinsics.pas");
    verifyUsages(
        12,
        10,
        reference(48, 2),
        reference(49, 2),
        reference(50, 2),
        reference(53, 2),
        reference(54, 2),
        reference(55, 2),
        reference(56, 2),
        reference(57, 2),
        reference(58, 2),
        reference(59, 2),
        reference(117, 2));
    verifyUsages(
        17,
        10,
        reference(62, 2),
        reference(63, 2),
        reference(64, 2),
        reference(65, 2),
        reference(66, 2),
        reference(79, 2),
        reference(80, 2),
        reference(81, 2),
        reference(82, 2),
        reference(83, 2));
    verifyUsages(
        22,
        10,
        reference(67, 2),
        reference(68, 2),
        reference(69, 2),
        reference(70, 2),
        reference(71, 2),
        reference(72, 2),
        reference(73, 2),
        reference(74, 2),
        reference(75, 2),
        reference(76, 2),
        reference(84, 2),
        reference(85, 2),
        reference(86, 2),
        reference(87, 2),
        reference(88, 2),
        reference(89, 2),
        reference(90, 2),
        reference(91, 2),
        reference(92, 2),
        reference(93, 2));
    verifyUsages(
        27,
        10,
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
        reference(109, 2),
        reference(110, 2));
    verifyUsages(32, 10, reference(113, 2), reference(114, 2));
    verifyUsages(37, 10, reference(118, 2), reference(119, 2), reference(120, 2));
  }

  @Test
  public void testBinaryOperatorOverloads() {
    execute("operators/BinaryOperatorOverloads.pas");
    verifyUsages(66, 10, reference(79, 2), reference(82, 2), reference(85, 2), reference(88, 2));
    verifyUsages(71, 10, reference(91, 2), reference(94, 2));
  }

  @Test
  public void testUnaryOperatorIntrinsics() {
    execute("operators/UnaryOperatorIntrinsics.pas");
    verifyUsages(12, 10, reference(42, 2));
    verifyUsages(17, 10, reference(45, 2), reference(49, 2), reference(50, 2));
    verifyUsages(22, 10, reference(46, 2), reference(51, 2), reference(52, 2));
    verifyUsages(27, 10, reference(53, 2), reference(54, 2));
  }

  @Test
  public void testUnaryOperatorOverloads() {
    execute("operators/UnaryOperatorOverloads.pas");
    verifyUsages(34, 10, reference(51, 2));
    verifyUsages(39, 10, reference(52, 2));
    verifyUsages(44, 10, reference(53, 2));
  }

  @Test
  public void testImplicitOperator() {
    execute("operators/ImplicitOperator.pas");
    verifyUsages(25, 10, reference(37, 2));
  }

  @Test
  public void testImplicitOperatorShouldHaveLowestPriority() {
    execute("operators/ImplicitOperatorLowestPriority.pas");
    verifyUsages(35, 10, reference(42, 2));
  }

  @Test
  public void testOperatorsAreNotCallable() {
    execute("operators/NotCallable.pas");
    verifyUsages(9, 20, reference(17, 7));
  }

  @Test
  public void testPointerMathOperators() {
    execute("operators/PointerMath.pas");
    verifyUsages(
        17,
        10,
        reference(51, 2),
        reference(62, 2),
        reference(63, 2),
        reference(74, 2),
        reference(75, 2),
        reference(52, 2),
        reference(53, 2),
        reference(64, 2),
        reference(65, 2),
        reference(76, 2),
        reference(77, 2));
    verifyUsages(
        22,
        10,
        reference(44, 2),
        reference(45, 2),
        reference(46, 2),
        reference(47, 2),
        reference(48, 2),
        reference(49, 2),
        reference(50, 2));
    verifyUsages(
        27,
        10,
        reference(55, 2),
        reference(56, 2),
        reference(57, 2),
        reference(58, 2),
        reference(59, 2),
        reference(60, 2),
        reference(61, 2));
    verifyUsages(
        32,
        10,
        reference(67, 2),
        reference(68, 2),
        reference(69, 2),
        reference(70, 2),
        reference(71, 2),
        reference(72, 2),
        reference(73, 2));
  }

  @Test
  public void testVariantOperators() {
    execute("operators/VariantOperators.pas");
    verifyUsages(
        10,
        10,
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
        reference(68, 2),
        reference(69, 2),
        reference(70, 2));
    verifyUsages(25, 10, reference(72, 2), reference(73, 2));
    verifyUsages(
        30,
        10,
        reference(75, 2),
        reference(76, 2),
        reference(77, 2),
        reference(78, 2),
        reference(79, 2),
        reference(80, 2),
        reference(81, 2),
        reference(82, 2),
        reference(83, 2),
        reference(84, 2),
        reference(85, 2),
        reference(86, 2));
  }

  @Test
  public void testSimpleProperties() {
    execute("properties/Simple.pas");
    verifyUsages(
        8,
        2,
        reference(16, 10),
        reference(18, 26),
        reference(19, 21),
        reference(20, 24),
        reference(21, 23),
        reference(22, 26),
        reference(27, 27),
        reference(32, 22),
        reference(39, 7),
        reference(41, 19),
        reference(44, 18),
        reference(47, 21));
    verifyUsages(10, 16, reference(41, 24), reference(44, 23), reference(47, 26));
    verifyUsages(11, 14, reference(42, 16), reference(45, 15), reference(48, 18));
    verifyUsages(14, 2, reference(27, 10), reference(32, 9), reference(37, 20));
    verifyUsages(
        16,
        4,
        reference(21, 33),
        reference(21, 44),
        reference(22, 36),
        reference(22, 47),
        reference(29, 2),
        reference(34, 12));
    verifyUsages(18, 14, reference(20, 47), reference(27, 15));
    verifyUsages(19, 13, reference(20, 34), reference(32, 14));
    verifyUsages(20, 13, reference(41, 6), reference(42, 6));
    verifyUsages(21, 13, reference(44, 6), reference(45, 6));
    verifyUsages(22, 13, reference(47, 6), reference(48, 6));
  }

  @Test
  public void testOverrideProperties() {
    execute("properties/OverrideProperties.pas");
    verifyUsages(
        10, 14, reference(31, 10), reference(32, 10), reference(33, 13), reference(34, 13));
    verifyUsages(18, 13, reference(31, 6), reference(33, 6));
    verifyUsages(23, 13, reference(32, 6), reference(34, 6));
  }

  @Test
  public void testProceduralProperties() {
    execute("properties/ProceduralProperties.pas");
    verifyUsages(14, 13, reference(21, 6));
    verifyUsages(19, 26, reference(21, 14));
  }

  @Test
  public void testHiddenDefaultProperties() {
    execute("properties/HiddenDefaultProperties.pas");
    verifyUsages(13, 14, reference(29, 25));
  }

  @Test
  public void testSimpleOverloads() {
    execute("overloads/Simple.pas");
    verifyUsages(10, 10, reference(16, 10), reference(37, 2));
    verifyUsages(11, 10, reference(21, 10), reference(38, 2));
    verifyUsages(12, 10, reference(26, 10), reference(39, 2), reference(40, 2));
  }

  @Test
  public void testTypeTypeOverloads() {
    execute("overloads/TypeType.pas");
    verifyUsages(8, 2, reference(17, 19), reference(25, 10));
    verifyUsages(12, 10, reference(27, 2));
    verifyUsages(17, 10, reference(28, 2));
  }

  @Test
  public void testNestedExpressions() {
    execute("overloads/NestedExpressions.pas");
    verifyUsages(9, 2, reference(23, 9), reference(40, 12), reference(43, 14));
    verifyUsages(10, 13, reference(23, 25), reference(44, 15));
    verifyUsages(13, 10, reference(28, 10), reference(44, 2));
    verifyUsages(14, 10, reference(33, 10), reference(45, 2), reference(46, 2));
    verifyUsages(18, 9, reference(45, 6), reference(46, 9));
  }

  @Test
  public void testAmbiguousMethodReferences() {
    execute("overloads/AmbiguousMethodReferences.pas");
    verifyUsages(8, 2, reference(10, 19), reference(20, 19), reference(32, 11));
    verifyUsages(10, 10, reference(20, 10), reference(35, 2), reference(36, 2));
    verifyUsages(11, 10, reference(25, 10), reference(37, 2), reference(38, 2));
    verifyUsages(15, 9, reference(36, 6));
  }

  @Test
  public void testProceduralVariables() {
    execute("overloads/ProceduralVariables.pas");
    verifyUsages(7, 10, reference(12, 10), reference(27, 20));
    verifyUsages(8, 10, reference(17, 10), reference(28, 19));
    verifyUsages(24, 2, reference(27, 2), reference(30, 2));
    verifyUsages(25, 2, reference(28, 2), reference(31, 2));
  }

  @Test
  public void testCharInSet() {
    execute("overloads/CharInSet.pas");
    verifyUsages(15, 13, reference(27, 22));
    verifyUsages(20, 10, reference(27, 12));
    verifyUsages(25, 19, reference(27, 36));
  }

  @Test
  public void testImportedOverloads() {
    execute(
        "overloads/Imports.pas",
        "overloads/imports/IntegerFoo.pas",
        "overloads/imports/StringFoo.pas");
    verifyUsages(15, 10, reference(32, 2), reference(33, 2));
    verifyUsages(26, 2, reference(30, 6));
    verifyUsages(27, 2, reference(31, 6));
    verifyUsages(28, 2, reference(32, 6));
  }

  @Test
  public void testDisambiguationOfOverloadsByDistanceFromCallSite() {
    execute("overloads/Distance.pas", "overloads/imports/DistantFoo.pas");
    verifyUsages(10, 10, reference(55, 2));
    verifyUsages(14, 16, reference(58, 14));
    verifyUsages(15, 14, reference(60, 6), reference(66, 2));
    verifyUsages(19, 16, reference(57, 14));
    verifyUsages(20, 14, reference(59, 6), reference(65, 2));
    verifyUsages(51, 15, reference(56, 6), reference(58, 21), reference(60, 10));
  }

  @Test
  public void testOverriddenOverloads() {
    execute("overloads/Overrides.pas", "overloads/imports/BaseFoo.pas");
    verifyUsages(33, 2, reference(37, 10));
    verifyUsages(34, 2, reference(38, 10));
    verifyUsages(35, 2, reference(39, 10));
  }

  @Test
  public void testRegularMethodPreferredOverImplicitSpecializations() {
    execute("generics/RegularMethodPreferredOverImplicitSpecialization.pas");
    verifyUsages(12, 20, reference(12, 26));
    verifyUsages(12, 15, reference(22, 2));
    verifyUsages(13, 15, reference(21, 2));
  }

  @Test
  public void testGenericArrayAssignmentCompatibility() {
    execute("generics/ArrayAssignmentCompatibility.pas");
    verifyUsages(14, 15, reference(27, 2));
    verifyUsages(15, 15, reference(28, 2));
    verifyUsages(16, 15, reference(29, 2));
    verifyUsages(17, 15, reference(30, 2));
  }

  @Test
  public void testStructAssignmentCompatibility() {
    execute("generics/StructAssignmentCompatibility.pas");
    verifyUsages(16, 15, reference(29, 2));
    verifyUsages(17, 15, reference(30, 2));
    verifyUsages(18, 15, reference(31, 2));
    verifyUsages(19, 15, reference(32, 2));
  }

  @Test
  public void testGenericMethodInterfaceNameResolution() {
    execute("generics/MethodInterfaceNameResolution.pas");
    verifyUsages(8, 2, reference(18, 10));
    verifyUsages(9, 15, reference(18, 18));
    verifyUsages(12, 2, reference(23, 10));
    verifyUsages(13, 15, reference(23, 15));
  }

  @Test
  public void testGenericSameNameTypes() {
    execute("generics/SameNameType.pas");
    verifyUsages(8, 2, reference(20, 15));
    verifyUsages(9, 2, reference(21, 16));
    verifyUsages(10, 2, reference(22, 18));
  }

  @Test
  public void testGenericParameterizedMethods() {
    execute("generics/ParameterizedMethod.pas");
    verifyUsages(
        13,
        14,
        reference(18, 15),
        reference(31, 2),
        reference(32, 2),
        reference(33, 2),
        reference(34, 2));
  }

  @Test
  public void testGenericImplicitSpecializations() {
    execute("generics/ImplicitSpecialization.pas");
    verifyUsages(16, 14, reference(21, 20), reference(28, 2), reference(29, 2), reference(30, 2));
  }

  @Test
  public void testGenericConstraints() {
    execute("generics/Constraint.pas");
    verifyUsages(13, 14, reference(22, 25), reference(55, 8));
  }

  @Test
  public void testGenericTypeParameterNameDeclarations() {
    execute("generics/TypeParameterNameDeclaration.pas");
    verifyUsages(
        8,
        7,
        reference(10, 22),
        reference(11, 19),
        reference(12, 18),
        reference(15, 49),
        reference(15, 53),
        reference(17, 45),
        reference(19, 31),
        reference(24, 14),
        reference(24, 35),
        reference(24, 39));
    verifyUsages(14, 13, reference(14, 27));
    verifyUsages(15, 22, reference(15, 41));
    verifyUsages(16, 11, reference(17, 31), reference(17, 54), reference(17, 58));
  }

  @Test
  public void testGenericTypeParameterConflicts() {
    execute("generics/TypeParameterNameConflict.pas");
    verifyUsages(9, 14, reference(27, 8));
    verifyUsages(13, 14, reference(28, 6), reference(29, 9));
    verifyUsages(16, 11, reference(17, 11), reference(23, 19));
    verifyUsages(
        18, 19, reference(18, 33), reference(23, 27), reference(23, 35), reference(25, 10));
  }

  @Test
  public void testPropertySpecialization() {
    execute("generics/PropertySpecialization.pas");
    verifyUsages(15, 10, reference(22, 2));
  }

  @Test
  public void testSimpleMethodResolutionClause() {
    execute("methodResolutionClauses/Simple.pas");
    verifyUsages(9, 14, reference(14, 26));
    verifyUsages(13, 14, reference(14, 42));
  }

  @Test
  public void testMethodResolutionClauseWithOverloadedImplementation() {
    execute("methodResolutionClauses/OverloadedImplementation.pas");
    verifyUsages(9, 14, reference(15, 26));
    verifyUsages(13, 14, reference(15, 42));
    verifyUsages(14, 14);
  }

  @Test
  public void testMethodResolutionClauseWithOverloadedInterfaceAndImplementation() {
    execute("methodResolutionClauses/OverloadedInterfaceAndImplementation.pas");
    verifyUsages(9, 14, reference(16, 26));
    verifyUsages(14, 14, reference(16, 42));
    verifyUsages(15, 14);
  }

  @Test
  public void testImports() {
    execute(
        "imports/source/Unit1.pas",
        "imports/Unit2.pas",
        "imports/source/Unit3.pas",
        "imports/ignored/Unit2.pas",
        "imports/ignored/Unit3.pas");
    verifyUsages(1, 5, reference(25, 2), reference(28, 18));
    verifyUsages(8, 2, reference(28, 2));
    verifyUsages(11, 2, reference(28, 24), reference(29, 12));
    verifyUsages(16, 2, reference(25, 18));
    verifyUsages(18, 10, reference(25, 8), reference(26, 2));
  }

  @Test
  public void testNamespaces() {
    execute(
        "namespaces/Namespaced.Unit1.pas",
        "namespaces/Unit1.pas",
        "namespaces/Namespaced.Unit2.pas",
        "namespaces/Unit3.pas",
        "namespaces/UnitScopeName.Unit2.pas",
        "namespaces/UnitScopeName.ScopedUnit3.pas");

    verifyUsages(1, 5, reference(25, 2), reference(28, 18));
    verifyUsages(8, 2, reference(32, 2));
    verifyUsages(8, 9, reference(28, 2));
    verifyUsages(11, 2, reference(28, 35), reference(29, 12), reference(31, 23), reference(32, 29));
    verifyUsages(16, 2, reference(25, 29));
    verifyUsages(18, 10, reference(25, 19), reference(26, 2));
  }

  @Test
  public void testUnitScopeNames() {
    unitScopeNames = Set.of("NonexistentUnitScope", "UnitScopeName", "ABCUnitScopeXYZ");

    execute(
        "namespaces/UnitScopeNameTest.pas",
        "namespaces/UnitScopeName.Unit2.pas",
        "namespaces/UnitScopeName.ScopedUnit3.pas",
        "namespaces/Namespaced.Unit1.pas",
        "namespaces/Namespaced.Unit2.pas",
        "namespaces/Unit3.pas");

    verifyUsages(1, 5, reference(25, 2), reference(28, 30));
    verifyUsages(8, 2, reference(28, 2));
    verifyUsages(11, 2, reference(28, 48), reference(29, 18));
    verifyUsages(16, 2, reference(25, 30));
    verifyUsages(18, 10, reference(25, 20), reference(26, 2));
  }

  @Test
  public void testUnitAliases() {
    unitAliases.put("UnitX", "Unit2");
    unitAliases.put("UnitY", "Unit3");

    execute("unitAliases/Unit1.pas", "unitAliases/Unit2.pas", "unitAliases/Unit3.pas");

    verifyUsages(1, 5, reference(25, 2), reference(28, 18));
    verifyUsages(8, 2, reference(28, 2));
    verifyUsages(11, 2, reference(28, 24), reference(29, 12));
    verifyUsages(16, 2, reference(25, 18));
    verifyUsages(18, 10, reference(25, 8), reference(26, 2));
  }

  @Test
  public void testUnscopedEnums() {
    execute("enums/UnscopedEnum.pas");
    verifyUsages(8, 2, reference(12, 19));
    verifyUsages(8, 9, reference(14, 11));
  }

  @Test
  public void testNestedUnscopedEnums() {
    execute("enums/NestedUnscopedEnum.pas");
    verifyUsages(10, 12, reference(17, 9));
    verifyUsages(22, 16, reference(26, 9));
    verifyUsages(31, 8, reference(33, 9));
  }

  @Test
  public void testScopedEnums() {
    execute("enums/ScopedEnum.pas");
    verifyUsages(10, 2);
    verifyUsages(10, 9);
  }

  @Test
  public void testSimpleHelpers() {
    execute("helpers/Simple.pas");
    verifyUsages(12, 14, reference(17, 21), reference(24, 6));
  }

  @Test
  public void testClassHelperOverrides() {
    execute("helpers/ClassHelperOverride.pas");
    verifyUsages(9, 14, reference(18, 15));
    verifyUsages(13, 14, reference(23, 21), reference(30, 6));
  }

  @Test
  public void testClassHelperOverloads() {
    execute("helpers/ClassHelperOverload.pas");
    verifyUsages(9, 14, reference(22, 15), reference(39, 6));
    verifyUsages(13, 14, reference(27, 25));
    verifyUsages(17, 14, reference(32, 21), reference(40, 6));
  }

  @Test
  public void testClassHelperSelfValues() {
    execute("helpers/ClassHelperSelfValue.pas");
    verifyUsages(17, 10, reference(24, 2));
  }

  @Test
  public void testClassHelperInheritedStatements() {
    execute("helpers/ClassHelperInheritedStatement.pas");
    verifyUsages(9, 14, reference(31, 19), reference(68, 2));
    verifyUsages(10, 14, reference(36, 19), reference(76, 2));
    verifyUsages(15, 14, reference(46, 15), reference(69, 12), reference(77, 12));
    verifyUsages(16, 14, reference(51, 15), reference(70, 12), reference(78, 12));
    verifyUsages(11, 14, reference(41, 19), reference(71, 12), reference(79, 12));
  }

  @Test
  public void testClassHelperAccessingExtendedTypes() {
    execute("helpers/ClassHelperAccessingExtendedType.pas");
    verifyUsages(10, 14, reference(28, 19));
    verifyUsages(12, 14, reference(33, 19), reference(54, 2));
    verifyUsages(17, 14, reference(39, 15));
    verifyUsages(19, 14, reference(44, 15), reference(55, 2));
  }

  @Test
  public void testRecordHelperLiterals() {
    execute("helpers/RecordHelperLiteral.pas");
    verifyUsages(9, 14, reference(26, 21), reference(48, 10));
    verifyUsages(13, 14, reference(31, 24), reference(49, 9));
    verifyUsages(17, 14, reference(36, 22), reference(50, 6));
    verifyUsages(21, 14, reference(41, 26), reference(51, 29));
  }

  @Test
  public void testRecordHelperTypeReference() {
    execute("helpers/RecordHelperTypeReference.pas");
    verifyUsages(9, 14, reference(21, 9), reference(22, 16));
  }

  @Test
  public void testRecordHelperConstants() {
    execute("helpers/RecordHelperConstant.pas");
    verifyUsages(11, 6, reference(18, 11), reference(19, 16));
    verifyUsages(16, 10, reference(18, 2), reference(19, 2));
  }

  @Test
  public void testRecordHelperSelfValues() {
    execute("helpers/RecordHelperSelfValue.pas");
    verifyUsages(14, 10, reference(21, 2));
  }

  @Test
  public void testHelperImports() {
    execute(
        "helpers/imports/Unit1.pas",
        "helpers/imports/Unit2.pas",
        "helpers/imports/Unit3.pas",
        "helpers/imports/Unit4.pas");
    verifyUsages(19, 10, reference(26, 2));
  }

  @Test
  public void testDependencyReferencedImplicitly() {
    execute("dependencies/Implicit.pas");
    verifyDependencies("System.SysUtils");
  }

  @Test
  public void testDependencyReferencedExplicitly() {
    execute("dependencies/Explicit.pas");
    verifyDependencies("System.SysUtils");
  }

  @Test
  public void testDependencyForHelperReference() {
    execute("dependencies/Helper.pas");
    verifyDependencies("System.SysUtils");
  }

  @Test
  public void testDependencyForComponentAncestor() {
    execute("dependencies/ComponentAncestor.pas");
    verifyDependencies("Vcl.Controls", "System.Classes");
  }

  @Test
  public void testDependencyForComponentAncestorDeclaredInImplementation() {
    execute("dependencies/ComponentAncestorDeclaredInImplementation.pas");
    verifyDependencies("Vcl.Controls");
  }

  @Test
  public void testDependencyForComponentAncestorWithPublishedFieldInNonNonComponentType() {
    execute("dependencies/ComponentAncestorWithPublishedFieldInNonComponentType.pas");
    verifyDependencies("Vcl.Controls");
  }

  @Test
  public void testDependencyForComponentAncestorDependencyWithNonPublishedField() {
    execute("dependencies/ComponentAncestorWithNonPublishedField.pas");
    verifyDependencies("Vcl.Controls");
  }

  @Test
  public void testDependencyRequiredForInlineMethodExpansion() {
    execute("dependencies/InlineMethodExpansion.pas");
    verifyDependencies("System.UITypes", "Vcl.Dialogs");
  }

  @Test
  public void testDependencyRequiredForInlineMethodExpansionViaDefaultArrayProperties() {
    execute(
        "dependencies/InlineMethodExpansionViaDefaultArrayProperty.pas",
        "dependencies/imports/UnitWithDefaultArrayPropertyBackedByInlineMethod.pas");
    verifyDependencies("UnitWithDefaultArrayPropertyBackedByInlineMethod", "System.SysUtils");
  }

  @Test
  public void testDependencyShouldNotBeIntroducedForImplementationMethods() {
    execute(
        "dependencies/ImplementationVisibility.pas",
        "dependencies/imports/UnitWithImplementationMethod.pas");
    verifyDependencies("System.SysUtils");
  }

  @Test
  public void testDependencyRequiredForImplicitInvocationOfGetEnumerator() {
    execute(
        "dependencies/Enumerator.pas", "dependencies/imports/UnitWithGetEnumeratorForTObject.pas");
    verifyDependencies("UnitWithGetEnumeratorForTObject");
  }

  private void execute(String filename, String... include) {
    mainFile = DelphiTestFileBuilder.fromResource(ROOT_PATH + filename).delphiFile();
    Map<String, InputFile> inputFiles = new HashMap<>();

    inputFiles.put(uriToAbsolutePath(mainFile.getInputFile().uri()), mainFile.getInputFile());

    for (String name : include) {
      String path = ROOT_PATH + name;
      InputFile inputFile = DelphiTestFileBuilder.fromResource(path).delphiFile().getInputFile();
      inputFiles.put(uriToAbsolutePath(inputFile.uri()), inputFile);
    }

    DelphiProjectHelper delphiProjectHelper = mock(DelphiProjectHelper.class);
    when(delphiProjectHelper.getFile(anyString()))
        .thenAnswer(
            invocation -> {
              String path = invocation.getArgument(0);
              return inputFiles.get(path);
            });

    symbolTable =
        SymbolTable.builder()
            .sourceFiles(
                inputFiles.values().stream()
                    .map(InputFile::uri)
                    .map(Path::of)
                    .collect(Collectors.toList()))
            .standardLibraryPath(DelphiUtils.getResource(STANDARD_LIBRARY).toPath())
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
}
