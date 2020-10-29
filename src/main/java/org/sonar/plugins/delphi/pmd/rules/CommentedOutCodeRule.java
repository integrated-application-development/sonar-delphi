package org.sonar.plugins.delphi.pmd.rules;

import static java.util.regex.Pattern.compile;

import com.google.common.base.Splitter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.token.DelphiToken;
import org.sonar.plugins.delphi.pmd.FilePosition;

public class CommentedOutCodeRule extends AbstractDelphiRule {

  private static final String IDENTIFIER = "([A-Z]|_)([A-Z]|_|[0-9])*";
  private static final String PRIMARY_EXPRESSION =
      "(inherited)?\\s*" + IDENTIFIER + "(\\^|(\\.(" + IDENTIFIER + ")|<.+>|(\\[.*])|(\\(.*\\))))*";
  private static final String ARGUMENT_LIST_EOL = "((\\(((\\(.*\\))|[^)])*\\s*,)\\s*$)";
  private static final String COMMENT_OR_WHITESPACE = "(\\s|//.*|\\{(.*?)}|\\(\\*(.*?)\\*\\))";

  private static final String TYPE_DECLARATION_REGEX =
      "(?i)^\\s*"
          + IDENTIFIER
          + "\\s*=\\s*"
          + "(packed)?"
          + "(array|set|file|class|interface|dispinterface|object"
          + "|record|reference|function|procedure|\\^|type|\\()";

  private static final String VAR_DECLARATION_REGEX =
      "(?i)^\\s*"
          + IDENTIFIER
          + "(\\s*,\\s*"
          + IDENTIFIER
          + ")*"
          + "\\s*:\\s*"
          + IDENTIFIER
          + "\\s*;";

  private static final String CONST_DECLARATION_REGEX =
      "(?i)^\\s*" + PRIMARY_EXPRESSION + "\\s*=\\s*.*;$";

  private static final String PROPERTY_DECLARATION_REGEX =
      "(?i)^\\s*property\\s*" + IDENTIFIER + "\\s*:\\s*" + IDENTIFIER;

  private static final String METHOD_HEADER_REGEX =
      "(?i)^\\s*(function|procedure|constructor|destructor)\\s*"
          + IDENTIFIER
          + "((\\."
          + IDENTIFIER
          + ")|<.+>)*"
          + "(((\\(.*\\))?(:.*)?\\s*;)|"
          + ARGUMENT_LIST_EOL
          + ")";

  private static final String COMPILER_DIRECTIVE_REGEX = "(?i)\\{\\$" + IDENTIFIER + ".*}";

  private static final String PRIMARY_EXPRESSION_STATEMENT_REGEX =
      "(?i)^\\s*"
          + PRIMARY_EXPRESSION
          + "("
          + COMMENT_OR_WHITESPACE
          + "*;"
          + COMMENT_OR_WHITESPACE
          + "*$|"
          + ARGUMENT_LIST_EOL
          + ")";

  private static final String ASSIGN_STATEMENT_REGEX =
      "(?i)^\\s*" + PRIMARY_EXPRESSION + "\\s*:=.*;$";

  private static final String IF_STATEMENT_REGEX =
      "(?i)^\\s*(else)?\\s*if"
          + "(?!\\s+"
          + IDENTIFIER
          + "\\s(?!(and|or|not|in|is|as|\\+|-|\\*|/|<|>|<>|<=|>=|\\(|@|xor|div|mod|shl|shr)\\b))"
          + "\\s+.*\\b((then\\s*(begin|(.*(else.*)?;))?)|and|or)\\s*$";

  private static final String ELSE_STATEMENT_REGEX = "(?i)^\\s*else(\\s*begin)?\\s*$";

  private static final String FOR_STATEMENT_REGEX =
      "(?i)^\\s*for.*\\b(in|to|downto)\\b.*(do\\s*(begin)?)\\s*$";

