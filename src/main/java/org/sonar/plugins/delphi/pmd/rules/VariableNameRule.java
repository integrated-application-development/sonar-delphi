package org.sonar.plugins.delphi.pmd.rules;

import java.util.List;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarSectionNode;
import org.sonar.plugins.delphi.symbol.scope.UnitScope;
import org.sonar.plugins.delphi.utils.NameConventionUtils;

public class VariableNameRule extends AbstractDelphiRule {
  private static final PropertyDescriptor<List<String>> GLOBAL_PREFIXES =
      PropertyFactory.stringListProperty("global_prefixes")
          .desc("If defined, global variables must begin with one of these prefixes.")
          .emptyDefaultValue()
          .build();

  public VariableNameRule() {
    definePropertyDescriptor(GLOBAL_PREFIXES);
  }

  @Override
  public RuleContext visit(VarDeclarationNode varDecl, RuleContext data) {
    if (isAutoCreateFormVar(varDecl)) {
      return data;
    }

    boolean globalVariable = varDecl.getScope() instanceof UnitScope;

    for (NameDeclarationNode declaration : varDecl.getNameDeclarationList().getDeclarations()) {
      if (isViolation(declaration, globalVariable)) {
        addViolation(data, declaration);
      }
    }

    return data;
  }

  @Override
  public RuleContext visit(FormalParameterNode parameter, RuleContext data) {
    for (FormalParameterData param : parameter.getParameters()) {
      NameDeclarationNode node = param.getNode();
      if (isViolation(node, false)) {
        addViolation(data, node);
      }
    }
    return data;
  }

  private static boolean isAutoCreateFormVar(VarDeclarationNode varDecl) {
    VarSectionNode varSection = varDecl.getVarSection();
    return varSection.isInterfaceSection()
        && varSection.getDeclarations().size() == 1
        && varSection.jjtGetChildIndex() == varSection.jjtGetParent().jjtGetNumChildren() - 1;
  }

  private boolean isViolation(NameDeclarationNode identifier, boolean globalVariable) {
    String image = identifier.getImage();
    if (globalVariable) {
      return !NameConventionUtils.compliesWithPrefix(image, getProperty(GLOBAL_PREFIXES));
    }
    return !NameConventionUtils.compliesWithPascalCase(image);
  }
}
