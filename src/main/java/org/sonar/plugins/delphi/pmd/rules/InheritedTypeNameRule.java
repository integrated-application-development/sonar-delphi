package org.sonar.plugins.delphi.pmd.rules;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.antlr.runtime.tree.Tree;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.antlr.ast.DelphiNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

public class InheritedTypeNameRule extends NameConventionRule {
  private static final Logger LOG = Loggers.get(InheritedTypeNameRule.class);

  public static final PropertyDescriptor<String> NAME_REGEX =
      PropertyFactory.stringProperty("nameRegex")
          .desc("The regular expression used to define the naming convention")
          .defaultValue("(?!)")
          .build();

  public static final PropertyDescriptor<String> PARENT_REGEX =
      PropertyFactory.stringProperty("parentNameRegex")
          .desc("The regular expression used to match parent types")
          .defaultValue("(?!)")
          .build();

  private static final PropertyDescriptor<String> MESSAGE =
      PropertyFactory.stringProperty("message").desc("The issue message").defaultValue("").build();

  private Pattern namePattern;
  private Pattern parentPattern;

  public InheritedTypeNameRule() {
    definePropertyDescriptor(NAME_REGEX);
    definePropertyDescriptor(PARENT_REGEX);
    definePropertyDescriptor(MESSAGE);
  }

  @Override
  public void start(RuleContext ctx) {
    namePattern = tryCompilePattern(getProperty(NAME_REGEX));
    parentPattern = tryCompilePattern(getProperty(PARENT_REGEX));
  }

  private Pattern tryCompilePattern(String regularExpression) {
    try {
      return Pattern.compile(regularExpression);
    } catch (PatternSyntaxException e) {
      LOG.error("Unable to compile regular expression: " + regularExpression, e);
      return null;
    }
  }

  @Override
  public DelphiNode findNode(DelphiNode node) {
    if (node.getType() != DelphiLexer.TkNewTypeName) {
      return null;
    }

    Tree typeDeclNode = node.nextNode();
    int type = typeDeclNode.getChild(0).getType();

    if (type != DelphiLexer.TkClass && type != DelphiLexer.TkInterface) {
      return null;
    }

    return (DelphiNode) node.getChild(0);
  }

  @Override
  protected boolean isViolation(DelphiNode node) {
    return inheritsFromType(node) && !namePattern.matcher(node.getText()).matches();
  }

  private boolean inheritsFromType(DelphiNode node) {
    DelphiNode newTypeName = (DelphiNode) node.getParent();
    DelphiNode typeDeclNode = (DelphiNode) newTypeName.nextNode().getChild(0);
    Tree classParents = typeDeclNode.getFirstChildWithType(DelphiLexer.TkClassParents);

    if (classParents != null) {
      for (int i = 0; i < classParents.getChildCount(); ++i) {
        String parentName = classParents.getChild(i).getText();

        if (parentPattern.matcher(parentName).matches()) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
