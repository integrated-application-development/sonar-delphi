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

import org.sonar.plugins.delphi.antlr.DelphiParser;
import org.sonar.plugins.delphi.core.language.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DelphiLanguage language function definition.
 * 
 * @see FunctionInterface
 */
public class DelphiFunction implements FunctionInterface {

  private int complexity = 0;
  private int overloads = -1;
  private int line = -1;
  private int bodyLine = -1;
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
  public DelphiFunction() {
    name = UNKNOWN_FUNCTION_NAME + (unknownFunctionCounter++);
    longName = name + "()";
    realName = name;
  }

  /**
   * Ctor, creates function with name
   * 
   * @param functionName Specified name
   */
  public DelphiFunction(String functionName) {
    setName(functionName);
    setRealName(functionName);
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public boolean isAccessor() {
    return isAccessor;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void setDeclaration(boolean value) {
    isDeclaration = value;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public int getVisibility() {
    return visibility;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void setVisibility(int value) {
    visibility = value;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public boolean isDeclaration() {
    return isDeclaration;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void increaseComplexity() {
    complexity++;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public int getComplexity() {
    return complexity;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void setComplexity(int complexity) {
    this.complexity = complexity;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public int getLine() {
    return line;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void setLine(int line) {
    this.line = line;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public int getColumn() {
    return column;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void setColumn(int column) {
    this.column = column;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public final String getName() {
    return name;
  }

  /**
   * {@inheritDoc}
   */

  @Override
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

  @Override
  public void addStatement(StatementInterface st) {
    statements.add(st);
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public List<StatementInterface> getStatements() {
    return statements;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public FunctionInterface[] getCalledFunctions() {
    return called.toArray(new FunctionInterface[called.size()]);
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void addCalledFunction(FunctionInterface function) {
    if (function != null && !function.equals(this)) {
      called.add(function);
    }
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public boolean isCalling(FunctionInterface calledFunc) {
    return called.contains(calledFunc);
  }

  /**
   * {@inheritDoc}
   */

  @Override
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

  @Override
  public boolean hasBody() {
    return complexity != 0 && !isDeclaration();
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public boolean isGlobal() {
    return parentClass == null;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void setParentClass(ClassInterface parent) {
    parentClass = parent;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public ClassInterface getParentClass() {
    return parentClass;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public ArgumentInterface[] getArguments() {
    return args.toArray(new ArgumentInterface[args.size()]);
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void addArgument(ArgumentInterface arg) {
    args.add(arg);
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void setLongName(String functionLongName) {
    longName = functionLongName;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public String getLongName() {
    return longName;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public int getOverloadsCount() {
    if (overloads < 0) {
      return 0;
    }
    return overloads;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void increaseFunctionOverload() {
    ++overloads;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void addOverloadFunction(FunctionInterface func) {
    overFunc.add(func);
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public FunctionInterface[] getOverloadedFunctions() {
    return overFunc.toArray(new FunctionInterface[overFunc.size()]);
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public UnitInterface getUnit() {
    return parentUnit;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void setUnit(UnitInterface functionUnit) {
    parentUnit = functionUnit;
  }

  @Override
  public void setVirtual(boolean value) {
    virtual = value;
  }

  @Override
  public boolean isVirtual() {
    return virtual;
  }

  @Override
  public void setMessage(boolean value) {
    message = value;
  }

  @Override
  public boolean isMessage() {
    return message;
  }

  @Override
  public final String getRealName() {
    return realName;
  }

  @Override
  public final void setRealName(String name) {
    realName = name;
  }

  @Override
  public int getBodyLine() {
    return bodyLine;
  }

  @Override
  public void setBodyLine(int line) {
    bodyLine = line;
  }
}
