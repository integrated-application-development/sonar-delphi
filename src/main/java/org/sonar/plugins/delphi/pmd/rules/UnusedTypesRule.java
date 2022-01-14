package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import net.sourceforge.pmd.lang.symboltable.Scope;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.symbol.scope.MethodScope;
import org.sonar.plugins.delphi.symbol.scope.TypeScope;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.HelperType;

public class UnusedTypesRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(TypeDeclarationNode type, RuleContext data) {
    NameDeclarationNode name = type.getTypeNameNode();
    if (canBeUnused(type.getType())
        && name.getUsages().stream()
            .allMatch(occurrence -> isWithinType(occurrence, type.getType()))) {
      addViolation(data, name);
    }
    return super.visit(type, data);
  }

  private static boolean canBeUnused(Type type) {
    return !(type instanceof HelperType);
  }

  private static boolean isWithinType(NameOccurrence occurrence, Type type) {
    Scope scope = occurrence.getLocation().getScope();
    while (scope != null) {
      Scope typeScope = DelphiScope.unknownScope();
      if (scope instanceof MethodScope) {
        typeScope = ((MethodScope) scope).getTypeScope();
      } else if (scope instanceof TypeScope) {
        typeScope = scope;
      }

      Type foundType = DelphiType.unknownType();
      if (typeScope instanceof TypeScope) {
        foundType = ((TypeScope) typeScope).getType();
      }

      if (type.is(foundType)) {
        return true;
      }

      scope = scope.getParent();
    }
    return false;
  }
}
