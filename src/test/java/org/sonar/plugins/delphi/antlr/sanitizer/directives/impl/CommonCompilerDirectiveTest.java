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
package org.sonar.plugins.delphi.antlr.sanitizer.directives.impl;

import org.junit.Test;
import org.sonar.plugins.delphi.antlr.directives.CompilerDirectiveType;
import org.sonar.plugins.delphi.antlr.directives.impl.CommonCompilerDirective;

import static org.junit.Assert.assertEquals;

public class CommonCompilerDirectiveTest {

  private CommonCompilerDirective directive;

  @Test
  public void getNameTest() {
    directive = new CommonCompilerDirective("name", "item", 0, 0);
    assertEquals("name", directive.getName());

    directive = new CommonCompilerDirective("NewName", "item", 0, 0);
    assertEquals("NewName", directive.getName());
  }

  @Test
  public void getItemTest() {
    directive = new CommonCompilerDirective("name", "item", 0, 0);
    assertEquals("item", directive.getItem());

    directive = new CommonCompilerDirective("name", "NewItem", 0, 0);
    assertEquals("NewItem", directive.getItem());
  }

  @Test
  public void getFirstCharPositionTest() {
    directive = new CommonCompilerDirective("name", "item", 0, 0);
    assertEquals(0, directive.getFirstCharPosition());

    directive = new CommonCompilerDirective("name", "item", 256, 256);
    assertEquals(256, directive.getFirstCharPosition());
  }

  @Test
  public void getTypeTest() {
    directive = new CommonCompilerDirective("name", "item", 0, 0);
    assertEquals(CompilerDirectiveType.UNKNOWN, directive.getType());
  }

}
