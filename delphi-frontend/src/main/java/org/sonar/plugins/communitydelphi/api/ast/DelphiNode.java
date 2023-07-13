package org.sonar.plugins.communitydelphi.api.ast;

import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import au.com.integradev.delphi.type.factory.TypeFactory;
import java.util.List;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;

public interface DelphiNode extends Node {
  DelphiToken getToken();

  DelphiToken jjtGetFirstToken();

  DelphiToken jjtGetLastToken();

  DelphiNode jjtGetParent();

  void jjtAddChild(DelphiNode child);

  void jjtSetChildIndex(int index);

  int jjtGetChildIndex();

  DelphiNode jjtGetChild(int index);

  int jjtGetNumChildren();

  DelphiNode getNthParent(int n);

  <T> T getFirstParentOfType(Class<T> type);

  <T> List<T> getParentsOfType(Class<T> type);

  <T> List<T> findChildrenOfType(Class<T> type);

  <T> List<T> findDescendantsOfType(Class<T> type);

  <T> T getFirstChildOfType(Class<T> type);

  <T> T getFirstDescendantOfType(Class<T> type);

  <T> boolean hasDescendantOfType(Class<T> type);

  DelphiNode getFirstChildWithTokenType(DelphiTokenType tokenType);

  /**
   * Returns the AST root node
   *
   * @return the AST root node
   */
  DelphiAst getAst();

  TypeFactory getTypeFactory();

  /**
   * Returns comments nested inside this node
   *
   * @return comments nested inside this node
   */
  List<DelphiToken> getComments();

  /**
   * Allow a DelphiParserVisitor to visit this node and do some work
   *
   * @param <T> the visitor's data type
   * @param visitor the visitor doing some work with the node
   * @param data the data being passed around and optionally mutated during the visitor's work
   * @return the visitor's data
   */
  <T> T accept(DelphiParserVisitor<T> visitor, T data);

  /**
   * Allow a DelphiParserVisitor to visit all the children of this node
   *
   * @param <T> The visitor's data type
   * @param visitor The DelphiParserVisitor
   * @param data Data related to this visit
   * @return Data related to this visit
   */
  default <T> T childrenAccept(DelphiParserVisitor<T> visitor, T data) {
    for (int i = 0; i < jjtGetNumChildren(); ++i) {
      jjtGetChild(i).accept(visitor, data);
    }
    return data;
  }
}
