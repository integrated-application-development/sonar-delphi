/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package au.com.integradev.delphi.checks;

import java.util.ArrayList;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationListNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineParametersNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFix;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFixEdit;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "GroupedParameterDeclarationRule", repositoryKey = "delph")
@Rule(key = "GroupedParameterDeclaration")
public class GroupedParameterDeclarationCheck extends AbstractGroupedDeclarationCheck {
  @Override
  protected String getIssueMessage() {
    return "Declare these parameters separately.";
  }

  @Override
  protected boolean isRelevantDeclarationList(NameDeclarationListNode declarationList) {
    return declarationList.getFirstParentOfType(RoutineParametersNode.class) != null;
  }

  private static String getFormalParameterTextPrefix(FormalParameterNode formalParameter) {
    if (formalParameter.isOut()) {
      return "out ";
    } else if (formalParameter.isConst()) {
      return "const ";
    } else if (formalParameter.isVar()) {
      return "var ";
    } else {
      return "";
    }
  }

  @Override
  protected QuickFix createQuickFix(
      NameDeclarationListNode declarationList, DelphiCheckContext context) {
    FormalParameterNode formalParameter = (FormalParameterNode) declarationList.getParent();

    TypeNode typeNode = formalParameter.getTypeNode();

    if (typeNode == null) {
      return null;
    }

    List<NameDeclarationNode> declarations = declarationList.getDeclarations();
    List<QuickFixEdit> fixEdits = new ArrayList<>();

    for (int i = 1; i < declarations.size(); i++) {
      NameDeclarationNode first = declarations.get(i - 1);
      NameDeclarationNode second = declarations.get(i);

      DelphiNode commaNode = second.getParent().getChild(second.getChildIndex() - 1);
      if (commaNode.getTokenType() == DelphiTokenType.COMMA) {
        fixEdits.add(QuickFixEdit.replace(commaNode, ";"));
      }

      fixEdits.add(QuickFixEdit.copyAfter(typeNode, first));
      fixEdits.add(QuickFixEdit.insertAfter(": ", first));

      String prefix = getFormalParameterTextPrefix(formalParameter);
      if (!prefix.isEmpty()) {
        fixEdits.add(QuickFixEdit.insertBefore(prefix, second));
      }
    }

    return QuickFix.newFix("Separate grouped parameters").withEdits(fixEdits);
  }
}
