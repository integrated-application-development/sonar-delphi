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
import java.util.Collections;
import java.util.List;
import org.sonar.plugins.delphi.antlr.directives.CompilerDirective;
import org.sonar.plugins.delphi.antlr.directives.CompilerDirectiveFactory;
import org.sonar.plugins.delphi.antlr.directives.CompilerDirectiveType;
import org.sonar.plugins.delphi.antlr.directives.exceptions.CompilerDirectiveFactorySyntaxException;
import org.sonar.plugins.delphi.antlr.sanitizer.SourceResolver;
import org.sonar.plugins.delphi.antlr.sanitizer.resolvers.exceptions.IncludeResolverException;
import org.sonar.plugins.delphi.antlr.sanitizer.subranges.SubRange;
import org.sonar.plugins.delphi.antlr.sanitizer.subranges.SubRangeAggregator;
import org.sonar.plugins.delphi.antlr.sanitizer.subranges.SubRangeFirstOccurenceComparator;
import org.sonar.plugins.delphi.antlr.sanitizer.subranges.impl.ReplacementSubRange;
import org.sonar.plugins.delphi.utils.DelphiUtils;

/**
 * add include files to a given file
 */
public class IncludeResolver extends SourceResolver {

  private static final int REPLACEMENT_OFFSET = 2;
  private boolean extendIncludes = true;
  private List<File> includes = null;
  private List<String> includedFiles = new ArrayList<String>();

  /**
   * ctor
   * 
   * @param shouldExtend should we add includes, or just cut their
   *            deffinitions out?
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
    resolveIncludes(results.getFileName(), results.getFileData(), results.getFileExcludes());
  }

  /**
   * Resolve includes
   * 
   * @param baseFileName base file name
   * @param baseFileData base file character data
   * @param excludes list of excluded areas (won't be parsed: strings,
   *            comments etc.)
   * @return new file character data
   */
  private StringBuilder resolveIncludes(String baseFileName, StringBuilder baseFileData, SubRangeAggregator excludes) {
    if (baseFileName == null || baseFileData == null) {
      return baseFileData;
    }

    baseFileName = DelphiUtils.normalizeFileName(baseFileName);

    StringBuilder newData = new StringBuilder(baseFileData);
    List<ReplacementSubRange> dataToInclude = new ArrayList<ReplacementSubRange>();

    try {
      CompilerDirectiveFactory factory = new CompilerDirectiveFactory();
      List<CompilerDirective> allDirectives = factory.produce(baseFileData.toString());

      for (CompilerDirective directive : allDirectives) {
        if (excludes.inRange(directive.getFirstCharPosition())
          || directive.getType() != CompilerDirectiveType.INCLUDE) {
          continue;
        }

        String includeFileName = directive.getItem();

        String currentDir = baseFileName.substring(0, baseFileName.lastIndexOf('/'));
        currentDir = backtrackDirectory(currentDir, DelphiUtils.countSubstrings(includeFileName, ".."));

        try
        {
          // string, that will be inserted in replacement of include statement
          String copyData = "";
          if (extendIncludes) {
            File includeFile = resolveIncludeFile(includeFileName, currentDir, includes);
            includedFiles.add(includeFile.getAbsolutePath());
            copyData = readFileIncludeData(includeFile);
          }
          dataToInclude.add(new ReplacementSubRange(directive.getFirstCharPosition(), directive
            .getFirstCharPosition()
            + directive.getLength() + REPLACEMENT_OFFSET, copyData));

        } catch (IncludeResolverException e) {
          DelphiUtils.LOG.warn(e.getMessage());
          continue;
        } catch (IOException e) {
          DelphiUtils.LOG.warn(e.getMessage());
          continue;
        }

      }
    } catch (CompilerDirectiveFactorySyntaxException e) {
      DelphiUtils.LOG.trace(e.getMessage());
    }

    return introduceIncludedData(newData, dataToInclude);
  }

  private String readFileIncludeData(File includeFile) throws IncludeResolverException, IOException {
    StringBuilder includeData = new StringBuilder(DelphiUtils.readFileContent(includeFile, null));
    SourceResolverResults includedResults = new SourceResolverResults(includeFile.getAbsolutePath(), includeData);

    ExcludeResolver excludeResolver = new ExcludeResolver();
    excludeResolver.resolve(includedResults);
    SubRangeAggregator newExcluded = includedResults.getFileExcludes();

    // do the same for include file, it could also have includes
    includeData = resolveIncludes(includeFile.getAbsolutePath(), includeData, newExcluded);
    return includeData.toString();
  }

  private String backtrackDirectory(String currentDir, int dotdotCount) {
    for (int i = 0; i < dotdotCount; ++i) {
      currentDir = currentDir.substring(0, currentDir.lastIndexOf('/'));
    }
    return currentDir;
  }

  private StringBuilder introduceIncludedData(StringBuilder newData, List<ReplacementSubRange> dataToInclude) {
    int replacedCharsShift = 0;
    Collections.sort(dataToInclude, new SubRangeFirstOccurenceComparator());
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

  private File resolveIncludeFile(String fileName, List<File> directories) throws IncludeResolverException {
    for (File dir : directories) {
      DelphiUtils.LOG.debug("Trying to include " + dir.getAbsolutePath() + File.separator + fileName);
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
