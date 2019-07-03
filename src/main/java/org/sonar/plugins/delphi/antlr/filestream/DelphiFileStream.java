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
package org.sonar.plugins.delphi.antlr.filestream;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.antlr.runtime.ANTLRStringStream;
import org.sonar.plugins.delphi.antlr.resolvers.SourceResolver;
import org.sonar.plugins.delphi.antlr.resolvers.DefineResolver;
import org.sonar.plugins.delphi.antlr.resolvers.IncludeResolver;
import org.sonar.plugins.delphi.antlr.resolvers.SourceResolverResults;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class DelphiFileStream extends ANTLRStringStream {

  private DelphiFileStreamConfig config;
  private String fileName;

  /**
   * Constructor
   *
   * @param fileName File name to stream
   * @param config Configures file encoding and pre-processing
   * @throws IOException If file not found
   */
  public DelphiFileStream(String fileName, DelphiFileStreamConfig config)
      throws IOException {
    this.fileName = fileName;
    this.config = config;
    load();
  }

  /**
   * Overrides AntlrStringStream LookAhead for case insensitivity.
   */
  @Override
  public int LA(int i) {
    int la = super.LA(i);

    return Character.toLowerCase(la);
  }

  private void load() throws IOException {
    Set<String> defs = config.getDefinitions();
    boolean extendIncludes = config.getExtendIncludes();
    List<File> includeDirectories = config.getIncludedDirs();

    File file = new File(fileName);
    String encoding = config.getEncoding();
    StringBuilder fileData = new StringBuilder(DelphiUtils.readFileContent(file, encoding));

    SourceResolverResults resolverResult = new SourceResolverResults(fileName, fileData);

    SourceResolver resolver = new IncludeResolver(extendIncludes, includeDirectories)
        .chain(new DefineResolver(defs));

    resolver.resolve(resolverResult);
    data = resolverResult.getFileData().toString().toCharArray();

    super.n = data.length;
  }

  @Override
  public String getSourceName() {
    return this.fileName;
  }

  /**
   * Creates a DelphiFileStreamConfig for a given DelphiProject
   * @param delphiProject a DelphiProject with its own include directories and compiler definitions
   * @param delphiProjectHelper the delphiProjectHelper, provides user settings like encoding
   * @return a new DelphiFileStreamConfig instance
   */
  public static DelphiFileStreamConfig createConfig(DelphiProject delphiProject,
      DelphiProjectHelper delphiProjectHelper) {
    String encoding = delphiProjectHelper.encoding();
    List<File> includedDirs = delphiProject.getIncludeDirectories();
    List<String> definitions = delphiProject.getDefinitions();
    boolean extendIncludes = delphiProjectHelper.shouldExtendIncludes();

    return new DelphiFileStreamConfig(encoding, includedDirs, definitions, extendIncludes);
  }
}
