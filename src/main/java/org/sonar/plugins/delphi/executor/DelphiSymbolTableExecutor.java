package org.sonar.plugins.delphi.executor;

import org.sonar.api.batch.sensor.symbol.NewSymbolTable;
import org.sonar.plugins.delphi.antlr.ast.visitors.SonarSymbolTableVisitor;
import org.sonar.plugins.delphi.antlr.ast.visitors.SymbolAssociationVisitor;
import org.sonar.plugins.delphi.antlr.ast.visitors.SymbolAssociationVisitor.Data;
import org.sonar.plugins.delphi.file.DelphiFile.DelphiInputFile;

public class DelphiSymbolTableExecutor implements Executor {
  private static final SymbolAssociationVisitor ASSOCIATE_VISITOR = new SymbolAssociationVisitor();
  private static final SonarSymbolTableVisitor SONAR_VISITOR = new SonarSymbolTableVisitor();

  @Override
  public void execute(Context context, DelphiInputFile file) {
    // Attach AST nodes to scopes, declarations, and occurrences from the symbol table.
    ASSOCIATE_VISITOR.visit(file.getAst(), new Data(context.symbolTable()));

    // Provide symbol information to the Sonar API, which can be viewed in the web interface.
    NewSymbolTable sonarSymbolTable =
        context.sensorContext().newSymbolTable().onFile(file.getInputFile());
    SONAR_VISITOR.visit(file.getAst(), sonarSymbolTable);
    sonarSymbolTable.save();
  }
}
