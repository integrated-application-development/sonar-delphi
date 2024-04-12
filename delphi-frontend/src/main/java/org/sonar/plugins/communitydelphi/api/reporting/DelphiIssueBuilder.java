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
package org.sonar.plugins.communitydelphi.api.reporting;

import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;
import java.util.List;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;

public interface DelphiIssueBuilder {
  DelphiIssueBuilder onNode(DelphiNode node);

  DelphiIssueBuilder onRange(DelphiNode startNode, DelphiNode endNode);

  DelphiIssueBuilder onFilePosition(FilePosition position);

  DelphiIssueBuilder withMessage(String message);

  @FormatMethod
  DelphiIssueBuilder withMessage(@FormatString String message, Object... args);

  DelphiIssueBuilder withSecondaries(DelphiCheckContext.Location... secondaries);

  DelphiIssueBuilder withSecondaries(List<DelphiCheckContext.Location> secondaries);

  DelphiIssueBuilder withFlows(List<List<DelphiCheckContext.Location>> flows);

  DelphiIssueBuilder withCost(int cost);

  void report();
}
