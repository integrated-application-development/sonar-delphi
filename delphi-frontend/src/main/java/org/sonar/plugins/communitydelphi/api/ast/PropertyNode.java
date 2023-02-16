package org.sonar.plugins.communitydelphi.api.ast;

import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Typed;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode.FormalParameterData;

public interface PropertyNode extends DelphiNode, Typed, Visibility {
  NameDeclarationNode getPropertyName();

  TypeNode getTypeNode();

  @Nullable
  FormalParameterListNode getParameterListNode();

  @Nullable
  PropertyReadSpecifierNode getReadSpecifier();

  @Nullable
  PropertyWriteSpecifierNode getWriteSpecifier();

  List<FormalParameterData> getParameters();

  List<Type> getParameterTypes();

  boolean isClassProperty();

  boolean isDefaultProperty();
}
