package org.sonar.plugins.delphi.symbol;

import java.util.HashMap;
import java.util.Map;
import org.sonar.plugins.delphi.symbol.declaration.UnitNameDeclaration;

public class SymbolTable {
  private final Map<String, UnitNameDeclaration> unitsByFilePath = new HashMap<>();

  public void addUnit(String path, UnitNameDeclaration declaration) {
    unitsByFilePath.put(path, declaration);
  }

  public UnitNameDeclaration getUnitByPath(String path) {
    return unitsByFilePath.get(path);
  }

  public static SymbolTableBuilder builder() {
    return new SymbolTableBuilder();
  }
}
