package org.sonar.plugins.delphi.antlr.ast.visitors;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import org.jetbrains.annotations.NotNull;
import org.sonar.api.batch.sensor.symbol.NewSymbol;
import org.sonar.api.batch.sensor.symbol.NewSymbolTable;
import org.sonar.plugins.delphi.antlr.ast.node.EnumElementNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodNameNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.symbol.SymbolicNode;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;

public class SonarSymbolTableVisitor implements DelphiParserVisitor<NewSymbolTable> {

  private static void createSymbol(
      @Nullable DelphiNameDeclaration declaration,
      @NotNull List<NameOccurrence> occurrences,
      @NotNull NewSymbolTable table) {
    if (declaration == null) {
      return;
    }

    SymbolicNode location = declaration.getNode();
    String symbolUnit = location.getUnitName();

    NewSymbol newSymbol =
        table.newSymbol(
            location.getBeginLine(),
            location.getBeginColumn(),
            location.getEndLine(),
            location.getEndColumn());

    DelphiNameDeclaration forward = declaration.getForwardDeclaration();
    if (forward != null) {
      List<NameOccurrence> forwardUsages = forward.getScope().getOccurrencesFor(forward);
      occurrences = Lists.newArrayList(occurrences);
      occurrences.addAll(forwardUsages);

      location = forward.getNode();
      newSymbol.newReference(
          location.getBeginLine(),
          location.getBeginColumn(),
          location.getEndLine(),
          location.getEndColumn());
    }

    for (NameOccurrence occurrence : occurrences) {
      location = (SymbolicNode) occurrence.getLocation();
      String referenceUnit = location.getUnitName();

      if (symbolUnit.equals(referenceUnit)) {
        newSymbol.newReference(
            location.getBeginLine(),
            location.getBeginColumn(),
            location.getEndLine(),
            location.getEndColumn());
      }
    }
  }

  @Override
  public NewSymbolTable visit(EnumElementNode element, NewSymbolTable data) {
    NameDeclarationNode decl = element.getNameDeclarationNode();
    return decl.accept(this, data);
  }

  @Override
  public NewSymbolTable visit(NameDeclarationNode name, NewSymbolTable data) {
    createSymbol(name.getNameDeclaration(), name.getUsages(), data);
    return DelphiParserVisitor.super.visit(name, data);
  }

  @Override
  public NewSymbolTable visit(MethodNameNode name, NewSymbolTable data) {
    createSymbol(name.getMethodNameDeclaration(), name.getUsages(), data);
    return DelphiParserVisitor.super.visit(name, data);
  }
}
