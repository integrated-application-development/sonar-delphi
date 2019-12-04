package org.sonar.plugins.delphi.symbol.scope;

import java.util.Set;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.IndexedNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodNameNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;

public interface FileScope extends DelphiScope {

  /**
   * Find declarations in this scope without traversing imports
   *
   * @param occurrence The name for which we are trying to find a matching declaration
   * @return Set of name declarations matching the name occurrence
   */
  Set<NameDeclaration> shallowFindDeclaration(DelphiNameOccurrence occurrence);

  /**
   * Registers a node as being associated with a scope so it can be re-attached later
   *
   * @param node The node which we are registering
   * @param scope The scope we want to associate the node to
   */
  void registerScope(IndexedNode node, DelphiScope scope);

  /**
   * Registers a node as being associated with a declaration so it can be re-attached later
   *
   * @param node The node which we want to associate the declaration with
   * @param declaration The declaration we are registering
   */
  void registerDeclaration(IndexedNode node, NameDeclaration declaration);

  /**
   * Registers a node as being associated with an occurrence so it can be re-attached later
   *
   * @param node The node which we want to associate the name occurrence with
   * @param occurrence The occurrence we are registering
   */
  void registerOccurrence(IndexedNode node, NameOccurrence occurrence);

  /**
   * Attaches scope information to a particular node
   *
   * @param node The node which we want to attach symbol information to
   */
  void attach(DelphiNode node);

  /**
   * Attaches symbol declaration information to a particular node
   *
   * @param node The node which we want to attach symbol information to
   */
  void attach(NameDeclarationNode node);

  /**
   * Attaches symbol declaration information to a method name node
   *
   * @param node The node which we want to attach symbol information to
   */
  void attach(MethodNameNode node);

  /**
   * Attaches symbol occurrence information to a particular node
   *
   * @param node The node which we want to attach symbol information to
   */
  void attach(NameReferenceNode node);

  /**
   * Returns the system scope
   *
   * @return System scope
   */
  SystemScope getSystemScope();
}
