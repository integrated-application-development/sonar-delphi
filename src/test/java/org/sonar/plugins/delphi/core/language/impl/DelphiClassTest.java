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

import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.core.language.ClassFieldInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;

import static org.junit.Assert.assertEquals;

public class DelphiClassTest {

  DelphiClass ancestor = null;
  DelphiClass parent = null;
  DelphiClass child = null;

  @Before
  public void init() {
    ancestor = new DelphiClass("ancestor");
    parent = new DelphiClass("parent");
    child = new DelphiClass("child");
    parent.addParent(ancestor);
    child.addParent(parent);
  }

  @Test
  public void getFileNameTest() {
    parent.setFileName("MyFile.pas");
    assertEquals("MyFile.pas", parent.getFileName());
  }

  @Test
  public void getVisibilityTest() {
    assertEquals(DelphiLexer.PRIVATE, parent.getVisibility());
    parent.setVisibility(DelphiLexer.PUBLIC);
    assertEquals(DelphiLexer.PUBLIC, parent.getVisibility());
  }

  @Test
  public void getPublicApiCountTest() {
    FunctionInterface func = new DelphiFunction("public");
    ClassFieldInterface field = new DelphiClassField();
    field.setVisibility(DelphiLexer.PUBLIC);
    func.setVisibility(DelphiLexer.PUBLIC);
    assertEquals(0, parent.getPublicApiCount());
    parent.addFunction(func);
    parent.addField(field);
    assertEquals(2, parent.getPublicApiCount());
    func.setVisibility(DelphiLexer.PRIVATE);
    parent.setVisibility(DelphiLexer.PUBLIC);
    assertEquals(2, parent.getPublicApiCount());
  }

  @Test
  public void getNameTest() {
    assertEquals("parent", parent.getName());
    assertEquals("parent", parent.getShortName());
  }

  @Test
  public void getComplexityTest() {
    FunctionInterface func = new DelphiFunction("parent.foo");
    FunctionInterface func2 = new DelphiFunction("parent.setBar");
    func.setComplexity(2);
    func2.setComplexity(1);
    assertEquals(0, parent.getComplexity());
    parent.addFunction(func);
    assertEquals(2, parent.getComplexity());
    parent.addFunction(func2);
    assertEquals(2, parent.getComplexity());
  }

  @Test
  public void hasFunctionTest() {
    FunctionInterface foo = new DelphiFunction("foo");
    FunctionInterface bar = new DelphiFunction("bar");

    assertEquals(false, ancestor.hasFunction(foo));
    ancestor.addFunction(foo);
    assertEquals(true, ancestor.hasFunction(foo));

    assertEquals(false, ancestor.hasFunction(bar));
    ancestor.addFunction(bar);
    assertEquals(true, ancestor.hasFunction(bar));

    assertEquals(false, ancestor.hasFunction(new DelphiFunction("foobar")));
    ancestor.addFunction(new DelphiFunction("foobar"));
    assertEquals(true, ancestor.hasFunction(new DelphiFunction("foobar")));

    assertEquals(true, ancestor.hasFunction(new DelphiFunction("ancestor.foobar")));
    assertEquals(true, ancestor.hasFunction(new DelphiFunction("ancestor.foo")));
    assertEquals(true, ancestor.hasFunction(new DelphiFunction("ancestor.bar")));
  }

  @Test
  public void getAccessorsCountTest() {
    ancestor.addFunction(new DelphiFunction("blah"));
    assertEquals(0, ancestor.getAccessorCount());
    ancestor.addFunction(new DelphiFunction("ancestor.setfield"));
    assertEquals(1, ancestor.getAccessorCount());
    ancestor.addFunction(new DelphiFunction("ancestor.getfield"));
    assertEquals(2, ancestor.getAccessorCount());
    ancestor.addFunction(new DelphiFunction("ancestor.foo"));
    assertEquals(2, ancestor.getAccessorCount());
  }

  @Test
  public void getChildrenTest() {
    assertEquals(1, ancestor.getChildren().length);
    assertEquals(1, parent.getChildren().length);
    assertEquals(0, child.getChildren().length);

    parent.addChild(new DelphiClass(null));
    assertEquals(1, ancestor.getChildren().length);
    assertEquals(2, parent.getChildren().length);
  }

  @Test
  public void getDescendantsTest() {
    parent.addChild(new DelphiClass(null));
    child.addParent(new DelphiClass(null));
    assertEquals(3, ancestor.getDescendants().length);
    assertEquals(2, parent.getDescendants().length);

    ancestor.addChild(new DelphiClass(null));
    assertEquals(4, ancestor.getDescendants().length);

    DelphiClass oldOne = new DelphiClass("Cthulhu");
    oldOne.addChild(ancestor);
    ancestor.addParent(oldOne);
    assertEquals(5, oldOne.getDescendants().length);
  }
}
