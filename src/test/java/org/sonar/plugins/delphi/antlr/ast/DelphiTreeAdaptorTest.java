package org.sonar.plugins.delphi.antlr.ast;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.Tree;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodDeclarationNode;

class DelphiTreeAdaptorTest {
  private final DelphiTreeAdaptor adaptor = new DelphiTreeAdaptor();

  @Test
  void testNullDupTree() {
    assertThat(adaptor.dupTree(null)).isNull();
  }

  @Test
  void testDupTree() {
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
  void testDupNode() {
    MethodDeclarationNode methodNode = new MethodDeclarationNode(DelphiLexer.TkMethodDeclaration);
    Object dupNode = adaptor.dupNode(methodNode);
    assertThat(methodNode).isNotEqualTo(dupNode).isInstanceOf(dupNode.getClass());
  }

  @Test
  void testBecomeRoot() {
    Object oldRoot = adaptor.create(Token.INVALID_TOKEN);
    Object newRoot = adaptor.create(Token.INVALID_TOKEN);
    assertThat(adaptor.becomeRoot(newRoot, oldRoot)).isEqualTo(newRoot);
    assertThat(adaptor.getParent(oldRoot)).isEqualTo(newRoot);
  }

  @Test
  void testBecomeRootWithNullOldRoot() {
    Object newRoot = adaptor.create(Token.INVALID_TOKEN);
    assertThat(adaptor.becomeRoot(newRoot, null)).isEqualTo(newRoot);
  }

  @Test
  void testBecomeRootWithNilNewRootWithSingleChild() {
    Object oldRoot = adaptor.create(Token.INVALID_TOKEN);
    Object newRoot = adaptor.nil();
    Object child = adaptor.create(Token.INVALID_TOKEN);
    adaptor.addChild(newRoot, child);
    assertThat(adaptor.becomeRoot(newRoot, oldRoot)).isEqualTo(child);
  }

  @Test
  void testBecomeRootWithNilNewRootWithMultipleChildren() {
    Object oldRoot = adaptor.create(Token.INVALID_TOKEN);
    Object newRoot = adaptor.nil();
    adaptor.addChild(newRoot, adaptor.create(Token.INVALID_TOKEN));
    adaptor.addChild(newRoot, adaptor.create(Token.INVALID_TOKEN));
    assertThatThrownBy(() -> adaptor.becomeRoot(newRoot, oldRoot))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void testDupNodeInstantiationError() {
    assertThatThrownBy(() -> adaptor.dupNode(Tree.INVALID_NODE)).isInstanceOf(AssertionError.class);
  }

  @Test
  void testRulePostProcessing() {
    Object root = adaptor.create(Token.INVALID_TOKEN);
    assertThat(adaptor.rulePostProcessing(root)).isEqualTo(root);
  }

  @Test
  void testRulePostProcessingNilWithSingleChild() {
    Object root = adaptor.nil();
    Object child = adaptor.create(Token.INVALID_TOKEN);
    adaptor.addChild(root, child);
    assertThat(adaptor.rulePostProcessing(root)).isEqualTo(child);
  }

  @Test
  void testRulePostProcessingNilWithoutChildren() {
    Object root = adaptor.nil();
    assertThat(adaptor.rulePostProcessing(root)).isNull();
  }

  @Test
  void testGetType() {
    Object node = adaptor.create(Token.INVALID_TOKEN);
    assertThat(adaptor.getType(node)).isEqualTo(Token.INVALID_TOKEN_TYPE);
  }

  @Test
  void testGetParent() {
    Object parent = adaptor.create(Token.INVALID_TOKEN);
    Object child = adaptor.create(Token.INVALID_TOKEN);
    adaptor.addChild(parent, child);
    assertThat(adaptor.getParent(child)).isEqualTo(parent);
  }

  @Test
  void testErrorNode() {
    assertThat(adaptor.errorNode(null, null, null, null)).isNull();
  }

  @Test
  void testCreateToken() {
    assertThat(adaptor.createToken(Token.INVALID_TOKEN_TYPE, "")).isInstanceOf(CommonToken.class);
  }

  @Test
  void testCreateTokenFromToken() {
    CommonToken token = new CommonToken(DelphiLexer.TkRootNode);
    assertThat(adaptor.createToken(token)).hasToString(token.toString());
  }

  @Test
  void testCreateDelphiNode() {
    Object node = adaptor.create(Token.INVALID_TOKEN_TYPE, "");
    assertThat(node).isInstanceOf(DelphiNode.class);
  }

  @Test
  void testGetTokenStartIndex() {
    assertThatThrownBy(() -> adaptor.getTokenStartIndex(null))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void testGetTokenStopIndex() {
    assertThatThrownBy(() -> adaptor.getTokenStopIndex(null))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void testDeleteChild() {
    assertThatThrownBy(() -> adaptor.deleteChild(null, 0))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void testReplaceChildren() {
    assertThatThrownBy(() -> adaptor.replaceChildren(null, 0, 0, null))
        .isInstanceOf(UnsupportedOperationException.class);
  }
}
