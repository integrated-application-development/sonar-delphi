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

import org.sonar.plugins.delphi.antlr.sanitizer.SourceResolver;

/**
 * Fixes source file: appends '\n' to the end, adds whitespaces when needed. All
 * that in order to ANTLR parser could work correctly.
 */
public class SourceFixerResolver extends SourceResolver {

  @Override
  protected void doResolve(SourceResolverResults results) {
    results.setFileData(fixSource(results.getFileData()));
  }

  /**
   * Fixes source
   * 
   * @param fileData file character data to fix
   * @return String containing fixed source code
   */
  private StringBuilder fixSource(StringBuilder fileData) {
    String fixed = fileData.toString();
    // replace ':' with ' :'
    //fixed = fixed.replaceAll(":", " :");
    // replace '..' with ' .. '
    //fixed = fixed.replaceAll("\\.\\.", " .. ");
    // adds '\n' before EOF
    fixed = fixed.concat("\n");
    return new StringBuilder(fixed);
  }

}
