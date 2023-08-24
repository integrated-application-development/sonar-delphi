/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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
package org.sonar.plugins.communitydelphi.api.ast;

import au.com.integradev.delphi.file.DelphiFile;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;

public interface DelphiAst extends DelphiNode {

  List<DelphiToken> getCommentsInsideNode(DelphiNode node);

  default List<DelphiToken> getCommentsBetweenTokens(DelphiToken first, DelphiToken last) {
    return getComments().stream()
        .filter(
            token -> {
              int index = token.getIndex();
              return index > first.getIndex() && index < last.getIndex();
            })
        .collect(Collectors.toList());
  }

  List<DelphiToken> getTokens();

  DelphiFile getDelphiFile();

  String getFileName();

  FileHeaderNode getFileHeader();

  boolean isProgram();

  boolean isUnit();

  boolean isPackage();
}
