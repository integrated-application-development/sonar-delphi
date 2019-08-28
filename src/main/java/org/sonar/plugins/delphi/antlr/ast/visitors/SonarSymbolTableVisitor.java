package org.sonar.plugins.delphi.antlr.ast.visitors;

import java.util.List;
import javax.annotation.Nullable;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import org.jetbrains.annotations.NotNull;
import org.sonar.api.batch.sensor.symbol.NewSymbol;
import org.sonar.api.batch.sensor.symbol.NewSymbolTable;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;

public class SonarSymbolTableVisitor implements DelphiParserVisitor<NewSymbolTable> {

  private static void createSymbol(
      @Nullable NameDeclaration declaration,
      @NotNull List<NameOccurrence> occurrences,
      @NotNull NewSymbolTable table) {
    if (declaration == null) {
      return;
    }

    Node location = declaration.getNode();
    NewSymbol newSymbol =
        table.newSymbol(
            location.getBeginLine(),
            location.getBeginColumn(),
            location.getEndLine(),
            location.getEndColumn());

    for (NameOccurrence occurrence : occurrences) {
      location = occurrence.getLocation();
      if (location instanceof NameReferenceNode) {
        location = ((NameReferenceNode) location).getIdentifier();
      }
      newSymbol.newReference(
          location.getBeginLine(),
          location.getBeginColumn(),
          location.getEndLine(),
          location.getEndColumn());
    }
  }

  @Override
  public NewSymbolTable visit(NameDeclarationNode name, NewSymbolTable data) {
    createSymbol(name.getNameDeclaration(), name.getUsages(), data);
    return DelphiParserVisitor.super.visit(name, data);
  }
}
