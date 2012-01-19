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
import org.sonar.plugins.delphi.core.language.ArgumentInterface;
import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.StatementInterface;
import org.sonar.plugins.delphi.core.language.UnitInterface;

/**
 * DelphiLanguage language function definition.
 * 
 * @see FunctionInterface
 */
public class DelphiFunction implements FunctionInterface {

  private int complexity = 0;
  private int overloads = -1;
  private int line = -1;
  private int column = -1;
  private int visibility = DelphiParser.PRIVATE;
  private String name = null;
  private String realName = null;
  private String longName = null;
  private boolean virtual = false;
  private boolean isAccessor = false;
  private boolean isDeclaration = false;
  private ClassInterface parentClass = null;
  private List<StatementInterface> statements = new ArrayList<StatementInterface>();
  private Set<FunctionInterface> called = new HashSet<FunctionInterface>();
  private List<ArgumentInterface> args = new ArrayList<ArgumentInterface>();
  private List<FunctionInterface> overFunc = new ArrayList<FunctionInterface>();
  private UnitInterface parentUnit = null;
  private boolean message = false;

  private static final String UNKNOWN_FUNCTION_NAME = "UnknownFunction_";

  private static int unknownFunctionCounter = 0;

  /**
   * Ctor, creates function with default name
   */
  public DelphiFunction() { // creates default name
    name = UNKNOWN_FUNCTION_NAME + (unknownFunctionCounter++);
    longName = name + "()";
    realName = name;
  }

  /**
   * Ctor, creates function with name
   * 
   * @param functionName
   *          Specified name
   */
  public DelphiFunction(String functionName) {
    setName(functionName);
    setRealName(functionName);
  }

  /**
   * {@inheritDoc}
   */

  public boolean isAccessor() {
    return isAccessor;
  }

  /**
   * {@inheritDoc}
   */

  public void setDeclaration(boolean value) {
    isDeclaration = value;
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

  public void setVisibility(int value) {
    visibility = value;
  }

  /**
   * {@inheritDoc}
   */

  public boolean isDeclaration() {
    return isDeclaration;
  }

  /**
   * {@inheritDoc}
   */

  public void increaseComplexity() {
    complexity++;
  }

  /**
   * {@inheritDoc}
   */

  public int getComplexity() {
    return complexity;
  }

  /**
   * {@inheritDoc}
   */

  public void setComplexity(int complexity) {
    this.complexity = complexity;
  }

  /**
   * {@inheritDoc}
   */

  public int getLine() {
    return line;
  }

  /**
   * {@inheritDoc}
   */

  public void setLine(int line) {
    this.line = line;
  }

  /**
   * {@inheritDoc}
   */

  public int getColumn() {
    return column;
  }

  /**
   * {@inheritDoc}
   */

  public void setColumn(int column) {
    this.column = column;
  }

  /**
   * {@inheritDoc}
   */

  public final String getName() {
    return name;
  }

  /**
   * {@inheritDoc}
   */

  public final void setName(String name) {
    this.name = name;
    if (longName == null) {
      longName = name;
    }
    int dot = name.lastIndexOf('.');
    if (dot != -1) {
      boolean b1 = name.startsWith("get", dot + 1);
      boolean b2 = name.startsWith("set", dot + 1);
      isAccessor = b1 || b2;
    }
  }

  /**
   * {@inheritDoc}
   */

  public void addStatement(StatementInterface st) {
    statements.add(st);
  }

  /**
   * {@inheritDoc}
   */

  public List<StatementInterface> getStatements() {
    return statements;
  }

  /**
   * {@inheritDoc}
   */

  public FunctionInterface[] getCalledFunctions() {
    return called.toArray(new FunctionInterface[called.size()]);
  }

  /**
   * {@inheritDoc}
   */

  public void addCalledFunction(FunctionInterface function) {
    if (function != null && !function.equals(this)) {
      called.add(function);
    }
  }

  /**
   * {@inheritDoc}
   */

  public boolean isCalling(FunctionInterface calledFunc) {
    return called.contains(calledFunc);
  }

  /**
   * {@inheritDoc}
   */

  public String getShortName() {
    int dot = name.lastIndexOf('.');
    if (dot != -1) {
      return name.substring(dot + 1, name.length());
    }
    return name;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    return toString().equalsIgnoreCase(o.toString());
  }

  /**
   * {@inheritDoc}
   */

  public boolean hasBody() {
    return complexity != 0 && !isDeclaration();
  }

  /**
   * {@inheritDoc}
   */

  public boolean isGlobal() {
    return parentClass == null;
  }

  /**
   * {@inheritDoc}
   */

  public void setParentClass(ClassInterface parent) {
    parentClass = parent;
  }

  /**
   * {@inheritDoc}
   */

  public ClassInterface getParentClass() {
    return parentClass;
  }

  /**
   * {@inheritDoc}
   */

  public ArgumentInterface[] getArguments() {
    return args.toArray(new ArgumentInterface[args.size()]);
  }

  /**
   * {@inheritDoc}
   */

  public void addArgument(ArgumentInterface arg) {
    args.add(arg);
  }

  /**
   * {@inheritDoc}
   */

  public void setLongName(String functionLongName) {
    longName = functionLongName;
  }

  /**
   * {@inheritDoc}
   */

  public String getLongName() {
    return longName;
  }

  /**
   * {@inheritDoc}
   */

  public int getOverloadsCount() {
    if (overloads < 0) {
      return 0;
    }
    return overloads;
  }

  /**
   * {@inheritDoc}
   */

  public void increaseFunctionOverload() {
    ++overloads;
  }

  /**
   * {@inheritDoc}
   */

  public void addOverloadFunction(FunctionInterface func) {
    overFunc.add(func);
  }

  /**
   * {@inheritDoc}
   */

  public FunctionInterface[] getOverloadedFunctions() {
    return overFunc.toArray(new FunctionInterface[overFunc.size()]);
  }

  /**
   * {@inheritDoc}
   */

  public UnitInterface getUnit() {
    return parentUnit;
  }

  /**
   * {@inheritDoc}
   */

  public void setUnit(UnitInterface functionUnit) {
    parentUnit = functionUnit;
  }

  public void setVirtual(boolean value) {
    virtual = value;
  }

  public boolean isVirtual() {
    return virtual;
  }

  public void setMessage(boolean value) {
    message = value;
  }

  public boolean isMessage() {
    return message;
  }

  public final String getRealName() {
    return realName;
  }

  public final void setRealName(String name) {
    realName = name;
  }

}
