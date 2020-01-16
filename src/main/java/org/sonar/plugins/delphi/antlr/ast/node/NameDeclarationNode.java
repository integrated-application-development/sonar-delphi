package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.Collections;
import java.util.List;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;

public abstract class NameDeclarationNode extends DelphiNode {
  private DelphiNameDeclaration declaration;
  private List<NameOccurrence> usages;

  public NameDeclarationNode(Token token) {
    super(token);
  }

  public NameDeclarationNode(int tokenType) {
    super(tokenType);
  }

  public DelphiNameDeclaration getNameDeclaration() {
    return declaration;
  }

  public void setNameDeclaration(DelphiNameDeclaration declaration) {
    this.declaration = declaration;
  }

  public List<NameOccurrence> getUsages() {
    if (usages == null) {
      if (declaration != null) {
        usages = declaration.getScope().getOccurrencesFor(declaration);
      } else {
        usages = Collections.emptyList();
      }
    }
    return usages;
  }
}
