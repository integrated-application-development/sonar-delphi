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

import org.sonar.plugins.delphi.antlr.sanitizer.subranges.SubRangeAggregator;
import org.sonar.plugins.delphi.antlr.sanitizer.subranges.SubRangeMergingAggregator;

/**
 * class holding results for resolvers
 */
public class SourceResolverResults {

  private String fileName;
  private StringBuilder data;
  private SubRangeAggregator excludes = new SubRangeMergingAggregator();

  /**
   * ctor
   * 
   * @param fileName The source code file name
   * @param data The source code file content
   */
  public SourceResolverResults(String fileName, StringBuilder data) {
    this.data = data;
    this.fileName = fileName;
  }

  /**
   * @return string data
   */
  public StringBuilder getFileData() {
    return data;
  }

  /**
   * sets new file data
   * 
   * @param newData new data to set
   */
  public void setFileData(StringBuilder newData) {
    data = newData;
  }

  /**
   * @return exludes
   */
  public SubRangeAggregator getFileExcludes() {
    return excludes;
  }

  /**
   * set the excludes
   * 
   * @param newExcludes excludes to set
   */
  public void setFileExcludes(SubRangeAggregator newExcludes) {
    excludes = newExcludes;
  }

  public String getFileName() {
    return fileName;
  }

}
