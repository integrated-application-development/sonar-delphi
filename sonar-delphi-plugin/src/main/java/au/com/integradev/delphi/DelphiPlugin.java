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
package au.com.integradev.delphi;

import au.com.integradev.delphi.core.DelphiLanguage;
import au.com.integradev.delphi.coverage.DelphiCoverageParserFactory;
import au.com.integradev.delphi.coverage.DelphiCoverageSensor;
import au.com.integradev.delphi.enviroment.DefaultEnvironmentVariableProvider;
import au.com.integradev.delphi.executor.DelphiCpdExecutor;
import au.com.integradev.delphi.executor.DelphiHighlightExecutor;
import au.com.integradev.delphi.executor.DelphiMasterExecutor;
import au.com.integradev.delphi.executor.DelphiMetricsExecutor;
import au.com.integradev.delphi.executor.DelphiPmdExecutor;
import au.com.integradev.delphi.executor.DelphiSymbolTableExecutor;
import au.com.integradev.delphi.msbuild.DelphiProjectHelper;
import au.com.integradev.delphi.nunit.DelphiNUnitSensor;
import au.com.integradev.delphi.pmd.DelphiPmdConfiguration;
import au.com.integradev.delphi.pmd.profile.DefaultDelphiProfile;
import au.com.integradev.delphi.pmd.profile.DelphiPmdProfileExporter;
import au.com.integradev.delphi.pmd.profile.DelphiPmdProfileImporter;
import au.com.integradev.delphi.pmd.profile.DelphiPmdRuleSetDefinitionProvider;
import au.com.integradev.delphi.pmd.profile.DelphiPmdRulesDefinition;
import au.com.integradev.delphi.pmd.violation.DelphiPmdViolationRecorder;
import com.google.common.collect.ImmutableList;
import org.sonar.api.Plugin;
import org.sonar.api.SonarProduct;

/** Main Sonar DelphiLanguage plugin class */
public class DelphiPlugin implements Plugin {
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  @Override
  public void define(Context context) {
    ImmutableList.Builder<Object> builder = ImmutableList.builder();

    builder.addAll(DelphiProperties.getProperties());

    builder.add(
        // Sensors
        DelphiSensor.class,
        // Executors
        DelphiMasterExecutor.class,
        DelphiSymbolTableExecutor.class,
        DelphiPmdExecutor.class,
        // Core
        DelphiLanguage.class,
        // Core helpers
        DelphiProjectHelper.class,
        // PMD
        DelphiPmdConfiguration.class,
        DelphiPmdRulesDefinition.class,
        DelphiPmdRuleSetDefinitionProvider.class,
        DefaultDelphiProfile.class,
        DelphiPmdProfileExporter.class,
        DelphiPmdProfileImporter.class,
        DelphiPmdViolationRecorder.class,
        // Environment
        DefaultEnvironmentVariableProvider.class);

    if (context.getRuntime().getProduct() == SonarProduct.SONARQUBE) {
      builder.add(
          // Sensors
          DelphiCoverageSensor.class,
          DelphiNUnitSensor.class,
          // Executors
          DelphiCpdExecutor.class,
          DelphiHighlightExecutor.class,
          DelphiMetricsExecutor.class,
          // Core helpers
          DelphiCoverageParserFactory.class);
    }

    context.addExtensions(builder.build());
  }
}
