package org.sonar.plugins.communitydelphi.api.ast;

import au.com.integradev.delphi.symbol.Qualifiable;
import au.com.integradev.delphi.symbol.declaration.TypeNameDeclaration;
import au.com.integradev.delphi.type.Typed;
import java.util.Deque;
import org.jetbrains.annotations.Nullable;

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