  private static final String WITH_STATEMENT_REGEX = "(?i)^\\s*with.*\\bdo\\b\\s*(begin)?\\s*$";

  private static final String RAISE_STATEMENT_REGEX = "(?i)^\\s*raise.*\\s*[(|;]\\s*$";

  private static final String VAR_SECTION_REGEX = "(?i)^\\s*(var|const)\\s*$";

  private static final String BEGIN_END_REGEX = "(?i)^\\s*(begin|end;?)\\s*$";

  private static final String VISIBILITY_SECTION_REGEX =
      "(?i)^\\s*(strict)?\\s*(published|public|protected|private)\\s*$";

  private static final String PARENTHESIZED_CAST_EXPRESSION =
      "\\(" + PRIMARY_EXPRESSION + "\\s*as\\s*" + PRIMARY_EXPRESSION + "\\)";

  private static final Set<Pattern> CODE_PATTERNS =
      Set.of(
          compile(PRIMARY_EXPRESSION_STATEMENT_REGEX),
          compile(ASSIGN_STATEMENT_REGEX),
          compile(METHOD_HEADER_REGEX),
          compile(BEGIN_END_REGEX),
          compile(TYPE_DECLARATION_REGEX),
          compile(VAR_DECLARATION_REGEX),
          compile(CONST_DECLARATION_REGEX),
          compile(PROPERTY_DECLARATION_REGEX),
          compile(COMPILER_DIRECTIVE_REGEX),
          compile(IF_STATEMENT_REGEX),
          compile(ELSE_STATEMENT_REGEX),
          compile(FOR_STATEMENT_REGEX),
          compile(WITH_STATEMENT_REGEX),
          compile(RAISE_STATEMENT_REGEX),
          compile(VAR_SECTION_REGEX),
          compile(VISIBILITY_SECTION_REGEX),
          compile(PARENTHESIZED_CAST_EXPRESSION));

  private static final Pattern NEW_LINE_DELIMITER = compile("\r\n?|\n");

  @Override
  public RuleContext visit(DelphiAST ast, RuleContext data) {
    List<Integer> commentedOutCodeLines = new ArrayList<>();
    for (DelphiToken comment : ast.getComments()) {
      commentedOutCodeLines.addAll(handleComment(comment));
    }

    // Greedy algorithm to split lines on blocks and to report only one violation per block
    Collections.sort(commentedOutCodeLines);
    int prev = Integer.MIN_VALUE;
    for (Integer commentedOutCodeLine : commentedOutCodeLines) {
      if (prev + 1 < commentedOutCodeLine) {
        newViolation(data).atPosition(FilePosition.atLineLevel(commentedOutCodeLine)).save();
      }
      prev = commentedOutCodeLine;
    }

    return data;
  }

  private static List<Integer> handleComment(DelphiToken comment) {
    List<Integer> commentedOutCodeLines = new ArrayList<>();
    String commentString = extractCommentString(comment);
    List<String> lines = Splitter.on(NEW_LINE_DELIMITER).splitToList(commentString);
    for (int i = 0; i < lines.size(); ++i) {
      String line = lines.get(i);
      if (isLineOfCode(line)) {
        // Mark all remaining lines from this comment as a commented out lines of code
        for (int j = i; j < lines.size(); j++) {
          commentedOutCodeLines.add(comment.getBeginLine() + j);
        }
        break;
      }
    }
    return commentedOutCodeLines;
  }

  private static String extractCommentString(DelphiToken comment) {
    String image = comment.getImage();
    if (image.startsWith("//")) {
      return image.substring(2);
    } else if (image.startsWith("{")) {
      return image.substring(1, image.length() - 1);
    } else {
      return image.substring(2, image.length() - 2);
    }
  }

  private static boolean isLineOfCode(String line) {
    for (Pattern pattern : CODE_PATTERNS) {
      if (pattern.matcher(line).find()) {
        return true;
      }
    }
    return false;
  }
}
