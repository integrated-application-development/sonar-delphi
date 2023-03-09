package au.com.integradev.delphi.preprocessor.directive;

import au.com.integradev.delphi.preprocessor.DelphiPreprocessor;
import org.sonar.plugins.communitydelphi.api.directive.ParameterDirective;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;

public class ParameterDirectiveImpl extends CompilerDirectiveImpl implements ParameterDirective {
  private final ParameterKind kind;

  ParameterDirectiveImpl(DelphiToken token, ParameterKind kind) {
    super(token);
    this.kind = kind;
  }

  @Override
  public void execute(DelphiPreprocessor preprocessor) {
    // do nothing
  }

  @Override
  public ParameterKind kind() {
    return kind;
  }
}
