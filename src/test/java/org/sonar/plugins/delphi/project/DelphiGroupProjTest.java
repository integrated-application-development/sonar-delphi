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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class DelphiGroupProjTest {

  private static final String XML_FILE =
      "/org/sonar/plugins/delphi/projects/SimpleProject/workgroup/All.groupproj";

  @Test
  public void testXmlWorkgroup() {
    DelphiGroupProj workGroup = DelphiGroupProj.parse(DelphiUtils.getResource(XML_FILE).toPath());
    assertThat(workGroup.getProjects()).hasSize(1);
    assertThat(workGroup.getProjects().get(0).getName()).isEqualTo("Simple Delphi Project");
  }
}
