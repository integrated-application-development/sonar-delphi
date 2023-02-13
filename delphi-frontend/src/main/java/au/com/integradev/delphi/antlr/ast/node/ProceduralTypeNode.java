package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.antlr.ast.node.FormalParameterNode.FormalParameterData;
import au.com.integradev.delphi.type.Type;
import java.util.List;

public interface ProceduralTypeNode extends TypeNode {
  Type getReturnType();

  List<FormalParameterData> getParameters();
}
