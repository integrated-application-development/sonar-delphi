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
import java.util.List;

import net.sourceforge.pmd.cpd.Tokenizer;

import org.sonar.api.batch.CpdMapping;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.delphi.core.DelphiFile;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;

/**
 * Mapping for DelphiLanguage language tokens used in CPD analysis
 */
public class DelphiCpdMapping implements CpdMapping {

  private Project project;

  /**
   * Ctor
   * 
   * @param project
   *          Sonar DelphiLanguage project
   */
  public DelphiCpdMapping(Project project) {
    this.project = project;
  }

  /**
   * @return The language tokenizer
   */

  public Tokenizer getTokenizer() {
    ProjectFileSystem fileSystem = project.getFileSystem();
    return new DelphiCpdTokenizer(fileSystem, DelphiProjectHelper.getInstance().getExcludedSources(fileSystem));
  }

  /**
   * Creates DelphiFile
   * 
   * @return DelphiFile
   */

  public Resource createResource(File file, List<File> sourceDirs) {
    return DelphiFile.fromIOFile(file, sourceDirs);
  }

  /**
   * @return Delphi language instance
   */

  public Language getLanguage() {
    return DelphiLanguage.instance;
  }

}
