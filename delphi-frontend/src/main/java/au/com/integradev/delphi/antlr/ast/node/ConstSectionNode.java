package au.com.integradev.delphi.antlr.ast.node;

public interface ConstSectionNode extends DelphiNode, Visibility {
  @Override
  VisibilityType getVisibility();
}
