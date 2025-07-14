grammar Delphi;

options {
  backtrack=true;
  memoize=true;
  output=AST;
}

tokens {
  //----------------------------------------------------------------------------
  // Deprecated tokens
  //----------------------------------------------------------------------------
  AMPERSAND__deprecated;
  TkGuid__deprecated;

  //----------------------------------------------------------------------------
  // Imaginary tokens
  //----------------------------------------------------------------------------
  TkRootNode;
  TkRoutineParameters;
  TkRoutineReturn;
  TkAttributeList;
  TkAttributeGroup;
  TkAttribute;
  TkTypeDeclaration;
  TkRecordVariantItem;
  TkRecordVariantTag;
  TkRecordExpressionItem;
  TkClassParents;
  TkLocalDeclarations;
  TkCaseItem;
  TkExpressionStatement;
  TkLabelStatement;
  TkStatementList;
  TkRoutineName;
  TkRoutineHeading;
  TkRoutineDeclaration;
  TkRoutineImplementation;
  TkRoutineBody;
  TkGenericDefinition;
  TkGenericArguments;
  TkWeakAlias;
  TkTypeReference;
  TkProcedureType;
  TkEnumElement;
  TkVisibilitySection;
  TkVisibility;
  TkFieldSection;
  TkFieldDeclaration;
  TkFormalParameterList;
  TkFormalParameter;
  TkVarDeclaration;
  TkNameDeclarationList;
  TkConstDeclaration;
  TkPrimaryExpression;
  TkNestedExpression;
  TkTextLiteral;
  TkMultilineString;
  TkNameDeclaration;
  TkNameReference;
  TkUnitImport;
  TkMethodResolveClause;
  TkEscapedCharacter;
  TkCompilerDirective;
  TkRealNumber;
  TkTypeParameter;
  TkTypeConstraint;
  TkForLoopVar;
  TkArrayAccessorNode;
  TkArrayConstructor;
  TkArrayIndices;
  TkArgument;
  TkAnonymousMethod;
  TkAnonymousMethodHeading;
  TkLessThanEqual;
  TkGreaterThanEqual;
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
import au.com.integradev.delphi.antlr.ast.token.DelphiTokenImpl;
import au.com.integradev.delphi.antlr.ast.token.IncludeToken;
import au.com.integradev.delphi.utils.LocatableException;
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

import org.apache.commons.lang3.Strings;
import au.com.integradev.delphi.utils.LocatableException;
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
    throwLexerException(hdr + " " + msg, e.line, e);
  }

  @Override
  public String getErrorHeader(RecognitionException e) {
    return "line " + e.line + ":" + e.charPositionInLine;
  }

  private void throwLexerException(String message, int line) {
    throwLexerException(message, line, null);
  }

  protected void throwLexerException(String message, int line, Throwable cause) {
    throw new LexerException(message, line, cause);
  }

  public static class LexerException extends RuntimeException implements LocatableException {
    private final int line;

    public LexerException(String message, int line) {
      super(message);
      this.line = line;
    }

    public LexerException(String message, int line, Throwable cause) {
      super(message, cause);
      this.line = line;
    }

    @Override
    public int getLine() {
      return line;
    }
  }

  private int lookaheadMultilineComment(String end) {
    return lookaheadMultilineComment(end, 1);
  }

  private int lookaheadMultilineComment(String end, int i) {
    char endStart = end.charAt(0);
    String directiveName = null;

    if (input.LA(i) == '$') {
      StringBuilder directiveNameBuilder = new StringBuilder();
      int character = input.LA(i + 1);

      while ((character >= 'a' && character <= 'z')
          || (character >= 'A' && character <= 'Z')
          || Character.isDigit(character)
          || character == '_') {
        ++i;
        directiveNameBuilder.append((char) character);
        character = input.LA(i + 1);
      }

      directiveName = directiveNameBuilder.toString();
    }

    boolean nestedExpression =
        "if".equalsIgnoreCase(directiveName) || "elseif".equalsIgnoreCase(directiveName);

    while (true) {
      int character = input.LA(i);

      if (character == endStart) {
        int j;
        for (j = 1; j < end.length(); ++j) {
          if (input.LA(i + j) != end.charAt(j)) {
            break;
          }
        }
        if (j == end.length()) {
          return i + j;
        }
      }

      switch (character) {
        case '\'':
          if (nestedExpression) {
            i = lookaheadString(i) - 1;
          }
          break;

        case '/':
          if (nestedExpression && input.LA(i + 1) == '/') {
            i = lookaheadLineComment(i + 2);
          }
          break;

        case '{':
          if (nestedExpression) {
            i = lookaheadMultilineComment("}", i + 1) - 1;
          }
          break;

        case '(':
          if (nestedExpression && input.LA(i + 1) == '*') {
            i = lookaheadMultilineComment("*)", i + 2) - 1;
          }
          break;

        case EOF:
          throwLexerException(
              "line "
                  + state.tokenStartLine
                  + ":"
                  + state.tokenStartCharPositionInLine
                  + " unterminated multi-line comment",
              state.tokenStartLine);
          break;

        default:
          // do nothing
      }

      ++i;
    }
  }

  private int lookaheadLineComment(int i) {
    while (true) {
      int character = input.LA(i);
      if (isNewLine(character) || character == EOF) {
        return i;
      }
      ++i;
    }
  }

  private int lookaheadString(int i) {
    int offset = lookaheadMultilineString(i);
    if (offset == 0) {
      offset = lookaheadSingleLineString(i);
    }
    return i + offset;
  }

  private int lookaheadMultilineString(int i) {
    int startQuotes = lookaheadSingleQuotes(i);
    if (startQuotes >= 3 && (startQuotes & 1) != 0 && isNewLine(input.LA(i + startQuotes))) {
      int offset = startQuotes - 1;
      while (true) {
        switch (input.LA(i + ++offset)) {
          case '\'':
            int quotes = Math.min(startQuotes, lookaheadSingleQuotes(i + offset));
            offset += quotes;
            if (quotes == startQuotes) {
              return offset;
            }
            break;

          case EOF:
            return 0;

          default:
            // do nothing
        }
      }
    }
    return 0;
  }

  private int lookaheadSingleQuotes(int i) {
    int result = 0;
    while (input.LA(i++) == '\'') {
      ++result;
    }
    return result;
  }

  private int lookaheadSingleLineString(int i) {
    int offset = 1;

    int character;

    while ((character = input.LA(i + offset)) != EOF && !isNewLine(character)) {
      ++offset;
      if (character == '\'') {
        if (input.LA(i + offset) == '\'') {
          ++offset;
        } else {
          break;
        }
      }
    }

    return offset;
  }

  private static boolean isNewLine(int c) {
    return c == '\r' || c == '\n';
  }
}

