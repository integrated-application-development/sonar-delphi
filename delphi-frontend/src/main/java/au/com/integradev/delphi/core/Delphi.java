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

import com.google.common.base.Splitter;
import java.util.Arrays;
import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;

/** Delphi language implementation */
public class Delphi extends AbstractLanguage {
  /** Delphi key */
  public static final String KEY = "delphi";

  /** Delphi name */
  public static final String NAME = "Delphi";

  /** Key of the file suffix parameter */
  public static final String FILE_SUFFIXES_KEY = "sonar.delphi.file.suffixes";

  /** Default Delphi file suffixes */
  public static final String DEFAULT_FILE_SUFFIXES = ".pas,.dpr,.dpk";

  private final Configuration settings;

  public Delphi(Configuration settings) {
    super(KEY, NAME);
    this.settings = settings;
  }

  @Override
  public String[] getFileSuffixes() {
    String[] suffixes =
        Arrays.stream(settings.getStringArray(Delphi.FILE_SUFFIXES_KEY))
            .filter(s -> s != null && !s.trim().isEmpty())
            .toArray(String[]::new);

    if (suffixes.length == 0) {
      suffixes = Splitter.on(',').splitToList(DEFAULT_FILE_SUFFIXES).toArray(String[]::new);
    }

    return suffixes;
  }
}
