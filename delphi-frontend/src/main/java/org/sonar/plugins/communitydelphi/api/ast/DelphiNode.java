/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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
package org.sonar.plugins.communitydelphi.api.ast;

import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import java.util.List;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;

public interface DelphiNode extends Node {
  DelphiToken getToken();

  DelphiToken getFirstToken();

  DelphiToken getLastToken();

  DelphiNode getParent();

  int getChildIndex();

  List<DelphiNode> getChildren();

  DelphiNode getChild(int index);

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
  <T> T childrenAccept(DelphiParserVisitor<T> visitor, T data);
}
