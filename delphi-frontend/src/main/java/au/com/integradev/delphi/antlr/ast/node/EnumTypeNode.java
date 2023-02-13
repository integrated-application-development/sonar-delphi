package au.com.integradev.delphi.antlr.ast.node;

import java.util.List;

public interface EnumTypeNode extends TypeNode {
  List<EnumElementNode> getElements();
}
