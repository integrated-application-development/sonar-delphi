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

import org.sonar.plugins.delphi.antlr.DelphiParser;
import org.sonar.plugins.delphi.core.language.ClassFieldInterface;
import org.sonar.plugins.delphi.core.language.ClassInterface;

/**
 * DelphiLanguage language class field.
 * 
 * @see ClassFieldInterface.
 */
public class DelphiClassField extends DelphiArgument implements ClassFieldInterface {

  private ClassInterface parent = null;
  private int visibility = DelphiParser.PRIVATE;

  /**
   * ctor
   */
  public DelphiClassField() {
  }

  /**
   * ctor
   * 
   * @param name
   *          filed name
   * @param type
   *          filed type
   * @param fieldVisibility
   *          field visibility
   */
  public DelphiClassField(String name, String type, int fieldVisibility) {
    super(name, type);
    visibility = fieldVisibility;
  }

  public void setParent(ClassInterface cl) {
    parent = cl;
  }

  public ClassInterface getParent() {
    return parent;
  }

  public int getVisibility() {
    return visibility;
  }

  public void setVisibility(int value) {
    visibility = value;
  }

  @Override
  public String toString() {
    if (parent == null) {
      return name;
    }
    return parent.getName() + "." + name;
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
