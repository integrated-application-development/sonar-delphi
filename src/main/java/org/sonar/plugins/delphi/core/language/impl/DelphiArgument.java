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

import org.sonar.plugins.delphi.core.language.ArgumentInterface;

/**
 * Delphi language function argument (parameter)
 */
public class DelphiArgument implements ArgumentInterface {

  protected String name;
  protected String type;

  /**
   * Default ctor, creates argument with default name and type
   */
  public DelphiArgument() {
    name = "UNKNOWN_ARGUMENT";
    type = "UNKNOWN_TYPE";
  }

  /**
   * Creates an argument with provided name and type
   * 
   * @param argName Argument name
   * @param argType Argument type
   */
  public DelphiArgument(String argName, String argType) {
    name = argName.toLowerCase();
    type = argType.toLowerCase();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String value) {
    name = value;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public void setType(String value) {
    type = value;
  }

  @Override
  public String toString() {
    return name + ":" + type;
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
