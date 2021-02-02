package org.sonar.plugins.delphi.antlr.ast.node;

import static java.util.Collections.emptyList;

import com.google.common.base.Suppliers;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.delphi.symbol.resolve.NameResolutionHelper;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;

public final class ForInStatementNode extends ForStatementNode {
  private final Supplier<NameResolutionHelper> nameResolutionHelper =
      Suppliers.memoize(
          () -> new NameResolutionHelper(getASTTree().getDelphiFile().getTypeFactory()));

  private final Supplier<MethodNameDeclaration> getEnumeratorDeclaration =
      Suppliers.memoize(
          () ->
              nameResolutionHelper
                  .get()
                  .findMethodMember(this, getEnumerable().getType(), "GetEnumerator", emptyList()));

  private final Supplier<MethodNameDeclaration> moveNextDeclaration =
      Suppliers.memoize(
          () ->
              nameResolutionHelper
                  .get()
                  .findMethodMember(this, getEnumeratorType(), "MoveNext", emptyList()));

  private final Supplier<PropertyNameDeclaration> currentDeclaration =
      Suppliers.memoize(
          () ->
              nameResolutionHelper
                  .get()
                  .findPropertyMember(this, getEnumeratorType(), "Current", emptyList()));

  public ForInStatementNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Nullable
  public MethodNameDeclaration getGetEnumeratorDeclaration() {
    return getEnumeratorDeclaration.get();
  }

  @Nullable
  public MethodNameDeclaration getMoveNextDeclaration() {
    return moveNextDeclaration.get();
  }

  @Nullable
  public PropertyNameDeclaration getCurrentDeclaration() {
    return currentDeclaration.get();
  }

  private Type getEnumeratorType() {
    MethodNameDeclaration enumerator = getGetEnumeratorDeclaration();
    if (enumerator != null) {
      return enumerator.getReturnType();
    }
    return DelphiType.unknownType();
  }

  public ExpressionNode getEnumerable() {
    return (ExpressionNode) jjtGetChild(2);
  }
}
