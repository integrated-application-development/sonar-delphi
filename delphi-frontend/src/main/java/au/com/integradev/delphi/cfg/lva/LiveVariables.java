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

import static au.com.integradev.delphi.cfg.lva.LiveVariableUtils.newLiveVariableSet;

import au.com.integradev.delphi.antlr.ast.node.NameDeclarationNodeImpl;
import au.com.integradev.delphi.antlr.ast.node.RoutineImplementationNodeImpl;
import au.com.integradev.delphi.antlr.ast.node.SimpleNameDeclarationNodeImpl;
import au.com.integradev.delphi.cfg.ControlFlowGraphUtils;
import au.com.integradev.delphi.cfg.api.Block;
import au.com.integradev.delphi.cfg.api.ControlFlowGraph;
import au.com.integradev.delphi.cfg.api.Terminus;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sonar.plugins.communitydelphi.api.ast.AnonymousMethodNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode;
import org.sonar.plugins.communitydelphi.api.ast.LocalDeclarationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineParametersNode;
import org.sonar.plugins.communitydelphi.api.ast.VarSectionNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonarsource.analyzer.commons.collections.ListUtils;

public class LiveVariables {
  private final Map<Block, Set<LiveVariable>> inputs = new HashMap<>();
  private final Map<Block, Set<LiveVariable>> outputs = new HashMap<>();
  private final List<LiveVariable> assignments = new ArrayList<>();
  private final List<LiveVariable> references = new ArrayList<>();
  private final Set<LiveVariable> allVariables = newLiveVariableSet();
  private final Set<LiveVariable> localVariables = newLiveVariableSet();
  private final Set<LiveVariable> localScopeVariables = newLiveVariableSet();

  private LiveVariables() {}

  public static LiveVariables analyze(ControlFlowGraph cfg) {
    LiveVariables liveVariables = new LiveVariables();
    liveVariables.analyzeGraph(cfg);
    return liveVariables;
  }

  public Set<LiveVariable> getBlockInputs(Block block) {
    return Collections.unmodifiableSet(inputs.get(block));
  }

  public Set<LiveVariable> getBlockOutputs(Block block) {
    return Collections.unmodifiableSet(outputs.get(block));
  }

  public List<LiveVariable> getAllAssignments() {
    return Collections.unmodifiableList(assignments);
  }

  public Set<LiveVariable> getLocalScopeVariables() {
    return Collections.unmodifiableSet(localScopeVariables);
  }

  private void analyzeGraph(ControlFlowGraph cfg) {
    populateUtilisedVariables(cfg);

    allVariables.addAll(assignments);
    allVariables.addAll(references);

    Optional<LiveVariable> cfgNodeElement = allVariables.stream().findFirst();

    Map<NameDeclaration, LiveVariable> externalVariablesMap = new HashMap<>();
    allVariables.forEach(
        variable -> externalVariablesMap.put(variable.getNameDeclaration(), variable));
    localVariables.forEach(variable -> externalVariablesMap.remove(variable.getNameDeclaration()));
    Set<LiveVariable> externalVariables = newLiveVariableSet();
    externalVariables.addAll(externalVariablesMap.values());

    Map<RoutineNameDeclaration, RoutineImplementationNodeImpl> subRoutines =
        cfgNodeElement.map(LiveVariables::getSubRoutineMap).orElseGet(Collections::emptyMap);

    // Analyze the individual blocks
    Map<Block, LocalDataFlowProperties> localDataFlowProperties = new HashMap<>();
    Function<LiveVariable, Collection<LiveVariable>> getLiveVariablesFunc =
        getLiveVariableFunction(subRoutines, externalVariables);

    for (Block block : cfg.getBlocks()) {
      LocalDataFlowProperties blockProperties =
          new LocalDataFlowProperties(block, getLiveVariablesFunc);
      localDataFlowProperties.put(block, blockProperties);
    }

    // Analyze the graph
    Deque<Block> queue = new ArrayDeque<>(ListUtils.reverse(cfg.getBlocks()));
    while (!queue.isEmpty()) {
      Block block = queue.pop();

      Set<LiveVariable> blockOut =
          outputs.computeIfAbsent(
              block,
              key -> {
                Set<LiveVariable> result = newLiveVariableSet();

                if (key instanceof Terminus) {
                  // The exit blocks must retain all externally accessible variables, as they are
                  // still alive after the CFG.
                  result.addAll(externalVariables);
                }
                return result;
              });
      block.getSuccessors().stream()
          .map(inputs::get)
          .filter(Objects::nonNull)
          .forEach(blockOut::addAll);

      // input = usedBeforeAssigned + (output - assigned)
      Set<LiveVariable> newIn = newLiveVariableSet();
      newIn.addAll(blockOut);
      newIn.removeAll(localDataFlowProperties.get(block).getAssigned());
      newIn.addAll(localDataFlowProperties.get(block).getUsedBeforeAssigned());

      if (!newIn.equals(inputs.get(block))) {
        inputs.put(block, newIn);
        block.getPredecessors().forEach(queue::addLast);
      }
    }
  }

