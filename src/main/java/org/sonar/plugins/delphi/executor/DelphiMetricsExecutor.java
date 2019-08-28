package org.sonar.plugins.delphi.executor;

import java.io.Serializable;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.plugins.delphi.DelphiFile;
import org.sonar.plugins.delphi.antlr.ast.visitors.MetricsVisitor;
import org.sonar.plugins.delphi.antlr.ast.visitors.MetricsVisitor.Data;

public class DelphiMetricsExecutor implements Executor {
  private static final MetricsVisitor VISITOR = new MetricsVisitor();
  private SensorContext context;
  private DelphiFile file;

  @Override
  public void execute(SensorContext context, DelphiFile file) {
    this.context = context;
    this.file = file;

    Data metrics = VISITOR.visit(file.getAst(), new Data());

    saveMetricOnFile(CoreMetrics.CLASSES, metrics.getClasses());
    saveMetricOnFile(CoreMetrics.FUNCTIONS, metrics.getMethods());
    saveMetricOnFile(CoreMetrics.COMPLEXITY, metrics.getComplexity());
    saveMetricOnFile(CoreMetrics.COMMENT_LINES, metrics.getCommentLines());
    saveMetricOnFile(CoreMetrics.STATEMENTS, metrics.getStatements());
    saveMetricOnFile(CoreMetrics.NCLOC, metrics.getCodeLines());
    saveMetricOnFile(CoreMetrics.COGNITIVE_COMPLEXITY, metrics.getCognitiveComplexity());
  }

  private <T extends Serializable> void saveMetricOnFile(Metric<T> metric, T value) {
    context.<T>newMeasure().forMetric(metric).on(file.getInputFile()).withValue(value).save();
  }
}
