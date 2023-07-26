package org.sonar.plugins.communitydelphi.api.ast;

import java.util.Deque;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.symbol.Qualifiable;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Typed;

public interface TypeDeclarationNode extends DelphiNode, Typed, Qualifiable {
  SimpleNameDeclarationNode getTypeNameNode();

  TypeNode getTypeNode();

  @Nullable
  TypeNameDeclaration getTypeNameDeclaration();

  String qualifiedNameExcludingUnit();

  Deque<TypeDeclarationNode> getOuterTypeDeclarationNodes();

  boolean isClass();

  boolean isClassHelper();

  boolean isClassReference();

  boolean isEnum();

  boolean isInterface();

  boolean isObject();

  boolean isRecord();

  boolean isRecordHelper();

  boolean isPointer();

  boolean isNestedType();

  boolean isTypeAlias();

  boolean isTypeType();

  boolean isForwardDeclaration();
}
