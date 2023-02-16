/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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
package au.com.integradev.delphi.antlr.ast;

import au.com.integradev.delphi.antlr.DelphiLexer;
import au.com.integradev.delphi.antlr.ast.node.CommonDelphiNodeImpl;
import au.com.integradev.delphi.antlr.ast.node.IdentifierNodeImpl;
import au.com.integradev.delphi.antlr.ast.token.DelphiToken;
import com.google.common.base.Preconditions;
import java.lang.reflect.Constructor;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.BaseTreeAdaptor;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.MutableDelphiNode;

public class DelphiTreeAdaptor extends BaseTreeAdaptor {

  @Override
  public Object create(Token token) {
    if (token != null && token.getType() == DelphiLexer.TkIdentifier) {
      return new IdentifierNodeImpl(token);
    }
    return new CommonDelphiNodeImpl(token);
  }

  @Override
  public Object create(int tokenType, String text) {
    return create(createToken(tokenType, text));
  }

  @Override
  public Token createToken(int tokenType, String text) {
    return new CommonToken(tokenType, text);
  }

  @Override
  public Token createToken(Token fromToken) {
    return new CommonToken(fromToken);
  }

  @Override
  public void setTokenBoundaries(Object node, Token startToken, Token stopToken) {
    if (node != null) {
      ((MutableDelphiNode) node).jjtSetFirstToken(new DelphiToken(startToken));
      ((MutableDelphiNode) node).jjtSetLastToken(new DelphiToken(stopToken));
    }
  }

  @Override
  public Object errorNode(TokenStream input, Token start, Token stop, RecognitionException e) {
    return null;
  }

  @Override
  public boolean isNil(Object node) {
    return getToken(node) == null;
  }

  @Override
  public Object dupTree(Object node) {
    return dupTree(node, null);
  }

  @Override
  public Object dupTree(Object node, Object parent) {
    if (node == null) {
      return null;
    } else {
      Object newTree = dupNode(node);
      setChildIndex(newTree, getChildIndex(node));
      setParent(newTree, parent);

      int count = getChildCount(node);

      for (int i = 0; i < count; ++i) {
        Object child = getChild(node, i);
        Object newSubTree = dupTree(child, node);
        addChild(newTree, newSubTree);
      }

      return newTree;
    }
  }

  @Override
  public Object dupNode(Object node) {
    try {
      Constructor<?> constructor = node.getClass().getConstructor(Token.class);
      MutableDelphiNode dupNode = (MutableDelphiNode) constructor.newInstance(getToken(node));
      dupNode.jjtSetFirstToken(getFirstToken(node));
      dupNode.jjtSetLastToken(getLastToken(node));
      return dupNode;
    } catch (Exception e) {
      throw new AssertionError(
          String.format(
              "%s must have an accessible constructor(Token)", node.getClass().getSimpleName()),
          e);
    }
  }

  @Override
  public void addChild(Object node, Object child) {
    if (node != null && child != null) {
      ((DelphiNode) node).jjtAddChild((DelphiNode) child);
    }
  }

  @Override
  public Object becomeRoot(Object newRoot, Object oldRoot) {
    if (oldRoot != null) {
      if (isNil(newRoot)) {
        int count = getChildCount(newRoot);

        if (count == 1) {
          newRoot = getChild(newRoot, 0);
        } else {
          Preconditions.checkState(count <= 1, "Multiple roots are not allowed.");
        }
      }

      addChild(newRoot, oldRoot);
    }
    return newRoot;
  }

  @Override
  public Object becomeRoot(Token newRoot, Object oldRoot) {
    return becomeRoot(create(newRoot), oldRoot);
  }

  @Override
  public Object rulePostProcessing(Object root) {
    Object result = root;
    if (result != null && isNil(result)) {
      if (getChildCount(result) == 0) {
        result = null;
      } else if (getChildCount(result) == 1) {
        result = getChild(result, 0);
        setParent(result, null);
        setChildIndex(result, -1);
      }
    }

    return result;
  }

  @Override
  public Token getToken(Object node) {
    return ((DelphiNode) node).getToken().getAntlrToken();
  }

  @Override
  public int getType(Object node) {
    return ((DelphiNode) node).jjtGetId();
  }

  @Override
  public Object getChild(Object node, int index) {
    return ((DelphiNode) node).jjtGetChild(index);
  }

  @Override
  public int getChildCount(Object node) {
    return ((DelphiNode) node).jjtGetNumChildren();
  }

  @Override
  public int getChildIndex(Object node) {
    return ((DelphiNode) node).jjtGetChildIndex();
  }

  @Override
  public void setChildIndex(Object node, int index) {
    ((DelphiNode) node).jjtSetChildIndex(index);
  }

  @Override
  public Object getParent(Object node) {
    return ((DelphiNode) node).jjtGetParent();
  }

  @Override
  public void setParent(Object node, Object parent) {
    ((MutableDelphiNode) node).jjtSetParent((DelphiNode) parent);
  }

  @Override
  public int getTokenStartIndex(Object node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getTokenStopIndex(Object node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object deleteChild(Object t, int i) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void replaceChildren(Object parent, int startChildIndex, int stopChildIndex, Object node) {
    throw new UnsupportedOperationException();
  }

  private DelphiToken getFirstToken(Object node) {
    return ((DelphiNode) node).jjtGetFirstToken();
  }

  private DelphiToken getLastToken(Object node) {
    return ((DelphiNode) node).jjtGetLastToken();
  }
}
