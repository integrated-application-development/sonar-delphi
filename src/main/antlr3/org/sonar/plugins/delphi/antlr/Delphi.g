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

package org.sonar.plugins.delphi.antlr;

import org.sonar.plugins.delphi.antlr.ast.node.*;
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

package org.sonar.plugins.delphi.antlr;

}

@lexer::members {
  boolean asmMode = false;

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

//----------------------------------------------------------------------------
// File head
//----------------------------------------------------------------------------

program                      : (programHead)? (usesFileClause)? block '.'
                             ;
programHead                  : 'program'<ProgramDeclarationNode>^ nameDeclaration (programParmSeq)? ';'!
                             ;
programParmSeq               : '(' (ident (',' ident)* )? ')'
                             ;
library                      : libraryHead (usesFileClause)? block '.'
                             ;
libraryHead                  : 'library'<LibraryDeclarationNode>^ nameDeclaration (portabilityDirective!)* ';'!
                             ;
package_                     : packageHead requiresClause (containsClause)? 'end' '.'
                             ;
packageHead                  : 'package'<PackageDeclarationNode>^ nameDeclaration ';'!
                             ;
unit                         : unitHead unitInterface unitImplementation unitBlock '.'
                             ;
unitHead                     : 'unit'<UnitDeclarationNode>^ nameDeclaration portabilityDirective* ';'!
                             ;
unitInterface                : 'interface'<InterfaceSectionNode>^ usesClause? interfaceDecl*
                             ;
unitImplementation           : 'implementation'<ImplementationSectionNode>^ usesClause? declSection*
                             ;
unitBlock                    : unitInitialization 'end'
                             | compoundStatement
                             | 'end'
                             ;
unitInitialization           : 'initialization'<InitializationSectionNode>^ statementList unitFinalization?
                             ;
unitFinalization             : 'finalization'<FinalizationSectionNode>^ statementList
                             ;

//----------------------------------------------------------------------------
// File usage
//----------------------------------------------------------------------------
containsClause               : 'contains'<ContainsClauseNode>^ unitInFileImportList
                             ;
requiresClause               : 'requires'<RequiresClauseNode>^ unitImportList
                             ;
usesClause                   : 'uses'<UsesClauseNode>^ unitImportList
                             ;
usesFileClause               : 'uses'<UsesClauseNode>^ unitInFileImportList
                             ;
unitInFileImportList         : unitInFileImport (','! unitInFileImport)* ';'!
                             ;
unitImportList               : unitImport (','! unitImport)* ';'!
                             ;
unitImport                   : nameDeclaration
                             -> ^(TkUnitImport<UnitImportNode> nameDeclaration)
                             ;
unitInFileImport             : nameDeclaration ('in' textLiteral)?
                             -> ^(TkUnitImport<UnitImportNode> nameDeclaration ('in' textLiteral)?)
                             ;

//----------------------------------------------------------------------------
// Declarations
//----------------------------------------------------------------------------
block                        : blockDeclSection? blockBody
                             ;
blockDeclSection             : declSection+ -> ^(TkBlockDeclSection<BlockDeclarationSectionNode> declSection+)
                             ;
blockBody                    : compoundStatement
                             | assemblerStatement
                             ;
declSection                  : labelDeclSection
                             | constSection
                             | typeSection
                             | varSection
                             | methodImplementation
                             | methodDeclaration
                             | exportsSection
                             ;
interfaceDecl                : constSection
                             | typeSection
                             | varSection
                             | exportsSection
                             | methodDeclaration
                             ;
labelDeclSection             : 'label' (label (','!)?)+ ';'
                             ;
constSection                 : ('const'<ConstSectionNode>^ | 'resourcestring'<ConstSectionNode>^) constDeclaration*
                             // constSection was changed at some point from "constDeclaration+" to "constDeclaration*" to cater to invalid includes
                             // example: "const {$include versioninfo.inc}"
                             // Is this really the appropriate solution?
                             ;
constDeclaration             : customAttribute? varNameDeclaration (':' varType)? '=' constExpression portabilityDirective* ';'
                             -> ^(TkConstDeclaration<ConstDeclarationNode> varNameDeclaration constExpression varType? portabilityDirective*)
                             ;
typeSection                  : 'type'^ typeDeclaration+
                             ;
innerTypeSection             : 'type'^ typeDeclaration*
                             ;
typeDeclaration              : customAttribute? genericNameDeclaration '=' typeDecl portabilityDirective* ';'
                             -> ^(TkNewType<TypeDeclarationNode> genericNameDeclaration typeDecl customAttribute? portabilityDirective*)
                             ;
varSection                   : ('var'<VarSectionNode>^ | 'threadvar'<VarSectionNode>^) varDeclaration varDeclaration*
                             ;
varDeclaration               : customAttribute? varNameDeclarationList ':' varType varValueSpec? portabilityDirective* ';'
                             -> ^(TkVarDeclaration<VarDeclarationNode> varNameDeclarationList varType customAttribute?)
                             ;
varValueSpec                 : 'absolute' ident
                             | 'absolute' constExpression
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
                             | variantType
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
                             | stringType
                             | procedureType
                             | variantType
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
                             -> ^('array'<ArrayTypeNode> 'of' arraySubType arrayIndices? )
                             ;
arrayIndices                 : '['<ArrayIndicesNode>^ (varType (','!)?)+ ']'!
                             ;
arraySubType                 : 'const'<ConstArraySubTypeNode>
                             | varType
                             ;
setType                      : 'set'<SetTypeNode>^ 'of' varType
                             ;
fileType                     : 'file'<FileTypeNode>^ ('of' varType)?
                             ;
pointerType                  : '^'<PointerTypeNode>^ varType
                             ;
stringType                   : 'string'<StringTypeNode>^ ('[' expression ']')?
                             | ANSISTRING<AnsiStringTypeNode>^ (codePageNumber)?
                             ;
codePageNumber               : '(' intNum ')'
                             ;
procedureType                : methodType
                             | procedureReference
                             | simpleProcedureType
                             ;
methodType                   : procedureTypeHeading 'of' 'object'<MethodTypeNode>^ ((';')? methodDirective)*
                             ;
procedureReference           : 'reference'<ProcedureReferenceTypeNode>^ 'to'! procedureTypeHeading
                             ;
simpleProcedureType          : procedureTypeHeading -> ^(TkProcedureType<ProcedureTypeNode> procedureTypeHeading)
                             ;
procedureTypeHeading         : 'function'<ProcedureTypeHeadingNode>^ methodParameters? methodReturnType? ((';')? methodDirective)*
                             | 'procedure'<ProcedureTypeHeadingNode>^ methodParameters? ((';')? methodDirective)*
                             ;
variantType                  : 'variant'<VariantTypeNode>^
                             ;
typeOfType                   : 'type'<TypeOfTypeNode>^ 'of' typeReference
                             ;
typeType                     : 'type'<TypeTypeNode>^ typeReference
                             ;
typeAlias                    : typeReference -> ^(TkTypeAlias<TypeAliasNode> typeReference)
                             ;
subRangeType                 : expression '..'<SubRangeTypeNode>^ expression
                             ;
enumType                     : '('<EnumTypeNode>^ (enumTypeElement (',')?)* ')'!
                             ;
enumTypeElement              : nameDeclaration ('=' expression)? -> ^(TkEnumElement<EnumElementNode> nameDeclaration expression?)
                             ;
typeReference                : nameReference -> ^(TkTypeReference<TypeReferenceNode> nameReference)
                             ;

//----------------------------------------------------------------------------
// Struct Types
//----------------------------------------------------------------------------
classReferenceType           : 'class'<ClassReferenceTypeNode>^ 'of' typeReference
                             ;
classType                    : 'class' classState? classParent? (visibilitySection* 'end')?
                             -> ^('class'<ClassTypeNode> classParent? classState? (visibilitySection* 'end')?)
                             ;
classState                   : 'sealed'
                             | 'abstract'
                             ;
classParent                  : '(' typeReference (',' typeReference)* ')'
                             -> ^(TkClassParents<AncestorListNode> typeReference typeReference*)
                             ;
visibilitySection            : visibilitySection_ -> ^(TkVisibilitySection<VisibilitySectionNode> visibilitySection_)
                             ;
visibilitySection_           : visibility visibilitySectionItem*
                             | visibilitySectionItem+
                             ;
visibilitySectionItem        : fieldSection
                             | methodDeclaration
                             | property
                             | constSection
                             | innerTypeSection
                             ;
fieldSection                 : 'class'? 'var' fieldDecl* -> ^(TkFieldSection<FieldSectionNode> 'class'? 'var' fieldDecl*)
                             | fieldDecl+ -> ^(TkFieldSection<FieldSectionNode> fieldDecl+)
                             ;
fieldDecl                    : customAttribute? varNameDeclarationList ':' varType portabilityDirective* ';'?
                             -> ^(TkFieldDeclaration<FieldDeclarationNode> varNameDeclarationList varType portabilityDirective* ';'?)
                             ;
classHelperType              : 'class'<ClassHelperTypeNode>^ 'helper' classParent? 'for' typeReference visibilitySection* 'end'
                             ;
interfaceType                : ('interface'<InterfaceTypeNode>^ | 'dispinterface'<InterfaceTypeNode>^) classParent? (interfaceGuid? interfaceItems? 'end')?
                             ;
interfaceGuid                : '[' textLiteral ']' -> ^(TkGuid<InterfaceGuidNode> textLiteral)
                             ;
interfaceItems               : interfaceItem+ -> ^(TkVisibilitySection<VisibilitySectionNode> interfaceItem+)
                             ;
interfaceItem                : methodDeclaration
                             | property
                             ;
objectType                   : 'object'<ObjectTypeNode>^ classParent? visibilitySection* 'end' // Obselete, kept for backwards compatibility with Turbo Pascal
                             ;                                                                 // See: https://www.oreilly.com/library/view/delphi-in-a/1565926595/re192.html
recordType                   : 'record'<RecordTypeNode>^ visibilitySection* recordVariantSection? 'end'
                             ;
recordVariantSection         : 'case'<RecordVariantSectionNode>^ (ident ':')? typeReference 'of' recordVariant+
                             ;
recordVariant                : expressionList ':' '(' fieldDecl* recordVariantSection? ')' ';'?
                             -> ^(TkRecordVariantItem<RecordVariantItemNode> expressionList fieldDecl* recordVariantSection? ';'?)
                             ;
recordHelperType             : 'record'<RecordHelperTypeNode>^ 'helper' 'for' typeReference visibilitySection* 'end'
                             ;
property                     : customAttribute? 'class'? 'property' nameDeclaration propertyArray? (':' varType)? (propertyDirective)* ';'
                             -> ^('property'<PropertyNode> nameDeclaration propertyArray? varType? 'class'? customAttribute? propertyDirective*)
                             ;
propertyArray                : '['! formalParameterList ']'!
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
propertyReadWrite            : ('read'<PropertyReadSpecifierNode>^ | 'write'<PropertyWriteSpecifierNode>^) primaryExpression
                             ;
propertyDispInterface        : 'readonly'
                             | 'writeonly'
                             | dispIDDirective
                             ;
visibility                   : STRICT? 'protected'<VisibilityNode>^
                             | STRICT? 'private'<VisibilityNode>^
                             | 'public'<VisibilityNode>
                             | 'published'<VisibilityNode>
                             | 'automated'<VisibilityNode> // Obselete directive used for RTTI.
                             ;                             // See: https://www.oreilly.com/library/view/delphi-in-a/1565926595/re24.html

//----------------------------------------------------------------------------
// Generics
//----------------------------------------------------------------------------
genericDefinition            : simpleGenericDefinition -> ^(TkGenericDefinition<GenericDefinitionNode> simpleGenericDefinition)
                             | complexGenericDefinition  -> ^(TkGenericDefinition<GenericDefinitionNode> complexGenericDefinition)
                             | constrainedGenericDefinition  -> ^(TkGenericDefinition<GenericDefinitionNode> constrainedGenericDefinition)
                             ;
simpleGenericDefinition      : '<' ident (',' ident)* '>'
                             ;
complexGenericDefinition     : '<' nameReference (simpleGenericDefinition)? (',' nameReference (simpleGenericDefinition)?)* '>'
                             ;
constrainedGenericDefinition : '<' constrainedGeneric (';' constrainedGeneric)* '>'
                             ;
constrainedGeneric           : ident (':' genericConstraint (',' genericConstraint)*)?
                             ;
genericConstraint            : nameReference
                             | 'record'
                             | 'class'
                             | 'constructor'
                             ;

//----------------------------------------------------------------------------
// Methods
//----------------------------------------------------------------------------
methodDeclaration            : methodDeclarationHeading
                             -> ^(TkMethodDeclaration<MethodDeclarationNode>
                                    methodDeclarationHeading
                                 )
                             ;
methodImplementation         : methodImplementationHeading methodBody
                             -> ^(TkMethodImplementation<MethodImplementationNode>
                                    methodImplementationHeading
                                    methodBody
                                 )
                             ;
methodDeclarationHeading     : customAttribute? 'class'? methodKey methodDeclarationName methodParameters? methodReturnType? methodDeclDirectiveSection
                             -> ^(TkMethodHeading<MethodHeadingNode>
                                    methodKey
                                    methodDeclarationName
                                    methodParameters?
                                    methodReturnType?
                                    customAttribute?
                                    'class'?
                                    methodDeclDirectiveSection
                                 )
                             ;
methodImplementationHeading  : customAttribute? 'class'? methodKey methodImplementationName methodParameters? methodReturnType? methodImplDirectiveSection
                             -> ^(TkMethodHeading<MethodHeadingNode>
                                    methodKey
                                    methodImplementationName
                                    methodParameters?
                                    methodReturnType?
                                    customAttribute?
                                    'class'?
                                    methodImplDirectiveSection
                                 )
                             ;
methodDeclarationName        : genericNameDeclaration -> ^(TkMethodName<MethodNameNode> genericNameDeclaration)
                             ;
methodImplementationName     : nameReference -> ^(TkMethodName<MethodNameNode> nameReference)
                             ;
methodKey                    : 'procedure'
                             | 'constructor'
                             | 'destructor'
                             | 'function'
                             | 'operator'
                             ;
methodReturnType             : ':' customAttribute? returnType -> ^(TkMethodReturn<MethodReturnTypeNode> returnType customAttribute?)
                             ;
returnType                   : stringType
                             | typeReference
                             ;
methodParameters             : '(' formalParameterList? ')' -> ^(TkMethodParameters<MethodParametersNode> '(' formalParameterList? ')')
                             ;
formalParameterList          : formalParameter (';' formalParameter)* -> ^(TkFormalParameterList<FormalParameterListNode> formalParameter formalParameter*)
                             ;
formalParameter              : customAttribute? paramSpecifier? varNameDeclarationList (':' parameterType)? ('=' expression)?
                             -> ^(TkFormalParameter<FormalParameterNode> varNameDeclarationList parameterType? paramSpecifier? expression? customAttribute?)
                             ;
paramSpecifier               : 'const'
                             | 'var'
                             | 'out'
                             ;
methodBody                   : block ';' -> ^(TkMethodBody<MethodBodyNode> block)
                             ;

//----------------------------------------------------------------------------
// Custom Attributes
//----------------------------------------------------------------------------
customAttribute              : customAttributeList -> ^(TkCustomAttributeList<CustomAttributeListNode> customAttributeList)
                             ;
customAttributeList          : customAttributeDecl+
                             ;
customAttributeDecl          : '[' nameReference argumentList? ']'
                             -> ^(TkCustomAttribute<CustomAttributeNode> '[' nameReference argumentList? ']')
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
primaryExpression            : atom -> ^(TkPrimaryExpression<PrimaryExpressionNode> atom)
                             | parenthesizedExpression
                             | 'inherited' atom? -> ^(TkPrimaryExpression<PrimaryExpressionNode> 'inherited' atom?)
                             ;
parenthesizedExpression      : '(' expression ')' -> ^(TkNestedExpression<ParenthesizedExpressionNode> expression)
                             ;
atom                         : particle particleItem*
                             ;
particle                     : intNum
                             | realNum
                             | textLiteral
                             | nilLiteral
                             | nameReference
                             | setLiteral
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
arrayAccessor                : '['<ArrayAccessorNode>^ expressionList ']'!
                             ;
argumentList                 : '('<ArgumentListNode>^ expressionOrAnonymousList? ')'
                             ;
anonymousMethod              : 'procedure'<AnonymousMethodNode>^ methodParameters? block
                             | 'function'<AnonymousMethodNode>^ methodParameters? methodReturnType block
                             ;
expressionOrRange            : expression ('..'<RangeExpressionNode>^ expression)?
                             ;
expressionOrAnonymousMethod  : anonymousMethod
                             | expression
                             ;
expressionList               : (expression (','!)?)+
                             ;
expressionOrRangeList        : (expressionOrRange (','!)?)+
                             ;
expressionOrAnonymousList    : (expressionOrAnonymousMethod (','!)?)+
                             ;
textLiteral                  : textLiteral_ -> ^(TkTextLiteral<TextLiteralNode> textLiteral_)
                             ;
textLiteral_                 : QuotedString (EscapedCharacter+ QuotedString)* EscapedCharacter*
                             | EscapedCharacter+ (QuotedString EscapedCharacter+)* QuotedString?
                             ;
nilLiteral                   : 'nil'<NilLiteralNode>
                             ;
setLiteral                   : '['<SetLiteralNode>^ (expressionOrRangeList)? ']'
                             ;
addOperator                  : '+'<BinaryExpressionNode>
                             | '-'<BinaryExpressionNode>
                             | 'or'<BinaryExpressionNode>
                             | 'xor'<BinaryExpressionNode>
                             ;
multOperator                 : '*'<BinaryExpressionNode>
                             | '/'<BinaryExpressionNode>
                             | 'div'<BinaryExpressionNode>
                             | 'mod'<BinaryExpressionNode>
                             | 'and'<BinaryExpressionNode>
                             | 'shl'<BinaryExpressionNode>
                             | 'shr'<BinaryExpressionNode>
                             ;
unaryOperator                : 'not'<UnaryExpressionNode>
                             | '+'<UnaryExpressionNode>
                             | '-'<UnaryExpressionNode>
                             | '@'<UnaryExpressionNode>
                             ;
relationalOperator           : '='<BinaryExpressionNode>
                             | '>'<BinaryExpressionNode>
                             | '<'<BinaryExpressionNode>
                             | '<='<BinaryExpressionNode>
                             | '>='<BinaryExpressionNode>
                             | '<>'<BinaryExpressionNode>
                             | 'in'<BinaryExpressionNode>
                             | 'is'<BinaryExpressionNode>
                             | 'as'<BinaryExpressionNode>
                             ;
constExpression              : expression
                             | recordExpression
                             | arrayExpression
                             ;
recordExpression             : '('<RecordExpressionNode>^ (recordExpressionItem (';')?)+ ')'
                             ;
recordExpressionItem         : ident ':' constExpression
                             -> ^(TkRecordExpressionItem<RecordExpressionItemNode> ident constExpression)
                             ;
arrayExpression              : '('<ArrayExpressionNode>^ (constExpression (','!)?)* ')'
                             ;

//----------------------------------------------------------------------------
// Statements
//----------------------------------------------------------------------------
statement                    : ifStatement
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
ifStatement                  : 'if'<IfStatementNode>^ expression 'then' (statement ('else' statement)?)?
                             ;
caseStatement                : 'case'<CaseStatementNode>^ expression 'of' caseItem* elseBlock? 'end'
                             ;
elseBlock                    : 'else'<ElseBlockNode>^ statementList
                             ;
caseItem                     : expressionOrRangeList ':' (statement)? (';')? -> ^(TkCaseItem<CaseItemStatementNode> expressionOrRangeList (statement)? (';')? )
                             ;
repeatStatement              : 'repeat'<RepeatStatementNode>^ statementList 'until' expression
                             ;
whileStatement               : 'while'<WhileStatementNode>^ expression 'do' statement
                             ;
forStatement                 : 'for'<ForStatementNode>^ ident ':=' expression 'to' expression 'do' statement
                             | 'for'<ForStatementNode>^ ident ':=' expression 'downto' expression 'do' statement
                             | 'for'<ForStatementNode>^ ident 'in' expression 'do' statement
                             ;
withStatement                : 'with'<WithStatementNode>^ expressionList 'do' statement
                             ;
compoundStatement            : 'begin'<CompoundStatementNode>^ statementList 'end'
                             ;
statementList                : delimetedStatements? -> ^(TkStatementList<StatementListNode> delimetedStatements?)
                             ;
delimetedStatements          : (statement | ';')+
                             ;
labelStatement               : label ':' statement -> ^(TkLabelStatement<LabelStatementNode> label statement)
                             ;
assignmentStatement          : expression ':='<AssignmentStatementNode>^ expressionOrAnonymousMethod
                             ;
expressionStatement          : expression -> ^(TkExpressionStatement<ExpressionStatementNode> expression)
                             ;
gotoStatement                : 'goto'<GotoStatementNode>^ label
                             | 'exit'<ExitStatementNode>^ ('(' expression ')')?
                             | 'break'<BreakStatementNode>
                             | 'continue'<ContinueStatementNode>
                             ;
tryStatement                 : 'try'<TryStatementNode>^ statementList (exceptBlock | finallyBlock) 'end'
                             ;
exceptBlock                  : 'except'<ExceptBlockNode>^ handlerList
                             ;
finallyBlock                 : 'finally'<FinallyBlockNode>^ statementList
                             ;
handlerList                  : handler+ elseBlock?
                             | statementList
                             ;
handler                      : 'on'<ExceptItemNode>^ (varNameDeclaration ':'!)? typeReference 'do' statement? (';')?
                             ;
raiseStatement               : 'raise'<RaiseStatementNode>^ expression? (AT! expression)?
                             ;
assemblerStatement           : 'asm'<AsmStatementNode>^ assemblerInstructions 'end'
                             ;
assemblerInstructions        : ~('end')* // Skip asm statements
                             ;

//----------------------------------------------------------------------------
// Directives
//----------------------------------------------------------------------------
methodImplDirectiveSection   : ((';')? methodDirective)* ';'
                             | standaloneOverloadDirective
                             ;
methodDeclDirectiveSection   : ((';')? (methodDirective | 'forward'))* ';'
                             | standaloneOverloadDirective
                             ;
standaloneOverloadDirective  : ';' 'overload' (';')?
                             ;
methodDirective              : 'overload'
                             | 'reintroduce'
                             | bindingDirective
                             | abstractDirective // virtual;
                             | inlineDirective   // niet virtual or dynamic
                             | callConvention
                             | portabilityDirective  // (niet abstract)
                             | oldCallConventionDirective
                             | dispIDDirective
                             | externalDirective
                             | 'unsafe'  // .net?
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
                             | 'local'    // niet in windows maakt functie niet exporteerbaar
                             | 'near'     // deprecated
                             ;
portabilityDirective         : 'deprecated'^ textLiteral?
                             | 'experimental'
                             | 'platform'
                             | 'library'
                             ;
externalDirective            : 'varargs'   // alleen bij external cdecl
                             | 'external'^ (expression)? (externalSpecifier)* // expression = dll name
                             ;
externalSpecifier            : 'name'^ constExpression
                             | 'index'^ constExpression   // specific to a platform
                             ;
dispIDDirective              : 'dispid' expression
                             ;

//----------------------------------------------------------------------------
// General
//----------------------------------------------------------------------------
ident                        : TkIdentifier
                             | '&'! TkIdentifier
                             | keywordsUsedAsNames -> ^({changeTokenType(TkIdentifier)})
                             ;
keywordsUsedAsNames          : (NAME | READONLY | ADD | AT | MESSAGE | POINTER | INDEX | DEFAULT | STRING | CONTINUE)
                             | (READ | WRITE | REGISTER | VARIANT | OPERATOR | REMOVE | LOCAL | REFERENCE | CONTAINS | FINAL)
                             | (BREAK | EXIT | STRICT | OUT | OBJECT | EXPORT | ANSISTRING | IMPLEMENTS | STORED | HELPER )
                             | (PACKAGE | DEPRECATED)
                             ;
keywords                     : (ABSOLUTE | ABSTRACT | ADD | AND | ANSISTRING | ARRAY | AS | ASM | ASSEMBLER | ASSEMBLY)
                             | (AT | AUTOMATED | BEGIN | BREAK | CASE | CDECL | CLASS | CONST | CONSTRUCTOR | CONTAINS)
                             | (CONTINUE | DEFAULT | DEPRECATED | DESTRUCTOR | DISPID | DISPINTERFACE | DIV | DO | DOWNTO)
                             | (DQ | DW | DYNAMIC | ELSE | END | EXCEPT | EXIT | EXPERIMENTAL | EXPORT | EXPORTS | EXTERNAL)
                             | (FAR | FILE | FINAL | FINALIZATION | FINALLY | FOR | FORWARD | FUNCTION | GOTO | HELPER | IF)
                             | (IMPLEMENTATION | IMPLEMENTS | IN | INDEX | INHERITED | INITIALIZATION | INLINE | INTERFACE)
                             | (IS | LABEL | LIBRARY | LOCAL | MESSAGE | MOD | NAME | NEAR | NIL | NODEFAULT | NOT | OBJECT)
                             | (OF | ON | OPERATOR | OR | OUT | OVERLOAD | OVERRIDE | PACKAGE | PACKED | PASCAL | PLATFORM)
                             | (PRIVATE | PROCEDURE | PROGRAM | PROPERTY | PROTECTED | PUBLIC | PUBLISHED | RAISE)
                             | (READ | READONLY | RECORD | REFERENCE | REGISTER | REINTRODUCE | REMOVE | REPEAT | REQUIRES)
                             | (RESIDENT | RESOURCESTRING | SAFECALL | SEALED | SET | SHL | SHR | STATIC | STDCALL | STORED)
                             | (STRICT | STRING | THEN | THREADVAR | TO | TRY | TYPE | UNIT | UNSAFE | UNTIL | USES | VAR)
                             | (VARARGS | VARIANT | VIRTUAL | WHILE | WITH | WRITE | WRITEONLY | XOR)
                             ;
varNameDeclarationList       : varNameDeclaration (',' varNameDeclaration)* -> ^(TkVarNameDeclList<VarNameDeclarationListNode> varNameDeclaration varNameDeclaration*)
                             ;
varNameDeclaration           : ident -> ^(TkVarNameDeclaration<VarNameDeclarationNode> ident)
                             ;
label                        : ident
                             | intNum
                             ;
nameDeclaration              : ident ('.' extendedIdent)*
                             -> ^(TkNameDeclaration<QualifiedNameDeclarationNode> ident ('.' extendedIdent)*)
                             ;
genericNameDeclaration       : ident ('.' extendedIdent)* genericDefinition?
                             -> ^(TkNameDeclaration<QualifiedNameDeclarationNode> ident ('.' extendedIdent)* genericDefinition?)
                             ;
nameReference                : ident genericDefinition? ('.' extendedNameReference)?
                             -> ^(TkNameReference<NameReferenceNode> ident genericDefinition? ('.' extendedNameReference)?)
                             ;
extendedNameReference        : extendedIdent genericDefinition? ('.' extendedNameReference)?
                             -> ^(TkNameReference<NameReferenceNode> extendedIdent genericDefinition? ('.' extendedNameReference)?)
                             ;
extendedIdent                : ident
                             | keywords -> ^({changeTokenType(TkIdentifier)})
                             ;
//----------------------------------------------------------------------------
// Literals
//----------------------------------------------------------------------------
intNum                       : TkIntNum<IntegerLiteralNode>
                             | TkHexNum<HexLiteralNode>
                             ;
realNum                      : TkRealNum<DecimalLiteralNode>
                             ;
asmHexNum                    : TkAsmHexNum<HexLiteralNode>
                             ;

//----------------------------------------------------------------------------
// Keywords
//----------------------------------------------------------------------------
ABSOLUTE          : 'absolute'       	         ;
ABSTRACT          : 'abstract'       	         ;
ADD               : 'add'            	         ;
AND               : 'and'           	         ;
ANSISTRING        : 'ansistring'     	         ;
ARRAY             : 'array'          	         ;
AS                : 'as'             	         ;
ASM               : 'asm' { asmMode = true; }  ;
ASSEMBLER         : 'assembler'       	       ;
ASSEMBLY          : 'assembly'       	         ;
AT                : 'at'             	         ;
AUTOMATED         : 'automated'      	         ;
BEGIN             : 'begin'          	         ;
BREAK             : 'break'          	         ;
CASE              : 'case'           	         ;
CDECL             : 'cdecl'          	         ;
CLASS             : 'class'          	         ;
CONST             : 'const'           	       ;
CONSTRUCTOR       : 'constructor'              ;
CONTAINS          : 'contains'                 ;
CONTINUE          : 'continue'                 ;
DEFAULT           : 'default'                  ;
DEPRECATED        : 'deprecated'               ;
DESTRUCTOR        : 'destructor'               ;
DISPID            : 'dispid'                   ;
DISPINTERFACE     : 'dispinterface'            ;
DIV               : 'div'                      ;
DO                : 'do'                       ;
DOWNTO            : 'downto'                   ;
DQ                : 'dq'                       ;
DW                : 'dw'                       ;
DYNAMIC           : 'dynamic'        	         ;
ELSE              : 'else'           	         ;
END               : 'end' { asmMode = false; } ;
EXCEPT            : 'except'                   ;
EXIT              : 'exit'                     ;
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
IMPLEMENTATION    : 'implementation'           ;
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
LPAREN            : '('   ;
RPAREN            : ')'   ;
LBRACK            : '['   ; // line_tab[line]
LBRACK2           : '(.'  ; // line_tab(.line.)
RBRACK            : ']'   ;
RBRACK2           : '.)'  ;
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
TkVarNameDeclaration    : 'VAR_NAME_DECLARATION'
                        ;
TkVarNameDeclList       : 'VAR_NAME_DECLARATION_LIST'
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

//****************************
// Tokens
//****************************
TkIdentifier            : (Alpha | '_') (Alpha | Digit | '_')*
                        ;
TkIntNum                : Digitseq
                        ;
                        // We use a lookahead here to avoid lexer failures on range operations like '1..2'
TkRealNum               : Digitseq ({ input.LA(2) != '.' }? => '.' Digitseq)? (('e'|'E') ('+'|'-')? Digitseq)?
                        ;
TkHexNum                : '$' Hexdigitseq
                        ;
TkAsmHexNum             : { asmMode }? => Hexdigitseq ('h'|'H')
                        ;
TkAsmHexLabel           : { asmMode }? => Hexdigitseq ':'
                        ;
QuotedString            : '\'' ('\'\'' | ~('\''))* '\''
                        ;
EscapedCharacter        : '#' Digitseq
                        | '#' '$' Hexdigitseq
                        ;

//----------------------------------------------------------------------------
// Fragments
//----------------------------------------------------------------------------
fragment
Alpha                   : 'a'..'z'
                        | 'A'..'Z'
                        | '\u0080'..'\uFFFE' ~('\uFEFF') // unicode support
                        ;
fragment
Digit                   : '0'..'9'
                        ;
fragment
Digitseq                : Digit (Digit)*
                        ;
fragment
Hexdigit                : Digit | 'a'..'f' | 'A'..'F'
                        ;
fragment
Hexdigitseq		          : Hexdigit (Hexdigit)*
                        ;

//----------------------------------------------------------------------------
// Hidden channel
//----------------------------------------------------------------------------		
COMMENT                 :  '//' ~('\n'|'\r')*                          {$channel=HIDDEN;}
                        |  '(*' ( options {greedy=false;} : . )* '*)'  {$channel=HIDDEN;}
                        |  '{' ( options {greedy=false;} : . )* '}'    {$channel=HIDDEN;}
                        ;
WS                      : (' '|'\t'|'\r'|'\n'|'\f')+ {$channel=HIDDEN;}
                        ;
UnicodeBOM              : '\uFEFF' {$channel=HIDDEN;}
                        ;

