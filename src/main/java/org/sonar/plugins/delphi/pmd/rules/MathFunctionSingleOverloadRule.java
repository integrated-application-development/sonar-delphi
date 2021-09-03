package org.sonar.plugins.delphi.pmd.rules;

import java.util.Set;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicType;

public class MathFunctionSingleOverloadRule extends AbstractDelphiRule {
  private static final Set<String> MATH_FUNCTIONS =
      Set.of(
          "System.Math.ArcCos",
          "System.Math.ArcSin",
          "System.Math.ArcTan2",
          "System.Math.SinCos",
          "System.Math.Tan",
          "System.Math.Cotan",
          "System.Math.Secant",
          "System.Math.Cosecant",
          "System.Math.Hypot",
          "System.Math.RadToDeg",
          "System.Math.RadToGrad",
          "System.Math.RadToCycle",
          "System.Math.DegToRad",
          "System.Math.DegToGrad",
          "System.Math.DegToCycle",
          "System.Math.DegNormalize",
          "System.Math.GradToRad",
          "System.Math.GradToDeg",
          "System.Math.GradToCycle",
          "System.Math.CycleToRad",
          "System.Math.CycleToDeg",
          "System.Math.CycleToGrad",
          "System.Math.Cot",
          "System.Math.Sec",
          "System.Math.Csc",
          "System.Math.Cosh",
          "System.Math.Sinh",
          "System.Math.Tanh",
          "System.Math.CotH",
          "System.Math.SecH",
          "System.Math.CscH",
          "System.Math.ArcCot",
          "System.Math.ArcSec",
          "System.Math.ArcCsc",
          "System.Math.ArcCosh",
          "System.Math.ArcSinh",
          "System.Math.ArcTanh",
          "System.Math.ArcCotH",
          "System.Math.ArcSecH",
          "System.Math.ArcCscH",
          "System.Math.LnXP1",
          "System.Math.Log10",
          "System.Math.Log2",
          "System.Math.LogN",
          "System.Math.IntPower",
          "System.Math.Power",
          "System.Math.Frexp",
          "System.Math.Ldexp",
          "System.Math.Poly",
          "System.Math.FMod",
          "System.Math.SimpleRoundTo");

  @Override
  public RuleContext visit(NameReferenceNode reference, RuleContext data) {
    DelphiNameDeclaration declaration = reference.getNameDeclaration();
    if (declaration instanceof MethodNameDeclaration
        && isMathFunctionSingleOverload((MethodNameDeclaration) declaration)) {
      addViolation(data, reference.getIdentifier());
    }
    return super.visit(reference, data);
  }

  private static boolean isMathFunctionSingleOverload(MethodNameDeclaration method) {
    return MATH_FUNCTIONS.contains(method.fullyQualifiedName())
        && method.getParameters().stream()
            .anyMatch(parameter -> parameter.getType().is(IntrinsicType.SINGLE));
  }
}
