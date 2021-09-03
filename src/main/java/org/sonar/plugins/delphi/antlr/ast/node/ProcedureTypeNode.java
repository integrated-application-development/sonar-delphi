package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.stream.Collectors;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.parameter.FormalParameter;

public final class ProcedureTypeNode extends ProceduralTypeNode {
  public ProcedureTypeNode(Token token) {
    super(token);
  }

  public ProcedureTypeNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  @NotNull
  public Type createType() {
    return getTypeFactory()
        .procedure(
            getParameters().stream()
                .map(FormalParameter::create)
                .collect(Collectors.toUnmodifiableList()),
            getReturnType());
  }
}
