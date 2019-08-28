package org.sonar.plugins.delphi.executor;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.symbol.NewSymbolTable;
import org.sonar.plugins.delphi.DelphiFile;
import org.sonar.plugins.delphi.antlr.ast.visitors.SonarSymbolTableVisitor;
import org.sonar.plugins.delphi.antlr.ast.visitors.SymbolTableVisitor;

public class DelphiSymbolTableExecutor implements Executor {
  private static final SymbolTableVisitor SYMBOL_VISITOR = new SymbolTableVisitor();
  private static final SonarSymbolTableVisitor SONAR_VISITOR = new SonarSymbolTableVisitor();

  @Override
  public void execute(SensorContext context, DelphiFile file) {
    // Create scopes, name declarations, name occurrences, link it all up to the AST
    SYMBOL_VISITOR.visit(file.getAst(), new SymbolTableVisitor.Data());

    // Provide symbol information to the Sonar API, which can be viewed in the web interface.
    NewSymbolTable sonarSymbolTable = context.newSymbolTable().onFile(file.getInputFile());
    SONAR_VISITOR.visit(file.getAst(), sonarSymbolTable);
    sonarSymbolTable.save();
  }
}
