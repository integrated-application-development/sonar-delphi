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
package org.sonar.plugins.delphi.core.language.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sonar.plugins.delphi.antlr.DelphiParser;
import org.sonar.plugins.delphi.core.language.ClassFieldInterface;
import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.ClassPropertyInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;

/**
 * DelphiLanguage language class.
 * 
 * @see ClassInterface.
 */
public class DelphiClass implements ClassInterface {

  private String name = null; // class name
  private String fileName = null; // class filename (with path)
  private int visibility = DelphiParser.PRIVATE; // class default visibility scope

  private List<ClassFieldInterface> fields = new ArrayList<ClassFieldInterface>();
  private List<ClassPropertyInterface> properties = new ArrayList<ClassPropertyInterface>();
  private Set<FunctionInterface> functions = new HashSet<FunctionInterface>();
  private Set<FunctionInterface> declarations = new HashSet<FunctionInterface>();
  private Set<ClassInterface> parents = new HashSet<ClassInterface>();
  private Set<ClassInterface> children = new HashSet<ClassInterface>();
  private String realName = null;

  /**
   * {@inheritDoc}
   */
  public DelphiClass(String newName) {
    name = newName;
    realName = newName;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o instanceof ClassInterface) {
      return name.equals(((ClassInterface) o).getName());
    }
    return toString().equals(o.toString());
  }

  @Override
  public int hashCode() {
    if (name == null) {
      return 0;
    }
    return name.hashCode();
  }

  /**
   * {@inheritDoc}
   */

  public int getAccessorCount() {
    int accessorsCount = 0;
    for (FunctionInterface function : functions) {
      if (function.isAccessor()) {
        accessorsCount += 1 + function.getOverloadsCount();
      }
    }
    return accessorsCount;
  }

  /**
   * {@inheritDoc}
   */

  public int getVisibility() {
    return visibility;
  }

  /**
   * {@inheritDoc}
   */

  public void addParent(ClassInterface parent) {
    parents.add(parent);
    parent.addChild(this);
  }

  /**
   * {@inheritDoc}
   */

  public ClassInterface[] getParents() {
    ClassInterface p[] = new ClassInterface[parents.size()];
    parents.toArray(p);
    return p;
  }

  /**
   * {@inheritDoc}
   */
  private int calculateDepth(ClassInterface cl) {
    int depth = 0;

    for (int i = 0; i < cl.getParents().length; ++i) {
      ClassInterface parent = cl.getParents()[i];
      int pd = calculateDepth(parent) + 1;
      if (pd > depth) {
        depth = pd;
      }
    }

    return depth;
  }

  /**
   * {@inheritDoc}
   */

  public int getDit() {
    return calculateDepth(this);
  }

  /**
   * {@inheritDoc}
   */

  public void setVisibility(int value) {
    visibility = value;
  }

  /**
   * {@inheritDoc}
   */

  public int getPublicApiCount() {
    int publicApiCount = 0;
    if (visibility == DelphiParser.PUBLIC) { // count class if public
      ++publicApiCount;
    }
    for (FunctionInterface func : functions) {
      if ( !func.isAccessor() && (func.getVisibility() == DelphiParser.PUBLIC || func.getVisibility() == DelphiParser.PUBLISHED)) {
        publicApiCount += 1 + func.getOverloadsCount();
      }
    }
    for (ClassFieldInterface field : fields) {
      if (field.getVisibility() == DelphiParser.PUBLIC || field.getVisibility() == DelphiParser.PUBLISHED) {
        ++publicApiCount;
      }
    }
    for (ClassPropertyInterface property : properties) {
      if (property.getVisibility() == DelphiParser.PUBLIC || property.getVisibility() == DelphiParser.PUBLISHED) {
        ++publicApiCount;
      }
    }

    return publicApiCount;
  }

  /**
   * {@inheritDoc}
   */

  public String getName() {
    return name;
  }

  /**
   * {@inheritDoc}
   */

  public void addField(ClassFieldInterface field) {
    fields.add(field);
  }

  /**
   * {@inheritDoc}
   */

  public ClassFieldInterface[] getFields() {
    return fields.toArray(new ClassFieldInterface[fields.size()]);
  }

  // calculating function complexity with its overloaded functions
  private int processFunctionComplexity(FunctionInterface func) {
    if (func.isAccessor()) {
      return 0;
    }
    int result = func.getComplexity();
    for (FunctionInterface over : func.getOverloadedFunctions()) {
      result += processFunctionComplexity(over);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */

  public int getComplexity() {
    int complexity = 0;
    for (FunctionInterface func : functions) {
      complexity += processFunctionComplexity(func);
    }
    return complexity;
  }

  /**
   * {@inheritDoc}
   */

  public FunctionInterface[] getFunctions() {
    return functions.toArray(new FunctionInterface[functions.size()]);
  }

  /**
   * {@inheritDoc}
   */

  public FunctionInterface[] getDeclarations() {
    return declarations.toArray(new FunctionInterface[declarations.size()]);
  }

  /**
   * {@inheritDoc}
   */

  public void addFunction(FunctionInterface func) {
    if (functions.contains(func)) {
      return; // function already registered in class
    }
    functions.add(func); // add a function to class
    func.setParentClass(this); // this class i a parent class of provided function
    if (func.isDeclaration()) {
      declarations.add(func); // add to declarations
    }
  }

  /**
   * {@inheritDoc}
   */

  public void addChild(ClassInterface child) {
    children.add(child);
  }

  /**
   * {@inheritDoc}
   */

  public ClassInterface[] getDescendants() {
    List<ClassInterface> allChildren = new ArrayList<ClassInterface>();
    for (ClassInterface child : children) {
      processChild((DelphiClass) child, allChildren);
    }
    return allChildren.toArray(new ClassInterface[allChildren.size()]);
  }

  /**
   * {@inheritDoc}
   */

  public ClassInterface[] getChildren() {
    return children.toArray(new ClassInterface[children.size()]);
  }

  private void processChild(DelphiClass parent, List<ClassInterface> list) {
    list.add(parent);
    for (ClassInterface child : parent.children) {
      processChild((DelphiClass) child, list);
    }
  }

  /**
   * {@inheritDoc}
   */

  public int getRfc() {
    int rfc = 0;
    Set<FunctionInterface> visited = new HashSet<FunctionInterface>();
    for (FunctionInterface func : functions) {
      if (func.isAccessor()) {
        continue;
      }
      rfc += analyseFunction(func, visited);
    }
    return rfc; // rfc = number of local methods + number of remote methods
  }

  /**
   * Analyses function for its function calls (how many other function call are in this function)
   * 
   * @param function
   *          Function to analyse
   * @param visited
   *          Set of already visited functions (to not repeat visited ones)
   * @return Number of functions called within this function
   */
  private int analyseFunction(FunctionInterface function, Set<FunctionInterface> visited) {

    int result = 1 + function.getOverloadsCount();
    for (FunctionInterface calledFunc : function.getCalledFunctions()) {
      if (visited.contains(calledFunc) || function.isAccessor()) {
        continue;
      }
      visited.add(calledFunc);
      result += analyseFunction(calledFunc, visited);
    }

    return result;
  }

  /**
   * {@inheritDoc}
   */

  public boolean hasFunction(FunctionInterface func) {
    boolean b1 = functions.contains(func);

    FunctionInterface foo1 = new DelphiFunction(func.getShortName());
    FunctionInterface foo2 = new DelphiFunction(name + "." + func.getShortName());

    boolean b2 = functions.contains(foo1);
    boolean b3 = functions.contains(foo2);

    return b1 || b2 || b3;
  }

  /**
   * {@inheritDoc}
   */

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  /**
   * {@inheritDoc}
   */

  public String getFileName() {
    return fileName;
  }

  @Override
  public String toString() {
    if (fileName != null) {
      return fileName + ":" + name;
    }
    return name;
  }

  /**
   * {@inheritDoc}
   */

  public String getShortName() {
    return name;
  }

  /**
   * {@inheritDoc}
   */

  public void addProperty(ClassPropertyInterface property) {
    properties.add(property);
  }

  /**
   * {@inheritDoc}
   */

  public ClassPropertyInterface[] getProperties() {
    return properties.toArray(new ClassPropertyInterface[properties.size()]);
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRealName() {
    return realName;
  }

  public void setRealName(String name) {
    realName = name;
  }

}
