/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
package org.sonar.plugins.delphi.executor;

import java.io.Serializable;
import java.util.Set;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.measures.Metric;
import org.sonar.plugins.delphi.antlr.ast.visitors.MetricsVisitor;
import org.sonar.plugins.delphi.antlr.ast.visitors.MetricsVisitor.Data;
import org.sonar.plugins.delphi.file.DelphiFile.DelphiInputFile;

public class DelphiMetricsExecutor implements Executor {
  private static final MetricsVisitor VISITOR = new MetricsVisitor();
  private SensorContext context;
  private final FileLinesContextFactory fileLinesContextFactory;
  private DelphiInputFile file;

  public DelphiMetricsExecutor(FileLinesContextFactory fileLinesContextFactory) {
    this.fileLinesContextFactory = fileLinesContextFactory;
  }

  @Override
  public void execute(Context context, DelphiInputFile file) {
    this.context = context.sensorContext();
    this.file = file;

    Data metrics = VISITOR.visit(file.getAst(), new Data());

    saveMetricOnFile(CoreMetrics.CLASSES, metrics.getClasses());
    saveMetricOnFile(CoreMetrics.FUNCTIONS, metrics.getMethods());
    saveMetricOnFile(CoreMetrics.COMPLEXITY, metrics.getComplexity());
    saveMetricOnFile(CoreMetrics.COMMENT_LINES, metrics.getCommentLines());
    saveMetricOnFile(CoreMetrics.STATEMENTS, metrics.getStatements());
    saveMetricOnFile(CoreMetrics.COGNITIVE_COMPLEXITY, metrics.getCognitiveComplexity());

    Set<Integer> codeLines = metrics.getCodeLines();
    saveMetricOnFile(CoreMetrics.NCLOC, codeLines.size());
    saveCodeLinesOnFile(codeLines);
  }

  private <T extends Serializable> void saveMetricOnFile(Metric<T> metric, T value) {
    context.<T>newMeasure().forMetric(metric).on(file.getInputFile()).withValue(value).save();
  }

  private void saveCodeLinesOnFile(Set<Integer> codeLines) {
    FileLinesContext fileLinesContext = fileLinesContextFactory.createFor(file.getInputFile());
    for (int line = 1; line <= file.getInputFile().lines(); line++) {
      fileLinesContext.setIntValue(
          CoreMetrics.NCLOC_DATA_KEY, line, codeLines.contains(line) ? 1 : 0);
    }
    fileLinesContext.save();
  }
}
