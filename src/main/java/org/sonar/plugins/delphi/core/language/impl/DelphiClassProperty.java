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

  private String readFunction;
  private String writeFunction;

  /**
   * Default ctor
   */
  public DelphiClassProperty() {
  }

  /**
   * Ctor
   *
   * @param name property name
   * @param type property type
   * @param visibility property visibility
   * @param read property getter
   * @param write property setter
   */
  public DelphiClassProperty(String name, String type, int visibility, String read,
      String write) {
    super(name, type, visibility);
    readFunction = read;
    writeFunction = write;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public String getReadFunction() {
    return readFunction;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public String getWriteFunction() {
    return writeFunction;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public boolean hasFunction(FunctionInterface function) {
    String name = function.getShortName();
    return name.equalsIgnoreCase(writeFunction) || name.equalsIgnoreCase(readFunction);
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void setReadFunction(String newFunction) {
    readFunction = newFunction;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void setWriteFunction(String newFunction) {
    writeFunction = newFunction;
  }

  @Override
  public String toString() {
    StringBuilder suffix = new StringBuilder();
    if (writeFunction != null) {
      suffix.append("@").append(writeFunction);
    }
    if (readFunction != null) {
      suffix.append("@").append(readFunction);
    }
    return super.toString() + suffix.toString();
  }

  @Override
  public boolean equals(Object o) {
    return o != null && toString().equals(o.toString());
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }
}
