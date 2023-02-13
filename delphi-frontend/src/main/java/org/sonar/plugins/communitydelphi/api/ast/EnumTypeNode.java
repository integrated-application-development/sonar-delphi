package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;

public interface EnumTypeNode extends TypeNode {
  List<EnumElementNode> getElements();
}
