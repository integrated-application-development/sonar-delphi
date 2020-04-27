package org.sonar.plugins.delphi.antlr.ast.visitors;

import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.node.FileHeaderNode;
import org.sonar.plugins.delphi.antlr.ast.node.FinalizationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.ImplementationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.InitializationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.UnitDeclarationNode;

public class SymbolTableInterfaceVisitor extends SymbolTableVisitor {
  @Override
  public Data visit(DelphiAST node, Data data) {
    FileHeaderNode header = node.getFileHeader();
    if (!(header instanceof UnitDeclarationNode)) {
      // Only units have interface sections.
      return data;
    }

    return super.visit(node, data);
  }

  @Override
  public Data visit(ImplementationSectionNode node, Data data) {
    return data;
  }

  @Override
  public Data visit(InitializationSectionNode node, Data data) {
    return data;
  }

  @Override
  public Data visit(FinalizationSectionNode node, Data data) {
    return data;
  }
}
