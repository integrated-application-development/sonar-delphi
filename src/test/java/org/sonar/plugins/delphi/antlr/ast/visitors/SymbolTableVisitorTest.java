package org.sonar.plugins.delphi.antlr.ast.visitors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
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
  public void testResolutionLevelNoneDoesNothing() {
    Data data = createData(ResolutionLevel.NONE);
    DelphiAST ast = new DelphiTestUnitBuilder().parse();

    VISITOR.visit(ast, data);
    assertThat(data.getUnitDeclaration()).isNull();
  }

  @Test
  public void testNonUnitFilesShouldOnlyResolveForResolutionLevelComplete() {
    Data data = createData(ResolutionLevel.INTERFACE);
    DelphiAST ast = new DelphiTestProgramBuilder().parse();

    VISITOR.visit(ast, data);
    assertThat(data.getUnitDeclaration()).isNull();

    data = createData(ResolutionLevel.COMPLETE);

    VISITOR.visit(new DelphiTestUnitBuilder().parse(), data);
    assertThat(data.getUnitDeclaration()).isNotNull();
  }

  private static Data createData(ResolutionLevel resolutionLevel) {
    return new Data(
        ResolutionLevel.NONE,
        resolutionLevel,
        mock(ImportResolutionHandler.class),
        mock(CompilerSwitchRegistry.class),
        new SystemScope(),
        null);
  }
}
