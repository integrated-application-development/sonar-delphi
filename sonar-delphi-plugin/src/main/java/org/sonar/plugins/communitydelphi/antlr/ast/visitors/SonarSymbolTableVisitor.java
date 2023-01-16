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
package org.sonar.plugins.communitydelphi.antlr.ast.visitors;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import org.jetbrains.annotations.NotNull;
import org.sonar.api.batch.sensor.symbol.NewSymbol;
import org.sonar.api.batch.sensor.symbol.NewSymbolTable;
import org.sonar.plugins.communitydelphi.antlr.ast.node.EnumElementNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.MethodNameNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.symbol.SymbolicNode;
import org.sonar.plugins.communitydelphi.symbol.declaration.DelphiNameDeclaration;

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
