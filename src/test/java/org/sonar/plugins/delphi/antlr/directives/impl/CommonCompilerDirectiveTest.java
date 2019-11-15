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
package org.sonar.plugins.delphi.antlr.directives.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.sonar.plugins.delphi.antlr.directives.CompilerDirectiveType;

public class CommonCompilerDirectiveTest {

  private CommonCompilerDirective directive;

  @Test
  public void testGetName() {
    directive = new CommonCompilerDirective("name", "item", 0, 0);
    assertThat(directive.getName()).isEqualTo("name");

    directive = new CommonCompilerDirective("NewName", "item", 0, 0);
    assertThat(directive.getName()).isEqualTo("NewName");
  }

  @Test
  public void testGetItem() {
    directive = new CommonCompilerDirective("name", "item", 0, 0);
    assertThat(directive.getItem()).isEqualTo("item");

    directive = new CommonCompilerDirective("name", "NewItem", 0, 0);
    assertThat(directive.getItem()).isEqualTo("NewItem");
  }

  @Test
  public void testGetFirstCharPosition() {
    directive = new CommonCompilerDirective("name", "item", 0, 0);
    assertThat(directive.getFirstCharPosition()).isEqualTo(0);

    directive = new CommonCompilerDirective("name", "item", 256, 256);
    assertThat(directive.getFirstCharPosition()).isEqualTo(256);
  }

  @Test
  public void testGetType() {
    directive = new CommonCompilerDirective("name", "item", 0, 0);
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.UNKNOWN);
  }

  @Test
  public void testGetLength() {
    directive = new CommonCompilerDirective("name", "item", 0, 30);
    assertThat(directive.getLength()).isEqualTo(30);
  }
}
