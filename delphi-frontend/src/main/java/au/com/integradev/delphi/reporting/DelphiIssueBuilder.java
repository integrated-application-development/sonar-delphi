/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package au.com.integradev.delphi.reporting;

import au.com.integradev.delphi.check.MasterCheckRegistrar;
import au.com.integradev.delphi.file.DelphiFile.DelphiInputFile;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rule.RuleScope;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext.Location;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;

/**
 * Based directly on {@code InternalJavaIssueBuilder} from the sonar-java project.
 *
 * @see <a
 *     href="https://github.com/SonarSource/sonar-java/blob/master/java-frontend/src/main/java/org/sonar/java/reporting/InternalJavaIssueBuilder.java">
 *     InternalJavaIssueBuilder </a>
 */
public final class DelphiIssueBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(DelphiIssueBuilder.class);

  private static final String MESSAGE_NAME = "message";
  private static final String FLOWS_NAME = "flows";
  private static final String SECONDARIES_NAME = "secondaries";

  private final DelphiCheck check;
  private final SensorContext context;
  private final DelphiInputFile delphiFile;
  private final MasterCheckRegistrar checkRegistrar;
  private FilePosition position;
  private String message;
  @Nullable private List<DelphiCheckContext.Location> secondaries;
  @Nullable private List<List<Location>> flows;
  @Nullable private Integer cost;
  private boolean reported;

  public DelphiIssueBuilder(
      DelphiCheck check,
      SensorContext context,
      DelphiInputFile delphiFile,
      MasterCheckRegistrar checkRegistrar) {
    this.check = check;
    this.context = context;
    this.delphiFile = delphiFile;
    this.checkRegistrar = checkRegistrar;
  }

  private static void requiresValueToBeSet(Object target, String targetName) {
    Preconditions.checkState(target != null, "A %s must be set first.", targetName);
  }

  private static void requiresValueNotToBeSet(Object target, String targetName, String otherName) {
    Preconditions.checkState(
        target == null, "Cannot set %s when %s is already set.", targetName, otherName);
  }

  private static void requiresSetOnlyOnce(Object target, String targetName) {
    Preconditions.checkState(target == null, "Cannot set %s multiple times.", targetName);
  }

  public DelphiIssueBuilder onNode(DelphiNode node) {
    return onFilePosition(FilePosition.from(node));
  }

  public DelphiIssueBuilder onRange(DelphiNode startNode, DelphiNode endNode) {
    return onFilePosition(
        FilePosition.from(
            startNode.getBeginLine(),
            startNode.getBeginColumn(),
            endNode.getEndLine(),
            endNode.getEndColumn()));
  }

  public DelphiIssueBuilder onFilePosition(FilePosition position) {
    this.position = position;
    return this;
  }

  public DelphiIssueBuilder withMessage(String message) {
    this.message = message;
    return this;
  }

  @FormatMethod
  public DelphiIssueBuilder withMessage(@FormatString String message, Object... args) {
    this.message = String.format(message, args);
    return this;
  }

  public DelphiIssueBuilder withSecondaries(DelphiCheckContext.Location... secondaries) {
    return withSecondaries(Arrays.asList(secondaries));
  }

  public DelphiIssueBuilder withSecondaries(List<DelphiCheckContext.Location> secondaries) {
    requiresValueToBeSet(this.message, MESSAGE_NAME);
    requiresValueNotToBeSet(this.flows, FLOWS_NAME, SECONDARIES_NAME);
    requiresSetOnlyOnce(this.secondaries, SECONDARIES_NAME);

    this.secondaries = Collections.unmodifiableList(secondaries);
    return this;
  }

  public DelphiIssueBuilder withFlows(List<List<DelphiCheckContext.Location>> flows) {
    requiresValueToBeSet(this.message, MESSAGE_NAME);
    requiresValueNotToBeSet(this.secondaries, SECONDARIES_NAME, FLOWS_NAME);
    requiresSetOnlyOnce(this.flows, FLOWS_NAME);

    this.flows = Collections.unmodifiableList(flows);
    return this;
  }

  public DelphiIssueBuilder withCost(int cost) {
    requiresValueToBeSet(this.message, MESSAGE_NAME);
    requiresSetOnlyOnce(this.cost, "cost");

    this.cost = cost;
    return this;
  }

  public void report() {
    Preconditions.checkState(!reported, "Can only be reported once.");
    requiresValueToBeSet(message, MESSAGE_NAME);

    Optional<RuleKey> ruleKey = checkRegistrar.getRuleKey(check);
    if (ruleKey.isEmpty()) {
      LOG.trace("Rule not enabled - discarding issue");
      return;
    }

    RuleScope scope = checkRegistrar.getScope(check);
    if (!filePositionInScope(scope)) {
      return;
    }

    NewIssue newIssue =
        context.newIssue().forRule(ruleKey.get()).gap(cost == null ? 0 : cost.doubleValue());

    InputFile inputFile = delphiFile.getInputFile();
    NewIssueLocation primaryLocation = newIssue.newLocation().on(inputFile).message(message);
    if (position != null) {
      primaryLocation.at(createTextRange(inputFile, position));
    }

    newIssue.at(primaryLocation);

    if (secondaries != null) {
      // Transform secondaries into flows
      // Keep secondaries and flows mutually exclusive.
      flows = secondaries.stream().map(Collections::singletonList).collect(Collectors.toList());
      secondaries = null;
    }

    if (flows != null) {
      for (List<DelphiCheckContext.Location> flow : flows) {
        newIssue.addFlow(
            flow.stream()
                .map(location -> createNewIssueLocation(inputFile, newIssue, location))
                .collect(Collectors.toList()));
      }
    }

    newIssue.save();
    reported = true;
  }

  private static NewIssueLocation createNewIssueLocation(
      InputFile inputFile, NewIssue newIssue, Location location) {
    return newIssue
        .newLocation()
        .on(inputFile)
        .at(createTextRange(inputFile, location.getFilePosition()))
        .message(location.getMessage());
  }

  private boolean filePositionInScope(RuleScope scope) {
    if (scope == RuleScope.ALL) {
      return true;
    }

    boolean inTestCode =
        new TestCodeDetector(context.config()).isInTestCode(delphiFile.getAst(), position);

    return (scope == RuleScope.TEST) == inTestCode;
  }

  private static TextRange createTextRange(InputFile inputFile, FilePosition position) {
    if (position.getBeginColumn() == FilePosition.UNDEFINED_COLUMN) {
      TextPointer start = inputFile.selectLine(position.getBeginLine()).start();
      TextPointer end = inputFile.selectLine(position.getEndLine()).end();
      return inputFile.newRange(start, end);
    } else {
      return inputFile.newRange(
          position.getBeginLine(),
          position.getBeginColumn(),
          position.getEndLine(),
          position.getEndColumn());
    }
  }
}
