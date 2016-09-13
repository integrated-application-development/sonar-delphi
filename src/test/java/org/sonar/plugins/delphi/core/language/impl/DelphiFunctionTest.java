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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DelphiFunctionTest {

  @Test
  public void equalsTest() {
    assertEquals(true, new DelphiFunction("test").equals(new DelphiFunction("test")));
    assertEquals(false, new DelphiFunction("test").equals(new DelphiFunction("test2")));
    assertEquals(false, new DelphiFunction("class.test").equals(new DelphiFunction("test")));
    assertEquals(true, new DelphiFunction("class.test").equals(new DelphiFunction("class.test")));
  }

  @Test
  public void hashCodeTest() {
    assertEquals(new DelphiFunction("test").hashCode(), new DelphiFunction("test").hashCode());
  }

  @Test
  public void getParentClassTest() {
    DelphiFunction func1 = new DelphiFunction();
    DelphiFunction func2 = new DelphiFunction();
    DelphiFunction func3 = new DelphiFunction();

    DelphiClass class1 = new DelphiClass("class1");
    DelphiClass class2 = new DelphiClass("class2");
    DelphiClass class3 = new DelphiClass("class3");
    class1.addFunction(func1);
    class2.addFunction(func2);
    class3.addFunction(func3);

    assertEquals(class1, func1.getParentClass());
    assertEquals(class2, func2.getParentClass());
    assertEquals(class3, func3.getParentClass());
  }

  @Test
  public void isGlobalTest() {
    DelphiFunction func1 = new DelphiFunction();
    assertEquals(true, func1.isGlobal());

    new DelphiClass("test").addFunction(func1);
    assertEquals(false, func1.isGlobal());
  }

  @Test
  public void getCalledFunctionTest() {
    DelphiFunction func1 = new DelphiFunction();
    DelphiFunction func2 = new DelphiFunction();
    DelphiFunction func3 = new DelphiFunction();

    func1.addCalledFunction(func2);
    assertEquals(1, func1.getCalledFunctions().length);

    func1.addCalledFunction(func3);
    assertEquals(2, func1.getCalledFunctions().length);

    func1.addCalledFunction(func3); // once more the same func, to ensure it
                                    // does not count
    assertEquals(2, func1.getCalledFunctions().length);
  }
}
