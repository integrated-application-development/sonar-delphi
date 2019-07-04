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
package org.sonar.plugins.delphi.antlr.sanitizer.resolvers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.sonar.plugins.delphi.DelphiPlugin;
import org.sonar.plugins.delphi.antlr.directives.CompilerDirective;
import org.sonar.plugins.delphi.antlr.directives.CompilerDirectiveFactory;
import org.sonar.plugins.delphi.antlr.directives.exceptions.CompilerDirectiveFactorySyntaxException;
import org.sonar.plugins.delphi.antlr.sanitizer.SourceResolver;
import org.sonar.plugins.delphi.antlr.sanitizer.resolvers.exceptions.IncludeResolverException;
import org.sonar.plugins.delphi.antlr.sanitizer.subranges.SubRange;
import org.sonar.plugins.delphi.antlr.sanitizer.subranges.SubRangeFirstOccurenceComparator;
import org.sonar.plugins.delphi.antlr.sanitizer.subranges.impl.ReplacementSubRange;
import org.sonar.plugins.delphi.utils.DelphiUtils;

/**
 * add include files to a given file
 */
public class IncludeResolver extends SourceResolver {

  private static final int REPLACEMENT_OFFSET = 2;
  private boolean extendIncludes = true;
  private List<File> includes;
  private List<String> includedFiles = new ArrayList<>();

  /**
   * ctor
   *
   * @param shouldExtend should we add includes, or just cut their definitions out?
   * @param includesList list of included dirs
   */
  public IncludeResolver(boolean shouldExtend, List<File> includesList) {
    extendIncludes = shouldExtend;
    includes = includesList;
  }

  /**
   * @return list of directories holding include files
   */
  public List<String> getIncludedFilesPath() {
    return includedFiles;
  }

  @Override
  protected void doResolve(SourceResolverResults results) {
    resolveIncludes(results.getFileName(), results.getFileData());
  }

  /**
   * Resolve includes
   *
   * @param baseFileName base file name
   * @param baseFileData base file character data
   * @return new file character data
   */
  private StringBuilder resolveIncludes(String baseFileName, StringBuilder baseFileData) {
    if (baseFileName == null || baseFileData == null) {
      return baseFileData;
    }

    baseFileName = DelphiUtils.normalizeFileName(baseFileName);

    StringBuilder newData = new StringBuilder(baseFileData);
    List<ReplacementSubRange> dataToInclude = new ArrayList<>();

    try {
      CompilerDirectiveFactory factory = new CompilerDirectiveFactory();
      List<CompilerDirective> allDirectives = factory.produce(baseFileData.toString());

      for (CompilerDirective directive : allDirectives) {
        String includeFileName = directive.getItem();

        String currentDir = baseFileName.substring(0, baseFileName.lastIndexOf('/'));
        currentDir = backtrackDirectory(currentDir,
            DelphiUtils.countSubstrings(includeFileName, ".."));

        dataToInclude.add(processIncludedFile(directive, includeFileName, currentDir));
      }
    } catch (CompilerDirectiveFactorySyntaxException e) {
      DelphiPlugin.LOG.trace("Compiler directive syntax error: ", e);
    }

    return introduceIncludedData(newData, dataToInclude);
  }

  private ReplacementSubRange processIncludedFile(CompilerDirective directive,
      String includeFileName, String currentDir) {
    // This string will be inserted in place of the include directive
    String copyData = "";

    try {
      if (extendIncludes) {
        File includeFile = resolveIncludeFile(includeFileName, currentDir, includes);
        includedFiles.add(includeFile.getAbsolutePath());
        copyData = readFileIncludeData(includeFile);
      }
    } catch (IncludeResolverException | IOException e) {
      DelphiPlugin.LOG.warn("Failed to resolve include: ", e);
    }

    int rangeStart = directive.getFirstCharPosition();
    int rangeEnd = directive.getFirstCharPosition() + directive.getLength() + REPLACEMENT_OFFSET;
    return new ReplacementSubRange(rangeStart, rangeEnd, copyData);
  }

  private String readFileIncludeData(File includeFile) throws IOException {
    StringBuilder includeData = new StringBuilder(DelphiUtils.readFileContent(includeFile, null));

    // do the same for include file, it could also have includes
    includeData = resolveIncludes(includeFile.getAbsolutePath(), includeData);
    return includeData.toString();
  }

  private String backtrackDirectory(String currentDir, int dotdotCount) {
    for (int i = 0; i < dotdotCount; ++i) {
      currentDir = currentDir.substring(0, currentDir.lastIndexOf('/'));
    }
    return currentDir;
  }

  private StringBuilder introduceIncludedData(StringBuilder newData,
      List<ReplacementSubRange> dataToInclude) {
    int replacedCharsShift = 0;
    dataToInclude.sort(new SubRangeFirstOccurenceComparator());
    for (SubRange range : dataToInclude) {
      newData.replace(range.getBegin() + replacedCharsShift, range.getEnd() + replacedCharsShift,
          range.toString());
      replacedCharsShift += range.toString().length() - (range.getEnd() - range.getBegin());
    }
    return newData;
  }

  private File resolveIncludeFile(String fileName, String directory, List<File> includes)
      throws IncludeResolverException {
    File resolved = getExistingFile(directory, fileName);
    if (resolved != null) {
      return resolved;
    }
    return resolveIncludeFile(fileName, includes);
  }

  private File resolveIncludeFile(String fileName, List<File> directories)
      throws IncludeResolverException {
    for (File dir : directories) {
      DelphiPlugin.LOG
          .debug("Trying to include {}{}{}", dir.getAbsolutePath(), File.separator, fileName);
      File file = getExistingFile(dir.getAbsolutePath(), fileName);
      if (file != null) {
        return file;
      }
    }
    throw new IncludeResolverException("Could not resolve include file: " + fileName);
  }

  private File getExistingFile(String directory, String fileName) {
    File file = new File(directory + File.separator + fileName);
    if (file.exists() && file.isFile()) {
      return file;
    }
    return null;
  }

}
