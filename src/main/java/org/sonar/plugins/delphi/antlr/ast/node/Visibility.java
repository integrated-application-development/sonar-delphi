package org.sonar.plugins.delphi.antlr.ast.node;

public interface Visibility {
  enum VisibilityType {
    IMPLICIT_PUBLISHED,
    PUBLISHED,
    PUBLIC,
    PROTECTED,
    STRICT_PROTECTED,
    PRIVATE,
    STRICT_PRIVATE
  }

  VisibilityType getVisibility();

  default boolean isImplicitPublished() {
    return getVisibility() == VisibilityType.IMPLICIT_PUBLISHED;
  }

  default boolean isPublished() {
    return getVisibility() == VisibilityType.PUBLISHED || isImplicitPublished();
  }

  default boolean isPublic() {
    return getVisibility() == VisibilityType.PUBLIC;
  }

  default boolean isProtected() {
    return getVisibility() == VisibilityType.PROTECTED || isStrictProtected();
  }

  default boolean isStrictProtected() {
    return getVisibility() == VisibilityType.STRICT_PROTECTED;
  }

  default boolean isPrivate() {
    return getVisibility() == VisibilityType.PRIVATE || isStrictPrivate();
  }

  default boolean isStrictPrivate() {
    return getVisibility() == VisibilityType.STRICT_PRIVATE;
  }

  default boolean isStrict() {
    return isStrictProtected() || isStrictPrivate();
  }
}
