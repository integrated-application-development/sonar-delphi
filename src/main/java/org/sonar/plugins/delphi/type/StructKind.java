package org.sonar.plugins.delphi.type;

import org.sonar.plugins.delphi.antlr.ast.node.ClassHelperTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.ClassTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.InterfaceTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.ObjectTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.RecordHelperTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.RecordTypeNode;

public enum StructKind {
  CLASS(ClassTypeNode.class),
  CLASS_HELPER(ClassHelperTypeNode.class),
  INTERFACE(InterfaceTypeNode.class),
  OBJECT(ObjectTypeNode.class),
  RECORD(RecordTypeNode.class),
  RECORD_HELPER(RecordHelperTypeNode.class);

  private final Class<? extends DelphiNode> nodeType;

  StructKind(Class<? extends DelphiNode> nodeType) {
    this.nodeType = nodeType;
  }

  public static StructKind fromNode(DelphiNode node) {
    for (StructKind kind : StructKind.values()) {
      if (kind.nodeType == node.getClass()) {
        return kind;
      }
    }
    throw new AssertionError("Unknown StructKind. TypeNode: " + node.getClass().getSimpleName());
  }
}
