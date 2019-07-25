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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.UnitInterface;

/**
 * Class for unit (usually one delphi source file), containing list of classes and functions in that
 * particular unit.
 *
 * @see UnitInterface
 */
public class DelphiUnit implements UnitInterface {

  private File file;
  private URI uri;
  private String name = "UNKNOWN_UNIT";
  private String realName = "UNKNOWN_UNIT";
  private final Set<String> includes = new HashSet<>();
  private final List<ClassInterface> classes = new ArrayList<>();
  private final List<FunctionInterface> functions = new ArrayList<>();
  private int line = 1;

  /** Default ctor */
  public DelphiUnit() {}

  /**
   * Name ctor
   *
   * @param unitName Unit name
   */
  public DelphiUnit(String unitName) {
    name = unitName;
    realName = unitName;
  }

  /** {@inheritDoc} */
  @Override
  public String getFileName() {
    return file.getName();
  }

  /** @return real unit name (with camelcase) */
  public String getRealName() {
    return realName;
  }

  /**
   * sets the real name of a unit
   *
   * @param name real name, not converted to lowercase
   */
  public void setRealName(String name) {
    realName = name;
  }

  /** {@inheritDoc} */
  @Override
  public void setPath(String path) {
    file = new File(path);
    uri = file.toPath().toUri();
  }

  /** {@inheritDoc} */
  @Override
  public String getPath() {
    return file.getAbsolutePath();
  }

  /** {@inheritDoc} */
  @Override
  public URI getUri() {
    return uri;
  }

  /** {@inheritDoc} */
  @Override
  public void setName(String unitName) {
    name = unitName;
  }

  /** {@inheritDoc} */
  @Override
  public String getName() {
    return name;
  }

  /** {@inheritDoc} */
  @Override
  public void addClass(ClassInterface cl) {
    classes.add(cl);
  }

  /** {@inheritDoc} */
  @Override
  public ClassInterface[] getClasses() {
    return classes.toArray(new ClassInterface[0]);
  }

  /** {@inheritDoc} */
  @Override
  public void addFunction(FunctionInterface func) {
    functions.add(func);
  }

  /** {@inheritDoc} */
  @Override
  public FunctionInterface[] getFunctions() {
    return functions.toArray(new FunctionInterface[0]);
  }

  /** {@inheritDoc} */
  @Override
  public void addIncludes(String includeName) {
    includes.add(includeName);
  }

  /** {@inheritDoc} */
  @Override
  public String[] getIncludes() {
    return includes.toArray(new String[0]);
  }

  /** {@inheritDoc} */
  @Override
  public ClassInterface findClass(String classShortName) {
    for (ClassInterface cl : classes) {
      if (cl.getShortName().equals(classShortName)) {
        return cl;
      }
    }
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public FunctionInterface findFunction(String functionName) {
    for (FunctionInterface func : functions) {
      if (func.getName().equals(functionName)) {
        return func;
      }
    }

    return null;
  }

  @Override
  public String toString() {
    String fileName = "no file";
    if (file != null) {
      fileName = file.getAbsolutePath();
    }
    return name + "(" + fileName + ")";
  }

  @Override
  public boolean equals(Object o) {
    return o != null && toString().equals(o.toString());
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isIncluding(UnitInterface unit) {
    for (String includeName : includes) {
      if (unit.getName().equals(includeName)) {
        return true;
      }
    }

    return false;
  }

  /** {@inheritDoc} */
  @Override
  public void setLine(int lineNumber) {
    line = lineNumber;
  }

  /** {@inheritDoc} */
  @Override
  public int getLine() {
    return line;
  }

  /** {@inheritDoc} */
  @Override
  public FunctionInterface[] getAllFunctions() {
    Set<FunctionInterface> result = new HashSet<>(functions);

    for (ClassInterface clazz : classes) {
      Collections.addAll(result, clazz.getFunctions());
    }

    return result.toArray(new FunctionInterface[0]);
  }

  /** {@inheritDoc} */
  @Override
  public Set<UnitInterface> getIncludedUnits(Set<UnitInterface> allUnits) {
    Set<UnitInterface> result = new HashSet<>();
    if (allUnits == null) {
      return result;
    }
    for (UnitInterface unit : allUnits) {
      for (String unitName : includes) {
        if (unit.getName().equalsIgnoreCase(unitName)) {
          result.add(unit);
        }
      }
    }

    return result;
  }
}
