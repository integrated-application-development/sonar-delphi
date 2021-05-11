package org.sonar.plugins.delphi.pmd.rules;

import com.google.common.collect.Iterables;
import java.util.Set;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.parameter.Parameter;

public class DateFormatSettingsRule extends AbstractDelphiRule {
  private static final String TFORMATSETTINGS = "System.SysUtils.TFormatSettings";
  private static final Set<String> METHOD_SIGNATURES =
      Set.of(
          "System.SysUtils.DateToStr",
          "System.SysUtils.DateTimeToStr",
          "System.SysUtils.StrToDate",
          "System.SysUtils.StrToDateDef",
          "System.SysUtils.TryStrToDate",
          "System.SysUtils.StrToDateTime",
          "System.SysUtils.StrToDateTimeDef",
          "System.SysUtils.TryStrToDateTime");

  @Override
  public RuleContext visit(NameReferenceNode reference, RuleContext data) {
    NameDeclaration declaration = reference.getNameDeclaration();
    if (declaration instanceof MethodNameDeclaration) {
      MethodNameDeclaration method = (MethodNameDeclaration) declaration;
      if (METHOD_SIGNATURES.contains(method.fullyQualifiedName())) {
        Parameter lastParameter = Iterables.getLast(method.getParameters());
        if (!lastParameter.getType().is(TFORMATSETTINGS)) {
          addViolation(data, reference);
        }
      }
    }
    return super.visit(reference, data);
  }
}