@parser::members {
  private Token changeTokenType(int type) {
    return changeTokenType(type, -1);
  }

  private Token changeTokenType(int type, int offset) {
    CommonToken result = cloneToken((CommonToken) input.LT(offset));
    result.setType(type);
    return result;
  }

  private Token combineLastNTokens(int type, int count) {
    CommonToken firstToken = (CommonToken) input.LT(-count);
    CommonToken lastToken = (CommonToken) input.LT(-1);
    CommonToken result = cloneToken(lastToken);
    result.setType(type);
    result.setStartIndex(firstToken.getStartIndex());
    result.setLine(firstToken.getLine());
    result.setCharPositionInLine(firstToken.getCharPositionInLine());
    return result;
  }

  private static CommonToken cloneToken(CommonToken other) {
    if (other instanceof IncludeToken) {
      return new IncludeToken((IncludeToken) other);
    }
    return new CommonToken(other);
  }

  private BinaryExpressionNodeImpl createBinaryExpression(Object operator) {
    Token token = adaptor.getToken(operator);
    return new BinaryExpressionNodeImpl(token);
  }

  @Override
  public void reportError(RecognitionException e) {
    String hdr = this.getErrorHeader(e);
    String msg = this.getErrorMessage(e, this.getTokenNames());

    String message = hdr + " " + msg;
    int line;

    if (e.token == null) {
      line = 0;
    } else if (e.token instanceof IncludeToken) {
      line = ((DelphiTokenImpl) ((IncludeToken) e.token).getInsertionToken()).getBeginLine();
      message = "included on line " + line + " :: " + message;
    } else {
      line = e.token.getLine();
    }

    throw new ParserException(message, line, e);
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

  public static class ParserException extends RuntimeException implements LocatableException {
    private final int line;

    public ParserException(String message, int line, Throwable cause) {
      super(message, cause);
      this.line = line;
    }

    @Override
    public int getLine() {
      return this.line;
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
programHead                  : PROGRAM<ProgramDeclarationNodeImpl>^ qualifiedNameDeclaration programParameters? ';'!
                             ;
programParameters            : '(' (ident (',' ident)* )? ')' // Used in standard Pascal; Delphi ignores them.
                             ;
programBody                  : localDeclSection? (compoundStatement | END)
                             ;
library                      : libraryHead usesFileClause? programBody '.'
                             ;
libraryHead                  : LIBRARY<LibraryDeclarationNodeImpl>^ qualifiedNameDeclaration (portabilityDirective!)* ';'!
                             ;
package_                     : packageHead requiresClause? containsClause? attributeList* END '.'
                             ;
packageHead                  : PACKAGE<PackageDeclarationNodeImpl>^ qualifiedNameDeclaration ';'!
                             ;
unit                         : unitHead unitInterface unitImplementation unitBlock '.'
                             ;
unitWithoutImplementation    : unitHead unitInterface
                             ;
unitHead                     : UNIT<UnitDeclarationNodeImpl>^ qualifiedNameDeclaration portabilityDirective* ';'!
                             ;
unitInterface                : INTERFACE<InterfaceSectionNodeImpl>^ usesClause? interfaceDecl*
                             ;
unitImplementation           : IMPLEMENTATION<ImplementationSectionNodeImpl>^ usesClause? declSection*
                             ;
unitBlock                    : initializationFinalization? END
                             | compoundStatement
                             ;
initializationFinalization   : initializationSection finalizationSection?
                             ;
initializationSection        : INITIALIZATION<InitializationSectionNodeImpl>^ statementList
                             ;
finalizationSection          : FINALIZATION<FinalizationSectionNodeImpl>^ statementList
                             ;

//----------------------------------------------------------------------------
// File usage
//----------------------------------------------------------------------------
containsClause               : CONTAINS<ContainsClauseNodeImpl>^ unitInFileImportList
                             ;
requiresClause               : REQUIRES<RequiresClauseNodeImpl>^ unitImportList
                             ;
usesClause                   : USES<UsesClauseNodeImpl>^ unitImportList
                             ;
usesFileClause               : USES<UsesClauseNodeImpl>^ unitInFileImportList
                             ;
unitInFileImportList         : unitInFileImport (',' unitInFileImport)* ';'
                             ;
unitImportList               : unitImport (',' unitImport)* ';'
                             ;
unitImport                   : qualifiedNameDeclaration
                             -> ^(TkUnitImport<UnitImportNodeImpl> qualifiedNameDeclaration)
                             ;
unitInFileImport             : qualifiedNameDeclaration (IN textLiteral)?
                             -> ^(TkUnitImport<UnitImportNodeImpl> qualifiedNameDeclaration (IN textLiteral)?)
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
                             | attributeList
                             ;
interfaceDecl                : constSection
                             | typeSection
                             | varSection
                             | exportsSection
                             | routineInterface
                             | attributeList
                             ;
labelDeclSection             : LABEL<LabelDeclarationNodeImpl>^ labelNameDeclaration (',' labelNameDeclaration)* ';'
                             ;
constSection                 : (CONST<ConstSectionNodeImpl>^ | RESOURCESTRING<ConstSectionNodeImpl>^) constDeclaration*
                             // constSection was changed at some point from "constDeclaration+" to "constDeclaration*" to cater to invalid includes
                             // example: "const {$include versioninfo.inc}"
                             // Is this really the appropriate solution?
                             ;
constDeclaration             : attributeList? nameDeclaration ':' fixedArrayType '=' arrayExpression portabilityDirective* ';'
                             -> ^(TkConstDeclaration<ConstDeclarationNodeImpl> nameDeclaration arrayExpression fixedArrayType attributeList? portabilityDirective*)
                             | attributeList? nameDeclaration (':' varType)? '=' constExpression portabilityDirective* ';'
                             -> ^(TkConstDeclaration<ConstDeclarationNodeImpl> nameDeclaration constExpression varType? attributeList? portabilityDirective*)
                             ;
typeSection                  : TYPE<TypeSectionNodeImpl>^ typeDeclaration+
                             ;
innerTypeSection             : TYPE<TypeSectionNodeImpl>^ typeDeclaration*
                             ;
typeDeclaration              : attributeList? genericNameDeclaration '=' typeDecl portabilityDirective* ';'
                             -> ^(TkTypeDeclaration<TypeDeclarationNodeImpl> genericNameDeclaration typeDecl attributeList? portabilityDirective*)
                             ;
varSection                   : (VAR<VarSectionNodeImpl>^ | THREADVAR<VarSectionNodeImpl>^) varDeclaration varDeclaration*
                             ;
varDeclaration               : attributeList? nameDeclarationList ':' fixedArrayType portabilityDirective* arrayVarValueSpec? portabilityDirective* ';'
                             -> ^(TkVarDeclaration<VarDeclarationNodeImpl> nameDeclarationList fixedArrayType arrayVarValueSpec? attributeList?)
                             | attributeList? nameDeclarationList ':' varType portabilityDirective* varValueSpec? portabilityDirective* ';'
                             -> ^(TkVarDeclaration<VarDeclarationNodeImpl> nameDeclarationList varType varValueSpec? attributeList?)
                             ;
arrayVarValueSpec            : ABSOLUTE expression
                             | '=' arrayExpression
                             ;
varValueSpec                 : ABSOLUTE expression
                             | '=' constExpression
                             ;
exportsSection               : EXPORTS ident exportItem (',' ident exportItem)* ';'
                             ;
exportItem                   : ('(' formalParameterList ')')? (INDEX expression)? (NAME expression)? (RESIDENT)?
                             ;

//----------------------------------------------------------------------------
// Types
//----------------------------------------------------------------------------
typeDecl                     : arrayType
                             | setType
                             | fullFileType
                             | classHelperType
                             | classReferenceType
                             | classType
                             | interfaceType
                             | objectType
                             | recordType
                             | recordHelperType
                             | pointerType
                             | fullStringType
                             | procedureType
                             | subRangeType
                             | typeOfType
                             | strongAliasType
                             | weakAliasType
                             | enumType
                             | PACKED typeDecl^
                             ;
varType                      : arrayType
                             | setType
                             | fullStringType
                             | fullFileType
                             | recordType
                             | pointerType
                             | procedureType
                             | subRangeType
                             | typeReference
                             | enumType
                             | PACKED varType^
                             ;
parameterType                : stringType
                             | fileType
                             | arrayType
                             | typeReference
                             | PACKED parameterType^
                             ;
fixedArrayType               : ARRAY arrayIndices OF arrayElementType
                             -> ^(ARRAY<ArrayTypeNodeImpl> OF arrayElementType arrayIndices )
                             ;
arrayType                    :  ARRAY arrayIndices? OF arrayElementType
                             -> ^(ARRAY<ArrayTypeNodeImpl> OF arrayElementType arrayIndices? )
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
arrayElementType             : CONST<ConstArrayElementTypeNodeImpl>
                             | varType
                             ;
fileType                     : FILE<FileTypeNodeImpl>^
                             ;
fullFileType                 : FILE<FileTypeNodeImpl>^ (OF varType)?
                             ;
setType                      : SET<SetTypeNodeImpl>^ OF varType
                             ;
pointerType                  : '^'<PointerTypeNodeImpl>^ varType
                             ;
stringType                   : STRING<StringTypeNodeImpl>^
                             ;
fullStringType               : STRING<StringTypeNodeImpl>^ (lbrack expression rbrack)?
                             ;
procedureType                : procedureOfObject
                             | procedureReference
                             | simpleProcedureType
                             ;
procedureOfObject            : procedureTypeHeading OF OBJECT<ProcedureOfObjectTypeNodeImpl>^ ((';')? interfaceDirective)*
                             ;
procedureReference           : REFERENCE<ProcedureReferenceTypeNodeImpl>^ TO! procedureTypeHeading
                             ;
simpleProcedureType          : procedureTypeHeading -> ^(TkProcedureType<ProcedureTypeNodeImpl> procedureTypeHeading)
                             ;
procedureTypeHeading         : FUNCTION<ProcedureTypeHeadingNodeImpl>^ routineParameters? routineReturnType? ((';')? interfaceDirective)*
                             | PROCEDURE<ProcedureTypeHeadingNodeImpl>^ routineParameters? ((';')? interfaceDirective)*
                             ;
typeOfType                   : TYPE<TypeOfTypeNodeImpl>^ OF typeReference
                             ;
strongAliasType              : TYPE<StrongAliasTypeNodeImpl>^ typeReferenceOrStringOrFile codePageExpression?
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
typeReference                : nameReference -> ^(TkTypeReference<TypeReferenceNodeImpl> nameReference)
                             ;
typeReferenceOrString        : stringType
                             | typeReference
                             ;
typeReferenceOrStringOrFile  : stringType
                             | fileType
                             | typeReference
                             ;

//----------------------------------------------------------------------------
// Struct Types
//----------------------------------------------------------------------------
classReferenceType           : CLASS<ClassReferenceTypeNodeImpl>^ OF typeReference
                             ;
classType                    : CLASS classState? classParent? (visibilitySection* END)?
                             -> ^(CLASS<ClassTypeNodeImpl> classParent? classState? (visibilitySection* END)?)
                             ;
classState                   : SEALED
                             | ABSTRACT
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
fieldSectionKey              : VAR
                             | THREADVAR
                             ;
fieldSection                 : CLASS? fieldSectionKey fieldDecl* -> ^(TkFieldSection<FieldSectionNodeImpl> CLASS? fieldSectionKey fieldDecl*)
                             | fieldDecl+ -> ^(TkFieldSection<FieldSectionNodeImpl> fieldDecl+)
                             ;
fieldDecl                    : attributeList? nameDeclarationList ':' varType portabilityDirective* ';'?
                             -> ^(TkFieldDeclaration<FieldDeclarationNodeImpl> nameDeclarationList varType portabilityDirective* attributeList? ';'?)
                             ;
classHelperType              : CLASS<ClassHelperTypeNodeImpl>^ HELPER classParent? FOR typeReference visibilitySection* END
                             ;
interfaceType                : (INTERFACE<InterfaceTypeNodeImpl>^ | DISPINTERFACE<InterfaceTypeNodeImpl>^) classParent? ((interfaceItems | attributeList?)  END)?
                             ;
interfaceItems               : interfaceItem+ -> ^(TkVisibilitySection<VisibilitySectionNodeImpl> interfaceItem+)
                             ;
interfaceItem                : routineInterface
                             | property
                             ;
objectType                   : OBJECT<ObjectTypeNodeImpl>^ classParent? visibilitySection* END // Obselete, kept for backwards compatibility with Turbo Pascal
                             ;                                                                 // See: https://www.oreilly.com/library/view/delphi-in-a/1565926595/re192.html
recordType                   : RECORD<RecordTypeNodeImpl>^ visibilitySection* recordVariantSection? END (ALIGN expression)?
                             ;
recordVariantSection         : CASE<RecordVariantSectionNodeImpl>^ recordVariantTag OF recordVariant+
                             ;
recordVariantTag             : (nameDeclaration ':')? typeReference
                             -> ^(TkRecordVariantTag<RecordVariantTagNodeImpl> nameDeclaration? typeReference)
                             ;
recordVariant                : expressionList ':' '(' fieldDecl* recordVariantSection? ')' ';'?
                             -> ^(TkRecordVariantItem<RecordVariantItemNodeImpl> expressionList fieldDecl* recordVariantSection? ';'?)
                             ;
recordHelperType             : RECORD<RecordHelperTypeNodeImpl>^ HELPER FOR typeReferenceOrStringOrFile visibilitySection* END
                             ;
property                     : attributeList? CLASS? PROPERTY nameDeclaration propertyArray? (':' varType)? (propertyDirective)* ';'
                             -> ^(PROPERTY<PropertyNodeImpl> nameDeclaration propertyArray? varType? CLASS? attributeList? propertyDirective*)
                             ;
propertyArray                : lbrack! formalParameterList rbrack!
                             ;
propertyDirective            : ';' propertyDefaultNoExpression
                             | propertyDefault
                             | propertyReadWrite
                             | propertyDispInterface
                             | propertyImplements
                             | propertyIndex
                             | propertyStored
                             | NODEFAULT
                             ;
propertyDefaultNoExpression  : DEFAULT<PropertyDefaultSpecifierNodeImpl>^
                             ;
propertyDefault              : DEFAULT<PropertyDefaultSpecifierNodeImpl>^ expression
                             ;
propertyReadWrite            : (READ<PropertyReadSpecifierNodeImpl>^ | WRITE<PropertyWriteSpecifierNodeImpl>^) primaryExpression
                             ;
propertyImplements           : IMPLEMENTS<PropertyImplementsSpecifierNodeImpl>^ typeReference (',' typeReference)*
                             ;
propertyIndex                : INDEX<PropertyIndexSpecifierNodeImpl>^ expression
                             ;
propertyStored               : STORED<PropertyStoredSpecifierNodeImpl>^ expression
                             ;
propertyDispInterface        : READONLY
                             | WRITEONLY
                             | dispIDDirective
                             ;
visibility                   : STRICT? PROTECTED<VisibilityNodeImpl>^
                             | STRICT? PRIVATE<VisibilityNodeImpl>^
                             | PUBLIC<VisibilityNodeImpl>
                             | PUBLISHED<VisibilityNodeImpl>
                             | AUTOMATED<VisibilityNodeImpl> // Obselete directive used for RTTI.
                             ;                               // See: https://www.oreilly.com/library/view/delphi-in-a/1565926595/re24.html

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
genericConstraint            : typeReference -> ^(TkTypeConstraint<TypeConstraintNodeImpl> typeReference)
                             | RECORD<RecordConstraintNodeImpl>^
                             | CLASS<ClassConstraintNodeImpl>^
                             | CONSTRUCTOR<ConstructorConstraintNodeImpl>^
                             ;
genericArguments             : '<' typeReferenceOrStringOrFile (',' typeReferenceOrStringOrFile)* '>'
                             -> ^(TkGenericArguments<GenericArgumentsNodeImpl> '<' typeReferenceOrStringOrFile (',' typeReferenceOrStringOrFile)* '>')
                             ;
routineNameGenericArguments  : '<' typeReferenceOrStringOrFile (commaOrSemicolon typeReferenceOrStringOrFile)* '>'
                             -> ^(TkGenericArguments<GenericArgumentsNodeImpl> '<' typeReferenceOrStringOrFile (commaOrSemicolon typeReferenceOrStringOrFile)* '>')
                             ;
commaOrSemicolon             : ',' | ';'
                             ;

//----------------------------------------------------------------------------
// Routines
//----------------------------------------------------------------------------
methodResolutionClause       : key=(FUNCTION | PROCEDURE) interfaceMethod=nameReference '=' implemented=nameReference ';'
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
routineInterfaceHeading      : attributeList? CLASS? routineKey routineDeclarationName routineParameters? routineReturnType? interfaceDirectiveSection
                             -> ^(TkRoutineHeading<RoutineHeadingNodeImpl>
                                    routineKey
                                    routineDeclarationName
                                    routineParameters?
                                    routineReturnType?
                                    attributeList?
                                    CLASS?
                                    interfaceDirectiveSection
                                 )
                             ;
routineImplementationHeading : attributeList? CLASS? routineKey routineImplementationName routineParameters? routineReturnType? implDirectiveSection
                             -> ^(TkRoutineHeading<RoutineHeadingNodeImpl>
                                    routineKey
                                    routineImplementationName
                                    routineParameters?
                                    routineReturnType?
                                    attributeList?
                                    CLASS?
                                    implDirectiveSection
                                 )
                             ;
externalRoutineHeading       : attributeList? CLASS? routineKey routineImplementationName routineParameters? routineReturnType? externalDirectiveSection
                             -> ^(TkRoutineHeading<RoutineHeadingNodeImpl>
                                    routineKey
                                    routineImplementationName
                                    routineParameters?
                                    routineReturnType?
                                    attributeList?
                                    CLASS?
                                    externalDirectiveSection
                                 )
                             ;
forwardRoutineHeading        : attributeList? CLASS? routineKey routineDeclarationName routineParameters? routineReturnType? forwardDirectiveSection
                             -> ^(TkRoutineHeading<RoutineHeadingNodeImpl>
                                    routineKey
                                    routineDeclarationName
                                    routineParameters?
                                    routineReturnType?
                                    attributeList?
                                    CLASS?
                                    forwardDirectiveSection
                                 )
                             ;
routineDeclarationName       : (
                                 decl=genericNameDeclaration
                               | decl=specialOpNameDeclaration
                             )
                             -> ^(TkRoutineName<RoutineNameNodeImpl> $decl)
                             ;
routineImplementationName    : routineNameReference -> ^(TkRoutineName<RoutineNameNodeImpl> routineNameReference)
                             ;
routineKey                   : PROCEDURE
                             | CONSTRUCTOR
                             | DESTRUCTOR
                             | FUNCTION
                             | OPERATOR
                             ;
routineReturnType            : ':' attributeList? returnType -> ^(TkRoutineReturn<RoutineReturnTypeNodeImpl> returnType attributeList?)
                             ;
returnType                   : typeReferenceOrString
                             ;
routineParameters            : '(' formalParameterList? ')' -> ^(TkRoutineParameters<RoutineParametersNodeImpl> '(' formalParameterList? ')')
                             ;
formalParameterList          : formalParameter (';' formalParameter)* -> ^(TkFormalParameterList<FormalParameterListNodeImpl> formalParameter formalParameter*)
                             ;
formalParameter              : a1=attributeList? (paramSpecifier a2=attributeList?)? nameDeclarationList (':' parameterType)? ('=' expression)?
                             -> ^(TkFormalParameter<FormalParameterNodeImpl> nameDeclarationList parameterType? paramSpecifier? expression? $a1? $a2?)
                             ;
paramSpecifier               : CONST
                             | VAR
                             | OUT
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
attribute                    : (ASSEMBLY ':')? expression (':' expression)*
                             -> ^(TkAttribute<AttributeNodeImpl> ASSEMBLY? expression (':' expression)*)
                             ;

//----------------------------------------------------------------------------
// Expressions
//----------------------------------------------------------------------------
expression                   : relationalExpression
                             | anonymousMethod
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
                             | INHERITED (nameReference? particleItem*)? -> ^(TkPrimaryExpression<PrimaryExpressionNodeImpl> INHERITED (nameReference? particleItem*)?)
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
                             | STRING
                             | FILE
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
argumentList                 : '('<ArgumentListNodeImpl>^ (argument (',' argument)* ','?)? ')'
                             ;
argument                     : argumentName? argumentExpression
                             -> ^(TkArgument<ArgumentNodeImpl> argumentName? argumentExpression)
                             ;
argumentName                 : ident ':='!
                             | keywords ':=' -> ^({changeTokenType(TkIdentifier, -2)})
                             ;
argumentExpression           : expression writeArguments?
                             ;
writeArguments               : ':'! expression (':'! expression)? // See: https://docwiki.embarcadero.com/Libraries/en/System.Write
                             ;
anonymousMethod              : anonymousMethodHeading block -> ^(TkAnonymousMethod<AnonymousMethodNodeImpl> anonymousMethodHeading block)
                             ;
anonymousMethodHeading       : PROCEDURE routineParameters? ((';')? interfaceDirective)*
                             -> ^(TkAnonymousMethodHeading<AnonymousMethodHeadingNodeImpl> PROCEDURE routineParameters? ((';')? interfaceDirective)*)
                             | FUNCTION routineParameters? routineReturnType ((';')? interfaceDirective)*
                             -> ^(TkAnonymousMethodHeading<AnonymousMethodHeadingNodeImpl> FUNCTION routineParameters? routineReturnType ((';')? interfaceDirective)*)
                             ;
expressionOrRange            : expression ('..'<RangeExpressionNodeImpl>^ expression)?
                             ;
expressionList               : (expression (','!)?)+
                             ;
expressionOrRangeList        : (expressionOrRange (','!)?)+
                             ;
textLiteral                  : singleLineTextLiteral -> ^(TkTextLiteral<TextLiteralNodeImpl> singleLineTextLiteral)
                             | multilineTextLiteral -> ^(TkTextLiteral<TextLiteralNodeImpl> multilineTextLiteral)
                             ;
singleLineTextLiteral        : TkQuotedString (escapedCharacter+ TkQuotedString)* escapedCharacter*
                             | escapedCharacter+ (TkQuotedString escapedCharacter+)* TkQuotedString?
                             ;
multilineTextLiteral         : TkMultilineString
                             ;
escapedCharacter             : TkCharacterEscapeCode
                             | '^' (TkIdentifier | TkIntNumber | TkAnyChar) -> ^({changeTokenType(TkEscapedCharacter)})
                             ;
nilLiteral                   : NIL<NilLiteralNodeImpl>
                             ;
arrayConstructor             : lbrack expressionOrRangeList? rbrack
                             -> ^(TkArrayConstructor<ArrayConstructorNodeImpl> lbrack expressionOrRangeList? rbrack)
                             ;
addOperator                  : '+'<BinaryExpressionNodeImpl>
                             | '-'<BinaryExpressionNodeImpl>
                             | OR<BinaryExpressionNodeImpl>
                             | XOR<BinaryExpressionNodeImpl>
                             ;
multOperator                 : '*'<BinaryExpressionNodeImpl>
                             | '/'<BinaryExpressionNodeImpl>
                             | DIV<BinaryExpressionNodeImpl>
                             | MOD<BinaryExpressionNodeImpl>
                             | AND<BinaryExpressionNodeImpl>
                             | SHL<BinaryExpressionNodeImpl>
                             | SHR<BinaryExpressionNodeImpl>
                             | AS<BinaryExpressionNodeImpl>
                             ;
unaryOperator                : NOT<UnaryExpressionNodeImpl>
                             | '+'<UnaryExpressionNodeImpl>
                             | '-'<UnaryExpressionNodeImpl>
                             | '@'<UnaryExpressionNodeImpl>
                             ;
relationalOperator           : '='<BinaryExpressionNodeImpl>
                             | '>'<BinaryExpressionNodeImpl>
                             | '<'<BinaryExpressionNodeImpl>
                             | op=lessThanEqualOperator -> {createBinaryExpression(op.getTree())}
                             | op=greaterThanEqualOperator -> {createBinaryExpression(op.getTree())}
                             | '<>'<BinaryExpressionNodeImpl>
                             | IN<BinaryExpressionNodeImpl>
                             | IS<BinaryExpressionNodeImpl>
                             ;
// We're only doing this for symmetry with greaterThanEqualOperator. (see comment below)
lessThanEqualOperator        : '<' '=' -> ^({combineLastNTokens(TkLessThanEqual, 2)})
                             ;
// We construct the "greater than equal" tokens while parsing binary expressions to preserve the
// individual '>' and '=' tokens in other cases like `const Foo: TArray<Byte>=[1, 2, 3];`, which
// we otherwise couldn't parse since the `>=` token would consume the closing angle bracket of the
// generic type arguments and the const assignment operator.
greaterThanEqualOperator     : '>' '=' -> ^({combineLastNTokens(TkGreaterThanEqual, 2)})
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
ifStatement                  : IF<IfStatementNodeImpl>^ expression THEN statement? (ELSE statement?)?
                             ;
varStatement                 : VAR attributeList? nameDeclarationList (':' varType)? (':=' expression)?
                             -> ^(VAR<VarStatementNodeImpl> nameDeclarationList (':' varType)? (':=' expression)? attributeList?)
                             ;
constStatement               : CONST attributeList? nameDeclaration (':' varType)? '=' expression
                             -> ^(CONST<ConstStatementNodeImpl> nameDeclaration (':' varType)? '=' expression attributeList?)
                             ;
caseStatement                : CASE<CaseStatementNodeImpl>^ expression OF caseItem* elseBlock? END
                             ;
elseBlock                    : ELSE<ElseBlockNodeImpl>^ statementList
                             ;
caseItem                     : expressionOrRangeList ':' (statement)? (';')? -> ^(TkCaseItem<CaseItemStatementNodeImpl> expressionOrRangeList (statement)? (';')? )
                             ;
repeatStatement              : REPEAT<RepeatStatementNodeImpl>^ statementList UNTIL expression
                             ;
whileStatement               : WHILE<WhileStatementNodeImpl>^ expression DO statement?
                             ;
forStatement                 : FOR<ForToStatementNodeImpl>^ forVar ':=' expression TO expression DO statement?
                             | FOR<ForToStatementNodeImpl>^ forVar ':=' expression DOWNTO expression DO statement?
                             | FOR<ForInStatementNodeImpl>^ forVar IN expression DO statement?
                             ;
forVar                       : VAR nameDeclaration (':' varType)? -> ^(TkForLoopVar<ForLoopVarDeclarationNodeImpl> nameDeclaration varType?)
                             | simpleNameReference -> ^(TkForLoopVar<ForLoopVarReferenceNodeImpl> simpleNameReference)
                             ;
withStatement                : WITH<WithStatementNodeImpl>^ expressionList DO statement?
                             ;
compoundStatement            : BEGIN<CompoundStatementNodeImpl>^ statementList END
                             ;
statementList                : delimitedStatements? -> ^(TkStatementList<StatementListNodeImpl> delimitedStatements?)
                             ;
delimitedStatements          : (statement | ';')+
                             ;
labelStatement               : {input.LA(2) == COLON}? => labelNameReference ':' statement?
                             -> ^(TkLabelStatement<LabelStatementNodeImpl> labelNameReference ':' statement?)
                             ;
assignmentStatement          : expression ':='<AssignmentStatementNodeImpl>^ expression
                             ;
expressionStatement          : expression -> ^(TkExpressionStatement<ExpressionStatementNodeImpl> expression)
                             ;
gotoStatement                : GOTO<GotoStatementNodeImpl>^ labelNameReference
                             ;
tryStatement                 : TRY<TryStatementNodeImpl>^ statementList (exceptBlock | finallyBlock) END
                             ;
exceptBlock                  : EXCEPT<ExceptBlockNodeImpl>^ handlerList
                             ;
finallyBlock                 : FINALLY<FinallyBlockNodeImpl>^ statementList
                             ;
handlerList                  : handler+ elseBlock?
                             | statementList
                             ;
handler                      : ON<ExceptItemNodeImpl>^ (nameDeclaration ':'!)? typeReference DO statement? (';')?
                             ;
raiseStatement               : RAISE<RaiseStatementNodeImpl>^ expression? (AT! expression)?
                             ;
assemblerStatement           : ASM<AsmStatementNodeImpl>^ assemblerInstructions END
                             ;
assemblerInstructions        : ~(END)* // Skip asm statements
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
forwardDirectiveSection      : (';'? implDirective)* ';'? FORWARD (';'? implDirective)*';'
                             | (';' implDirective)* ';' FORWARD (';' implDirective)*
                             ;
implDirective                : OVERLOAD
                             | REINTRODUCE
                             | bindingDirective
                             | abstractDirective
                             | inlineDirective
                             | callConvention
                             | portabilityDirective
                             | oldCallConventionDirective
                             | dispIDDirective
                             | VARARGS // Only permitted for cdecl calling convention
                             | UNSAFE // .net?
                             ;
interfaceDirective           : FORWARD
                             | externalDirective
                             | implDirective
                             ;
bindingDirective             : MESSAGE expression
                             | STATIC
                             | DYNAMIC
                             | OVERRIDE
                             | VIRTUAL
                             ;
abstractDirective            : ABSTRACT
                             | FINAL
                             ;
inlineDirective              : INLINE
                             | ASSEMBLER // deprecated
                             ;
callConvention               : CDECL
                             | PASCAL
                             | REGISTER
                             | SAFECALL
                             | STDCALL
                             | WINAPI
                             | EXPORT // deprecated
                             ;
oldCallConventionDirective   : FAR // deprecated
                             | LOCAL // deprecated. Introduced in the Kylix Linux compiler, makes function non-exportable. (No effect in Windows)
                             | NEAR // deprecated
                             ;
portabilityDirective         : DEPRECATED^ textLiteral?
                             | EXPERIMENTAL
                             | PLATFORM
                             | LIBRARY
                             ;
externalDirective            : EXTERNAL^ dllName? externalSpecifier*
                             ;
dllName                      : {!input.LT(1).getText().equals("name")}? expression
                             ;
externalSpecifier            : NAME^ expression
                             | INDEX^ expression // specific to a platform
                             | DELAYED // Use delayed loading (See: http://docwiki.embarcadero.com/RADStudio/en/Libraries_and_Packages_(Delphi))
                             ;
dispIDDirective              : DISPID expression
                             ;

//----------------------------------------------------------------------------
// General
//----------------------------------------------------------------------------
ident                        : TkIdentifier
                             | keywordsUsedAsNames -> ^({changeTokenType(TkIdentifier)})
                             ;
keywordsUsedAsNames          : (ABSOLUTE | ABSTRACT | ALIGN | ASSEMBLER | ASSEMBLY | AT | AUTOMATED | CDECL)
                             | (CONTAINS | DEFAULT | DELAYED | DEPRECATED | DISPID | DYNAMIC | EXPERIMENTAL | EXPORT)
                             | (EXTERNAL | FAR | FINAL | FORWARD | HELPER | IMPLEMENTS | INDEX | LOCAL | MESSAGE | NAME)
                             | (NEAR | NODEFAULT | ON | OPERATOR | OUT | OVERLOAD | OVERRIDE | PACKAGE | PASCAL | PLATFORM)
                             | (PRIVATE | PROTECTED | PUBLIC | PUBLISHED | READ | READONLY | REFERENCE | REGISTER | REINTRODUCE)
                             | (REQUIRES | RESIDENT | SAFECALL | SEALED | STATIC | STDCALL | STORED | STRICT | UNSAFE)
                             | (VARARGS | VIRTUAL | WINAPI | WRITE | WRITEONLY)
                             | (LABEL) // Used to be allowed in Delphi.NET.
                             ;
keywords                     : (ABSOLUTE | ABSTRACT | AND | ALIGN | ARRAY | AS | ASM | ASSEMBLER | ASSEMBLY)
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
                             | (VIRTUAL | WHILE | WINAPI | WITH | WRITE | WRITEONLY | XOR)
                             ;
nameDeclarationList          : nameDeclaration (',' nameDeclaration)* -> ^(TkNameDeclarationList<NameDeclarationListNodeImpl> nameDeclaration (',' nameDeclaration)*)
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
specialOperatorName          : IN -> ^({changeTokenType(TkIdentifier)})
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
routineNameReference         : ident routineNameGenericArguments? ('.' extendedRoutineNameReference)?
                             -> ^(TkNameReference<NameReferenceNodeImpl> ident routineNameGenericArguments? ('.' extendedRoutineNameReference)?)
                             ;
extendedRoutineNameReference : extendedIdent routineNameGenericArguments? ('.' extendedRoutineNameReference)?
                             -> ^(TkNameReference<NameReferenceNodeImpl> extendedIdent routineNameGenericArguments? ('.' extendedRoutineNameReference)?)
                             ;
extendedIdent                : ident
                             | keywords -> ^({changeTokenType(TkIdentifier)})
                             ;
labelIdent                   : ident
                             | TkIntNumber -> ^({changeTokenType(TkIdentifier)})
                             | TkHexNumber -> ^({changeTokenType(TkIdentifier)})
                             | TkBinaryNumber -> ^({changeTokenType(TkIdentifier)})
                             ;
labelNameDeclaration         : labelIdent -> ^(TkNameDeclaration<SimpleNameDeclarationNodeImpl> labelIdent)
                             ;
labelNameReference           : labelIdent -> ^(TkNameReference<NameReferenceNodeImpl> labelIdent)
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
ABSOLUTE          : A B S O L U T E             ;
ABSTRACT          : A B S T R A C T             ;
ALIGN             : A L I G N                   ;
AND               : A N D                       ;
ARRAY             : A R R A Y                   ;
AS                : A S                         ;
ASM               : A S M { asmMode = true; }   ;
ASSEMBLER         : A S S E M B L E R           ;
ASSEMBLY          : A S S E M B L Y             ;
AT                : A T                         ;
AUTOMATED         : A U T O M A T E D           ;
BEGIN             : B E G I N {
                    // HACK:
                    //  We exit asmMode in the lexer if we encounter a BEGIN token, which is a hack
                    //  to support partial asm blocks sharing an END keyword with some other block
                    //  due to conditional compilation.
                    //
                    //  For more information, see:
                    //    https://github.com/integrated-application-development/sonar-delphi/issues/116
                    asmMode = false;
                  };
CASE              : C A S E                     ;
CDECL             : C D E C L                   ;
CLASS             : C L A S S                   ;
CONST             : C O N S T                   ;
CONSTRUCTOR       : C O N S T R U C T O R       ;
CONTAINS          : C O N T A I N S             ;
DEFAULT           : D E F A U L T               ;
DELAYED           : D E L A Y E D               ;
DEPRECATED        : D E P R E C A T E D         ;
DESTRUCTOR        : D E S T R U C T O R         ;
DISPID            : D I S P I D                 ;
DISPINTERFACE     : D I S P I N T E R F A C E   ;
DIV               : D I V                       ;
DO                : D O                         ;
DOWNTO            : D O W N T O                 ;
DYNAMIC           : D Y N A M I C               ;
ELSE              : E L S E                     ;
END               : E N D { asmMode = false; }  ;
EXCEPT            : E X C E P T                 ;
EXPERIMENTAL      : E X P E R I M E N T A L     ;
EXPORT            : E X P O R T                 ;
EXPORTS           : E X P O R T S               ;
EXTERNAL          : E X T E R N A L             ;
FAR               : F A R                       ;
FILE              : F I L E                     ;
FINAL             : F I N A L                   ;
FINALIZATION      : F I N A L I Z A T I O N     ;
FINALLY           : F I N A L L Y               ;
FOR               : F O R                       ;
FORWARD           : F O R W A R D               ;
FUNCTION          : F U N C T I O N             ;
GOTO              : G O T O                     ;
HELPER            : H E L P E R                 ;
IF                : I F                         ;
IMPLEMENTATION    : I M P L E M E N T A T I O N {
                     if (shouldSkipImplementation && directiveNesting == 0) {
                       skip();
                       while (input.LA(1) != EOF) {
                         input.consume();
                       }
                     }
                  };
IMPLEMENTS        : I M P L E M E N T S         ;
IN                : I N                         ;
INDEX             : I N D E X                   ;
INHERITED         : I N H E R I T E D           ;
INITIALIZATION    : I N I T I A L I Z A T I O N ;
INLINE            : I N L I N E                 ;
INTERFACE         : I N T E R F A C E           ;
IS                : I S                         ;
LABEL             : L A B E L                   ;
LIBRARY           : L I B R A R Y               ;
LOCAL             : L O C A L                   ;
MESSAGE           : M E S S A G E               ;
MOD               : M O D                       ;
NAME              : N A M E                     ;
NEAR              : N E A R                     ;
NIL               : N I L                       ;
NODEFAULT         : N O D E F A U L T           ;
NOT               : N O T                       ;
OBJECT            : O B J E C T                 ;
OF                : O F                         ;
ON                : O N                         ;
OPERATOR          : O P E R A T O R             ;
OR                : O R                         ;
OUT               : O U T                       ;
OVERLOAD          : O V E R L O A D             ;
OVERRIDE          : O V E R R I D E             ;
PACKAGE           : P A C K A G E               ;
PACKED            : P A C K E D                 ;
PASCAL            : P A S C A L                 ;
PLATFORM          : P L A T F O R M             ;
PRIVATE           : P R I V A T E               ;
PROCEDURE         : P R O C E D U R E           ;
PROGRAM           : P R O G R A M               ;
PROPERTY          : P R O P E R T Y             ;
PROTECTED         : P R O T E C T E D           ;
PUBLIC            : P U B L I C                 ;
PUBLISHED         : P U B L I S H E D           ;
RAISE             : R A I S E                   ;
READ              : R E A D                     ;
READONLY          : R E A D O N L Y             ;
RECORD            : R E C O R D                 ;
REFERENCE         : R E F E R E N C E           ;
REGISTER          : R E G I S T E R             ;
REINTRODUCE       : R E I N T R O D U C E       ;
REPEAT            : R E P E A T                 ;
REQUIRES          : R E Q U I R E S             ;
RESIDENT          : R E S I D E N T             ;
RESOURCESTRING    : R E S O U R C E S T R I N G ;
SAFECALL          : S A F E C A L L             ;
SEALED            : S E A L E D                 ;
SET               : S E T                       ;
SHL               : S H L                       ;
SHR               : S H R                       ;
STATIC            : S T A T I C                 ;
STDCALL           : S T D C A L L               ;
STORED            : S T O R E D                 ;
STRICT            : S T R I C T                 ;
STRING            : S T R I N G                 ;
THEN              : T H E N                     ;
THREADVAR         : T H R E A D V A R           ;
TO                : T O                         ;
TRY               : T R Y                       ;
TYPE              : T Y P E                     ;
UNIT              : U N I T                     ;
UNSAFE            : U N S A F E                 ;
UNTIL             : U N T I L                   ;
USES              : U S E S                     ;
VAR               : V A R                       ;
VARARGS           : V A R A R G S               ;
VIRTUAL           : V I R T U A L               ;
WHILE             : W H I L E                   ;
WINAPI            : W I N A P I                 ;
WITH              : W I T H                     ;
WRITE             : W R I T E                   ;
WRITEONLY         : W R I T E O N L Y           ;
XOR               : X O R                       ;

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
TkQuotedString          @init { int multilineStringRemaining = lookaheadMultilineString(1); }
                        : '\''
                          ({ multilineStringRemaining != 0 }? => {
                            int i = multilineStringRemaining;
                            while (--i > 0) {
                              matchAny();
                            }
                            $type = TkMultilineString;
                          })?
                          ({ multilineStringRemaining == 0 }? => ('\'\'' | ~('\''))* '\'')?
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
// Case-insensitivity fragments
//----------------------------------------------------------------------------
fragment A              : 'a' | 'A';
fragment B              : 'b' | 'B';
fragment C              : 'c' | 'C';
fragment D              : 'd' | 'D';
fragment E              : 'e' | 'E';
fragment F              : 'f' | 'F';
fragment G              : 'g' | 'G';
fragment H              : 'h' | 'H';
fragment I              : 'i' | 'I';
fragment J              : 'j' | 'J';
fragment K              : 'k' | 'K';
fragment L              : 'l' | 'L';
fragment M              : 'm' | 'M';
fragment N              : 'n' | 'N';
fragment O              : 'o' | 'O';
fragment P              : 'p' | 'P';
fragment Q              : 'q' | 'Q';
fragment R              : 'r' | 'R';
fragment S              : 's' | 'S';
fragment T              : 't' | 'T';
fragment U              : 'u' | 'U';
fragment V              : 'v' | 'V';
fragment W              : 'w' | 'W';
fragment X              : 'x' | 'X';
fragment Y              : 'y' | 'Y';
fragment Z              : 'z' | 'Z';

//----------------------------------------------------------------------------
// Hidden channel
//----------------------------------------------------------------------------
COMMENT                 :  '//' ~('\n'|'\r')*                          {$channel=HIDDEN;}
                        |  ('(*' | '{')
                           {
                              $channel=HIDDEN;

                              String start = $text;
                              String end = start.equals("{") ? "}" : "*)";

                              int multilineCommentRemaining = lookaheadMultilineComment(end);

                              while (--multilineCommentRemaining > 0) {
                                matchAny();
                              }

                              if ($text.startsWith(start + "\$")) {
                                $type = TkCompilerDirective;
                                if (Strings.CI.startsWith($text, start + "\$endif") || Strings.CI.startsWith($text, start + "\$ifend")) {
                                  --directiveNesting;
                                } else if (Strings.CI.startsWith($text, start + "\$if")) {
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
