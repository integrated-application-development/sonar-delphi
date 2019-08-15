package org.sonar.plugins.delphi.pmd.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.ast.DelphiNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

public class EmptyMethodRule extends DelphiRule {

  private final List<DelphiNode> typeNodes = new ArrayList<>();

  @Override
  public void start(RuleContext ctx) {
    typeNodes.clear();
  }

  @Override
  public void visit(DelphiNode node, RuleContext ctx) {
    handleMethods(node, ctx);
    handleTypes(node);
  }

  private void handleMethods(DelphiNode method, RuleContext ctx) {
    if (isEmptyMethod(method) && shouldAddViolation(method)) {
      addViolation(ctx, getNameNode(method));
    }
  }

  private void handleTypes(DelphiNode node) {
    if (isNewTypeDeclaration(node)) {
      typeNodes.add(node);
    }
  }

  private boolean isNewTypeDeclaration(DelphiNode node) {
    if (node.getType() != DelphiLexer.TkNewType) {
      return false;
    }

    Tree typeDecl = node.getFirstChildWithType(DelphiLexer.TkNewTypeDecl).getChild(0);
    // 2 children or less means that it's a forward declaration.
    // This holds true for both class and interface declarations,
    // which are what we're interested in.
    return typeDecl.getChildCount() > 2;
  }

  private boolean shouldAddViolation(DelphiNode method) {
    DelphiNode methodBody = method.nextNode().nextNode();
    List<Token> comments = methodBody.getComments();
    if (comments.isEmpty()) {
      // All exclusions aside, an explanatory comment is mandatory
      return true;
    }

    DelphiNode methodDecl = findMethodDecl(method);
    if (methodDecl == null) {
      return true;
    }

    return !hasExcludedDirective(methodDecl);
  }

  private boolean hasExcludedDirective(DelphiNode methodDecl) {
    for (int i = 0; i < methodDecl.getChildCount(); ++i) {
      int type = methodDecl.getChild(i).getType();

      if (type == DelphiLexer.OVERRIDE || type == DelphiLexer.VIRTUAL) {
        return true;
      }
    }

    return false;
  }

  private DelphiNode findMethodDecl(DelphiNode method) {
    List<String> methodSignature = readQualifiedMethodSignature(method);

    if (methodSignature.size() < 2) {
      return null;
    }

    DelphiNode type = null;
    List<DelphiNode> typesToSearch = typeNodes;

    for (int i = 0; i < methodSignature.size() - 1; ++i) {
      type = findTypeWithName(methodSignature.get(i), typesToSearch);

      if (type == null) {
        return null;
      }

      typesToSearch = findNestedTypes(type);
    }

    String methodName = methodSignature.get(methodSignature.size() - 1);
    Objects.requireNonNull(type);

    return findMethodDeclInType(methodName, type);
  }

  private DelphiNode findTypeWithName(String searchName, List<DelphiNode> typesToSearch) {
    for (DelphiNode type : typesToSearch) {
      StringBuilder typeName = new StringBuilder();
      Tree nameNode = type.getFirstChildWithType(DelphiLexer.TkNewTypeName);
      for (int i = 0; i < nameNode.getChildCount(); ++i) {
        typeName.append(nameNode.getChild(i).getText());
      }

      if (typeName.toString().equalsIgnoreCase(searchName)) {
        return type;
      }
    }

    return null;
  }

  private List<DelphiNode> findNestedTypes(DelphiNode type) {
    Tree typeDecl = type.getFirstChildWithType(DelphiLexer.TkNewTypeDecl).getChild(0);
    List<Tree> typeSections = new ArrayList<>();

    for (int i = 0; i < typeDecl.getChildCount(); ++i) {
      Tree child = typeDecl.getChild(i);
      if (child.getType() == DelphiLexer.TYPE) {
        typeSections.add(child);
      }
    }

    List<DelphiNode> nestedTypes = new ArrayList<>();
    for (Tree section : typeSections) {
      for (int i = 0; i < section.getChildCount(); ++i) {
        nestedTypes.add((DelphiNode) section.getChild(i));
      }
    }

    return nestedTypes;
  }

