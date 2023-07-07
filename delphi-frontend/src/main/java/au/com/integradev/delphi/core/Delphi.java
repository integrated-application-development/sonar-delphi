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
package au.com.integradev.delphi.core;

import java.util.List;
import org.sonar.api.resources.AbstractLanguage;

/** Delphi language implementation */
public class Delphi extends AbstractLanguage {
  public static final String KEY = "delph";
  public static final String LANGUAGE_NAME = "Delphi";
  public static final List<String> FILE_SUFFIXES = List.of("pas", "dpr", "dpk");

  public Delphi() {
    super(KEY, LANGUAGE_NAME);
  }

  @Override
  public String[] getFileSuffixes() {
    return FILE_SUFFIXES.toArray(new String[0]);
  }
}
