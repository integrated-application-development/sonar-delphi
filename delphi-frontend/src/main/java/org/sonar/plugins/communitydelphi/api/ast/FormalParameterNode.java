package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Typed;

public interface FormalParameterNode extends DelphiNode, Typed {
  List<FormalParameterData> getParameters();

  @Nullable
  TypeNode getTypeNode();

  default ExpressionNode getDefaultValue() {
    return getFirstChildOfType(ExpressionNode.class);
  }

  default boolean isOut() {
    return getFirstChildWithTokenType(DelphiTokenType.OUT) != null;
  }

  default boolean isVar() {
    return getFirstChildWithTokenType(DelphiTokenType.VAR) != null;
  }

  default boolean isConst() {
    return getFirstChildWithTokenType(DelphiTokenType.CONST) != null;
  }

  interface FormalParameterData extends Typed {
    NameDeclarationNode getNode();

    @Override
    @Nonnull
    Type getType();

    String getImage();

    boolean hasDefaultValue();

    ExpressionNode getDefaultValue();

    boolean isOut();

    boolean isVar();

    boolean isConst();
  }
}
