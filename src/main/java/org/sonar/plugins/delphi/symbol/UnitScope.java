package org.sonar.plugins.delphi.symbol;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.IndexedNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;

public class UnitScope extends AbstractDelphiScope {
  private final String name;
  private final List<UnitImportNameDeclaration> imports = new ArrayList<>();
  private final HashMap<Integer, DelphiScope> registeredScopes = new HashMap<>();
  private final HashMap<Integer, DelphiNameDeclaration> registeredDeclarations = new HashMap<>();
  private final HashMap<Integer, DelphiNameOccurrence> registeredOccurrences = new HashMap<>();

  public UnitScope(String name) {
    this.name = name;
  }

  @Override
  public Set<NameDeclaration> findDeclaration(DelphiNameOccurrence occurrence) {
    Set<NameDeclaration> result = shallowFindDeclaration(occurrence);
    if (result.isEmpty()) {
      for (UnitImportNameDeclaration unitImport : Lists.reverse(imports)) {
        UnitScope unitScope = unitImport.getUnitScope();
        if (unitScope != null) {
          result = unitScope.shallowFindDeclaration(occurrence);
        }

        if (!result.isEmpty()) {
          break;
        }
      }
    }
    return result;
  }

  private Set<NameDeclaration> shallowFindDeclaration(DelphiNameOccurrence occurrence) {
    return super.findDeclaration(occurrence);
  }

  @Override
  public void addDeclaration(NameDeclaration declaration) {
    if (declaration instanceof UnitImportNameDeclaration) {
      imports.add((UnitImportNameDeclaration) declaration);
    }
    super.addDeclaration(declaration);
  }

  public String getName() {
    return name;
  }

  /**
   * Registers a node as being associated with a scope so it can be re-attached later
   *
   * @param node The node which we are registering
   * @param scope The scope we want to associate the node to
   */
  public void registerScope(IndexedNode node, DelphiScope scope) {
    registeredScopes.put(node.getTokenIndex(), scope);
  }

  /**
   * Registers a node as being associated with a declaration so it can be re-attached later
   *
   * @param node The node which we want to associate the declaration with
   * @param declaration The declaration we are registering
   */
  public void registerDeclaration(IndexedNode node, NameDeclaration declaration) {
    registeredDeclarations.put(node.getTokenIndex(), (DelphiNameDeclaration) declaration);
  }

  /**
   * Registers a node as being associated with an occurrence so it can be re-attached later
   *
   * @param node The node which we want to associate the name occurrence with
   * @param occurrence The occurrence we are registering
   */
  public void registerOccurrence(IndexedNode node, NameOccurrence occurrence) {
    registeredOccurrences.put(node.getTokenIndex(), (DelphiNameOccurrence) occurrence);
  }

  /**
   * Attaches scope information to a particular node
   *
   * @param node The node which we want to attach symbol information to
   */
  public void attach(DelphiNode node) {
    node.setScope(registeredScopes.get(node.getTokenIndex()));
  }

  /**
   * Attaches symbol declaration information to a particular node
   *
   * @param node The node which we want to attach symbol information to
   */
  public void attach(NameDeclarationNode node) {
    node.setNameDeclaration(registeredDeclarations.get(node.getTokenIndex()));
  }

  /**
   * Attaches symbol occurrence information to a particular node
   *
   * @param node The node which we want to attach symbol information to
   */
  public void attach(NameReferenceNode node) {
    node.setNameOccurrence(registeredOccurrences.get(node.getTokenIndex()));
  }

  @Override
  public String toString() {
    return name + "<UnitScope>";
  }
}
