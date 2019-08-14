package org.sonar.plugins.delphi.antlr.ast;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.dfa.DataFlowNode;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class DelphiNodeTest {

  private static final String TEST_FILE = "/org/sonar/plugins/delphi/grammar/GrammarTest.pas";
  private ASTTree ast = new DelphiAST(DelphiUtils.getResource(TEST_FILE));
  private DelphiNode node;

  @Before
  public void setup() {
    node = (DelphiNode) ast.getChild(0);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testJjtOpen() {
    node.jjtOpen();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testJjtClose() {
    node.jjtClose();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testJjtSetParent() {
    node.jjtSetParent(mock(Node.class));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testJjtGetParent() {
    node.jjtGetParent();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testJjtAddChild() {
    node.jjtAddChild(mock(Node.class), 0);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testJjtGetChild() {
    node.jjtGetChild(0);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testJjtSetChildIndex() {
    node.jjtSetChildIndex(0);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testJjtGetChildIndex() {
    node.jjtGetChildIndex();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testJjtGetNumChildren() {
    node.jjtGetNumChildren();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testJjtGetId() {
    node.jjtGetId();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetAsDocument() {
    node.getAsDocument();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetUserData() {
    node.getUserData();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetImage() {
    node.getImage();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSetImage() {
    node.setImage("");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testHasImageEqualTo() {
    node.hasImageEqualTo("");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetDataFlowNode() {
    node.getDataFlowNode();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSetDataFlowNode() {
    node.setDataFlowNode(mock(DataFlowNode.class));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testIsFindBoundary() {
    node.isFindBoundary();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetNthParent() {
    node.getNthParent(0);
  }

  @SuppressWarnings("unchecked")
  @Test(expected = UnsupportedOperationException.class)
  public void testGetFirstParentOfType() {
    node.getFirstParentOfAnyType(Object.class);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetParentsOfType() {
    node.getParentsOfType(Object.class);
  }

  @SuppressWarnings("unchecked")
  @Test(expected = UnsupportedOperationException.class)
  public void testGetFirstParentOfAnyType() {
    node.getFirstParentOfAnyType();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testFindChildrenOfType() {
    node.findChildrenOfType(Object.class);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testFindDescendantOfType() {
    node.findDescendantsOfType(Object.class);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testFindDescendantsOfTypeOverload() {
    node.findDescendantsOfType(Object.class, new ArrayList<>(), true);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetFirstChildOfType() {
    node.getFirstParentOfType(Object.class);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetFirstDescendantOfType() {
    node.getFirstDescendantOfType(Object.class);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testHasDescendantOfType() {
    node.hasDescendantOfType(Object.class);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testFindChildNodesWithXPath() {
    node.findChildNodesWithXPath("");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testHasDescendantMatchingXPath() {
    node.hasDescendantMatchingXPath("");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSetUserData() {
    node.setUserData(mock(Object.class));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetScope() {
    node.getScope();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testRemove() {
    node.remove();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testRemoveChildAtIndex() {
    node.removeChildAtIndex(0);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetXPathNodeName() {
    node.getXPathNodeName();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetXPathAttributesIterator() {
    node.getXPathAttributesIterator();
  }
}
