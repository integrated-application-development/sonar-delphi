package au.com.integradev.delphi.check;

import au.com.integradev.delphi.file.DelphiFile.DelphiInputFile;
import au.com.integradev.delphi.preprocessor.CompilerSwitchRegistry;
import au.com.integradev.delphi.reporting.DelphiIssueBuilder;
import java.util.List;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.directive.CompilerDirectiveParser;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

public class DelphiCheckContextImpl implements DelphiCheckContext {
  private final DelphiCheck check;
  private final SensorContext sensorContext;
  private final DelphiInputFile delphiFile;
  private final CompilerDirectiveParser compilerDirectiveParser;
  private final MasterCheckRegistrar checkRegistrar;

  public DelphiCheckContextImpl(
      DelphiCheck check,
      SensorContext sensorContext,
      DelphiInputFile delphiFile,
      CompilerDirectiveParser compilerDirectiveParser,
      MasterCheckRegistrar checkRegistrar) {
    this.check = check;
    this.sensorContext = sensorContext;
    this.delphiFile = delphiFile;
    this.compilerDirectiveParser = compilerDirectiveParser;
    this.checkRegistrar = checkRegistrar;
  }

  @Override
  public DelphiAst getAst() {
    return delphiFile.getAst();
  }

  @Override
  public List<DelphiToken> getTokens() {
    return delphiFile.getTokens();
  }

  @Override
  public List<String> getFileLines() {
    return delphiFile.getSourceCodeFilesLines();
  }

  @Override
  public CompilerSwitchRegistry getCompilerSwitchRegistry() {
    return delphiFile.getCompilerSwitchRegistry();
  }

  @Override
  public CompilerDirectiveParser getCompilerDirectiveParser() {
    return compilerDirectiveParser;
  }

  @Override
  public TypeFactory getTypeFactory() {
    return delphiFile.getTypeFactory();
  }

  @Override
  public DelphiIssueBuilder newIssue() {
    return new DelphiIssueBuilder(check, sensorContext, delphiFile, checkRegistrar);
  }
}
