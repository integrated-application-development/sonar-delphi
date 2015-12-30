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

import org.sonar.plugins.delphi.core.language.ClassPropertyInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;

/**
 * DelphiLanguage class property class
 * 
 * @see ClassPropertyInterface
 */
public class DelphiClassProperty extends DelphiClassField implements ClassPropertyInterface {

  FunctionInterface readFunction = null;
  FunctionInterface writeFunction = null;

  /**
   * Default ctor
   */
  public DelphiClassProperty() {
  }

  /**
   * Ctor
   * @param name property name
   * @param type property type
   * @param visibility property visibility
   * @param read property getter
   * @param write property setter
   */
  public DelphiClassProperty(String name, String type, int visibility, FunctionInterface read, FunctionInterface write) {
    super(name, type, visibility);
    readFunction = read;
    writeFunction = write;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public FunctionInterface getReadFunction() {
    return readFunction;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public FunctionInterface getWriteFunction() {
    return writeFunction;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public boolean hasFunction(FunctionInterface function) {
    FunctionInterface func = new DelphiFunction(function.getShortName());
    boolean b1 = false;
    boolean b2 = false;
    if (writeFunction != null) {
      b1 = writeFunction.equals(func);
    }
    if (readFunction != null) {
      b2 = readFunction.equals(func);
    }
    return b1 || b2;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void setReadFunction(FunctionInterface newFunction) {
    readFunction = newFunction;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void setWriteFunction(FunctionInterface newFunction) {
    writeFunction = newFunction;
  }

  @Override
  public String toString() {
    StringBuilder suffix = new StringBuilder();
    if (writeFunction != null) {
      suffix.append("@" + writeFunction.toString());
    }
    if (readFunction != null) {
      suffix.append("@" + readFunction.toString());
    }
    return super.toString() + suffix.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    return toString().equals(o.toString());
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }
}
