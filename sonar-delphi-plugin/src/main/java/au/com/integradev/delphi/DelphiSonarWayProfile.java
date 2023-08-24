/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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
package au.com.integradev.delphi;

import au.com.integradev.delphi.checks.CheckList;
import au.com.integradev.delphi.core.Delphi;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonarsource.analyzer.commons.BuiltInQualityProfileJsonLoader;

public class DelphiSonarWayProfile implements BuiltInQualityProfilesDefinition {
  private final DelphiSonarWayResourcePath resourcePath;

  public DelphiSonarWayProfile(DelphiSonarWayResourcePath resourcePath) {
    this.resourcePath = resourcePath;
  }

  @Override
  public void define(Context context) {
    NewBuiltInQualityProfile sonarWay =
        context.createBuiltInQualityProfile("Sonar way", Delphi.KEY);

    BuiltInQualityProfileJsonLoader.load(sonarWay, CheckList.REPOSITORY_KEY, resourcePath.get());

    sonarWay.done();
  }
}
