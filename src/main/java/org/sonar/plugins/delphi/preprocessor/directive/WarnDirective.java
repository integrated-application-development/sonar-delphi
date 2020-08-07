package org.sonar.plugins.delphi.preprocessor.directive;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import java.util.Arrays;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.preprocessor.DelphiPreprocessor;

public class WarnDirective extends AbstractCompilerDirective {
  public enum WarnDirectiveValue {
    ON,
    OFF,
    ERROR,
    DEFAULT,
    UNKNOWN
  }

  private static final Pattern WHITESPACE = Pattern.compile("\\s+");
  private final WarnDirectiveValue value;

  public WarnDirective(Token token, CompilerDirectiveType type, String item) {
    super(token, type);
    String valueString = extractValueString(item);
    this.value =
        Arrays.stream(WarnDirectiveValue.values())
            .filter(wdv -> wdv.name().equalsIgnoreCase(valueString))
            .findFirst()
            .orElse(WarnDirectiveValue.UNKNOWN);
  }

  public WarnDirectiveValue getValue() {
    return value;
  }

  @Override
  public void execute(DelphiPreprocessor preprocessor) {
    // Do nothing
  }

  @Nullable
  private static String extractValueString(String item) {
    return Iterables.get(Splitter.on(WHITESPACE).split(item), 1, null);
  }
}
