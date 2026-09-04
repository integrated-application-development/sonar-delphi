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
package au.com.integradev.delphi.cfg;

import static au.com.integradev.delphi.cfg.ControlFlowGraphTestUtils.buildUnit;
import static org.assertj.core.api.Assertions.*;

import au.com.integradev.delphi.cfg.api.ControlFlowGraph;
import au.com.integradev.delphi.file.DelphiFile;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;

class ControlFlowGraphUtilsTest {
  private static final String NAME_TO_FIND = "ABC123";

  private static void testFindCfg(String... unitLines) {
    DelphiFile unit = buildUnit(unitLines);
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
        "unit Test;",
        "interface",
        "implementation",
        "procedure Test;",
        "begin",
        String.format("  %s;", NAME_TO_FIND),
        "end;",
        "end.");
  }

  @Test
  void testFindCfgInAnonymousRoutine() {
    testFindCfg(
        "unit Test;",
        "interface",
        "implementation",
        "procedure Test;",
        "begin",
        String.format("  %s;", NAME_TO_FIND),
        String.format("  var Proc := procedure begin %s; end;", NAME_TO_FIND),
        String.format("  %s;", NAME_TO_FIND),
        "end;",
        "end.");
  }

  @Test
  void testFindCfgInUnitBegin() {
    testFindCfg(
        "unit Test;",
        "interface",
        "implementation",
        "begin",
        String.format("  %s;", NAME_TO_FIND),
        "end.");
  }

  @Test
  void testFindCfgInInitialization() {
    testFindCfg(
        "unit Test;",
        "interface",
        "implementation",
        "initialization",
        String.format("  %s;", NAME_TO_FIND),
        "end.");
  }

  @Test
  void testFindCfgInFinalization() {
    testFindCfg(
        "unit Test;",
        "interface",
        "implementation",
        "initialization",
        "finalization",
        String.format("  %s;", NAME_TO_FIND),
        "end.");
  }

  @Test
  void testFindCfgInProgram() {
    testFindCfg("program Test;", "begin", String.format("  %s;", NAME_TO_FIND), "end.");
  }
}
