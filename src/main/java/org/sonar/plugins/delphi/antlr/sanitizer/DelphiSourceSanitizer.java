/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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
package org.sonar.plugins.delphi.antlr.sanitizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.antlr.runtime.ANTLRFileStream;
import org.apache.commons.io.FileUtils;
import org.sonar.plugins.delphi.antlr.sanitizer.resolvers.DefineResolver;
import org.sonar.plugins.delphi.antlr.sanitizer.resolvers.ExcludeResolver;
import org.sonar.plugins.delphi.antlr.sanitizer.resolvers.IncludeResolver;
import org.sonar.plugins.delphi.antlr.sanitizer.resolvers.SourceFixerResolver;
import org.sonar.plugins.delphi.antlr.sanitizer.resolvers.SourceResolverResults;
import org.sonar.plugins.delphi.utils.DelphiUtils;

/**
 * Antlr Class that handles some common grammar problems, see ReadMe.docx for
 * more info.
 */
public class DelphiSourceSanitizer extends ANTLRFileStream {

  private static List<File> includeDirectories = new ArrayList<File>();
  private static Set<String> includedFiles = new HashSet<String>();
  private static Set<String> definitions = new HashSet<String>();

  /**
   * Ctor with file name
   * 
   * @param fileName File name to stream
   * @throws IOException If no file found
   */
  public DelphiSourceSanitizer(String fileName) throws IOException {
    super(fileName);
  }

  /**
   * C-tor with file name and encoding
   * 
   * @param fileName File namt to stream
   * @param encoding Encoding to use
   * @throws IOException If file not found
   */
  public DelphiSourceSanitizer(String fileName, String encoding)
    throws IOException {
    super(fileName, encoding);
  }

  /**
   * Sets the include directories, method is static, so we could cache the
   * directories and not calculate them each time a new file is parsed
   * 
   * @param list List of include directories
   */
  public static void setIncludeDirectories(List<File> list) {
    includeDirectories.clear();
    includeDirectories.addAll(list);
  }

  /**
   * Sets the preprocessor definitions, method is static, so we could cache
   * the definitions and not remake them each time a new file is parsed
   * 
   * @param list List of preprocessor definitions
   * 
   * @param newDefinitions List of definitions
   */
  public static void setDefinitions(List<String> newDefinitions) {
    definitions.clear();
    definitions.addAll(newDefinitions);
  }

  /**
   * Overrides AntlrStringStream LookAhead for case insensitivity.
   */

  @Override
  public int LA(int i) {
    int la = super.LA(i);

    return Character.toLowerCase(la);
  }

  /**
   * Overloading the load method from ANTRLFileStream, to add whitespace where
   * it is required (':', '..'), and preform additional actions
   */

  @Override
  public void load(String fileName, String encoding) throws IOException {
    if (fileName == null) {
      return;
    }

    Set<String> defs = new HashSet<String>(definitions);

    // TODO delphiProjectHelper.shouldExtendIncludes();
    boolean extendIncludes = true;

    StringBuilder fileData = new StringBuilder(DelphiUtils.readFileContent(new File(fileName), encoding));

    SourceResolverResults resolverResult = new SourceResolverResults(fileName, fileData);

    SourceResolver resolver = new ExcludeResolver();
    resolver.chain(new IncludeResolver(extendIncludes, includeDirectories)).chain(new ExcludeResolver())
      .chain(new DefineResolver(defs))
      .chain(new SourceFixerResolver());

    resolver.resolve(resolverResult);
    data = resolverResult.getFileData().toString().toCharArray();

    String newFileName = fileName.replace(".pas", "_new.pas");
    FileUtils.write(new File(newFileName), resolverResult.getFileData().toString(), encoding);

    super.n = data.length;
  }

  /**
   * Gets the set of files, that already have been included in other files
   * 
   * @return
   */
  public static Set<String> getIncludedFiles() {
    return includedFiles;
  }

}
