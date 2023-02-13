package org.sonar.plugins.communitydelphi.api.ast;

import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode.FormalParameterData;
import au.com.integradev.delphi.type.Type;
import au.com.integradev.delphi.type.Typed;
import java.util.List;
import javax.annotation.Nullable;

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
