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
