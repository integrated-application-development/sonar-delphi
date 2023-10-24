/*
 * Sonar Delphi Plugin
 * Copyright (C) 2024 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.communitydelphi.api.reporting;

import au.com.integradev.delphi.reporting.edits.DeleteQuickFixEdit;
import au.com.integradev.delphi.reporting.edits.InsertQuickFixEdit;
import au.com.integradev.delphi.reporting.edits.NodeCopyQuickFixEdit;
import au.com.integradev.delphi.reporting.edits.NodeMoveQuickFixEdit;
import au.com.integradev.delphi.reporting.edits.ReplaceQuickFixEdit;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;

public interface QuickFixEdit {
  static QuickFixEdit insert(String text, int line, int column) {
    return new InsertQuickFixEdit(text, line, column);
  }

  static QuickFixEdit insertBefore(String text, DelphiNode node) {
    return insert(text, node.getBeginLine(), node.getBeginColumn());
  }

  static QuickFixEdit insertAfter(String text, DelphiNode node) {
    return insert(text, node.getEndLine(), node.getEndColumn());
  }

  static QuickFixEdit copy(DelphiNode node, FilePosition location) {
    return new NodeCopyQuickFixEdit(node, location);
  }

  static QuickFixEdit copyBefore(DelphiNode node, DelphiNode referenceNode) {
    return copy(
        node,
        FilePosition.from(
            referenceNode.getBeginLine(),
            referenceNode.getBeginColumn(),
            referenceNode.getBeginLine(),
            referenceNode.getBeginColumn()));
  }

  static QuickFixEdit copyAfter(DelphiNode node, DelphiNode referenceNode) {
    return copy(
        node,
        FilePosition.from(
            referenceNode.getEndLine(),
            referenceNode.getEndColumn(),
            referenceNode.getEndLine(),
            referenceNode.getEndColumn()));
  }

  static QuickFixEdit copyReplacing(DelphiNode node, DelphiNode referenceNode) {
    return copy(node, FilePosition.from(referenceNode));
  }

  static QuickFixEdit move(DelphiNode node, FilePosition location) {
    return new NodeMoveQuickFixEdit(node, location);
  }

  static QuickFixEdit moveBefore(DelphiNode node, DelphiNode referenceNode) {
    return move(
        node,
        FilePosition.from(
            referenceNode.getBeginLine(),
            referenceNode.getBeginColumn(),
            referenceNode.getBeginLine(),
            referenceNode.getBeginColumn()));
  }

  static QuickFixEdit moveAfter(DelphiNode node, DelphiNode referenceNode) {
    return move(
        node,
        FilePosition.from(
            referenceNode.getEndLine(),
            referenceNode.getEndColumn(),
            referenceNode.getEndLine(),
            referenceNode.getEndColumn()));
  }

  static QuickFixEdit moveReplacing(DelphiNode node, DelphiNode referenceNode) {
    return new NodeMoveQuickFixEdit(node, FilePosition.from(referenceNode));
  }

  static QuickFixEdit replace(DelphiNode node, String text) {
    return replace(FilePosition.from(node), text);
  }

  static QuickFixEdit replace(FilePosition range, String text) {
    return new ReplaceQuickFixEdit(range, text);
  }

  static QuickFixEdit delete(DelphiNode node) {
    return delete(FilePosition.from(node));
  }

  static QuickFixEdit delete(FilePosition range) {
    return new DeleteQuickFixEdit(range);
  }
}
