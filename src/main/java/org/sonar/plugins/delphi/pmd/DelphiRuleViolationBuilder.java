package org.sonar.plugins.delphi.pmd;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.ast.DelphiNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.pmd.rules.DelphiRule;
import org.sonarsource.analyzer.commons.TokenLocation;

public class DelphiRuleViolationBuilder {
  private static final int[] METHOD_TYPES = {
    DelphiLexer.CONSTRUCTOR, DelphiLexer.DESTRUCTOR, DelphiLexer.FUNCTION, DelphiLexer.PROCEDURE
  };

  private DelphiRule rule;
  private RuleContext ctx;
  private DelphiRuleViolation ruleViolation;

  private DelphiRuleViolationBuilder(DelphiRule rule, RuleContext ctx) {
    this.rule = rule;
    this.ctx = ctx;
    this.ruleViolation = new DelphiRuleViolation(rule, ctx);
  }

  public static DelphiRuleViolationBuilder newViolation(DelphiRule rule, RuleContext ctx) {
    return new DelphiRuleViolationBuilder(rule, ctx);
  }

  public DelphiRuleViolationBuilder fileLocation(int line, int column, int endLine, int endColumn) {
    ruleViolation.setBeginLine(line);
    ruleViolation.setBeginColumn(column);
    ruleViolation.setEndLine(endLine);
    ruleViolation.setEndColumn(endColumn);
    return this;
  }

  public DelphiRuleViolationBuilder fileLocation(DelphiNode node) {
    return fileLocation(
        node.getBeginLine(), node.getBeginColumn(), node.getEndLine(), node.getEndColumn());
  }

  public DelphiRuleViolationBuilder fileLocation(Token token) {
    TokenLocation location =
        new TokenLocation(token.getLine(), token.getCharPositionInLine(), token.getText());

    return fileLocation(
        location.startLine(),
        location.startLineOffset(),
        location.endLine(),
        location.endLineOffset());
  }

  public DelphiRuleViolationBuilder logicalLocation(
      String packageName, String className, String methodName) {
    ruleViolation.setPackageName(packageName);
    ruleViolation.setClassName(className);
    ruleViolation.setMethodName(methodName);
    return this;
  }

  public DelphiRuleViolationBuilder logicalLocation(DelphiNode node) {
    findLogicalLocation(node);
    return this;
  }

  public DelphiRuleViolationBuilder message(String message) {
    ruleViolation.setDescription(message);
    return this;
  }

  public void save() {
    checkIfViolationSuppressed();
    ctx.getReport().addRuleViolation(ruleViolation);
  }

  private void checkIfViolationSuppressed() {
    boolean suppressed = rule.getSuppressions().contains(ruleViolation.getBeginLine());
    ruleViolation.setSuppressed(suppressed);
  }

  private void findLogicalLocation(DelphiNode node) {
    Tree unitNode = findUnitNode(node);

    if (unitNode != null) {
      StringBuilder name = new StringBuilder();
      for (int i = 0; i < unitNode.getChildCount(); ++i) {
        name.append(unitNode.getChild(i).getText());
      }

      ruleViolation.setPackageName(name.toString());
    }

    Tree classTypeNode = node.getAncestor(DelphiLexer.TkNewType);
    if (classTypeNode != null) {
      Tree classNameNode = classTypeNode.getChild(0).getChild(0);
      ruleViolation.setClassName(classNameNode.getText());
    }

    Tree methodNode = findMethodNode(node);

    if (methodNode != null) {
      StringBuilder qualifiedName = new StringBuilder();
      Tree nameNode = ((CommonTree) methodNode).getFirstChildWithType(DelphiLexer.TkFunctionName);
      for (int i = 0; i < nameNode.getChildCount(); ++i) {
        qualifiedName.append(nameNode.getChild(i).getText());
      }

      String methodName = qualifiedName.toString();
      int dotIndex = methodName.lastIndexOf('.');

      if (dotIndex > 0) {
        ruleViolation.setClassName(methodName.substring(0, dotIndex));
        ruleViolation.setMethodName(methodName.substring(dotIndex + 1));
      } else {
        ruleViolation.setMethodName(methodName);
      }
    }
  }

  private Tree findUnitNode(DelphiNode node) {
    return node.getASTTree().getFirstChildWithType(DelphiLexer.UNIT);
  }

  private Tree findMethodNode(DelphiNode node) {
    // Node is inside of the method signature
    for (int methodType : METHOD_TYPES) {
      Tree ancestor = node.getAncestor(methodType);

      if (ancestor != null) {
        return ancestor;
      }
    }

    // Node is inside of the method declaration section
    DelphiNode blockDeclSection = (DelphiNode) node.getAncestor(DelphiLexer.TkBlockDeclSection);
    if (blockDeclSection != null) {
      return findMethodNodeFromBlockDeclSection(blockDeclSection);
    }

    // Node is inside of the method body
    Tree currentNode = node;

    while ((currentNode = currentNode.getAncestor(DelphiLexer.BEGIN)) != null) {
      Tree methodNode = findMethodNodeFromBeginNode(currentNode);
      if (methodNode != null) {
        return methodNode;
      }
    }

    return null;
  }

  private Tree findMethodNodeFromBlockDeclSection(DelphiNode blockDeclSection) {
    Tree prevNode = blockDeclSection.prevNode();
    if (isMethodNode(prevNode)) {
      return prevNode;
    }

    return null;
  }

  private Tree findMethodNodeFromBeginNode(Tree beginNode) {
    int index = beginNode.getChildIndex();

    if (index > 1) {
      Tree parent = beginNode.getParent();
      Tree node = parent.getChild(index - 2);

      if (isMethodNode(node)) {
        return node;
      }
    }

    return null;
  }

  private static boolean isMethodNode(Tree node) {
    for (int methodType : METHOD_TYPES) {
      if (methodType == node.getType()) {
        return true;
      }
    }
    return false;
  }
}
