package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.MethodUtils.isMethodStubWithStackUnwinding;

import java.util.HashSet;
import java.util.Set;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import net.sourceforge.pmd.lang.symboltable.Scope;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.node.MethodDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodNode;
import org.sonar.plugins.delphi.symbol.declaration.MethodDirective;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.scope.MethodScope;
import org.sonar.plugins.delphi.symbol.scope.UnitScope;

public class UnusedMethodsRule extends AbstractDelphiRule {
  private final Set<MethodNameDeclaration> seenMethods = new HashSet<>();

  @Override
  public RuleContext visit(DelphiAST ast, RuleContext data) {
    seenMethods.clear();
    return super.visit(ast, data);
  }

  @Override
  public RuleContext visit(MethodNode method, RuleContext data) {
    MethodNameDeclaration methodDeclaration = method.getMethodNameDeclaration();

    if (!seenMethods.contains(methodDeclaration) && isViolation(method)) {
      addViolation(data, method.getMethodNameNode());
    }

    seenMethods.add(methodDeclaration);

    return super.visit(method, data);
  }

  private static boolean isViolation(MethodNode method) {
    if (method.isPublished()) {
      return false;
    }

    MethodNameDeclaration methodDeclaration = method.getMethodNameDeclaration();
    if (methodDeclaration == null) {
      return false;
    }

    if (methodDeclaration.hasDirective(MethodDirective.OVERRIDE)) {
      return false;
    }

    if (methodDeclaration.hasDirective(MethodDirective.MESSAGE)) {
      return false;
    }

    if (!methodDeclaration.isCallable()) {
      return false;
    }

    if (methodDeclaration.getName().equalsIgnoreCase("Register")
        && methodDeclaration.getScope() instanceof UnitScope) {
      return false;
    }

    if (methodDeclaration.implementsInterface()) {
      return false;
    }

    if (isForbiddenConstructor(method)) {
      return false;
    }

    return method.getMethodNameNode().getUsages().stream()
        .allMatch(occurrence -> isWithinMethod(occurrence, methodDeclaration));
  }

  private static boolean isForbiddenConstructor(MethodNode method) {
    if (method instanceof MethodDeclarationNode && method.isConstructor()) {
      DelphiAST ast = method.getASTTree();
      return ast.findDescendantsOfType(MethodImplementationNode.class).stream()
          .anyMatch(
              implementation -> {
                MethodNameDeclaration declaration = implementation.getMethodNameDeclaration();
                return declaration != null
                    && declaration.equals(method.getMethodNameDeclaration())
                    && isMethodStubWithStackUnwinding(implementation);
              });
    }
    return false;
  }

  private static boolean isWithinMethod(NameOccurrence occurrence, MethodNameDeclaration method) {
    Scope scope = occurrence.getLocation().getScope();
    while (scope != null) {
      if (scope instanceof MethodScope) {
        MethodScope methodScope = (MethodScope) scope;
        if (method.equals(methodScope.getMethodNameDeclaration())) {
          return true;
        }
      }
      scope = scope.getParent();
    }
    return false;
  }
}
