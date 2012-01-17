/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
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
package org.sonar.plugins.delphi.antlr.ast;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.sonar.plugins.delphi.core.language.ClassFieldInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;

/**
 * Class used for LCOM4 parsing, holds a reference to object (it may hold a function or a class field). Tag is used while searching for
 * related subtrees.
 */
public class DelphiLCOMNode extends CommonTree {

  private int tag = 0;
  private Object ref = null;
  private List<DelphiLCOMNode> children = null;

  /**
   * Construtor, reference will hold a function
   * 
   * @param function
   *          Function to hold
   */
  public DelphiLCOMNode(FunctionInterface function) {
    ref = function;
  }

  /**
   * Construtor, reference will hold an class field
   * 
   * @param field
   *          Field to hold
   */
  public DelphiLCOMNode(ClassFieldInterface field) {
    ref = field;
  }

  /**
   * Set LOC4 value
   * 
   * @param value
   *          Value to set
   */
  public void setTag(int value) {
    tag = value;
  }

  /**
   * Gets LOC4 value
   * 
   * @return LOC4 value
   */
  public int getLOC4() {
    return tag;
  }

  /**
   * Gets the reference
   * 
   * @return Reference to function or class field
   */
  public Object getReference() {
    return ref;
  }

  /**
   * adds child node to current node
   * 
   * @param child
   *          child node
   */
  public void addChild(DelphiLCOMNode child) {
    if (child == null) {
      return;
    }
    if (children == null) {
      children = new ArrayList<DelphiLCOMNode>();
    }
    children.add(child);
    child.setParent(this);
  }

  @Override
  public DelphiLCOMNode getChild(int index) {
    if (children == null || index < 0 || index > children.size()) {
      return null;
    }
    return children.get(index);
  }

  @Override
  public int getChildCount() {
    if (children == null) {
      return 0;
    }
    return children.size();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    return ref.toString().equals(o.toString());
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public String toString() {
    return ref.toString();
  }

}
