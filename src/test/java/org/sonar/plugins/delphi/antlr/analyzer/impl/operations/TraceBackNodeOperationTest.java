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
package org.sonar.plugins.delphi.antlr.analyzer.impl.operations;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TraceBackNodeOperationTest extends OperationsTestsCommon {

  public TraceBackNodeOperationTest() {
    super(new TraceBackNodeOperation());
  }

  @Override
  @Before
  public void init() {
    super.init();
  }

  @Test
  public void traceback() {
    assertFalse(operation.execute(parent).isValid());
    for (int i = 0; i < LAYER_NODES - 1; ++i) {

      assertNotNull(operation.execute(parent.getChild(i)));
      assertEquals(i + 2, operation.execute(parent.getChild(i)).getNode().getType());

      assertNotNull(operation.execute(parent.getChild(i).getChild(0)));
      assertEquals(i + 2, operation.execute(parent.getChild(i).getChild(0)).getNode().getType());
    }

    assertFalse(operation.execute(parent.getChild(LAYER_NODES - 1).getChild(0)).isValid());
    assertFalse(operation.execute(parent.getChild(LAYER_NODES - 1)).isValid());
  }

}
