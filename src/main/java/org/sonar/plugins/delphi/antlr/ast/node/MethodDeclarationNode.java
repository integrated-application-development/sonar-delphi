package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class MethodDeclarationNode extends MethodNode implements Visibility {
  private Boolean isOverride;
  private Boolean isVirtual;
  private Boolean isMessage;

  private VisibilityType visibility;

  public MethodDeclarationNode(Token token) {
    super(token);
  }

  public MethodDeclarationNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public boolean isOverride() {
    if (isOverride == null) {
      isOverride = getMethodHeading().getFirstChildWithId(DelphiLexer.OVERRIDE) != null;
    }
    return isOverride;
  }

  public boolean isVirtual() {
    if (isVirtual == null) {
      isVirtual = getMethodHeading().getFirstChildWithId(DelphiLexer.VIRTUAL) != null;
    }
    return isVirtual;
  }

  public boolean isMessage() {
    if (isMessage == null) {
      isMessage = getMethodHeading().getFirstChildWithId(DelphiLexer.MESSAGE) != null;
    }
    return isMessage;
  }

  @Override
  public VisibilityType getVisibility() {
    if (visibility == null) {
      if (jjtGetParent() instanceof VisibilitySectionNode) {
        visibility = ((VisibilitySectionNode) jjtGetParent()).getVisibility();
      } else {
        visibility = VisibilityType.PUBLIC;
      }
    }
    return visibility;
  }
}
