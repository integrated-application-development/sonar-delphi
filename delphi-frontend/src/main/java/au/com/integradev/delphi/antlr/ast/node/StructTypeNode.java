package au.com.integradev.delphi.antlr.ast.node;

import java.util.List;

public interface StructTypeNode extends TypeNode {

  List<VisibilitySectionNode> getVisibilitySections();
}
