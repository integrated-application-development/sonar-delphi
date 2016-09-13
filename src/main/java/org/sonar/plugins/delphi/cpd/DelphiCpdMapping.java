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
package org.sonar.plugins.delphi.cpd;

import net.sourceforge.pmd.cpd.Tokenizer;
import org.sonar.api.batch.CpdMapping;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;

import java.io.File;
import java.lang.annotation.Inherited;
import java.util.List;

/**
 * Mapping for DelphiLanguage language tokens used in CPD analysis
 */
public class DelphiCpdMapping implements CpdMapping {

  private final DelphiProjectHelper delphiProjectHelper;

  public DelphiCpdMapping(DelphiProjectHelper delphiProjectHelper) {
    this.delphiProjectHelper = delphiProjectHelper;
  }

  /**
   * @return The language tokenizer
   */
  @Override
  public Tokenizer getTokenizer() {
    return new DelphiCpdTokenizer(delphiProjectHelper);
  }

  /**
   * {@link Inherited}
   */
  @Override
  public Resource createResource(File file, List<File> sourceDirs) {
    throw new UnsupportedOperationException();
  }

  /**
   * @return Delphi language instance
   */
  @Override
  public Language getLanguage() {
    return DelphiLanguage.instance;
  }

}
