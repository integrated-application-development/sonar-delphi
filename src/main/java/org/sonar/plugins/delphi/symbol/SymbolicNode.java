package org.sonar.plugins.delphi.symbol;

import java.util.concurrent.atomic.AtomicInteger;
import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.symboltable.ScopedNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.IndexedNode;

public final class SymbolicNode extends AbstractNode implements ScopedNode, IndexedNode {
  private static AtomicInteger imaginaryTokenIndex = new AtomicInteger(-100);
  private final String unitName;
  private final DelphiScope scope;
  private final int tokenIndex;

  public SymbolicNode(DelphiNode node, DelphiScope scope) {
    this(
        node.jjtGetId(),
        node.getBeginLine(),
        node.getEndLine(),
        node.getBeginColumn(),
        node.getEndColumn(),
        node.getUnitName(),
        node.getImage(),
        scope,
        node.getTokenIndex());
  }

  private SymbolicNode(
      int id,
      int beginLine,
      int endLine,
      int beginColumn,
      int endColumn,
      String unitName,
      String image,
      DelphiScope scope,
      int tokenIndex) {
    super(id, beginLine, endLine, beginColumn, endColumn);
    this.setImage(image);
    this.unitName = unitName;
    this.scope = scope;
    this.tokenIndex = tokenIndex;
  }

  public static SymbolicNode imaginary(String image, DelphiScope scope) {
    return new SymbolicNode(
        0,
        0,
        0,
        0,
        0,
        UnitNameDeclaration.UNKNOWN_UNIT,
        image,
        scope,
        imaginaryTokenIndex.decrementAndGet());
  }

  @Override
  public DelphiScope getScope() {
    return scope;
  }

  @Override
  public int getTokenIndex() {
    return tokenIndex;
  }

  public String getUnitName() {
    return unitName;
  }

  @Override
  public String getXPathNodeName() {
    return getImage();
  }
}
