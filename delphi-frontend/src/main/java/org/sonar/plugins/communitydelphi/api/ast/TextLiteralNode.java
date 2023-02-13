package org.sonar.plugins.communitydelphi.api.ast;

public interface TextLiteralNode extends LiteralNode {
  CharSequence getImageWithoutQuotes();
}
