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
package org.sonar.plugins.delphi.core;

import org.sonar.api.resources.AbstractLanguage;

/**
 * Class representing Delphi language
 */
public class DelphiLanguage extends AbstractLanguage {

  public static final String KEY = "delph";
  public static DelphiLanguage instance = null;

  private static final String LANGUAGE_NAME = "Delphi";
  private static final String[] FILE_SUFFIXES = { "pas", "dpr", "dpk" };

  /**
   * Default ctor
   */
  public DelphiLanguage() {
    super(KEY, LANGUAGE_NAME);
    instance = this;
  }

  /**
   * @return Delphi source code file suffixes
   */
  @Override
  public String[] getFileSuffixes() {
    return FILE_SUFFIXES;
  }
}