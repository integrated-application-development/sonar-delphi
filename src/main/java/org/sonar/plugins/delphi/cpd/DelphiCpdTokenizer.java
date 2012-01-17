/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
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
package org.sonar.plugins.delphi.cpd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sourceforge.pmd.cpd.SourceCode;
import net.sourceforge.pmd.cpd.TokenEntry;
import net.sourceforge.pmd.cpd.Tokenizer;
import net.sourceforge.pmd.cpd.Tokens;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.Token;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.sanitizer.DelphiSourceSanitizer;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.utils.DelphiUtils;

/**
 * DelphiLanguage tokenizer class. It creates tokens based on antlr Lexer class.
 */
public class DelphiCpdTokenizer implements Tokenizer {

  private ProjectFileSystem fileSystem = null;
  private static List<File> excluded = null;

  public DelphiCpdTokenizer() {
  }

  public DelphiCpdTokenizer(ProjectFileSystem fileSystem, List<File> excluded) {
    this.fileSystem = fileSystem;
    DelphiCpdTokenizer.excluded = excluded;
  }

  /**
   * Create tokens (stored in cpdTokens) from source.
   * 
   * @param source
   *          The source code to parse for tokens
   * @param cpdTokens
   *          Where tokens will be held
   */
  @Override
  public final void tokenize(SourceCode source, Tokens cpdTokens) {
    String fileName = source.getFileName();
    if ( !canTokenize(fileName)) {
      return;
    }
    doTokenize(cpdTokens, fileName);
  }

  private void doTokenize(Tokens cpdTokens, String fileName) {
    try {
      DelphiLexer lexer = new DelphiLexer(new DelphiSourceSanitizer(fileName));
      Token token = lexer.nextToken();
      while (token.getType() != Token.EOF) {
        cpdTokens.add(new TokenEntry(token.getText(), fileName, token.getLine()));
        token = lexer.nextToken();
      }
    } catch (FileNotFoundException ex) {
      DelphiUtils.LOG.error("Could not find : " + fileName, ex);
      DelphiUtils.getDebugLog().println(">>!! Could not find : " + fileName);
    } catch (IOException ex) {
      DelphiUtils.LOG.error("IO Exception on " + fileName, ex);
      DelphiUtils.getDebugLog().println(">>!! IO Exception on " + fileName);
    }
    cpdTokens.add(TokenEntry.getEOF());
  }

  private boolean canTokenize(String fileName) {
    Set<String> includedFiles = DelphiSourceSanitizer.getIncludedFiles();
    if (includedFiles.contains(fileName)) {
      return false;
    }
    if (DelphiProjectHelper.getInstance().isExcluded(fileName, excluded)) {
      return false;
    }
    return true;
  }

  /**
   * Create tokens from text.
   * 
   * @param source
   *          The source code to parse for tokens
   * @return List of found tokens
   */
  public final List<Token> tokenize(String[] source) {
    List<Token> tokens = new ArrayList<Token>();

    for (String string : source) {
      DelphiLexer lexer = new DelphiLexer(new ANTLRStringStream(string));
      Token token = lexer.nextToken();
      token.setText(token.getText().toLowerCase());
      while (token.getType() != Token.EOF) {
        tokens.add(token);
        token = lexer.nextToken();
      }
    }
    tokens.add(Token.EOF_TOKEN);
    return tokens;
  }

}
