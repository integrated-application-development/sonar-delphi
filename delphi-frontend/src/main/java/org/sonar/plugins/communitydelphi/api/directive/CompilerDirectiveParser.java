package org.sonar.plugins.communitydelphi.api.directive;

import java.util.Optional;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;

/**
 * Parses a CompilerDirective object from a {@code COMPILER_DIRECTIVE} token.
 *
 * <p>Example: A token with the text "{$include unit.pas}" will create an {@link IncludeDirective}.
 */
public interface CompilerDirectiveParser {
  /**
   * Produce a compiler directive from a string
   *
   * @param token token to parse into a CompilerDirective object
   * @return compiler directive
   */
  Optional<CompilerDirective> parse(DelphiToken token);
}
