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
package au.com.integradev.delphi.executor;

import au.com.integradev.delphi.antlr.ast.visitors.MetricsVisitor;
import au.com.integradev.delphi.antlr.ast.visitors.MetricsVisitor.Data;
import au.com.integradev.delphi.core.DelphiFeatures;
import au.com.integradev.delphi.core.DelphiFeatures.Feature;
import au.com.integradev.delphi.file.DelphiFile.DelphiInputFile;
import java.io.Serializable;
import java.util.Set;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.measures.Metric;

public class DelphiMetricsExecutor implements Executor {
  private static final MetricsVisitor VISITOR = new MetricsVisitor();

  private final FileLinesContextFactory fileLinesContextFactory;
  private final boolean publishFileMetrics;
  private final boolean publishNclocData;

  public DelphiMetricsExecutor(
      FileLinesContextFactory fileLinesContextFactory, DelphiFeatures features) {
    this.fileLinesContextFactory = fileLinesContextFactory;
    publishFileMetrics = features.isEnabled(Feature.FILE_METRICS);
    publishNclocData = features.isEnabled(Feature.NCLOC_DATA);
  }

  @Override
  public void execute(Context context, DelphiInputFile file) {
    InputFile inputFile = file.getInputFile();
    if (inputFile.type() != InputFile.Type.MAIN) {
      return;
    }

    Data metrics = VISITOR.visit(file.getAst(), new Data());
    if (publishFileMetrics) {
      saveFileMetrics(context.sensorContext(), inputFile, metrics);
    }
    if (publishNclocData) {
      saveCodeLinesOnFile(inputFile, metrics.getCodeLines());
    }
  }

  private static void saveFileMetrics(SensorContext context, InputFile inputFile, Data metrics) {
    saveMetricOnFile(context, inputFile, CoreMetrics.CLASSES, metrics.getClasses());
    saveMetricOnFile(context, inputFile, CoreMetrics.FUNCTIONS, metrics.getRoutines());
    saveMetricOnFile(context, inputFile, CoreMetrics.COMPLEXITY, metrics.getComplexity());
    saveMetricOnFile(context, inputFile, CoreMetrics.COMMENT_LINES, metrics.getCommentLines());
    saveMetricOnFile(context, inputFile, CoreMetrics.STATEMENTS, metrics.getStatements());
    saveMetricOnFile(
        context, inputFile, CoreMetrics.COGNITIVE_COMPLEXITY, metrics.getCognitiveComplexity());
    saveMetricOnFile(context, inputFile, CoreMetrics.NCLOC, metrics.getCodeLines().size());
  }

  private static <T extends Serializable> void saveMetricOnFile(
      SensorContext context, InputFile inputFile, Metric<T> metric, T value) {
    context.<T>newMeasure().forMetric(metric).on(inputFile).withValue(value).save();
  }

  private void saveCodeLinesOnFile(InputFile inputFile, Set<Integer> codeLines) {
    FileLinesContext fileLinesContext = fileLinesContextFactory.createFor(inputFile);
    for (int line = 1; line <= inputFile.lines(); line++) {
      fileLinesContext.setIntValue(
          CoreMetrics.NCLOC_DATA_KEY, line, codeLines.contains(line) ? 1 : 0);
    }
    fileLinesContext.save();
  }
}
