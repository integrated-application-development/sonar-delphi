package org.sonar.plugins.delphi.symbol.scope;

import com.google.common.collect.Iterables;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import org.sonar.plugins.delphi.antlr.ast.node.ArrayAccessorNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.IndexedNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodNameNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.HelperType;

abstract class AbstractFileScope extends AbstractDelphiScope implements FileScope {
  private final String name;
  private final Deque<FileScope> imports = new ArrayDeque<>();
  private final Map<Integer, DelphiScope> registeredScopes = new HashMap<>();
  private final Map<Integer, DelphiNameDeclaration> registeredDeclarations = new HashMap<>();
  private final Map<Integer, DelphiNameOccurrence> registeredOccurrences = new HashMap<>();

  protected AbstractFileScope(String name) {
    this.name = name;
  }

  @Override
  public Set<NameDeclaration> findDeclaration(DelphiNameOccurrence occurrence) {
    Set<NameDeclaration> result = super.findDeclaration(occurrence);
    for (FileScope importScope : imports) {
      if (result.isEmpty()) {
        result = importScope.shallowFindDeclaration(occurrence);
      } else {
        importScope.findMethodOverloads(occurrence, result);
      }
    }
    return result;
  }

  @Override
  public HelperType getHelperForType(Type type) {
    HelperType result = super.getHelperForType(type);
    if (result == null) {
      for (FileScope importScope : imports) {
        result = importScope.shallowGetHelperForType(type);
        if (result != null) {
          break;
        }
      }
    }
    return result;
  }

  @Override
  public HelperType shallowGetHelperForType(Type type) {
    return findHelper(type);
  }

  @Override
  public Set<NameDeclaration> shallowFindDeclaration(DelphiNameOccurrence occurrence) {
    Set<NameDeclaration> result = super.findDeclaration(occurrence);
    result.removeIf(UnitImportNameDeclaration.class::isInstance);
    return result;
  }

  @Override
  public void addDeclaration(NameDeclaration declaration) {
    if (declaration instanceof UnitImportNameDeclaration) {
      FileScope scope = ((UnitImportNameDeclaration) declaration).getUnitScope();
      if (scope != null) {
        imports.addFirst(scope);
      }
    }
    super.addDeclaration(declaration);
  }

  @Override
  protected boolean overloadsRequireOverloadDirective() {
    return true;
  }

  public String getName() {
    return name;
  }

  protected void addImport(FileScope scope) {
    this.imports.addFirst(scope);
  }

  @Override
  public void registerScope(IndexedNode node, DelphiScope scope) {
    registeredScopes.put(node.getTokenIndex(), scope);
  }

  @Override
  public void registerDeclaration(IndexedNode node, NameDeclaration declaration) {
    registeredDeclarations.put(node.getTokenIndex(), (DelphiNameDeclaration) declaration);
  }

  @Override
  public void registerOccurrence(IndexedNode node, NameOccurrence occurrence) {
    registeredOccurrences.put(node.getTokenIndex(), (DelphiNameOccurrence) occurrence);
  }

  @Override
  public void attach(DelphiNode node) {
    node.setScope(registeredScopes.get(node.getTokenIndex()));
  }

  @Override
  public void attach(NameDeclarationNode node) {
    node.setNameDeclaration(registeredDeclarations.get(node.getTokenIndex()));
  }

  @Override
  public void attach(MethodNameNode node) {
    var declaration = (MethodNameDeclaration) registeredDeclarations.get(node.getTokenIndex());
    node.setMethodNameDeclaration(declaration);
  }

  @Override
  public void attach(NameReferenceNode node) {
    node.setNameOccurrence(registeredOccurrences.get(node.getTokenIndex()));
  }

  @Override
  public void attach(ArrayAccessorNode node) {
    node.setImplicitNameOccurrence(registeredOccurrences.get(node.getTokenIndex()));
  }

  @Override
  public UnitNameDeclaration getUnitDeclaration() {
    return Iterables.getLast(getUnitDeclarations());
  }
}
