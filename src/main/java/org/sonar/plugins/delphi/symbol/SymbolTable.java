package org.sonar.plugins.delphi.symbol;

import java.util.HashMap;
import java.util.Map;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.project.DelphiProject;

public class SymbolTable {
  private Map<String, UnitNameDeclaration> unitsByFilePath = new HashMap<>();

  public void addUnit(String path, UnitNameDeclaration declaration) {
    unitsByFilePath.put(path, declaration);
  }

  public UnitNameDeclaration getUnitByPath(String path) {
    return unitsByFilePath.get(path);
  }

  public static SymbolTable buildSymbolTable(DelphiProject project, DelphiProjectHelper helper) {
    SymbolTableBuilder builder = new SymbolTableBuilder(project, helper);
    return builder.build();
  }
}
