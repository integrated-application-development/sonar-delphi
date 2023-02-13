package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;

public interface StructTypeNode extends TypeNode {

  List<VisibilitySectionNode> getVisibilitySections();
}
