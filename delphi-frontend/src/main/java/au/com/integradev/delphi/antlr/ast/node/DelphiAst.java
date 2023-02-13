package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.antlr.ast.node.DelphiNode;
import au.com.integradev.delphi.antlr.ast.node.FileHeaderNode;
import au.com.integradev.delphi.antlr.ast.token.DelphiToken;
import au.com.integradev.delphi.file.DelphiFile;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

  Set<Integer> getSuppressions();

  FileHeaderNode getFileHeader();

  boolean isProgram();

  boolean isUnit();

  boolean isPackage();
}