  private static Stream<LiveVariable> getLocalVariables(LiveVariable cfgNodeElement) {

    return Streams.concat(
        streamInlineDeclaredVariables(cfgNodeElement),
        streamLocallyDeclaredVariables(cfgNodeElement),
        streamRoutineParameters(cfgNodeElement)
            .filter(param -> !(param.isOut() || param.isVar() || param.getType().isPointer()))
            .map(FormalParameterNode.FormalParameterData::getNode)
            .filter(NameDeclarationNodeImpl.class::isInstance)
            .map(LiveVariable.class::cast));
  }

  private void populateUtilisedVariables(ControlFlowGraph cfg) {
    Consumer<LiveVariable> addVariable =
        variable -> {
          if (!(variable.getNameDeclaration() instanceof RoutineNameDeclaration)) {
            references.add(variable);
          }
        };

    BlockDataFlowVisitor visitor =
        new BlockDataFlowVisitor().withOnAssign(assignments::add).withOnReference(addVariable);
    cfg.getBlocks().forEach(visitor::visit);

    allVariables.addAll(assignments);
    allVariables.addAll(references);

    Optional<LiveVariable> cfgNodeElement = allVariables.stream().findFirst();

    cfgNodeElement.stream().flatMap(LiveVariables::getLocalVariables).forEach(localVariables::add);

    localScopeVariables.addAll(localVariables);
    cfgNodeElement.stream()
        .flatMap(LiveVariables::streamRoutineParameters)
        .map(FormalParameterNode.FormalParameterData::getNode)
        .filter(NameDeclarationNodeImpl.class::isInstance)
        .map(LiveVariable.class::cast)
        .forEach(localScopeVariables::add);
  }

  private static Function<LiveVariable, Collection<LiveVariable>> getLiveVariableFunction(
      Map<RoutineNameDeclaration, RoutineImplementationNodeImpl> subRoutines,
      Set<LiveVariable> externalVariables) {
    return liveVariable -> {
      NameDeclaration declaration = liveVariable.getNameDeclaration();
      if (declaration instanceof RoutineNameDeclaration) {
        RoutineNameDeclaration routineNameDeclaration = (RoutineNameDeclaration) declaration;
        if (subRoutines.containsKey(routineNameDeclaration)) {
          return Optional.ofNullable(subRoutines.get(routineNameDeclaration).getControlFlowGraph())
              .stream()
              .flatMap(
                  cfg -> LiveVariables.analyze(cfg).getBlockInputs(cfg.getEntryBlock()).stream())
              .collect(Collectors.toSet());
        } else {
          return externalVariables;
        }
      } else {
        return Set.of(liveVariable);
      }
    };
  }

