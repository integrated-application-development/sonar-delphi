package org.sonar.plugins.delphi.symbol;

import java.util.HashSet;
import java.util.Set;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.symbol.scope.MethodScope;

public class Search {
  private static final Logger LOG = Loggers.get(Search.class);
  private static final boolean TRACE = false;

  private final DelphiNameOccurrence occurrence;
  private final Set<NameDeclaration> declarations = new HashSet<>();

  public Search(DelphiNameOccurrence occ) {
    if (TRACE) {
      LOG.info("new search for reference " + occ);
    }
    this.occurrence = occ;
  }

  public void execute(DelphiScope startingScope) {
    Set<NameDeclaration> found = searchUpward(occurrence, startingScope);
    if (TRACE) {
      LOG.info("found " + found);
    }
    declarations.addAll(found);
  }

  public Set<NameDeclaration> getResult() {
    return declarations;
  }

  private Set<NameDeclaration> searchUpward(DelphiNameOccurrence occurrence, DelphiScope scope) {
    if (TRACE) {
      LOG.info(" checking scope " + scope + " for name occurrence " + occurrence);
    }

    Set<NameDeclaration> result = scope.findDeclaration(occurrence);

    if (result.isEmpty()) {
      if (scope instanceof MethodScope) {
        DelphiScope typeScope = ((MethodScope) scope).getTypeScope();
        if (typeScope != null) {
          result = searchUpward(occurrence, typeScope);
        }
      }

      if (result.isEmpty() && scope.getParent() != null) {
        if (TRACE) {
          LOG.info(" moving up from " + scope + " to " + scope.getParent());
        }
        return searchUpward(occurrence, scope.getParent());
      }
    } else if (TRACE) {
      LOG.info(" found it!");
    }

    return result;
  }
}
