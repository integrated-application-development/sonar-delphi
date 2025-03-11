/*
 * Sonar Delphi Plugin
 * Copyright (C) 2025 Integrated Application Development
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
package au.com.integradev.delphi.utils;

import static org.assertj.core.api.Assertions.*;

import au.com.integradev.delphi.builders.DelphiTestProgramBuilder;
import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.cfg.api.ControlFlowGraph;
import au.com.integradev.delphi.file.DelphiFile;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;

class ControlFlowGraphUtilsTest {
  private static final String NAME_TO_FIND = "ABC123";

  private static void testFindCfg(DelphiFile unit) {
    DelphiNode node =
        unit.getAst().findDescendantsOfType(NameReferenceNode.class).stream()
            .filter(n -> n.getIdentifier().getImage().equals(NAME_TO_FIND))
            .findFirst()
            .orElseThrow();

    ControlFlowGraph cfg = ControlFlowGraphUtils.findContainingCFG(node);
    assertThat(cfg).isNotNull();
    assertThat(cfg.getBlocks().stream().filter(b -> b.getElements().contains(node)).count())
        .isEqualTo(1);
  }

  @Test
  void testFindCfgInRoutine() {
    testFindCfg(
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl(String.format("  %s;", NAME_TO_FIND))
            .appendImpl("end;")
            .delphiFile());
  }

  @Test
  void testFindCfgInAnonymousRoutine() {
    testFindCfg(
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl(String.format("  %s;", NAME_TO_FIND))
            .appendImpl(String.format("  var Proc := procedure begin %s; end;", NAME_TO_FIND))
            .appendImpl(String.format("  %s;", NAME_TO_FIND))
            .appendImpl("end;")
            .delphiFile());
  }

  @Test
  void testFindCfgInUnitBegin() {
    testFindCfg(
        new DelphiTestUnitBuilder()
            .appendImpl("begin")
            .appendImpl(String.format("  %s;", NAME_TO_FIND))
            .delphiFile());
  }

  @Test
  void testFindCfgInInitialization() {
    testFindCfg(
        new DelphiTestUnitBuilder()
            .appendImpl("initialization")
            .appendImpl(String.format("  %s;", NAME_TO_FIND))
            .delphiFile());
  }

  @Test
  void testFindCfgInFinalization() {
    testFindCfg(
        new DelphiTestUnitBuilder()
            .appendImpl("initialization")
            .appendImpl("finalization")
            .appendImpl(String.format("  %s;", NAME_TO_FIND))
            .delphiFile());
  }

  @Test
  void testFindCfgInProgram() {
    testFindCfg(
        new DelphiTestProgramBuilder()
            .appendImpl(String.format("  %s;", NAME_TO_FIND))
            .delphiFile());
  }
}
