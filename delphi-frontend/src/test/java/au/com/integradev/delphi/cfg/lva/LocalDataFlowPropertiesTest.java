/*
 * Sonar Delphi Plugin
 * Copyright (C) 2026 Integrated Application Development
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
package au.com.integradev.delphi.cfg.lva;

import static org.assertj.core.api.Assertions.*;

import au.com.integradev.delphi.cfg.ControlFlowGraphTestUtils;
import au.com.integradev.delphi.cfg.api.Block;
import au.com.integradev.delphi.cfg.api.ControlFlowGraph;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.ast.SimpleNameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.VarStatementNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;

class LocalDataFlowPropertiesTest {

  private static Stream<String> toIdentifiers(Collection<LiveVariable> identifiers) {
    return identifiers.stream().map(LiveVariable::getNameDeclaration).map(NameDeclaration::getName);
  }

  private static Collection<LiveVariable> ignoreRoutineReferences(LiveVariable variable) {
    if (variable.getNameDeclaration() instanceof RoutineNameDeclaration) {
      return Collections.emptySet();
    } else {
      return Set.of(variable);
    }
  }

  @Test
  void testAssignment() {
    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfg("var A := 1;");
    Block target = cfg.getEntryBlock();
    LocalDataFlowProperties properties =
        new LocalDataFlowProperties(target, LocalDataFlowPropertiesTest::ignoreRoutineReferences);

    assertThat(toIdentifiers(properties.getAssigned())).containsExactly("A");
    assertThat(toIdentifiers(properties.getUsedBeforeAssigned())).isEmpty();
  }

  @Test
  void testRepeatedAssignment() {
    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfg("var A := 1;", "A := 2;");
    Block target = cfg.getEntryBlock();
    LocalDataFlowProperties properties =
        new LocalDataFlowProperties(target, LocalDataFlowPropertiesTest::ignoreRoutineReferences);

    assertThat(toIdentifiers(properties.getAssigned())).containsExactly("A");
    assertThat(toIdentifiers(properties.getUsedBeforeAssigned())).isEmpty();
  }

  @Test
  void testUseWithoutAssignment() {
    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfg("var A;", "Writeln(A);");
    Block target = cfg.getEntryBlock();
    LocalDataFlowProperties properties =
        new LocalDataFlowProperties(target, LocalDataFlowPropertiesTest::ignoreRoutineReferences);

    assertThat(toIdentifiers(properties.getAssigned())).isEmpty();
    assertThat(toIdentifiers(properties.getUsedBeforeAssigned())).containsExactly("A");
  }

  @Test
  void testUseBeforeAssignment() {
    final ControlFlowGraph cfg =
        ControlFlowGraphTestUtils.buildCfg("var A;", "Writeln(A);", "A := 1;");
    Block target = cfg.getEntryBlock();
    LocalDataFlowProperties properties =
        new LocalDataFlowProperties(target, LocalDataFlowPropertiesTest::ignoreRoutineReferences);

    assertThat(toIdentifiers(properties.getAssigned())).containsExactly("A");
    assertThat(toIdentifiers(properties.getUsedBeforeAssigned())).containsExactly("A");
  }

  @Test
  void testRepeatedUseBeforeAssignment() {
    final ControlFlowGraph cfg =
        ControlFlowGraphTestUtils.buildCfg("var A;", "Writeln(A, A, A);", "A := 1;");
    Block target = cfg.getEntryBlock();
    LocalDataFlowProperties properties =
        new LocalDataFlowProperties(target, LocalDataFlowPropertiesTest::ignoreRoutineReferences);

    assertThat(toIdentifiers(properties.getAssigned())).containsExactly("A");
    assertThat(toIdentifiers(properties.getUsedBeforeAssigned())).containsExactly("A");
  }

  @Test
  void testUseAfterAssignment() {
    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfg("var A := 1;", "Writeln(A);");
    Block target = cfg.getEntryBlock();
    LocalDataFlowProperties properties =
        new LocalDataFlowProperties(target, LocalDataFlowPropertiesTest::ignoreRoutineReferences);

    assertThat(toIdentifiers(properties.getAssigned())).containsExactly("A");
    assertThat(toIdentifiers(properties.getUsedBeforeAssigned())).isEmpty();
  }

  @Test
  void testLiveVariableCallback() {
    final ControlFlowGraph cfg = ControlFlowGraphTestUtils.buildCfg("var A, B, C;", "Writeln(A);");
    Block target = cfg.getEntryBlock();
    List<LiveVariable> assignments =
        target.getElements().stream()
            .findFirst()
            .flatMap(
                element ->
                    Optional.ofNullable(element.getFirstParentOfType(VarStatementNode.class)))
            .stream()
            .flatMap(
                statement ->
                    statement.findDescendantsOfType(SimpleNameDeclarationNode.class).stream())
            .map(LiveVariable.class::cast)
            .collect(Collectors.toList());

    LocalDataFlowProperties properties =
        new LocalDataFlowProperties(target, liveVariable -> assignments);

    assertThat(properties.getAssigned()).isEmpty();
    // These are the variables returned by the callback
    assertThat(properties.getUsedBeforeAssigned()).containsExactlyElementsOf(assignments);
  }
}
