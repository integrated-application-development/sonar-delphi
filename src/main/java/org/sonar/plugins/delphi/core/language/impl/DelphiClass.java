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
package org.sonar.plugins.delphi.core.language.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sonar.plugins.delphi.antlr.generated.DelphiParser;
import org.sonar.plugins.delphi.core.language.ClassFieldInterface;
import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.ClassPropertyInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;

/**
 * DelphiLanguage language class.
 *
 * @see ClassInterface
 */
public final class DelphiClass implements ClassInterface {

  private static final String UNKNOWN_CLASS_NAME = "UnknownClass";

  private static final String UNKNOWN_FILE_NAME = "UnknownUnit";

  private String name;
  private String fileName;
  private int visibility = DelphiParser.PRIVATE;

  private final List<ClassFieldInterface> fields = new ArrayList<>();
  private final List<ClassPropertyInterface> properties = new ArrayList<>();
  private final Set<FunctionInterface> functions = new HashSet<>();
  private final Set<FunctionInterface> declarations = new HashSet<>();
  private final Set<ClassInterface> parents = new HashSet<>();
  private final Set<ClassInterface> children = new HashSet<>();
  private String realName;

  /** {@inheritDoc} */
  public DelphiClass(String newName) {
    if (newName == null) {
      name = UNKNOWN_CLASS_NAME;
      realName = UNKNOWN_CLASS_NAME;
    } else {
      name = newName;
      realName = newName;
    }
    fileName = UNKNOWN_FILE_NAME;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (this.getClass() == o.getClass()) {
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

  /** {@inheritDoc} */
  @Override
  public int getAccessorCount() {
    int accessorsCount = 0;
    for (FunctionInterface function : functions) {
      if (function.isAccessor()) {
        accessorsCount += 1 + function.getOverloadsCount();
      }
    }
    return accessorsCount;
  }

  /** {@inheritDoc} */
  @Override
  public int getVisibility() {
    return visibility;
  }

  /** {@inheritDoc} */
  @Override
  public void addParent(ClassInterface parent) {
    if (parent != null && !parent.equals(this)) {
      parents.add(parent);
      parent.addChild(this);
    }
  }

  /** {@inheritDoc} */
  @Override
  public ClassInterface[] getParents() {
    ClassInterface[] p = new ClassInterface[parents.size()];
    parents.toArray(p);
    return p;
  }

  /** {@inheritDoc} */
  @Override
  public void setVisibility(int value) {
    visibility = value;
  }

  /** {@inheritDoc} */
  @Override
  public int getPublicApiCount() {
    int count = getPublicFunctionCount() + getPublicFieldCount() + getPublicPropertyCount();
    if (visibility == DelphiParser.PUBLIC) {
      ++count;
    }
    return count;
  }

  private int getPublicFunctionCount() {
    int count = 0;
    for (FunctionInterface func : functions) {
      if ((func.getVisibility() == DelphiParser.PUBLIC
              || func.getVisibility() == DelphiParser.PUBLISHED)
          && !func.isAccessor()) {
        count += 1 + func.getOverloadsCount();
      }
    }
    return count;
  }

  private int getPublicFieldCount() {
    int count = 0;
    for (ClassFieldInterface field : fields) {
      if (field.getVisibility() == DelphiParser.PUBLIC
          || field.getVisibility() == DelphiParser.PUBLISHED) {
        ++count;
      }
    }
    return count;
  }

  private int getPublicPropertyCount() {
    int count = 0;
    for (ClassPropertyInterface property : properties) {
      if (property.getVisibility() == DelphiParser.PUBLIC
          || property.getVisibility() == DelphiParser.PUBLISHED) {
        ++count;
      }
    }
    return count;
  }

  /** {@inheritDoc} */
  @Override
  public String getName() {
    return name;
  }

  /** {@inheritDoc} */
  @Override
  public void addField(ClassFieldInterface field) {
    fields.add(field);
  }

  /** {@inheritDoc} */
  @Override
  public ClassFieldInterface[] getFields() {
    return fields.toArray(new ClassFieldInterface[0]);
  }

  /**
   * Calculating function Cyclomatic Complexity with its overloaded functions.
   *
   * @param func The function
   * @return Cyclomatic Complexity
   */
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

  /** {@inheritDoc} */
  @Override
  public int getComplexity() {
    int complexity = 0;
    for (FunctionInterface func : functions) {
      complexity += processFunctionComplexity(func);
    }
    return complexity;
  }

  /** {@inheritDoc} */
  @Override
  public FunctionInterface[] getFunctions() {
    return functions.toArray(new FunctionInterface[0]);
  }

  /** {@inheritDoc} */
  @Override
  public FunctionInterface[] getDeclarations() {
    return declarations.toArray(new FunctionInterface[0]);
  }

  /** {@inheritDoc} */
  @Override
  public void addFunction(FunctionInterface func) {
    if (functions.contains(func)) {
      return;
    }
    functions.add(func);
    func.setParentClass(this);
    if (func.isDeclaration()) {
      declarations.add(func);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void addChild(ClassInterface child) {
    if (child != null && !this.equals(child)) {
      children.add(child);
    }
  }

  /** {@inheritDoc} */
  @Override
  public ClassInterface[] getDescendants() {
    List<ClassInterface> allChildren = new ArrayList<>();
    for (ClassInterface child : children) {
      processChild((DelphiClass) child, allChildren);
    }
    return allChildren.toArray(new ClassInterface[0]);
  }

  /** {@inheritDoc} */
  @Override
  public ClassInterface[] getChildren() {
    return children.toArray(new ClassInterface[0]);
  }

  private void processChild(DelphiClass parent, List<ClassInterface> list) {
    list.add(parent);
    for (ClassInterface child : parent.children) {
      processChild((DelphiClass) child, list);
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean hasFunction(FunctionInterface func) {
    return functions
        .parallelStream()
        .anyMatch(
            f ->
                f.getShortName().equalsIgnoreCase(func.getShortName())
                    && Arrays.equals(f.getArguments(), func.getArguments()));
  }

  /** {@inheritDoc} */
  @Override
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  /** {@inheritDoc} */
  @Override
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

  /** {@inheritDoc} */
  @Override
  public String getShortName() {
    return getName();
  }

  /** {@inheritDoc} */
  @Override
  public void addProperty(ClassPropertyInterface property) {
    properties.add(property);
  }

  /** {@inheritDoc} */
  @Override
  public ClassPropertyInterface[] getProperties() {
    return properties.toArray(new ClassPropertyInterface[0]);
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getRealName() {
    return realName;
  }

  @Override
  public void setRealName(String name) {
    realName = name;
  }
}
