package org.sonar.plugins.delphi.antlr.ast.visitors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.antlr.runtime.Token;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.FinalizationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.ImplementationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.InitializationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.InterfaceSectionNode;
import org.sonar.plugins.delphi.antlr.ast.token.DelphiToken;
import org.sonar.plugins.delphi.antlr.ast.visitors.SymbolTableVisitor.Data;
import org.sonar.plugins.delphi.antlr.ast.visitors.SymbolTableVisitor.ResolutionLevel;
import org.sonar.plugins.delphi.preprocessor.CompilerSwitchRegistry;
import org.sonar.plugins.delphi.symbol.ImportResolutionHandler;
import org.sonar.plugins.delphi.symbol.scope.SystemScope;
import org.sonar.plugins.delphi.utils.builders.DelphiTestProgramBuilder;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class SymbolTableVisitorTest {
  private static final SymbolTableVisitor VISITOR = new SymbolTableVisitor();

  @Test
  public void testResolvedNoneShouldDoNothingForResolutionLevelNone() {
    checkResolutionOccurs(ResolutionLevel.NONE, ResolutionLevel.NONE, false);
  }

  @Test
  public void testResolvedInterfaceShouldDoNothingForResolutionLevelInterface() {
    checkResolutionOccurs(ResolutionLevel.INTERFACE, ResolutionLevel.INTERFACE, false);
  }

  @Test
  public void testResolvedCompleteShouldDoNothingForResolutionLevelInterface() {
    checkResolutionOccurs(ResolutionLevel.COMPLETE, ResolutionLevel.INTERFACE, false);
  }

  @Test
  public void testResolvedCompleteShouldDoNothingForResolutionLevelComplete() {
    checkResolutionOccurs(ResolutionLevel.COMPLETE, ResolutionLevel.COMPLETE, false);
  }

  @Test
  public void testResolvedNoneShouldDoResolutionForResolutionLevelComplete() {
    checkResolutionOccurs(ResolutionLevel.NONE, ResolutionLevel.COMPLETE, true);
  }

  @Test
  public void testResolvedInterfaceShouldDoResolutionForResolutionLevelComplete() {
    checkResolutionOccurs(ResolutionLevel.INTERFACE, ResolutionLevel.COMPLETE, true);
  }

  @Test
  public void testNonUnitFilesShouldOnlyResolveForResolutionLevelComplete() {
    Data data = createData(ResolutionLevel.NONE, ResolutionLevel.INTERFACE);
    DelphiAST ast = new DelphiTestProgramBuilder().parse();

    VISITOR.visit(ast, data);
    assertThat(data.getUnitDeclaration()).isNull();

    data = createData(ResolutionLevel.NONE, ResolutionLevel.COMPLETE);

    VISITOR.visit(new DelphiTestUnitBuilder().parse(), data);
    assertThat(data.getUnitDeclaration()).isNotNull();
  }

  private static void checkResolutionOccurs(
      ResolutionLevel resolvedLevel,
      ResolutionLevel wantedResolutionLevel,
      boolean shouldResolutionOccur) {
    Data data = createData(resolvedLevel, wantedResolutionLevel);
    DelphiAST ast = new DelphiTestUnitBuilder().parse();

    var initializationSection = new InitializationSectionNode(Token.INVALID_TOKEN);
    var finalizationSection = new FinalizationSectionNode(Token.INVALID_TOKEN);
    var interfaceSection = ast.getFirstDescendantOfType(InterfaceSectionNode.class);
    var implementationSection = ast.getFirstDescendantOfType(ImplementationSectionNode.class);

    DelphiNode testNode = mock(DelphiNode.class);
    when(testNode.getToken()).thenReturn(new DelphiToken(Token.INVALID_TOKEN));

    interfaceSection.jjtAddChild(testNode);
    initializationSection.jjtAddChild(testNode);
    finalizationSection.jjtAddChild(testNode);
    implementationSection.jjtAddChild(testNode);

    ast.jjtAddChild(initializationSection);
    ast.jjtAddChild(finalizationSection);

    int times = 0;
    if (shouldResolutionOccur) {
      times = 4;
      if (resolvedLevel == ResolutionLevel.INTERFACE) {
        --times;
      }
    }

    VISITOR.visit(ast, data);
    verify(testNode, times(times)).accept(VISITOR, data);
  }

  private static Data createData(ResolutionLevel resolved, ResolutionLevel resolutionLevel) {
    return new Data(
        resolved,
        resolutionLevel,
        mock(ImportResolutionHandler.class),
        mock(CompilerSwitchRegistry.class),
        new SystemScope(),
        null);
  }
}