  private DelphiNode findMethodDeclInType(String searchName, DelphiNode type) {
    Tree typeDecl = type.getFirstChildWithType(DelphiLexer.TkNewTypeDecl).getChild(0);
    for (int i = 0; i < typeDecl.getChildCount(); ++i) {
      DelphiNode child = (DelphiNode) typeDecl.getChild(i);
      if (isMethodNodeWithName(child, searchName)) {
        return child;
      }
    }
    return null;
  }

  private boolean isMethodNodeWithName(DelphiNode method, String searchName) {
    if (isMethodNode(method)) {
      StringBuilder name = new StringBuilder();
      Tree nameNode = method.getFirstChildWithType(DelphiLexer.TkFunctionName);
      for (int i = 0; i < nameNode.getChildCount(); ++i) {
        name.append(nameNode.getChild(i).getText());
      }

      if (!searchName.regionMatches(true, 0, name.toString(), 0, name.length())) {
        return false;
      }

      name.append(readMethodArgumentSignature(method));

      return searchName.equalsIgnoreCase(name.toString());
    }

    return false;
  }

  private List<String> readQualifiedMethodSignature(DelphiNode method) {
    List<String> signature = new ArrayList<>();
    Tree nameNode = method.getFirstChildWithType(DelphiLexer.TkFunctionName);

    if (nameNode == null) {
      return signature;
    }

    StringBuilder signaturePart = new StringBuilder();
    int genericNesting = 0;

    for (int i = 0; i < nameNode.getChildCount(); ++i) {
      Tree child = nameNode.getChild(i);
      int type = child.getType();

      if (type == DelphiLexer.LT) {
        ++genericNesting;
      }

      if (type == DelphiLexer.GT) {
        --genericNesting;
      }

      if (type == DelphiLexer.DOT && genericNesting == 0) {
        signature.add(signaturePart.toString());
        signaturePart.setLength(0);
        continue;
      }

      signaturePart.append(child.getText());
    }

    signaturePart.append(readMethodArgumentSignature(method));
    signature.add(signaturePart.toString());

    return signature;
  }

  private String readMethodArgumentSignature(DelphiNode method) {
    StringBuilder signature = new StringBuilder();
    StringBuilder typeBuilder = new StringBuilder();
    Tree argsNode = method.getFirstChildWithType(DelphiLexer.TkFunctionArgs);

    for (int i = 0; i < argsNode.getChildCount(); ++i) {
      DelphiNode argNames = (DelphiNode) argsNode.getChild(i);
      if (argNames.getType() != DelphiLexer.TkVariableIdents) {
        continue;
      }

      DelphiNode argType = argNames.nextNode();

      for (int ii = 0; ii < argType.getChildCount(); ++ii) {
        typeBuilder.append(argType.getChild(ii).getText());
      }

      String typeName = typeBuilder.toString();

      for (int ii = 0; ii < argNames.getChildCount(); ++ii) {
        signature.append(";");
        signature.append(typeName);
      }

      typeBuilder.setLength(0);
    }

    return signature.toString();
  }

  private DelphiNode getNameNode(DelphiNode method) {
    return (DelphiNode) method.getFirstChildWithType(DelphiLexer.TkFunctionName);
  }

  private boolean isEmptyMethod(DelphiNode method) {
    if (!isMethodNode(method)) {
      return false;
    }

    DelphiNode blockDeclSection = method.nextNode();

    if (blockDeclSection == null || blockDeclSection.getType() != DelphiLexer.TkBlockDeclSection) {
      return false;
    }

    return blockDeclSection.nextNode().getChildCount() == 1;
  }

  private boolean isMethodNode(Tree method) {
    int type = method.getType();
    return type == DelphiLexer.CONSTRUCTOR
        || type == DelphiLexer.DESTRUCTOR
        || type == DelphiLexer.FUNCTION
        || type == DelphiLexer.PROCEDURE;
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
