package org.sonar.plugins.delphi.pmd.rules;

import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/**
 * Checks the name declarations used for the names of classes and enumerated class types, and raises
 * a violation if they do not begin with 'T', 'TForm', or 'E'
 *
 * <p>This is done by searching for nodes of the type 'TkNewType', and getting the next child of
 * that node which contains the name of the class or enumeration declaration. Within the context of
 * the API this seems to be the best way to find both class and enumerations (as there is no
 * enumeration keyword in Delphi), but may give some false positives.
 */
public class ClassNameRule extends NameConventionRule {
  private static final String[] PREFIXES = {"T", "TForm", "E"};

  @Override
  public DelphiPMDNode findNode(DelphiPMDNode node) {
    if (node.getType() != DelphiLexer.TkNewTypeName) {
      return null;
    }

    // Other name conventions are handled in InterfaceNameRule, RecordNameRule, and PointerNameRule
    Tree typeDeclNode = node.nextNode();
    int type = typeDeclNode.getChild(0).getType();

    if (type == DelphiLexer.TkInterface
        || type == DelphiLexer.TkRecord
        || type == DelphiLexer.POINTER2) {
      return null;
    }

    return new DelphiPMDNode((CommonTree) node.getChild(0), node.getASTTree());
  }

  @Override
  protected boolean isViolation(DelphiPMDNode nameNode) {
    return !compliesWithPrefixNamingConvention(nameNode.getText(), PREFIXES);
  }
}
