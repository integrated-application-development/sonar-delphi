package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.antlr.ast.node.GenericDefinitionNode.TypeParameter;
import au.com.integradev.delphi.symbol.NameDeclaration;
import au.com.integradev.delphi.symbol.NameOccurrence;
import java.util.List;

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
