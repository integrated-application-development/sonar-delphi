package org.sonar.plugins.delphi.antlr.ast;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.Tree;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodDeclarationNode;

public class DelphiTreeAdaptorTest {
  private final DelphiTreeAdaptor adaptor = new DelphiTreeAdaptor();

  @Test
  public void testNullDupTree() {
    assertThat(adaptor.dupTree(null)).isNull();
  }

  @Test
  public void testDupTree() {
    Object node = adaptor.create(Token.INVALID_TOKEN);
    Object parent = adaptor.create(Token.INVALID_TOKEN);
    Object child = adaptor.create(Token.INVALID_TOKEN);

    adaptor.addChild(node, child);
    adaptor.setParent(node, parent);

    Object newTree = adaptor.dupTree(node, parent);
    assertThat(adaptor.getParent(newTree)).isEqualTo(parent);
    assertThat(adaptor.getChildCount(newTree)).isEqualTo(1);
  }

  @Test
  public void testDupNode() {
    MethodDeclarationNode methodNode = new MethodDeclarationNode(DelphiLexer.TkMethodDeclaration);
    Object dupNode = adaptor.dupNode(methodNode);
    assertThat(methodNode).isNotEqualTo(dupNode).isInstanceOf(dupNode.getClass());
  }

  @Test
  public void testBecomeRoot() {
    Object oldRoot = adaptor.create(Token.INVALID_TOKEN);
    Object newRoot = adaptor.create(Token.INVALID_TOKEN);
    assertThat(adaptor.becomeRoot(newRoot, oldRoot)).isEqualTo(newRoot);
    assertThat(adaptor.getParent(oldRoot)).isEqualTo(newRoot);
  }

  @Test
  public void testBecomeRootWithNullOldRoot() {
    Object newRoot = adaptor.create(Token.INVALID_TOKEN);
    assertThat(adaptor.becomeRoot(newRoot, null)).isEqualTo(newRoot);
  }

  @Test
  public void testBecomeRootWithNilNewRootWithSingleChild() {
    Object oldRoot = adaptor.create(Token.INVALID_TOKEN);
    Object newRoot = adaptor.nil();
    Object child = adaptor.create(Token.INVALID_TOKEN);
    adaptor.addChild(newRoot, child);
    assertThat(adaptor.becomeRoot(newRoot, oldRoot)).isEqualTo(child);
  }

  @Test
  public void testBecomeRootWithNilNewRootWithMultipleChildren() {
    Object oldRoot = adaptor.create(Token.INVALID_TOKEN);
    Object newRoot = adaptor.nil();
    adaptor.addChild(newRoot, adaptor.create(Token.INVALID_TOKEN));
    adaptor.addChild(newRoot, adaptor.create(Token.INVALID_TOKEN));
    assertThatThrownBy(() -> adaptor.becomeRoot(newRoot, oldRoot))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  public void testDupNodeInstantiationError() {
    assertThatThrownBy(() -> adaptor.dupNode(Tree.INVALID_NODE)).isInstanceOf(AssertionError.class);
  }

  @Test
  public void testRulePostProcessing() {
    Object root = adaptor.create(Token.INVALID_TOKEN);
    assertThat(adaptor.rulePostProcessing(root)).isEqualTo(root);
  }

  @Test
  public void testRulePostProcessingNilWithSingleChild() {
    Object root = adaptor.nil();
    Object child = adaptor.create(Token.INVALID_TOKEN);
    adaptor.addChild(root, child);
    assertThat(adaptor.rulePostProcessing(root)).isEqualTo(child);
  }

  @Test
  public void testRulePostProcessingNilWithoutChildren() {
    Object root = adaptor.nil();
    assertThat(adaptor.rulePostProcessing(root)).isNull();
  }

  @Test
  public void testGetType() {
    Object node = adaptor.create(Token.INVALID_TOKEN);
    assertThat(adaptor.getType(node)).isEqualTo(Token.INVALID_TOKEN_TYPE);
  }

  @Test
  public void testGetParent() {
    Object parent = adaptor.create(Token.INVALID_TOKEN);
    Object child = adaptor.create(Token.INVALID_TOKEN);
    adaptor.addChild(parent, child);
    assertThat(adaptor.getParent(child)).isEqualTo(parent);
  }

  @Test
  public void testErrorNode() {
    assertThat(adaptor.errorNode(null, null, null, null)).isNull();
  }

  @Test
  public void testCreateToken() {
    assertThat(adaptor.createToken(Token.INVALID_TOKEN_TYPE, "")).isInstanceOf(CommonToken.class);
  }

  @Test
  public void testCreateTokenFromToken() {
    CommonToken token = new CommonToken(DelphiLexer.TkRootNode);
    assertThat(adaptor.createToken(token)).hasToString(token.toString());
  }

  @Test
  public void testCreateDelphiNode() {
    Object node = adaptor.create(Token.INVALID_TOKEN_TYPE, "");
    assertThat(node).isInstanceOf(DelphiNode.class);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetTokenStartIndex() {
    adaptor.getTokenStartIndex(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetTokenStopIndex() {
    adaptor.getTokenStopIndex(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testDeleteChild() {
    adaptor.deleteChild(null, 0);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testReplaceChildren() {
    adaptor.replaceChildren(null, 0, 0, null);
  }
}
