package au.com.integradev.delphi.preprocessor.directive;

import org.sonar.plugins.communitydelphi.api.directive.ConditionalDirective;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;

public abstract class ConditionalDirectiveImpl extends CompilerDirectiveImpl
    implements ConditionalDirective {
  private final ConditionalKind kind;

  ConditionalDirectiveImpl(DelphiToken token, ConditionalKind kind) {
    super(token);
    this.kind = kind;
  }

  @Override
  public final ConditionalKind kind() {
    return kind;
  }
}
