/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
package org.sonar.plugins.delphi.coverage;

import java.util.Optional;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.coverage.delphicodecoveragetool.DelphiCodeCoverageToolParser;
import org.sonar.plugins.delphi.msbuild.DelphiProjectHelper;

@ScannerSide
public class DelphiCoverageParserFactory {
  private static final Logger LOG = Loggers.get(DelphiCoverageParserFactory.class);

  public Optional<DelphiCoverageParser> getParser(String key, DelphiProjectHelper helper) {
    if (DelphiCodeCoverageToolParser.KEY.equals(key)) {
      return Optional.of(new DelphiCodeCoverageToolParser(helper));
    } else {
      LOG.warn("Unsupported coverage tool '{}'", key);
      return Optional.empty();
    }
  }
}
