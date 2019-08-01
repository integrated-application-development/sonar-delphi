package org.sonar.plugins.delphi.pmd.rules;

import java.util.List;
import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

/**
 * This rule adds violations when the Duplicates method, (foo.Duplicates := dupError) is called on a
 * list, but the preceding line did not first sort the list (using foo.Sorted := True)
 */
public class DuplicatesRule extends DelphiRule {

  private String listName;

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    List children = node.getChildren();
    if (children == null) {
      return;
    }

    for (int i = 0; i < children.size(); i++) {
      if (isDuplicatesLine(children, i)) {
        if (isDupAccept(children, i)) {
          return;
        }

        listName = getListNameBackwards(children, i);

        if (sortedOnPreviousLine(children, i) || sortedOnNextLine(children, i)) {
          return;
        }

        addViolation(ctx, (DelphiPMDNode) children.get(i + 2));
      }
    }
  }

  private boolean isDuplicatesLine(List children, int childIndex) {
    if (childIndex < 2 || childIndex > children.size() - 2) {
      return false;
    }

    Tree dot = (Tree) children.get(childIndex - 1);
    Tree duplicates = (Tree) children.get(childIndex);
    Tree assign = (Tree) children.get(childIndex + 1);

    if (dot.getType() != DelphiLexer.DOT
        || duplicates.getType() != DelphiLexer.TkIdentifier
        || assign.getType() != DelphiLexer.ASSIGN) {
      return false;
    }

    return duplicates.getText().equalsIgnoreCase("duplicates");
  }

  private boolean isDupAccept(List children, int childIndex) {
    Tree dupType = (Tree) children.get(childIndex + 2);
    return dupType.getText().equalsIgnoreCase("dupAccept");
  }

  private boolean sortedOnPreviousLine(List children, int childIndex) {
    int index = findPreviousLineStart(children, childIndex);
    if (index == -1) {
      return false;
    }

    return sortedOnLine(children, index);
  }

  private boolean sortedOnNextLine(List children, int childIndex) {
    int index = findNextLineStart(children, childIndex);
    if (index == -1) {
      return false;
    }

    return sortedOnLine(children, index);
  }

  private boolean sortedOnLine(List children, int childIndex) {
    Tree identStart = (Tree) children.get(childIndex++);

    if (identStart.getType() != DelphiLexer.TkIdentifier) {
      return false;
    }

    StringBuilder builder = new StringBuilder(identStart.getText());
    boolean sortedFound = false;

    while (childIndex < children.size()) {
      Tree dot = (Tree) children.get(childIndex);
      if (dot.getType() != DelphiLexer.DOT) {
        return false;
      }

      Tree namePartNode = (Tree) children.get(++childIndex);
      String namePart = namePartNode.getText();

      if (namePart.equalsIgnoreCase("sorted")) {
        sortedFound = true;
        break;
      }

      builder.append(".").append(namePart);
      ++childIndex;
    }

    if (!sortedFound) {
      return false;
    }

    String sortedName = builder.toString();

    if (sortedName.isEmpty() || !listName.equalsIgnoreCase(sortedName)) {
      return false;
    }

    return sortedEqualsTrue(children, childIndex);
  }

  private boolean sortedEqualsTrue(List children, int childIndex) {
    if (childIndex + 2 >= children.size()) {
      return false;
    }

    Tree assignNode = (Tree) children.get(++childIndex);
    Tree trueNode = (Tree) children.get(++childIndex);

    return assignNode.getType() == DelphiLexer.ASSIGN && trueNode.getType() == DelphiLexer.TRUE;
  }

  private int findPreviousLineStart(List children, int childIndex) {
    boolean foundSemicolon = false;
    while (childIndex-- > 0) {
      int type = ((Tree) children.get(childIndex)).getType();
      if (type == DelphiLexer.SEMI) {
        if (foundSemicolon) {
          return childIndex + 1;
        }
        foundSemicolon = true;
      }
    }

    if (foundSemicolon) {
      return 0;
    }

    return -1;
  }

  private int findNextLineStart(List children, int childIndex) {
    while (++childIndex < children.size()) {
      Tree child = (Tree) children.get(childIndex);
      if (child.getType() == DelphiLexer.SEMI) {
        return childIndex + 1;
      }
    }

    return -1;
  }

  private String getListNameBackwards(List children, int childIndex) {
    StringBuilder name = new StringBuilder();
    childIndex -= 2;

    while (childIndex >= 0) {
      Tree child = (Tree) children.get(childIndex);
      int type = child.getType();

      if (type != DelphiLexer.TkIdentifier && type != DelphiLexer.DOT) {
        break;
      }

      name.insert(0, child.getText());
      childIndex--;
    }

    return name.toString();
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
