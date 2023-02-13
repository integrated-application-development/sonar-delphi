package org.sonar.plugins.communitydelphi.api.ast;

import javax.annotation.Nonnull;

public interface HelperTypeNode extends StructTypeNode {
  @Nonnull
  TypeNode getFor();
}
