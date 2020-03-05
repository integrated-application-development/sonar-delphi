package org.sonar.plugins.delphi.symbol;

import java.util.concurrent.atomic.AtomicInteger;
import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.symboltable.ScopedNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.IndexedNode;
import org.sonar.plugins.delphi.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;

public final class SymbolicNode extends AbstractNode implements ScopedNode, IndexedNode {
  private static final AtomicInteger IMAGINARY_TOKEN_INDEX = new AtomicInteger(Integer.MIN_VALUE);
  private final String unitName;
  private final DelphiScope scope;
  private final int tokenIndex;

  public SymbolicNode(DelphiNode node) {
    this(node, node.getScope());
  }

  public SymbolicNode(DelphiNode node, DelphiScope scope) {
    this(
        node.jjtGetId(),
        node.getBeginLine(),
        node.getEndLine(),
        node.getBeginColumn(),
        node.getEndColumn(),
        node.findUnitName(),
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
        IMAGINARY_TOKEN_INDEX.incrementAndGet());
  }

  public static SymbolicNode fromRange(String image, DelphiNode begin, Node end) {
    return new SymbolicNode(
        begin.jjtGetId(),
        begin.getBeginLine(),
        end.getEndLine(),
        begin.getBeginColumn(),
        end.getEndColumn(),
        begin.findUnitName(),
        image,
        begin.getScope(),
        begin.getTokenIndex());
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
