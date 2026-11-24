/*
 * Sonar Delphi Plugin
 * Copyright (C) 2026 Integrated Application Development
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

import java.util.Set;
import org.sonar.api.scanner.ScannerSide;
import org.sonarsource.api.sonarlint.SonarLintSide;

/** Features provided by the community plugin after negotiation. */
@ScannerSide
@SonarLintSide
public final class DelphiFeatures {
  public enum Feature {
    LANGUAGE_REGISTRATION,
    BUILT_IN_QUALITY_PROFILE,
    FILE_METRICS,
    NCLOC_DATA,
    DUPLICATION_TOKENS,
    SYNTAX_HIGHLIGHTING,
    SYMBOL_PUBLICATION,
    SONAR_RESOLVE,
    COVERAGE_IMPORT,
    TEST_EXECUTION_IMPORT,
    TOOLCHAIN,
    PROJECT_OPTIONS
  }

  private final Set<Feature> disabledFeatures;

  public DelphiFeatures(Set<Feature> disabledFeatures) {
    this.disabledFeatures = Set.copyOf(disabledFeatures);
  }

  public boolean isEnabled(Feature feature) {
    return !disabledFeatures.contains(feature);
  }
}