  private static Map<RoutineNameDeclaration, RoutineImplementationNodeImpl> getSubRoutineMap(
      DelphiNode node) {
    Map<RoutineNameDeclaration, RoutineImplementationNodeImpl> result = new HashMap<>();
    AnonymousMethodNode anonymous = node.getFirstParentOfType(AnonymousMethodNode.class);
    if (anonymous != null) {
      Optional.ofNullable(anonymous.getDeclarationSection()).stream()
          .flatMap(
              section -> section.findChildrenOfType(RoutineImplementationNodeImpl.class).stream())
          .forEach(routine -> result.put(routine.getRoutineNameDeclaration(), routine));
    }

    // Local routines
    RoutineImplementationNodeImpl routine =
        node.getFirstParentOfType(RoutineImplementationNodeImpl.class);
    if (routine != null) {
      Optional.ofNullable(routine.getDeclarationSection()).stream()
          .flatMap(section -> section.findChildrenOfType(RoutineImplementationNode.class).stream())
          .filter(RoutineImplementationNodeImpl.class::isInstance)
          .map(RoutineImplementationNodeImpl.class::cast)
          .forEach(subRoutine -> result.put(subRoutine.getRoutineNameDeclaration(), subRoutine));
    }
    return result;
  }

  private static Stream<LocalDeclarationSectionNode> streamLocalDeclarationSectionNode(
      DelphiNode node) {
    AnonymousMethodNode anonymous = node.getFirstParentOfType(AnonymousMethodNode.class);
    if (anonymous != null) {
      return Stream.ofNullable(anonymous.getDeclarationSection());
    }
    RoutineImplementationNode routine = node.getFirstParentOfType(RoutineImplementationNode.class);
    if (routine != null) {
      return Stream.ofNullable(routine.getDeclarationSection());
    }

    return Stream.empty();
  }

  /// Local variables that accessible by this control flow graph. E.g.,
  ///
  /// <pre>
  /// procedure Test;
  /// var Above: Integer; // <<< This variable
  ///   procedure Sub;
  ///   var SubVar: Integer;
  ///   begin
  ///   end;
  /// var Below: Integer; // <<< This variable
  /// begin
  /// end;
  /// </pre>
  private static Stream<LiveVariable> streamLocallyDeclaredVariables(DelphiNode cfgNode) {
    return streamLocalDeclarationSectionNode(cfgNode)
        .flatMap(section -> Lists.reverse(section.getChildren()).stream())
        .filter(VarSectionNode.class::isInstance)
        .flatMap(node -> ((VarSectionNode) node).getDeclarations().stream())
        .flatMap(declaration -> declaration.getNameDeclarationList().getDeclarations().stream())
        .filter(NameDeclarationNodeImpl.class::isInstance)
        .map(LiveVariable.class::cast);
  }

  /// Variables declared within this control flow graph. E.g.,
  ///
  /// <pre>
  /// procedure Test;
  /// begin
  ///   var Inline; // <<< This variable
  ///   try
  ///     for var A in [] do; // <<< This variable
  ///   except
  ///     on E: Exception do; // <<< This variable
  ///   end;
  /// end;
  private static Stream<LiveVariable> streamInlineDeclaredVariables(DelphiNode cfgNode) {
    return Optional.ofNullable(ControlFlowGraphUtils.findContainingCFG(cfgNode)).stream()
        .flatMap(cfg -> cfg.getBlocks().stream())
        .flatMap(block -> block.getElements().stream())
        .filter(SimpleNameDeclarationNodeImpl.class::isInstance)
        .map(SimpleNameDeclarationNodeImpl.class::cast)
        .map(LiveVariable.class::cast);
  }

  private static Stream<FormalParameterNode.FormalParameterData> streamRoutineParameters(
      DelphiNode cfgNode) {
    return Optional.ofNullable(getRoutineParameters(cfgNode)).stream()
        .flatMap(node -> node.getParameters().stream());
  }

  private static RoutineParametersNode getRoutineParameters(DelphiNode node) {
    AnonymousMethodNode anonymous = node.getFirstParentOfType(AnonymousMethodNode.class);
    if (anonymous != null) {
      return anonymous.getRoutineParametersNode();
    }
    RoutineImplementationNode routine = node.getFirstParentOfType(RoutineImplementationNode.class);
    if (routine != null) {
      return routine.getRoutineHeading().getRoutineParametersNode();
    }

    return null;
  }
}
