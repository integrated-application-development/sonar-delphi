grammar Delphi;

options {
    backtrack=true;
    memoize=true;
    output=AST;
}

@header
{
/*
 * Sonar Delphi Plugin
 * Copyright (C) 2010 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package au.com.integradev.delphi.antlr;

import au.com.integradev.delphi.antlr.ast.node.*;
}

@lexer::header
{
/*
 * Sonar Delphi Plugin
 * Copyright (C) 2010 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package au.com.integradev.delphi.antlr;

}

@lexer::members {
  private boolean shouldSkipImplementation;
  private int directiveNesting = 0;
  private boolean asmMode = false;

  public DelphiLexer(CharStream input, boolean shouldSkipImplementation) {
    this(input);
    this.shouldSkipImplementation = shouldSkipImplementation;
  }

  @Override
  public void reportError(RecognitionException e) {
    String hdr = this.getErrorHeader(e);
    String msg = this.getErrorMessage(e, this.getTokenNames());
    throw new LexerException(hdr + " " + msg, e);
  }

  @Override
  public String getErrorHeader(RecognitionException e) {
    return "line " + e.line + ":" + e.charPositionInLine;
  }

  public static class LexerException extends RuntimeException {
    public LexerException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}

@parser::members {
  private Token changeTokenType(int type) {
    CommonToken t = new CommonToken(input.LT(-1));
    t.setType(type);
    return t;
  }

  private Token combineLastNTokens(int count) {
    CommonToken firstToken = (CommonToken) input.LT(-count);
    CommonToken lastToken = (CommonToken) input.LT(-1);
    lastToken.setStartIndex(firstToken.getStartIndex());
    return lastToken;
  }

  @Override
  public void reportError(RecognitionException e) {
    String hdr = this.getErrorHeader(e);
    String msg = this.getErrorMessage(e, this.getTokenNames());
    throw new ParserException(hdr + " " + msg, e);
  }

  @Override
  public String getErrorHeader(RecognitionException e) {
    return "line " + e.line + ":" + e.charPositionInLine;
  }

  private void resetBinaryExpressionTokens(Object rootExpression) {
    if (!(rootExpression instanceof BinaryExpressionNodeImpl)) {
      return;
    }

    var binaryExpression = (BinaryExpressionNodeImpl) rootExpression;

    binaryExpression.setFirstToken(null);
    binaryExpression.setLastToken(null);

    resetBinaryExpressionTokens(binaryExpression.getLeft());
    resetBinaryExpressionTokens(binaryExpression.getRight());
  }

  public static class ParserException extends RuntimeException {
    public ParserException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}

//----------------------------------------------------------------------------
// File root
//----------------------------------------------------------------------------
file                         : program | library | unit | package_
                             ;
fileWithoutImplementation    : program | library | unitWithoutImplementation | package_
                             ;

//----------------------------------------------------------------------------
// File head
//----------------------------------------------------------------------------

program                      : programHead? usesFileClause? programBody '.'
                             ;
programHead                  : 'program'<ProgramDeclarationNodeImpl>^ qualifiedNameDeclaration programParameters? ';'!
                             ;
programParameters            : '(' (ident (',' ident)* )? ')' // Used in standard Pascal; Delphi ignores them.
                             ;
programBody                  : localDeclSection? (compoundStatement | 'end')
                             ;
library                      : libraryHead usesFileClause? programBody '.'
                             ;
libraryHead                  : 'library'<LibraryDeclarationNodeImpl>^ qualifiedNameDeclaration (portabilityDirective!)* ';'!
                             ;
package_                     : packageHead requiresClause? containsClause 'end' '.'
                             ;
packageHead                  : 'package'<PackageDeclarationNodeImpl>^ qualifiedNameDeclaration ';'!
                             ;
unit                         : unitHead unitInterface unitImplementation unitBlock '.'
                             ;
unitWithoutImplementation    : unitHead unitInterface
                             ;
unitHead                     : 'unit'<UnitDeclarationNodeImpl>^ qualifiedNameDeclaration portabilityDirective* ';'!
                             ;
unitInterface                : 'interface'<InterfaceSectionNodeImpl>^ usesClause? interfaceDecl*
                             ;
unitImplementation           : 'implementation'<ImplementationSectionNodeImpl>^ usesClause? declSection*
                             ;
unitBlock                    : initializationFinalization? 'end'
                             | compoundStatement
                             ;
initializationFinalization   : initializationSection finalizationSection?
                             ;
initializationSection        : 'initialization'<InitializationSectionNodeImpl>^ statementList
                             ;
finalizationSection          : 'finalization'<FinalizationSectionNodeImpl>^ statementList
                             ;

//----------------------------------------------------------------------------
// File usage
//----------------------------------------------------------------------------
containsClause               : 'contains'<ContainsClauseNodeImpl>^ unitInFileImportList
                             ;
requiresClause               : 'requires'<RequiresClauseNodeImpl>^ unitImportList
                             ;
usesClause                   : 'uses'<UsesClauseNodeImpl>^ unitImportList
                             ;
usesFileClause               : 'uses'<UsesClauseNodeImpl>^ unitInFileImportList
                             ;
unitInFileImportList         : unitInFileImport (','! unitInFileImport)* ';'!
                             ;
unitImportList               : unitImport (','! unitImport)* ';'!
                             ;
unitImport                   : qualifiedNameDeclaration
                             -> ^(TkUnitImport<UnitImportNodeImpl> qualifiedNameDeclaration)
                             ;
unitInFileImport             : qualifiedNameDeclaration ('in' textLiteral)?
                             -> ^(TkUnitImport<UnitImportNodeImpl> qualifiedNameDeclaration ('in' textLiteral)?)
                             ;

//----------------------------------------------------------------------------
// Declarations
//----------------------------------------------------------------------------
block                        : localDeclSection? blockBody
                             ;
localDeclSection             : declSection+ -> ^(TkLocalDeclarations<LocalDeclarationSectionNodeImpl> declSection+)
                             ;
blockBody                    : compoundStatement
                             | assemblerStatement
                             ;
declSection                  : labelDeclSection
                             | constSection
                             | typeSection
                             | varSection
                             | routineImplementation
                             | exportsSection
                             ;
interfaceDecl                : constSection
                             | typeSection
                             | varSection
                             | exportsSection
                             | routineInterface
                             ;
labelDeclSection             : 'label' (label (','!)?)+ ';'
                             ;
constSection                 : ('const'<ConstSectionNodeImpl>^ | 'resourcestring'<ConstSectionNodeImpl>^) constDeclaration*
                             // constSection was changed at some point from "constDeclaration+" to "constDeclaration*" to cater to invalid includes
                             // example: "const {$include versioninfo.inc}"
                             // Is this really the appropriate solution?
                             ;
constDeclaration             : attributeList? nameDeclaration (':' varType)? '=' constExpression portabilityDirective* ';'
                             -> ^(TkConstDeclaration<ConstDeclarationNodeImpl> nameDeclaration constExpression varType? attributeList? portabilityDirective*)
                             ;
typeSection                  : 'type'<TypeSectionNodeImpl>^ typeDeclaration+
                             ;
innerTypeSection             : 'type'<TypeSectionNodeImpl>^ typeDeclaration*
                             ;
typeDeclaration              : attributeList? genericNameDeclaration '=' typeDecl portabilityDirective* ';'
                             -> ^(TkTypeDeclaration<TypeDeclarationNodeImpl> genericNameDeclaration typeDecl attributeList? portabilityDirective*)
                             ;
varSection                   : ('var'<VarSectionNodeImpl>^ | 'threadvar'<VarSectionNodeImpl>^) varDeclaration varDeclaration*
                             ;
varDeclaration               : attributeList? nameDeclarationList ':' varType portabilityDirective* varValueSpec? portabilityDirective* ';'
                             -> ^(TkVarDeclaration<VarDeclarationNodeImpl> nameDeclarationList varType varValueSpec? attributeList?)
                             ;
varValueSpec                 : 'absolute' constExpression
                             | '=' constExpression
                             ;
exportsSection               : 'exports' ident exportItem (',' ident exportItem)* ';'
                             ;
exportItem                   : ('(' formalParameterList ')')? ('index' expression)? ('name' expression)? ('resident')?
                             ;

//----------------------------------------------------------------------------
// Types
//----------------------------------------------------------------------------
typeDecl                     : arrayType
                             | setType
                             | fileType
                             | classHelperType
                             | classReferenceType
                             | classType
                             | interfaceType
                             | objectType
                             | recordType
                             | recordHelperType
                             | pointerType
                             | stringType
                             | procedureType
                             | subRangeType
                             | typeOfType
                             | strongAliasType
                             | weakAliasType
                             | enumType
                             | 'packed' typeDecl^
                             ;
varType                      : arrayType
                             | setType
                             | fileType
                             | recordType
                             | pointerType
                             | procedureType
                             | subRangeType
                             | typeReference
                             | enumType
                             | 'packed' varType^
                             ;
parameterType                : stringType
                             | fileType
                             | arrayType
                             | typeReference
                             | 'packed' parameterType^
                             ;
arrayType                    :  'array' arrayIndices? 'of' arrayElementType
                             -> ^('array'<ArrayTypeNodeImpl> 'of' arrayElementType arrayIndices? )
                             ;
lbrack                       : '['
                             | '(.'
                             ;
rbrack                       : ']'
                             | '.)'
                             ;
arrayIndices                 : lbrack (varType ','?)+ rbrack
                             -> ^(TkArrayIndices<ArrayIndicesNodeImpl> lbrack (varType ','?)+ rbrack)
                             ;
arrayElementType             : 'const'<ConstArrayElementTypeNodeImpl>
                             | varType
                             ;
setType                      : 'set'<SetTypeNodeImpl>^ 'of' varType
                             ;
fileType                     : 'file'<FileTypeNodeImpl>^ ('of' varType)?
                             ;
pointerType                  : '^'<PointerTypeNodeImpl>^ varType
                             ;
stringType                   : 'string'<StringTypeNodeImpl>^ (lbrack! expression rbrack!)?
                             ;
procedureType                : procedureOfObject
                             | procedureReference
                             | simpleProcedureType
                             ;
procedureOfObject            : procedureTypeHeading 'of' 'object'<ProcedureOfObjectTypeNodeImpl>^ ((';')? interfaceDirective)*
                             ;
procedureReference           : 'reference'<ProcedureReferenceTypeNodeImpl>^ 'to'! procedureTypeHeading
                             ;
simpleProcedureType          : procedureTypeHeading -> ^(TkProcedureType<ProcedureTypeNodeImpl> procedureTypeHeading)
                             ;
procedureTypeHeading         : 'function'<ProcedureTypeHeadingNodeImpl>^ routineParameters? routineReturnType? ((';')? interfaceDirective)*
                             | 'procedure'<ProcedureTypeHeadingNodeImpl>^ routineParameters? ((';')? interfaceDirective)*
                             ;
typeOfType                   : 'type'<TypeOfTypeNodeImpl>^ 'of' typeDecl
                             ;
strongAliasType              : 'type'<StrongAliasTypeNodeImpl>^ typeReference codePageExpression?
                             ;
codePageExpression           : '('! expression ')'!
                             ;
weakAliasType                : typeReference -> ^(TkWeakAlias<WeakAliasTypeNodeImpl> typeReference)
                             ;
subRangeType                 : expression '..'<SubRangeTypeNodeImpl>^ expression
                             ;
enumType                     : '('<EnumTypeNodeImpl>^ (enumTypeElement (',')?)* ')'!
                             ;
enumTypeElement              : nameDeclaration ('=' expression)? -> ^(TkEnumElement<EnumElementNodeImpl> nameDeclaration expression?)
                             ;
typeReference                : stringType
                             | 'file'<FileTypeNodeImpl>^
                             | nameReference -> ^(TkTypeReference<TypeReferenceNodeImpl> nameReference)
                             ;

//----------------------------------------------------------------------------
// Struct Types
//----------------------------------------------------------------------------
classReferenceType           : 'class'<ClassReferenceTypeNodeImpl>^ 'of' typeReference
                             ;
classType                    : 'class' classState? classParent? (visibilitySection* 'end')?
                             -> ^('class'<ClassTypeNodeImpl> classParent? classState? (visibilitySection* 'end')?)
                             ;
classState                   : 'sealed'
                             | 'abstract'
                             ;
classParent                  : '(' typeReference (',' typeReference)* ')'
                             -> ^(TkClassParents<AncestorListNodeImpl> typeReference typeReference*)
                             ;
visibilitySection            : visibilitySection_ -> ^(TkVisibilitySection<VisibilitySectionNodeImpl> visibilitySection_)
                             ;
visibilitySection_           : visibility visibilitySectionItem*
                             | visibilitySectionItem+
                             ;
visibilitySectionItem        : fieldSection
                             | routineInterface
                             | methodResolutionClause
                             | property
                             | constSection
                             | innerTypeSection
                             ;
fieldSectionKey              : 'var'
                             | 'threadvar'
                             ;
fieldSection                 : 'class'? fieldSectionKey fieldDecl* -> ^(TkFieldSection<FieldSectionNodeImpl> 'class'? fieldSectionKey fieldDecl*)
                             | fieldDecl+ -> ^(TkFieldSection<FieldSectionNodeImpl> fieldDecl+)
                             ;
fieldDecl                    : attributeList? nameDeclarationList ':' varType portabilityDirective* ';'?
                             -> ^(TkFieldDeclaration<FieldDeclarationNodeImpl> nameDeclarationList varType portabilityDirective* attributeList? ';'?)
                             ;
classHelperType              : 'class'<ClassHelperTypeNodeImpl>^ 'helper' classParent? 'for' typeReference visibilitySection* 'end'
                             ;
interfaceType                : ('interface'<InterfaceTypeNodeImpl>^ | 'dispinterface'<InterfaceTypeNodeImpl>^) classParent? (interfaceGuid? interfaceItems? 'end')?
                             ;
interfaceGuid                : lbrack expression rbrack -> ^(TkGuid<InterfaceGuidNodeImpl> expression)
                             ;
interfaceItems               : interfaceItem+ -> ^(TkVisibilitySection<VisibilitySectionNodeImpl> interfaceItem+)
                             ;
interfaceItem                : routineInterface
                             | property
                             ;
objectType                   : 'object'<ObjectTypeNodeImpl>^ classParent? visibilitySection* 'end' // Obselete, kept for backwards compatibility with Turbo Pascal
                             ;                                                                     // See: https://www.oreilly.com/library/view/delphi-in-a/1565926595/re192.html
recordType                   : 'record'<RecordTypeNodeImpl>^ visibilitySection* recordVariantSection? 'end' ('align' constExpression)?
                             ;
recordVariantSection         : 'case'<RecordVariantSectionNodeImpl>^ recordVariantTag 'of' recordVariant+
                             ;
recordVariantTag             : (nameDeclaration ':')? typeReference
                             -> ^(TkRecordVariantTag<RecordVariantTagNodeImpl> nameDeclaration? typeReference)
                             ;
recordVariant                : expressionList ':' '(' fieldDecl* recordVariantSection? ')' ';'?
                             -> ^(TkRecordVariantItem<RecordVariantItemNodeImpl> expressionList fieldDecl* recordVariantSection? ';'?)
                             ;
recordHelperType             : 'record'<RecordHelperTypeNodeImpl>^ 'helper' 'for' typeReference visibilitySection* 'end'
                             ;
property                     : attributeList? 'class'? 'property' nameDeclaration propertyArray? (':' varType)? (propertyDirective)* ';'
                             -> ^('property'<PropertyNodeImpl> nameDeclaration propertyArray? varType? 'class'? attributeList? propertyDirective*)
                             ;
propertyArray                : lbrack! formalParameterList rbrack!
                             ;
propertyDirective            : ';' 'default'
                             | 'default' expression
                             | propertyReadWrite
                             | propertyDispInterface
                             | IMPLEMENTS typeReference (',' typeReference)*
                             | 'index' expression
                             | 'nodefault'
                             | STORED expression
                             ;
propertyReadWrite            : ('read'<PropertyReadSpecifierNodeImpl>^ | 'write'<PropertyWriteSpecifierNodeImpl>^) primaryExpression
                             ;
propertyDispInterface        : 'readonly'
                             | 'writeonly'
                             | dispIDDirective
                             ;
visibility                   : STRICT? 'protected'<VisibilityNodeImpl>^
                             | STRICT? 'private'<VisibilityNodeImpl>^
                             | 'public'<VisibilityNodeImpl>
                             | 'published'<VisibilityNodeImpl>
                             | 'automated'<VisibilityNodeImpl> // Obselete directive used for RTTI.
                             ;                                 // See: https://www.oreilly.com/library/view/delphi-in-a/1565926595/re24.html

//----------------------------------------------------------------------------
// Generics
//----------------------------------------------------------------------------
genericDefinition            : '<' typeParameterList '>'
                             -> ^(TkGenericDefinition<GenericDefinitionNodeImpl> typeParameterList)
                             ;
typeParameterList            : typeParameter (';'! typeParameter)*
                             ;
typeParameter                :  nameDeclaration (',' nameDeclaration)* (':' genericConstraint (',' genericConstraint)*)?
                             -> ^(TkTypeParameter<TypeParameterNodeImpl>
                                    nameDeclaration (nameDeclaration)* (genericConstraint genericConstraint*)?
                                 )
                             ;
genericConstraint            : typeReference
                             | 'record'
                             | 'class'
                             | 'constructor'
                             ;
genericArguments             : '<' typeReference (',' typeReference)* '>'
                             -> ^(TkGenericArguments<GenericArgumentsNodeImpl> typeReference typeReference*)
                             ;

//----------------------------------------------------------------------------
// Routines
//----------------------------------------------------------------------------
methodResolutionClause       : key=('function' | 'procedure') interfaceMethod=nameReference '=' implemented=nameReference ';'
                             -> ^(TkMethodResolveClause<MethodResolutionClauseNodeImpl>
                                    $key $interfaceMethod $implemented
                                 )
                             ;
routineInterface             : routineInterfaceHeading
                             -> ^(TkRoutineDeclaration<RoutineDeclarationNodeImpl>
                                    routineInterfaceHeading
                                 )
                             ;
routineImplementation        : fullRoutineImplementation
                             | externalRoutine
                             | forwardRoutine
                             ;
fullRoutineImplementation    : routineImplementationHeading routineBody
                             -> ^(TkRoutineImplementation<RoutineImplementationNodeImpl>
                                    routineImplementationHeading
                                    routineBody
                                 )
                             ;
externalRoutine              : externalRoutineHeading
                             -> ^(TkRoutineImplementation<RoutineImplementationNodeImpl>
                                    externalRoutineHeading
                                 )
                             ;
forwardRoutine               : forwardRoutineHeading
                             -> ^(TkRoutineDeclaration<RoutineDeclarationNodeImpl>
                                    forwardRoutineHeading
                                 )
                             ;
routineInterfaceHeading      : attributeList? 'class'? routineKey routineDeclarationName routineParameters? routineReturnType? interfaceDirectiveSection
                             -> ^(TkRoutineHeading<RoutineHeadingNodeImpl>
                                    routineKey
                                    routineDeclarationName
                                    routineParameters?
                                    routineReturnType?
                                    attributeList?
                                    'class'?
                                    interfaceDirectiveSection
                                 )
                             ;
routineImplementationHeading : attributeList? 'class'? routineKey routineImplementationName routineParameters? routineReturnType? implDirectiveSection
                             -> ^(TkRoutineHeading<RoutineHeadingNodeImpl>
                                    routineKey
                                    routineImplementationName
                                    routineParameters?
                                    routineReturnType?
                                    attributeList?
                                    'class'?
                                    implDirectiveSection
                                 )
                             ;
externalRoutineHeading       : attributeList? 'class'? routineKey routineImplementationName routineParameters? routineReturnType? externalDirectiveSection
                             -> ^(TkRoutineHeading<RoutineHeadingNodeImpl>
                                    routineKey
                                    routineImplementationName
                                    routineParameters?
                                    routineReturnType?
                                    attributeList?
                                    'class'?
                                    externalDirectiveSection
                                 )
                             ;
forwardRoutineHeading        : attributeList? 'class'? routineKey routineDeclarationName routineParameters? routineReturnType? forwardDirectiveSection
                             -> ^(TkRoutineHeading<RoutineHeadingNodeImpl>
                                    routineKey
                                    routineDeclarationName
                                    routineParameters?
                                    routineReturnType?
                                    attributeList?
                                    'class'?
                                    forwardDirectiveSection
                                 )
                             ;
routineDeclarationName       : (
                                 decl=genericNameDeclaration
                               | decl=specialOpNameDeclaration
                             )
                             -> ^(TkRoutineName<RoutineNameNodeImpl> $decl)
                             ;
routineImplementationName    : nameReference -> ^(TkRoutineName<RoutineNameNodeImpl> nameReference)
                             ;
routineKey                   : 'procedure'
                             | 'constructor'
                             | 'destructor'
                             | 'function'
                             | 'operator'
                             ;
routineReturnType            : ':' attributeList? returnType -> ^(TkRoutineReturn<RoutineReturnTypeNodeImpl> returnType attributeList?)
                             ;
returnType                   : stringType
                             | typeReference
                             ;
routineParameters            : '(' formalParameterList? ')' -> ^(TkRoutineParameters<RoutineParametersNodeImpl> '(' formalParameterList? ')')
                             ;
formalParameterList          : formalParameter (';' formalParameter)* -> ^(TkFormalParameterList<FormalParameterListNodeImpl> formalParameter formalParameter*)
                             ;
formalParameter              : a1=attributeList? (paramSpecifier a2=attributeList?)? nameDeclarationList (':' parameterType)? ('=' expression)?
                             -> ^(TkFormalParameter<FormalParameterNodeImpl> nameDeclarationList parameterType? paramSpecifier? expression? $a1? $a2?)
                             ;
paramSpecifier               : 'const'
                             | 'var'
                             | 'out'
                             ;
routineBody                  : block ';' -> ^(TkRoutineBody<RoutineBodyNodeImpl> block)
                             ;

//----------------------------------------------------------------------------
// Attributes
//----------------------------------------------------------------------------
attributeList                : attributeGroup+
                             -> ^(TkAttributeList<AttributeListNodeImpl> attributeGroup+)
                             ;
attributeGroup               : lbrack (attribute ','?)+ rbrack
                             -> ^(TkAttributeGroup<AttributeGroupNodeImpl> attribute+)
                             ;
attribute                    : nameReference argumentList?
                             -> ^(TkAttribute<AttributeNodeImpl> nameReference argumentList?)
                             ;

//----------------------------------------------------------------------------
// Expressions
//----------------------------------------------------------------------------
expression                   : relationalExpression
                             ;
// ANTLR sets the begin and end tokens for nested binary expression nodes
// in relationalOperator, not relationalExpression, meaning that their
// token range only contains the operator. resetBinaryExpressionTokens is needed
// to reset the start and end tokens so that they must be recalculated
// when retrieved (i.e., after their children have been correctly assigned).
relationalExpression         : additiveExpression (relationalOperator^ additiveExpression)* { resetBinaryExpressionTokens(root_0); }
                             ;
additiveExpression           : multiplicativeExpression (addOperator^ multiplicativeExpression)* { resetBinaryExpressionTokens(root_0); }
                             ;
multiplicativeExpression     : unaryExpression (multOperator^ unaryExpression)* { resetBinaryExpressionTokens(root_0); }
                             ;
unaryExpression              : unaryOperator^ unaryExpression
                             | primaryExpression
                             ;
primaryExpression            : atom -> ^(TkPrimaryExpression<PrimaryExpressionNodeImpl> atom)
                             | parenthesizedExpression
                             | 'inherited' (nameReference? particleItem*)? -> ^(TkPrimaryExpression<PrimaryExpressionNodeImpl> 'inherited' (nameReference? particleItem*)?)
                             ;
parenthesizedExpression      : '(' expression ')' -> ^(TkNestedExpression<ParenthesizedExpressionNodeImpl> '(' expression ')')
                             ;
atom                         : particle particleItem*
                             ;
particle                     : intNum
                             | realNum
                             | textLiteral
                             | nilLiteral
                             | nameReference
                             | arrayConstructor
                             | 'string'
                             | 'file'
                             | parenthesizedExpression particleItem+
                             ; // parenthesizedExpressions are a special case.
                               // If they are followed by particleItems then we want to consider them as particles so a PrimaryExpressionNode is created to wrap it.
                               // Otherwise, we flatten it to a ParenthesizedExpressionNode.
particleItem                 : '.' extendedNameReference
                             | argumentList
                             | arrayAccessor
                             | '^'
                             ;
arrayAccessor                : lbrack expressionList rbrack
                             -> ^(TkArrayAccessorNode<ArrayAccessorNodeImpl> expressionList)
                             ;
argumentList                 : '('<ArgumentListNodeImpl>^ (argument ','?)* ')'
                             ;
argument                     : anonymousMethod
                             | expression (':' expression! (':' expression!)?)? // This strange colon construct at the end is the result
                             ;                                                  // of compiler hackery for intrinsic procedures like Str and WriteLn
                                                                                // See: http://www.delphibasics.co.uk/RTL.asp?Name=str
                                                                                // See: https://stackoverflow.com/questions/617654/how-does-writeln-really-work
anonymousMethod              : 'procedure'<AnonymousMethodNodeImpl>^ routineParameters? block
                             | 'function'<AnonymousMethodNodeImpl>^ routineParameters? routineReturnType block
                             ;
expressionOrRange            : expression ('..'<RangeExpressionNodeImpl>^ expression)?
                             ;
expressionOrAnonymousMethod  : anonymousMethod
                             | expression
                             ;
exprOrRangeOrAnonMethod      : anonymousMethod
                             | expression ('..'<RangeExpressionNodeImpl>^ expression)?
                             ;
expressionList               : (expression (','!)?)+
                             ;
expressionOrRangeList        : (expressionOrRange (','!)?)+
                             ;
exprOrRangeOrAnonMethodList  : (exprOrRangeOrAnonMethod (','!)?)+
                             ;
textLiteral                  : textLiteral_ -> ^(TkTextLiteral<TextLiteralNodeImpl> textLiteral_)
                             ;
textLiteral_                 : TkQuotedString (escapedCharacter+ TkQuotedString)* escapedCharacter*
                             | escapedCharacter+ (TkQuotedString escapedCharacter+)* TkQuotedString?
                             ;
escapedCharacter             : TkCharacterEscapeCode
                             | '^' (TkIdentifier | TkIntNumber | TkAnyChar) -> ^({changeTokenType(TkEscapedCharacter)})
                             ;
nilLiteral                   : 'nil'<NilLiteralNodeImpl>
                             ;
arrayConstructor             : lbrack exprOrRangeOrAnonMethodList? rbrack
                             -> ^(TkArrayConstructor<ArrayConstructorNodeImpl> lbrack exprOrRangeOrAnonMethodList? rbrack)
                             ;
addOperator                  : '+'<BinaryExpressionNodeImpl>
                             | '-'<BinaryExpressionNodeImpl>
                             | 'or'<BinaryExpressionNodeImpl>
                             | 'xor'<BinaryExpressionNodeImpl>
                             ;
multOperator                 : '*'<BinaryExpressionNodeImpl>
                             | '/'<BinaryExpressionNodeImpl>
                             | 'div'<BinaryExpressionNodeImpl>
                             | 'mod'<BinaryExpressionNodeImpl>
                             | 'and'<BinaryExpressionNodeImpl>
                             | 'shl'<BinaryExpressionNodeImpl>
                             | 'shr'<BinaryExpressionNodeImpl>
                             | 'as'<BinaryExpressionNodeImpl>
                             ;
unaryOperator                : 'not'<UnaryExpressionNodeImpl>
                             | '+'<UnaryExpressionNodeImpl>
                             | '-'<UnaryExpressionNodeImpl>
                             | '@'<UnaryExpressionNodeImpl>
                             ;
relationalOperator           : '='<BinaryExpressionNodeImpl>
                             | '>'<BinaryExpressionNodeImpl>
                             | '<'<BinaryExpressionNodeImpl>
                             | '<='<BinaryExpressionNodeImpl>
                             | '>='<BinaryExpressionNodeImpl>
                             | '<>'<BinaryExpressionNodeImpl>
                             | 'in'<BinaryExpressionNodeImpl>
                             | 'is'<BinaryExpressionNodeImpl>
                             ;
constExpression              : expression
                             | recordExpression
                             | arrayExpression
                             ;
recordExpression             : '('<RecordExpressionNodeImpl>^ (recordExpressionItem (';')?)+ ')'
                             ;
recordExpressionItem         : ident ':' constExpression
                             -> ^(TkRecordExpressionItem<RecordExpressionItemNodeImpl> ident constExpression)
                             ;
arrayExpression              : '('<ArrayExpressionNodeImpl>^ (constExpression (','!)?)* ')'
                             ;

//----------------------------------------------------------------------------
// Statements
//----------------------------------------------------------------------------
statement                    : ifStatement
                             | varStatement
                             | constStatement
                             | caseStatement
                             | repeatStatement
                             | whileStatement
                             | forStatement
                             | withStatement
                             | tryStatement
                             | raiseStatement
                             | assemblerStatement
                             | compoundStatement
                             | labelStatement
                             | assignmentStatement
                             | expressionStatement
                             | gotoStatement
                             ;
ifStatement                  : 'if'<IfStatementNodeImpl>^ expression 'then' statement? ('else' statement?)?
                             ;
varStatement                 : 'var' attributeList? nameDeclarationList (':' varType)? (':=' expressionOrAnonymousMethod)?
                             -> ^('var'<VarStatementNodeImpl> nameDeclarationList (':' varType)? (':=' expressionOrAnonymousMethod)? attributeList?)
                             ;
constStatement               : 'const' attributeList? nameDeclaration (':' varType)? '=' expressionOrAnonymousMethod
                             -> ^('const'<ConstStatementNodeImpl> nameDeclaration (':' varType)? '=' expressionOrAnonymousMethod attributeList?)
                             ;
caseStatement                : 'case'<CaseStatementNodeImpl>^ expression 'of' caseItem* elseBlock? 'end'
                             ;
elseBlock                    : 'else'<ElseBlockNodeImpl>^ statementList
                             ;
caseItem                     : expressionOrRangeList ':' (statement)? (';')? -> ^(TkCaseItem<CaseItemStatementNodeImpl> expressionOrRangeList (statement)? (';')? )
                             ;
repeatStatement              : 'repeat'<RepeatStatementNodeImpl>^ statementList 'until' expression
                             ;
whileStatement               : 'while'<WhileStatementNodeImpl>^ expression 'do' statement?
                             ;
forStatement                 : 'for'<ForToStatementNodeImpl>^ forVar ':=' expression 'to' expression 'do' statement?
                             | 'for'<ForToStatementNodeImpl>^ forVar ':=' expression 'downto' expression 'do' statement?
                             | 'for'<ForInStatementNodeImpl>^ forVar 'in' expression 'do' statement?
                             ;
forVar                       : 'var' nameDeclaration (':' varType)? -> ^(TkForLoopVar<ForLoopVarDeclarationNodeImpl> nameDeclaration varType?)
                             | simpleNameReference -> ^(TkForLoopVar<ForLoopVarReferenceNodeImpl> simpleNameReference)
                             ;
withStatement                : 'with'<WithStatementNodeImpl>^ expressionList 'do' statement?
                             ;
compoundStatement            : 'begin'<CompoundStatementNodeImpl>^ statementList 'end'
                             ;
statementList                : delimitedStatements? -> ^(TkStatementList<StatementListNodeImpl> delimitedStatements?)
                             ;
delimitedStatements          : (statement | ';')+
                             ;
labelStatement               : label ':' statement? -> ^(TkLabelStatement<LabelStatementNodeImpl> label statement?)
                             ;
assignmentStatement          : expression ':='<AssignmentStatementNodeImpl>^ expressionOrAnonymousMethod
                             ;
expressionStatement          : expression -> ^(TkExpressionStatement<ExpressionStatementNodeImpl> expression)
                             ;
gotoStatement                : 'goto'<GotoStatementNodeImpl>^ label
                             ;
tryStatement                 : 'try'<TryStatementNodeImpl>^ statementList (exceptBlock | finallyBlock) 'end'
                             ;
exceptBlock                  : 'except'<ExceptBlockNodeImpl>^ handlerList
                             ;
finallyBlock                 : 'finally'<FinallyBlockNodeImpl>^ statementList
                             ;
handlerList                  : handler+ elseBlock?
                             | statementList
                             ;
handler                      : 'on'<ExceptItemNodeImpl>^ (nameDeclaration ':'!)? typeReference 'do' statement? (';')?
                             ;
raiseStatement               : 'raise'<RaiseStatementNodeImpl>^ expression? (AT! expression)?
                             ;
assemblerStatement           : 'asm'<AsmStatementNodeImpl>^ assemblerInstructions 'end'
                             ;
assemblerInstructions        : ~('end')* // Skip asm statements
                             ;

//----------------------------------------------------------------------------
// Directives
//----------------------------------------------------------------------------
implDirectiveSection         : (';'? implDirective)* ';'
                             | (';' implDirective)+
                             ;
interfaceDirectiveSection    : (';'? interfaceDirective)* ';'
                             | (';' interfaceDirective)+
                             ;
externalDirectiveSection      : (';'? implDirective)* ';'? externalDirective (';'? implDirective)*';'
                             | (';' implDirective)* ';' externalDirective (';' implDirective)*
                             ;
forwardDirectiveSection      : (';'? implDirective)* ';'? 'forward' (';'? implDirective)*';'
                             | (';' implDirective)* ';' 'forward' (';' implDirective)*
                             ;
implDirective                : 'overload'
                             | 'reintroduce'
                             | bindingDirective
                             | abstractDirective
                             | inlineDirective
                             | callConvention
                             | portabilityDirective
                             | oldCallConventionDirective
                             | dispIDDirective
                             | 'varargs' // Only permitted for cdecl calling convention
                             | 'unsafe' // .net?
                             ;
interfaceDirective           : 'forward'
                             | externalDirective
                             | implDirective
                             ;
bindingDirective             : 'message' expression
                             | 'static'
                             | 'dynamic'
                             | 'override'
                             | 'virtual'
                             ;
abstractDirective            : 'abstract'
                             | 'final'
                             ;
inlineDirective              : 'inline'
                             | 'assembler' // deprecated
                             ;
callConvention               : 'cdecl'
                             | 'pascal'
                             | 'register'
                             | 'safecall'
                             | 'stdcall'
                             | 'export' // deprecated
                             ;
oldCallConventionDirective   : 'far' // deprecated
                             | 'local' // deprecated. Introduced in the Kylix Linux compiler, makes function non-exportable. (No effect in Windows)
                             | 'near' // deprecated
                             ;
portabilityDirective         : 'deprecated'^ textLiteral?
                             | 'experimental'
                             | 'platform'
                             | 'library'
                             ;
externalDirective            : 'external'^ dllName? externalSpecifier*
                             ;
dllName                      : {!input.LT(1).getText().equals("name")}? expression
                             ;
externalSpecifier            : 'name'^ constExpression
                             | 'index'^ constExpression // specific to a platform
                             | 'delayed' // Use delayed loading (See: http://docwiki.embarcadero.com/RADStudio/en/Libraries_and_Packages_(Delphi))
                             ;
dispIDDirective              : 'dispid' expression
                             ;

//----------------------------------------------------------------------------
// General
//----------------------------------------------------------------------------
ident                        : TkIdentifier
                             | keywordsUsedAsNames -> ^({changeTokenType(TkIdentifier)})
                             ;
identifierOrKeyword          : TkIdentifier
                             | keywords -> ^({changeTokenType(TkIdentifier)})
                             ;
keywordsUsedAsNames          : (ABSOLUTE | ABSTRACT | ALIGN | ASSEMBLER | AT | AUTOMATED | CDECL)
                             | (CONTAINS | DEFAULT | DELAYED | DEPRECATED | DISPID | DYNAMIC | EXPERIMENTAL | EXPORT)
                             | (EXTERNAL | FAR | FINAL | FORWARD | HELPER | IMPLEMENTS | INDEX | LOCAL | MESSAGE | NAME)
                             | (NEAR | NODEFAULT | ON | OPERATOR | OUT | OVERLOAD | OVERRIDE | PACKAGE | PASCAL | PLATFORM)
                             | (PRIVATE | PROTECTED | PUBLIC | PUBLISHED | READ | READONLY | REFERENCE | REGISTER | REINTRODUCE)
                             | (REQUIRES | RESIDENT | SAFECALL | SEALED | STATIC | STDCALL | STORED | STRICT | UNSAFE)
                             | (VARARGS | VIRTUAL | WRITE | WRITEONLY)
                             ;
keywords                     : (ABSOLUTE | ABSTRACT | AND | ALIGN | ARRAY | AS | ASM | ASSEMBLER)
                             | (AT | AUTOMATED | BEGIN | CASE | CDECL | CLASS | CONST | CONSTRUCTOR | CONTAINS| DEFAULT)
                             | (DELAYED | DEPRECATED | DESTRUCTOR | DISPID | DISPINTERFACE | DIV | DO | DOWNTO | DYNAMIC)
                             | (ELSE | END | EXCEPT | EXPERIMENTAL | EXPORT | EXPORTS | EXTERNAL | FAR | FILE | FINAL)
                             | (FINALIZATION | FINALLY | FOR | FORWARD | FUNCTION | GOTO | HELPER | IF | IMPLEMENTATION)
                             | (IMPLEMENTS | IN | INDEX | INHERITED | INITIALIZATION | INLINE | INTERFACE | IS | LABEL)
                             | (LIBRARY | LOCAL | MESSAGE | MOD | NAME | NEAR | NIL | NODEFAULT | NOT | OBJECT | OF | ON)
                             | (OPERATOR | OR | OUT | OVERLOAD | OVERRIDE | PACKAGE | PACKED | PASCAL | PLATFORM | PRIVATE)
                             | (PROCEDURE | PROGRAM | PROPERTY | PROTECTED | PUBLIC | PUBLISHED | RAISE | READ | READONLY)
                             | (RECORD | REFERENCE | REGISTER | REINTRODUCE | REPEAT | REQUIRES | RESIDENT)
                             | (RESOURCESTRING | SAFECALL | SEALED | SET | SHL | SHR | STATIC | STDCALL | STORED | STRICT)
                             | (STRING | THEN | THREADVAR | TO | TRY | TYPE | UNIT | UNSAFE | UNTIL | USES | VAR | VARARGS)
                             | (VIRTUAL | WHILE | WITH | WRITE | WRITEONLY | XOR)
                             ;
nameDeclarationList          : nameDeclaration (',' nameDeclaration)* -> ^(TkNameDeclarationList<NameDeclarationListNodeImpl> nameDeclaration nameDeclaration*)
                             ;
label                        : ident
                             | intNum
                             ;
nameDeclaration              : ident -> ^(TkNameDeclaration<SimpleNameDeclarationNodeImpl> ident)
                             ;
genericNameDeclaration       : ident ('.' extendedIdent)* genericDefinition?
                             -> ^(TkNameDeclaration<SimpleNameDeclarationNodeImpl> ident extendedIdent* genericDefinition?)
                             ;
qualifiedNameDeclaration     : ident ('.' extendedIdent)*
                             -> ^(TkNameDeclaration<QualifiedNameDeclarationNodeImpl> ident extendedIdent*)
                             ;
specialOpNameDeclaration     : specialOperatorName
                             -> ^(TkNameDeclaration<SimpleNameDeclarationNodeImpl> specialOperatorName)
                             ;
specialOperatorName          : 'in' -> ^({changeTokenType(TkIdentifier)})
                             ;
nameReference                : ident genericArguments? ('.' extendedNameReference)?
                             -> ^(TkNameReference<NameReferenceNodeImpl> ident genericArguments? ('.' extendedNameReference)?)
                             ;
simpleNameReference          : ident
                             -> ^(TkNameReference<NameReferenceNodeImpl> ident)
                             ;
extendedNameReference        : extendedIdent genericArguments? ('.' extendedNameReference)?
                             -> ^(TkNameReference<NameReferenceNodeImpl> extendedIdent genericArguments? ('.' extendedNameReference)?)
                             ;
extendedIdent                : ident
                             | keywords -> ^({changeTokenType(TkIdentifier)})
                             ;
//----------------------------------------------------------------------------
// Literals
//----------------------------------------------------------------------------
intNum                       : TkIntNumber<IntegerLiteralNodeImpl>
                             | TkHexNumber<IntegerLiteralNodeImpl>
                             | TkBinaryNumber<IntegerLiteralNodeImpl>
                             ;
realNum                      : TkRealNumber<RealLiteralNodeImpl>
                             ;

//----------------------------------------------------------------------------
// Keywords
//----------------------------------------------------------------------------
ABSOLUTE          : 'absolute'       	         ;
ABSTRACT          : 'abstract'       	         ;
ALIGN             : 'align'                    ;
AND               : 'and'           	         ;
ARRAY             : 'array'          	         ;
AS                : 'as'             	         ;
ASM               : 'asm' { asmMode = true; }  ;
ASSEMBLER         : 'assembler'       	       ;
AT                : 'at'             	         ;
AUTOMATED         : 'automated'      	         ;
BEGIN             : 'begin'          	         ;
CASE              : 'case'           	         ;
CDECL             : 'cdecl'          	         ;
CLASS             : 'class'          	         ;
CONST             : 'const'           	       ;
CONSTRUCTOR       : 'constructor'              ;
CONTAINS          : 'contains'                 ;
DEFAULT           : 'default'                  ;
DELAYED           : 'delayed'                  ;
DEPRECATED        : 'deprecated'               ;
DESTRUCTOR        : 'destructor'               ;
DISPID            : 'dispid'                   ;
DISPINTERFACE     : 'dispinterface'            ;
DIV               : 'div'                      ;
DO                : 'do'                       ;
DOWNTO            : 'downto'                   ;
DYNAMIC           : 'dynamic'        	         ;
ELSE              : 'else'           	         ;
END               : 'end' { asmMode = false; } ;
EXCEPT            : 'except'                   ;
EXPERIMENTAL      : 'experimental'             ;
EXPORT            : 'export'                   ;
EXPORTS           : 'exports'                  ;
EXTERNAL          : 'external'                 ;
FAR               : 'far'                      ;
FILE              : 'file'                     ;
FINAL             : 'final'                    ;
FINALIZATION      : 'finalization'             ;
FINALLY           : 'finally'                  ;
FOR               : 'for'                      ;
FORWARD           : 'forward'                  ;
FUNCTION          : 'function'                 ;
GOTO              : 'goto'                     ;
HELPER            : 'helper'                   ;
IF                : 'if'                       ;
IMPLEMENTATION    : 'implementation' {
                     if (shouldSkipImplementation && directiveNesting == 0) {
                       skip();
                       while (input.LA(1) != EOF) {
                         input.consume();
                       }
                     }
                  };
IMPLEMENTS        : 'implements'               ;
IN                : 'in'                       ;
INDEX             : 'index'                    ;
INHERITED         : 'inherited'                ;
INITIALIZATION    : 'initialization'           ;
INLINE            : 'inline'                   ;
INTERFACE         : 'interface'                ;
IS                : 'is'                       ;
LABEL             : 'label'                    ;
LIBRARY           : 'library'                  ;
LOCAL             : 'local'                    ;
MESSAGE           : 'message'                  ;
MOD               : 'mod'                      ;
NAME              : 'name'                     ;
NEAR              : 'near'                     ;
NIL               : 'nil'                      ;
NODEFAULT         : 'nodefault'                ;
NOT               : 'not'                      ;
OBJECT            : 'object'                   ;
OF                : 'of'                       ;
ON                : 'on'                       ;
OPERATOR          : 'operator'                 ;
OR                : 'or'                       ;
OUT               : 'out'                      ;
OVERLOAD          : 'overload'                 ;
OVERRIDE          : 'override'                 ;
PACKAGE           : 'package'                  ;
PACKED            : 'packed'                   ;
PASCAL            : 'pascal'                   ;
PLATFORM          : 'platform'                 ;
PRIVATE           : 'private'                  ;
PROCEDURE         : 'procedure'                ;
PROGRAM           : 'program'                  ;
PROPERTY          : 'property'                 ;
PROTECTED         : 'protected'                ;
PUBLIC            : 'public'                   ;
PUBLISHED         : 'published'                ;
RAISE             : 'raise'                    ;
READ              : 'read'                     ;
READONLY          : 'readonly'                 ;
RECORD            : 'record'                   ;
REFERENCE         : 'reference'                ;
REGISTER          : 'register'                 ;
REINTRODUCE       : 'reintroduce'              ;
REPEAT            : 'repeat'                   ;
REQUIRES          : 'requires'                 ;
RESIDENT          : 'resident'                 ;
RESOURCESTRING    : 'resourcestring'           ;
SAFECALL          : 'safecall'                 ;
SEALED            : 'sealed'                   ;
SET               : 'set'                      ;
SHL               : 'shl'                      ;
SHR               : 'shr'                      ;
STATIC            : 'static'                   ;
STDCALL           : 'stdcall'                  ;
STORED            : 'stored'                   ;
STRICT            : 'strict'                   ;
STRING            : 'string'                   ;
THEN              : 'then'                     ;
THREADVAR         : 'threadvar'                ;
TO                : 'to'                       ;
TRY               : 'try'                      ;
TYPE              : 'type'                     ;
UNIT              : 'unit'                     ;
UNSAFE            : 'unsafe'                   ;
UNTIL             : 'until'                    ;
USES              : 'uses'                     ;
VAR               : 'var'                      ;
VARARGS           : 'varargs'                  ;
VIRTUAL           : 'virtual'                  ;
WHILE             : 'while'                    ;
WITH              : 'with'                     ;
WRITE             : 'write'                    ;
WRITEONLY         : 'writeonly'                ;
XOR               : 'xor'                      ;

//----------------------------------------------------------------------------
// Operators
//----------------------------------------------------------------------------
PLUS                 : '+'  ;
MINUS                : '-'  ;
MULTIPLY             : '*'  ;
DIVIDE               : '/'  ;
ASSIGN               : ':=' ;
COMMA                : ','  ;
SEMICOLON            : ';'  ;
COLON                : ':'  ;
EQUAL                : '='  ;
NOT_EQUAL            : '<>' ;
LESS_THAN            : '<'  ;
LESS_THAN_EQUAL      : '<=' ;
GREATER_THAN_EQUAL   : '>=' ;
GREATER_THAN         : '>'  ;
SQUARE_BRACKET_LEFT  : '['  ;
SQUARE_BRACKET_RIGHT : ']'  ;
PAREN_BRACKET_LEFT   : '(.' ;
PAREN_BRACKET_RIGHT  : '.)' ;
PAREN_LEFT           : '('  ;
PAREN_RIGHT          : ')'  ;
DEREFERENCE          : '^'  ;
ADDRESS              : '@'  ;
DOT                  : '.'  ;
DOT_DOT              : '..' ;

//****************************
// Imaginary tokens
//****************************
TkRootNode              : 'ROOT_NODE'
                        ;
TkRoutineParameters     : 'ROUTINE_PARAMETERS'
                        ;
TkRoutineReturn         : 'ROUTINE_RETURN'
                        ;
TkAttributeList         : 'ATTRIBUTE_LIST'
                        ;
TkAttributeGroup        : 'ATTRIBUTE_GROUP'
                        ;
TkAttribute             : 'ATTRIBUTE'
                        ;
TkTypeDeclaration       : 'TYPE_DECLARATION'
                        ;
TkRecordVariantItem     : 'RECORD_VARIANT_ITEM'
                        ;
TkRecordVariantTag      : 'RECORD_VARIANT_TAG'
                        ;
TkRecordExpressionItem  : 'RECORD_EXPRESSION_ITEM'
                        ;
TkGuid                  : 'INTERFACE_GUID'
                        ;
TkClassParents          : 'CLASS_PARENTS'
                        ;
TkLocalDeclarations     : 'LOCAL_DECLARATIONS'
                        ;
TkCaseItem              : 'CASE_ITEM'
                        ;
TkExpressionStatement   : 'EXPRESSION_STATEMENT'
                        ;
TkLabelStatement        : 'LABEL_STATEMENT'
                        ;
TkStatementList         : 'STATEMENT_LIST'
                        ;
TkRoutineName           : 'ROUTINE_NAME'
                        ;
TkRoutineHeading        : 'ROUTINE_HEADING'
                        ;
TkRoutineDeclaration    : 'ROUTINE_DECLARATION'
                        ;
TkRoutineImplementation : 'ROUTINE_IMPLEMENTATION'
                        ;
TkRoutineBody           : 'ROUTINE_BODY'
                        ;
TkGenericDefinition     : 'GENERIC_DEFINITION'
                        ;
TkGenericArguments      : 'GENERIC_ARGUMENTS'
                        ;
TkWeakAlias             : 'WEAK_ALIAS'
                        ;
TkTypeReference         : 'TYPE_REFERENCE'
                        ;
TkProcedureType         : 'PROCEDURE_TYPE'
                        ;
TkEnumElement           : 'ENUM_ELEMENT'
                        ;
TkVisibilitySection     : 'VISIBILITY_SECTION'
                        ;
TkVisibility            : 'VISIBILITY'
                        ;
TkFieldSection          : 'FIELD_SECTION'
                        ;
TkFieldDeclaration      : 'FIELD_DECLARATION'
                        ;
TkFormalParameterList   : 'FORMAL_PARAMETER_LIST'
                        ;
TkFormalParameter       : 'FORMAL_PARAMETER'
                        ;
TkVarDeclaration        : 'VAR_DECLARATION'
                        ;
TkNameDeclarationList   : 'NAME_DECLARATION_LIST'
                        ;
TkConstDeclaration      : 'CONST_DECLARATION'
                        ;
TkPrimaryExpression     : 'PRIMARY_EXPRESSION'
                        ;
TkNestedExpression      : 'NESTED_EXPRESSION'
                        ;
TkTextLiteral           : 'TEXT_LITERAL'
                        ;
TkNameDeclaration       : 'NAME_DECLARATION'
                        ;
TkNameReference         : 'NAME_REFERENCE'
                        ;
TkUnitImport            : 'UNIT_IMPORT'
                        ;
TkMethodResolveClause   : 'METHOD_RESOLUTION_CLAUSE'
                        ;
TkEscapedCharacter      : 'ESCAPED_CHARACTER'
                        ;
TkCompilerDirective     : 'COMPILER_DIRECTIVE'
                        ;
TkRealNumber            : 'REAL_NUMBER'
                        ;
TkTypeParameter         : 'TYPE_PARAMETER'
                        ;
TkForLoopVar            : 'FOR_LOOP_VAR'
                        ;
TkArrayAccessorNode     : 'ARRAY_ACCESSOR'
                        ;
TkArrayConstructor      : 'ARRAY_CONSTRUCTOR'
                        ;
TkArrayIndices          : 'ARRAY_INDICES'
                        ;

//****************************
// Tokens
//****************************
TkIdentifier            : '&'* (Alpha | '_') (Alpha | FullWidthNumeral | Digit | '_')*
                        ;
                        // We use a lookahead here to avoid lexer failures on range operations like '1..2'
                        // or record helper invocations on Integer literals
TkIntNumber             : '&'* DigitSeq (
                            {input.LA(1) != '.' || Character.isDigit(input.LA(2))}? =>
                              (
                                '.' DigitSeq
                                {$type = TkRealNumber;}
                              )?
                              (
                                ScaleFactor
                                {$type = TkRealNumber;}
                              )?
                          )?
                        ;
TkHexNumber             : '$' ('_' | HexDigit)*
                        ;
TkBinaryNumber          : '%' ('_' | BinaryDigit)*
                        ;
TkAsmId                 : { asmMode }? => '@' '@'? (Alpha | '_' | Digit)+
                        ;
TkAsmHexNum             : { asmMode }? => HexDigitSeq ('h'|'H')
                        ;
TkQuotedString          : '\'' ('\'\'' | ~('\''))* '\''
                        ;
TkAsmDoubleQuotedString : { asmMode }? => '"' (~('\"'))* '"'
                        ;
TkCharacterEscapeCode   : '#' ('_' | Digit)+
                        | '#' '$' ('_' | HexDigit)+
                        | '#' '%' ('_' | BinaryDigit)+
                        ;
//----------------------------------------------------------------------------
// Fragments
//----------------------------------------------------------------------------
fragment
Alpha                   : 'a'..'z'
                        | 'A'..'Z'
                        // every non-ASCII character until the surrogate pair range (except \u3000 which is whitespace)
                        | '\u0080'..'\u2FFF'
                        | '\u3001'..'\uD7FF'
                        // everything after the surrogate range up to the start of the fullwidth numerals
                        | '\uE000'..'\uFF0F'
                        // after the fullwidth numerals up to the second last codepoint in the BMP
                        //   \uFFFF is valid in Delphi identifiers but in antlr3 the lexer is broken when you include it
                        | '\uFF1A'..'\uFFFE'
                        // surrogate pairs
                        | ('\uD800'..'\uDBFF') ('\uDC00'..'\uDFFF')
                        ;
fragment
FullWidthNumeral        : '\uFF10'..'\uFF19'
                        ;
fragment
Digit                   : '0'..'9'
                        ;
fragment
DigitSeq                : Digit (Digit | '_')*
                        ;
fragment
ScaleFactor             : (('e'|'E') ('+'|'-')? DigitSeq)
                        ;
fragment
HexDigit                : Digit | 'a'..'f' | 'A'..'F'
                        ;
fragment
HexDigitSeq		          : HexDigit (HexDigit | '_')*
                        ;
fragment
BinaryDigit             : '0'..'1'
                        ;
fragment
BinaryDigitSeq		      : BinaryDigit (BinaryDigit | '_')*
                        ;

//----------------------------------------------------------------------------
// Hidden channel
//----------------------------------------------------------------------------
COMMENT                 :  '//' ~('\n'|'\r')*                          {$channel=HIDDEN;}
                        |  '(*' ( options {greedy=false;} : . )* '*)'
                        {
                          $channel=HIDDEN;
                          if ($text.startsWith("(*\$")) {
                            $type = TkCompilerDirective;
                            if ($text.startsWith("(*\$endif") || $text.startsWith("(*\$ifend")) {
                              --directiveNesting;
                            } else if ($text.startsWith("(*\$if")) {
                              ++directiveNesting;
                            }
                          }
                        }
                        |  '{' ( options {greedy=false;} : . )* '}'
                        {
                          $channel=HIDDEN;
                          if ($text.startsWith("{\$")) {
                            $type = TkCompilerDirective;
                            if ($text.startsWith("{\$endif") || $text.startsWith("{\$ifend")) {
                              --directiveNesting;
                            } else if ($text.startsWith("{\$if")) {
                              ++directiveNesting;
                            }
                          }
                        }
                        ;
WHITESPACE              : ('\u0000'..'\u0020' | '\u3000')+ {$channel=HIDDEN;}
                        ;
//----------------------------------------------------------------------------
// Any character
//----------------------------------------------------------------------------
TkAnyChar               : .
                        ;

//----------------------------------------------------------------------------
// Deprecated tokens
//----------------------------------------------------------------------------
AMPERSAND__deprecated   : '@AMPERSAND@';
