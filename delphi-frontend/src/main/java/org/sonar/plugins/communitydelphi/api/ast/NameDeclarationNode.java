package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;
import org.sonar.plugins.communitydelphi.api.ast.GenericDefinitionNode.TypeParameter;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;

public interface NameDeclarationNode extends DelphiNode {
  GenericDefinitionNode getGenericDefinition();

  List<TypeParameter> getTypeParameters();

  NameDeclaration getNameDeclaration();

  List<NameOccurrence> getUsages();

  DeclarationKind getKind();

  enum DeclarationKind {
    CONST,
    ENUM_ELEMENT,
    EXCEPT_ITEM,
    FIELD,
    IMPORT,
    INLINE_CONST,
    INLINE_VAR,
    LOOP_VAR,
    METHOD,
    PARAMETER,
    PROPERTY,
    RECORD_VARIANT_TAG,
    TYPE,
    TYPE_PARAMETER,
    VAR,
    UNIT
  }
}
