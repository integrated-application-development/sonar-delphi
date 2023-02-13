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
    throw new RuntimeException(hdr + " " + msg, e);
  }

  @Override
  public String getErrorHeader(RecognitionException e) {
    return "line " + e.line + ":" + e.charPositionInLine;
  }
}

@parser::members {
  private Token changeTokenType(int type) {
    CommonToken t = new CommonToken(input.LT(-1));
    t.setType(type);
    return t;
  }

  @Override
  public void reportError(RecognitionException e) {
    String hdr = this.getErrorHeader(e);
    String msg = this.getErrorMessage(e, this.getTokenNames());
    throw new RuntimeException(hdr + " " + msg, e);
  }

  @Override
  public String getErrorHeader(RecognitionException e) {
    return "line " + e.line + ":" + e.charPositionInLine;
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

program                      : (programHead)? (usesFileClause)? block '.'
                             ;
programHead                  : 'program'<ProgramDeclarationNodeImpl>^ qualifiedNameDeclaration (programParmSeq)? ';'!
                             ;
programParmSeq               : '(' (ident (',' ident)* )? ')'
                             ;
library                      : libraryHead (usesFileClause)? block '.'
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
block                        : blockDeclSection? blockBody
                             ;
blockDeclSection             : declSection+ -> ^(TkBlockDeclSection<BlockDeclarationSectionNodeImpl> declSection+)
                             ;
blockBody                    : compoundStatement
                             | assemblerStatement
                             ;
declSection                  : labelDeclSection
                             | constSection
                             | typeSection
                             | varSection
                             | methodImplementation
                             | exportsSection
                             ;
interfaceDecl                : constSection
                             | typeSection
                             | varSection
                             | exportsSection
                             | methodInterface
                             ;
labelDeclSection             : 'label' (label (','!)?)+ ';'
                             ;
constSection                 : ('const'<ConstSectionNodeImpl>^ | 'resourcestring'<ConstSectionNodeImpl>^) constDeclaration*
                             // constSection was changed at some point from "constDeclaration+" to "constDeclaration*" to cater to invalid includes
                             // example: "const {$include versioninfo.inc}"
                             // Is this really the appropriate solution?
                             ;
constDeclaration             : customAttribute? nameDeclaration (':' varType)? '=' constExpression portabilityDirective* ';'
                             -> ^(TkConstDeclaration<ConstDeclarationNodeImpl> nameDeclaration constExpression varType? portabilityDirective*)
                             ;
typeSection                  : 'type'<TypeSectionNodeImpl>^ typeDeclaration+
                             ;
innerTypeSection             : 'type'<TypeSectionNodeImpl>^ typeDeclaration*
                             ;
typeDeclaration              : customAttribute? genericNameDeclaration '=' typeDecl portabilityDirective* ';'
                             -> ^(TkNewType<TypeDeclarationNodeImpl> genericNameDeclaration typeDecl customAttribute? portabilityDirective*)
                             ;
varSection                   : ('var'<VarSectionNodeImpl>^ | 'threadvar'<VarSectionNodeImpl>^) varDeclaration varDeclaration*
                             ;
varDeclaration               : customAttribute? nameDeclarationList ':' varType portabilityDirective* varValueSpec? portabilityDirective* ';'
                             -> ^(TkVarDeclaration<VarDeclarationNodeImpl> nameDeclarationList varType varValueSpec? customAttribute?)
                             ;
varValueSpec                 : 'absolute' constExpression
                             | '=' constExpression
                             ;
exportsSection               : 'exports' ident exportItem (',' ident exportItem)* ';'
                             ;
exportItem                   : ('(' formalParameterList ')')? (INDEX expression)? (NAME expression)? ('resident')?
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
                             | typeType
                             | typeAlias
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
arrayType                    :  'array' arrayIndices? 'of' arraySubType
                             -> ^('array'<ArrayTypeNodeImpl> 'of' arraySubType arrayIndices? )
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
arraySubType                 : 'const'<ConstArraySubTypeNodeImpl>
                             | varType
                             ;
setType                      : 'set'<SetTypeNodeImpl>^ 'of' varType
                             ;
fileType                     : 'file'<FileTypeNodeImpl>^ ('of' varType)?
                             ;
pointerType                  : '^'<PointerTypeNodeImpl>^ varType
                             ;
stringType                   : 'string'<StringTypeNodeImpl>^ (lbrack! expression rbrack!)?
                             | ANSISTRING<AnsiStringTypeNodeImpl>^ ('('! expression ')'!)?
                             ;
procedureType                : methodType
                             | procedureReference
                             | simpleProcedureType
                             ;
methodType                   : procedureTypeHeading 'of' 'object'<MethodTypeNodeImpl>^ ((';')? interfaceDirective)*
                             ;
procedureReference           : 'reference'<ProcedureReferenceTypeNodeImpl>^ 'to'! procedureTypeHeading
                             ;
simpleProcedureType          : procedureTypeHeading -> ^(TkProcedureType<ProcedureTypeNodeImpl> procedureTypeHeading)
                             ;
procedureTypeHeading         : 'function'<ProcedureTypeHeadingNodeImpl>^ methodParameters? methodReturnType? ((';')? interfaceDirective)*
                             | 'procedure'<ProcedureTypeHeadingNodeImpl>^ methodParameters? ((';')? interfaceDirective)*
                             ;
typeOfType                   : 'type'<TypeOfTypeNodeImpl>^ 'of' typeDecl
                             ;
typeType                     : 'type'<TypeTypeNodeImpl>^ typeDecl
                             ;
typeAlias                    : typeReference -> ^(TkTypeAlias<TypeAliasNodeImpl> typeReference)
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
                             | methodInterface
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
fieldDecl                    : customAttribute? nameDeclarationList ':' varType portabilityDirective* ';'?
                             -> ^(TkFieldDeclaration<FieldDeclarationNodeImpl> nameDeclarationList varType portabilityDirective* ';'?)
                             ;
classHelperType              : 'class'<ClassHelperTypeNodeImpl>^ 'helper' classParent? 'for' typeReference visibilitySection* 'end'
                             ;
interfaceType                : ('interface'<InterfaceTypeNodeImpl>^ | 'dispinterface'<InterfaceTypeNodeImpl>^) classParent? (interfaceGuid? interfaceItems? 'end')?
                             ;
interfaceGuid                : lbrack expression rbrack -> ^(TkGuid<InterfaceGuidNodeImpl> expression)
                             ;
interfaceItems               : interfaceItem+ -> ^(TkVisibilitySection<VisibilitySectionNodeImpl> interfaceItem+)
                             ;
interfaceItem                : methodInterface
                             | property
                             ;
objectType                   : 'object'<ObjectTypeNodeImpl>^ classParent? visibilitySection* 'end' // Obselete, kept for backwards compatibility with Turbo Pascal
                             ;                                                                 // See: https://www.oreilly.com/library/view/delphi-in-a/1565926595/re192.html
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
property                     : customAttribute? 'class'? 'property' nameDeclaration propertyArray? (':' varType)? (propertyDirective)* ';'
                             -> ^('property'<PropertyNodeImpl> nameDeclaration propertyArray? varType? 'class'? customAttribute? propertyDirective*)
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
                             ;                             // See: https://www.oreilly.com/library/view/delphi-in-a/1565926595/re24.html

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
// Methods
//----------------------------------------------------------------------------
methodResolutionClause       : key=('function' | 'procedure') interfaceMethod=nameReference '=' implemented=nameReference ';'
                             -> ^(TkMethodResolveClause<MethodResolutionClauseNodeImpl>
                                    $key $interfaceMethod $implemented
                                 )
                             ;
methodInterface              : methodInterfaceHeading
                             -> ^(TkMethodDeclaration<MethodDeclarationNodeImpl>
                                    methodInterfaceHeading
                                 )
                             ;
methodImplementation         : fullMethodImplementation
                             | externalMethod
                             | forwardMethod
                             ;
fullMethodImplementation     : methodImplementationHeading methodBody
                             -> ^(TkMethodImplementation<MethodImplementationNodeImpl>
                                    methodImplementationHeading
                                    methodBody
                                 )
                             ;
externalMethod               : externalMethodHeading
                             -> ^(TkMethodImplementation<MethodImplementationNodeImpl>
                                    externalMethodHeading
                                 )
                             ;
forwardMethod                : forwardMethodHeading
                             -> ^(TkMethodDeclaration<MethodDeclarationNodeImpl>
                                    forwardMethodHeading
                                 )
                             ;
methodInterfaceHeading       : customAttribute? 'class'? methodKey methodDeclarationName methodParameters? methodReturnType? interfaceDirectiveSection
                             -> ^(TkMethodHeading<MethodHeadingNodeImpl>
                                    methodKey
                                    methodDeclarationName
                                    methodParameters?
                                    methodReturnType?
                                    customAttribute?
                                    'class'?
                                    interfaceDirectiveSection
                                 )
                             ;
methodImplementationHeading  : customAttribute? 'class'? methodKey methodImplementationName methodParameters? methodReturnType? implDirectiveSection
                             -> ^(TkMethodHeading<MethodHeadingNodeImpl>
                                    methodKey
                                    methodImplementationName
                                    methodParameters?
                                    methodReturnType?
                                    customAttribute?
                                    'class'?
                                    implDirectiveSection
                                 )
                             ;
externalMethodHeading        : customAttribute? 'class'? methodKey methodImplementationName methodParameters? methodReturnType? externalDirectiveSection
                             -> ^(TkMethodHeading<MethodHeadingNodeImpl>
                                    methodKey
                                    methodImplementationName
                                    methodParameters?
                                    methodReturnType?
                                    customAttribute?
                                    'class'?
                                    externalDirectiveSection
                                 )
                             ;
forwardMethodHeading         : customAttribute? 'class'? methodKey methodDeclarationName methodParameters? methodReturnType? forwardDirectiveSection
                             -> ^(TkMethodHeading<MethodHeadingNodeImpl>
                                    methodKey
                                    methodDeclarationName
                                    methodParameters?
                                    methodReturnType?
                                    customAttribute?
                                    'class'?
                                    forwardDirectiveSection
                                 )
                             ;
methodDeclarationName        : genericNameDeclaration -> ^(TkMethodName<MethodNameNodeImpl> genericNameDeclaration)
                             ;
methodImplementationName     : nameReference -> ^(TkMethodName<MethodNameNodeImpl> nameReference)
                             ;
methodKey                    : 'procedure'
                             | 'constructor'
                             | 'destructor'
                             | 'function'
                             | 'operator'
                             ;
methodReturnType             : ':' customAttribute? returnType -> ^(TkMethodReturn<MethodReturnTypeNodeImpl> returnType customAttribute?)
                             ;
returnType                   : stringType
                             | typeReference
                             ;
methodParameters             : '(' formalParameterList? ')' -> ^(TkMethodParameters<MethodParametersNodeImpl> '(' formalParameterList? ')')
                             ;
formalParameterList          : formalParameter (';' formalParameter)* -> ^(TkFormalParameterList<FormalParameterListNodeImpl> formalParameter formalParameter*)
                             ;
formalParameter              : a1=customAttribute? (paramSpecifier a2=customAttribute?)? nameDeclarationList (':' parameterType)? ('=' expression)?
                             -> ^(TkFormalParameter<FormalParameterNodeImpl> nameDeclarationList parameterType? paramSpecifier? expression? $a1? $a2?)
                             ;
paramSpecifier               : 'const'
                             | 'var'
                             | 'out'
                             ;
methodBody                   : block ';' -> ^(TkMethodBody<MethodBodyNodeImpl> block)
                             ;

//----------------------------------------------------------------------------
// Custom Attributes
//----------------------------------------------------------------------------
customAttribute              : customAttributeList -> ^(TkCustomAttributeList<CustomAttributeListNodeImpl> customAttributeList)
                             ;
customAttributeList          : customAttributeDecl+
                             ;
customAttributeDecl          : lbrack (nameReference argumentList? ','?)+ rbrack
                             -> ^(TkCustomAttribute<CustomAttributeNodeImpl> lbrack (nameReference argumentList?)+ rbrack)
                             ;

//----------------------------------------------------------------------------
// Expressions
//----------------------------------------------------------------------------
expression                   : relationalExpression
                             ;
relationalExpression         : additiveExpression (relationalOperator^ additiveExpression)*
                             ;
additiveExpression           : multiplicativeExpression (addOperator^ multiplicativeExpression)*
                             ;
multiplicativeExpression     : unaryExpression (multOperator^ unaryExpression)*
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
anonymousMethod              : 'procedure'<AnonymousMethodNodeImpl>^ methodParameters? block
                             | 'function'<AnonymousMethodNodeImpl>^ methodParameters? methodReturnType block
                             ;
expressionOrRange            : expression ('..'<RangeExpressionNodeImpl>^ expression)?
                             ;
expressionOrAnonymousMethod  : anonymousMethod
                             | expression
                             ;
expressionList               : (expression (','!)?)+
                             ;
expressionOrRangeList        : (expressionOrRange (','!)?)+
                             ;
textLiteral                  : textLiteral_ -> ^(TkTextLiteral<TextLiteralNodeImpl> textLiteral_)
                             ;
textLiteral_                 : TkQuotedString (escapedCharacter+ TkQuotedString)* escapedCharacter*
                             | escapedCharacter+ (TkQuotedString escapedCharacter+)* TkQuotedString?
                             ;
escapedCharacter             : TkCharacterEscapeCode
                             | '^' (TkIdentifier | TkIntNum | TkAnyChar) -> ^({changeTokenType(TkEscapedCharacter)})
                             ;
nilLiteral                   : 'nil'<NilLiteralNodeImpl>
                             ;
arrayConstructor             : lbrack expressionOrRangeList? rbrack
                             -> ^(TkArrayConstructor<ArrayConstructorNodeImpl> lbrack expressionOrRangeList? rbrack)
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
varStatement                 : 'var' customAttribute? nameDeclarationList (':' varType)? (':=' expressionOrAnonymousMethod)?
                             -> ^('var'<VarStatementNodeImpl> nameDeclarationList (':' varType)? (':=' expressionOrAnonymousMethod)? customAttribute?)
                             ;
constStatement               : 'const' customAttribute? nameDeclaration (':' varType)? '=' expressionOrAnonymousMethod
                             -> ^('const'<ConstStatementNodeImpl> nameDeclaration (':' varType)? '=' expressionOrAnonymousMethod customAttribute?)
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
                             | abstractDirective // virtual;
                             | inlineDirective   // niet virtual or dynamic
                             | callConvention
                             | portabilityDirective  // (niet abstract)
                             | oldCallConventionDirective
                             | dispIDDirective
                             | 'varargs'  // Only permitted for cdecl calling convention
                             | 'unsafe'  // .net?
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
callConvention               : 'cdecl'    //
                             | 'pascal'   //
                             | 'register' //
                             | 'safecall' //
                             | 'stdcall'  //
                             | 'export'   // deprecated
                             ;
oldCallConventionDirective   : 'far'      // deprecated
                             | 'local'    // deprecated. Introduced in the Kylix Linux compiler, makes function non-exportable. (No effect in Windows)
                             | 'near'     // deprecated
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
                             | 'index'^ constExpression   // specific to a platform
                             | 'delayed' // Use delayed loading (See: http://docwiki.embarcadero.com/RADStudio/Rio/en/Libraries_and_Packages_(Delphi))
                             ;
dispIDDirective              : 'dispid' expression
                             ;

//----------------------------------------------------------------------------
// General
//----------------------------------------------------------------------------
ident                        : TkIdentifier
                             | '&'! identifierOrKeyword
                             | keywordsUsedAsNames -> ^({changeTokenType(TkIdentifier)})
                             ;
identifierOrKeyword          : TkIdentifier
                             | keywords -> ^({changeTokenType(TkIdentifier)})
                             ;
keywordsUsedAsNames          : (ABSOLUTE | ABSTRACT | ADD | ALIGN | ANSISTRING | ASSEMBLER | AT | AUTOMATED | CDECL)
                             | (CONTAINS |  DEFAULT | DELAYED | DEPRECATED | DISPID | DYNAMIC | EXPERIMENTAL | EXPORT)
                             | (EXTERNAL | FAR | FINAL | FORWARD | HELPER | IMPLEMENTS | INDEX | LOCAL | MESSAGE | NAME)
                             | (NEAR | NODEFAULT | ON | OPERATOR | OUT | OVERLOAD | OVERRIDE | PACKAGE | PASCAL | PLATFORM)
                             | (PRIVATE | PROTECTED | PUBLIC | PUBLISHED | READ | READONLY | REFERENCE | REGISTER | REINTRODUCE)
                             | (REMOVE | REQUIRES | RESIDENT | SAFECALL | SEALED | STATIC | STDCALL | STORED | STRICT | UNSAFE)
                             | (VARARGS | VARIANT | VIRTUAL | WRITE | WRITEONLY)
                             ;
keywords                     : (ABSOLUTE | ABSTRACT | ADD | AND | ALIGN |ANSISTRING | ARRAY | AS | ASM | ASSEMBLER)
                             | (AT | AUTOMATED | BEGIN | CASE | CDECL | CLASS | CONST | CONSTRUCTOR | CONTAINS| DEFAULT)
                             | (DELAYED | DEPRECATED | DESTRUCTOR | DISPID | DISPINTERFACE | DIV | DO | DOWNTO | DYNAMIC)
                             | (ELSE | END | EXCEPT | EXPERIMENTAL | EXPORT | EXPORTS | EXTERNAL | FAR | FILE | FINAL)
                             | (FINALIZATION | FINALLY | FOR | FORWARD | FUNCTION | GOTO | HELPER | IF | IMPLEMENTATION)
                             | (IMPLEMENTS | IN | INDEX | INHERITED | INITIALIZATION | INLINE | INTERFACE | IS | LABEL)
                             | (LIBRARY | LOCAL | MESSAGE | MOD | NAME | NEAR | NIL | NODEFAULT | NOT | OBJECT | OF | ON)
                             | (OPERATOR | OR | OUT | OVERLOAD | OVERRIDE | PACKAGE | PACKED | PASCAL | PLATFORM | PRIVATE)
                             | (PROCEDURE | PROGRAM | PROPERTY | PROTECTED | PUBLIC | PUBLISHED | RAISE | READ | READONLY)
                             | (RECORD | REFERENCE | REGISTER | REINTRODUCE | REMOVE | REPEAT | REQUIRES | RESIDENT)
                             | (RESOURCESTRING | SAFECALL | SEALED | SET | SHL | SHR | STATIC | STDCALL | STORED | STRICT)
                             | (STRING | THEN | THREADVAR | TO | TRY | TYPE | UNIT | UNSAFE | UNTIL | USES | VAR | VARARGS)
                             | (VARIANT | VIRTUAL | WHILE | WITH | WRITE | WRITEONLY | XOR)
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
intNum                       : TkIntNum<IntegerLiteralNodeImpl>
                             | TkHexNum<IntegerLiteralNodeImpl>
                             | TkBinaryNum<IntegerLiteralNodeImpl>
                             ;
realNum                      : TkRealNum<DecimalLiteralNodeImpl>
                             ;

//----------------------------------------------------------------------------
// Keywords
//----------------------------------------------------------------------------
ABSOLUTE          : 'absolute'       	         ;
ABSTRACT          : 'abstract'       	         ;
ADD               : 'add'            	         ;
ALIGN             : 'align'                    ;
AND               : 'and'           	         ;
ANSISTRING        : 'ansistring'     	         ;
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
REMOVE            : 'remove'                   ;
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
VARIANT           : 'variant'                  ;
VIRTUAL           : 'virtual'                  ;
WHILE             : 'while'                    ;
WITH              : 'with'                     ;
WRITE             : 'write'                    ;
WRITEONLY         : 'writeonly'                ;
XOR               : 'xor'                      ;

//----------------------------------------------------------------------------
// Operators
//----------------------------------------------------------------------------
PLUS              : '+'   ;
MINUS             : '-'   ;
STAR              : '*'   ;
SLASH             : '/'   ;
ASSIGN            : ':='  ;
COMMA             : ','   ;
SEMI              : ';'   ;
COLON             : ':'   ;
EQUAL             : '='   ;
NOT_EQUAL         : '<>'  ;
LT                : '<'   ;
LE                : '<='  ;
GE                : '>='  ;
GT                : '>'   ;
LBRACK            : '['   ;
LBRACK2           : '(.'  ;
RBRACK            : ']'   ;
RBRACK2           : '.)'  ;
LPAREN            : '('   ;
RPAREN            : ')'   ;
POINTER           : '^'   ;
AT2               : '@'   ;
DOT               : '.'   ;
DOTDOT            : '..'  ;
LCURLY            : '{'   ;
RCURLY            : '}'   ;
AMPERSAND         : '&'   ;

//****************************
// Imaginary tokens
//****************************
TkRootNode              : 'ROOT_NODE'
                        ;
TkMethodParameters      : 'METHOD_PARAMETERS'
                        ;
TkMethodReturn          : 'METHOD_RETURN'
                        ;
TkCustomAttributeList   : 'CUSTOM_ATTRIBUTE_LIST'
                        ;
TkCustomAttribute       : 'CUSTOM_ATTRIBUTE'
                        ;
TkCustomAttributeArgs   : 'CUSTOM_ATTRIBUTE_ARGS'
                        ;
TkNewType               : 'NEW_TYPE'
                        ;
TkNewTypeDecl           : 'NEW_TYPE_DECL'
                        ;
TkClass                 : 'CLASS'
                        ;
TkRecord                : 'RECORD_TYPE'
                        ;
TkRecordVariantItem     : 'RECORD_VARIANT_ITEM'
                        ;
TkRecordVariantTag      : 'RECORD_VARIANT_TAG'
                        ;
TkRecordExpressionItem  : 'RECORD_EXPRESSION_ITEM'
                        ;
TkRecordHelper          : 'RECORD_HELPER'
                        ;
TkInterface             : 'INTERFACE_TYPE'
                        ;
TkObject                : 'OBJECT_TYPE'
                        ;
TkClassOfType           : 'CLASS_OF_TYPE'
                        ;
TkVariableType          : 'VARIABLE_TYPE'
                        ;
TkVariableIdents        : 'VARIABLE_IDENTS'
                        ;
TkVariableParam         : 'VARIABLE_PARAM'
                        ;
TkGuid                  : 'INTERFACE_GUID'
                        ;
TkClassParents          : 'CLASS_PARENTS'
                        ;
TkClassField            : 'CLASS_FIELD'
                        ;
TkAnonymousExpression   : 'ANONYMOUS_EXPRESSION'
                        ;
TkBlockDeclSection      : 'BLOCK_DECL_SECTION'
                        ;
TkCaseItem              : 'CASE_ITEM'
                        ;
TkExpressionStatement   : 'EXPRESSION_STATEMENT'
                        ;
TkLabelStatement        : 'LABEL_STATEMENT'
                        ;
TkStatementList         : 'STATEMENT_LIST'
                        ;
TkMethodName            : 'METHOD_NAME'
                        ;
TkMethodHeading         : 'METHOD_HEADING'
                        ;
TkMethodDeclaration     : 'METHOD_DECLARATION'
                        ;
TkMethodImplementation  : 'METHOD_IMPLEMENTATION'
                        ;
TkMethodBody            : 'METHOD_BODY'
                        ;
TkGenericDefinition     : 'GENERIC_DEFINITION'
                        ;
TkGenericArguments      : 'GENERIC_ARGUMENTS'
                        ;
TkTypeAlias             : 'TYPE_ALIAS'
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
TkIdentifierList        : 'IDENTIFIER_LIST'
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
TkRealNum               : 'REAL_NUMBER'
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
TkIdentifier            : (Alpha | '_') (Alpha | Digit | '_')*
                        ;
                        // We use a lookahead here to avoid lexer failures on range operations like '1..2'
                        // or record helper invocations on Integer literals
TkIntNum                : DigitSeq (
                            {input.LA(1) != '.' || Character.isDigit(input.LA(2))}? =>
                              (
                                '.' DigitSeq
                                {$type = TkRealNum;}
                              )?
                              (
                                ScaleFactor
                                {$type = TkRealNum;}
                              )?
                          )?
                        ;
TkHexNum                : '$' HexDigitSeq
                        ;
TkBinaryNum             : '%' BinaryDigitSeq
                        ;
TkAsmId                 : { asmMode }? => '@' '@'? (Alpha | '_' | Digit)+
                        ;
TkAsmHexNum             : { asmMode }? => HexDigitSeq ('h'|'H')
                        ;
TkQuotedString          : '\'' ('\'\'' | ~('\''))* '\''
                        ;
TkAsmDoubleQuotedString : { asmMode }? => '"' (~('\"'))* '"'
                        ;
TkCharacterEscapeCode   : '#' DigitSeq
                        | '#' '$' HexDigitSeq
                        | '#' '%' BinaryDigitSeq
                        ;
//----------------------------------------------------------------------------
// Fragments
//----------------------------------------------------------------------------
fragment
Alpha                   : 'a'..'z'
                        | 'A'..'Z'
                        | ('\ud800'..'\udbff') ('\udc00'..'\udfff')  // unicode support
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
WS                      : (' '|'\t'|'\r'|'\n'|'\f')+ {$channel=HIDDEN;}
                        ;
UnicodeBOM              : '\uFEFF' {$channel=HIDDEN;}
                        ;
//----------------------------------------------------------------------------
// Any character
//----------------------------------------------------------------------------
TkAnyChar               : .
                        ;

