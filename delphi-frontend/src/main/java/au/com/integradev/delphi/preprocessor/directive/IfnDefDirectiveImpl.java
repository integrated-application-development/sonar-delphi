package au.com.integradev.delphi.preprocessor.directive;

import au.com.integradev.delphi.preprocessor.DelphiPreprocessor;
import org.sonar.plugins.communitydelphi.api.directive.IfnDefDirective;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;

public class IfnDefDirectiveImpl extends BranchDirective implements IfnDefDirective {
  private final String symbol;

  IfnDefDirectiveImpl(DelphiToken token, String symbol) {
    super(token, ConditionalKind.IFDEF);
    this.symbol = symbol;
  }

  @Override
  public boolean isSuccessfulBranch(DelphiPreprocessor preprocessor) {
    return !preprocessor.isDefined(symbol);
  }

  @Override
  public String getSymbol() {
    return symbol;
  }
}
