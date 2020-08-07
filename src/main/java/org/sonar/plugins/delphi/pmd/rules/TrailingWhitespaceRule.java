package org.sonar.plugins.delphi.pmd.rules;

import static java.util.regex.Pattern.compile;

import com.google.common.base.Splitter;
import java.util.regex.Pattern;
import net.sourceforge.pmd.RuleContext;
import org.apache.commons.lang3.StringUtils;
import org.sonar.plugins.delphi.antlr.ast.token.DelphiToken;
import org.sonar.plugins.delphi.pmd.FilePosition;

public class TrailingWhitespaceRule extends AbstractDelphiRule {
  private static final Pattern NEW_LINE_DELIMITER = compile("\r\n?|\n");

  @Override
  public void visitToken(DelphiToken token, RuleContext data) {
    if (!token.isWhitespace()) {
      return;
    }

    if (!NEW_LINE_DELIMITER.matcher(token.getImage()).find()) {
      return;
    }

    int line = token.getBeginLine();
    int column = token.getBeginColumn();

    String image = StringUtils.stripEnd(token.getImage(), " \t\f");
    var parts = Splitter.on(NEW_LINE_DELIMITER).split(image);
    for (String whitespace : parts) {
      if (!whitespace.isEmpty()) {
        newViolation(data)
            .atPosition(FilePosition.from(line, column, line, column + whitespace.length()))
            .save();
      }
      ++line;
      column = 0;
    }
  }
}
