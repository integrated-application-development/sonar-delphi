package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.symbol.scope.FileScope;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;

public final class ConstArraySubTypeNode extends TypeNode {
  public ConstArraySubTypeNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  @NotNull
  public Type createType() {
    FileScope fileScope = getASTTree().getScope().getEnclosingScope(FileScope.class);
    if (fileScope != null) {
      TypeNameDeclaration varRec = fileScope.getSystemScope().getTVarRecDeclaration();
      return varRec.getType();
    }
    return DelphiType.unknownType();
  }
}
