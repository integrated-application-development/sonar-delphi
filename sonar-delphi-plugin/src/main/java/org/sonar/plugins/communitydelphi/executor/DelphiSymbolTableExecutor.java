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
package org.sonar.plugins.communitydelphi.executor;

import org.sonar.api.batch.sensor.symbol.NewSymbolTable;
import org.sonar.plugins.communitydelphi.antlr.ast.visitors.SonarSymbolTableVisitor;
import org.sonar.plugins.communitydelphi.antlr.ast.visitors.SymbolAssociationVisitor;
import org.sonar.plugins.communitydelphi.antlr.ast.visitors.SymbolAssociationVisitor.Data;
import org.sonar.plugins.communitydelphi.file.DelphiFile.DelphiInputFile;

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
