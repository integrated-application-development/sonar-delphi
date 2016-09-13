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
package org.sonar.plugins.delphi.project;

import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class DelphiWorkgroupTest {

  private static String XML_FILE = "/org/sonar/plugins/delphi/SimpleDelphiProject/dproj/workgroup/All.groupproj";
  private DelphiWorkgroup workGroup;

  @Before
  public void init() {
    workGroup = new DelphiWorkgroup();
  }

  @Test
  public void simpleWorkgroupTest() {
    assertEquals(0, workGroup.getProjects().size());
    workGroup.addProject(new DelphiProject("Sample"));
    assertEquals(1, workGroup.getProjects().size());
  }

  @Test
  public void xmlWorkgroupTest() throws IOException {
    DelphiWorkgroup workGroup = new DelphiWorkgroup(DelphiUtils.getResource(XML_FILE));
    assertEquals(1, workGroup.getProjects().size());
    assertEquals("Simple Delphi Product", workGroup.getProjects().get(0).getName());
  }

}
