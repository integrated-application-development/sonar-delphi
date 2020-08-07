package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;

public final class MethodImplementationNode extends MethodNode {
  private TypeNameDeclaration typeDeclaration;

  public MethodImplementationNode(Token token) {
    super(token);
  }

  public MethodImplementationNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public MethodBodyNode getMethodBody() {
    return (MethodBodyNode) jjtGetChild(1);
  }

  @Nullable
  public BlockDeclarationSectionNode getDeclarationSection() {
    return fromBody(MethodBodyNode::getDeclarationSection);
  }

  @Nullable
  public DelphiNode getBlock() {
    return fromBody(MethodBodyNode::getBlock);
  }

  @Nullable
  public CompoundStatementNode getStatementBlock() {
    return fromBody(MethodBodyNode::getStatementBlock);
  }

  @Nullable
  public AsmStatementNode getAsmBlock() {
    return fromBody(MethodBodyNode::getAsmBlock);
  }

  @Nullable
  private <T> T fromBody(Function<MethodBodyNode, T> getter) {
    MethodBodyNode body = getMethodBody();
    if (body != null) {
      return getter.apply(body);
    }
    return null;
  }

  public boolean isEmptyMethod() {
    AsmStatementNode asmBlock = getAsmBlock();
    CompoundStatementNode statementBlock = getStatementBlock();
    return (asmBlock != null && asmBlock.isEmpty())
        || (statementBlock != null && statementBlock.isEmpty());
  }

  @Override
  @Nullable
  public TypeNameDeclaration getTypeDeclaration() {
    if (typeDeclaration == null) {
      NameReferenceNode name = findTopLevelMethod().getMethodNameNode().getNameReferenceNode();
      while (name != null) {
        NameDeclaration nameDecl = name.getNameDeclaration();
        if (nameDecl instanceof TypeNameDeclaration) {
          typeDeclaration = (TypeNameDeclaration) nameDecl;
        }
        name = name.nextName();
      }
    }
    return typeDeclaration;
  }

  private MethodImplementationNode findTopLevelMethod() {
    MethodImplementationNode result = this;
    MethodImplementationNode nextMethod;
    while ((nextMethod = result.getFirstParentOfType(MethodImplementationNode.class)) != null) {
      result = nextMethod;
    }
    return result;
  }

  @NotNull
  public NameReferenceNode getNameReferenceNode() {
    return getMethodNameNode().getNameReferenceNode();
  }

  @Override
  public VisibilityType createVisibility() {
    return VisibilityType.PUBLIC;
  }
}
