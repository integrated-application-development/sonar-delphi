package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.Objects;
import javax.annotation.Nullable;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.symbol.DelphiScope;
import org.sonar.plugins.delphi.symbol.TypeNameDeclaration;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ScopedType;

public final class MethodImplementationNode extends MethodNode {
  private MethodBodyNode methodBody;
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
    if (methodBody == null) {
      methodBody = (MethodBodyNode) jjtGetChild(1);
    }
    return methodBody;
  }

  @Override
  public NameReferenceNode getMethodName() {
    return Objects.requireNonNull(getMethodHeading().getMethodNameNode().getNameReferenceNode());
  }

  @Override
  @Nullable
  public TypeNameDeclaration getTypeDeclaration() {
    if (typeDeclaration == null) {
      NameReferenceNode name = getMethodName();
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

  @Nullable
  public DelphiScope getTypeScope() {
    if (getTypeDeclaration() != null) {
      Type type = getTypeDeclaration().getType();
      if (type instanceof ScopedType) {
        return ((ScopedType) type).typeScope();
      }
    }
    return null;
  }
}
