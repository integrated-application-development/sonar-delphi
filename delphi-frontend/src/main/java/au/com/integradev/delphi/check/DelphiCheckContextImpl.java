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
package au.com.integradev.delphi.check;

import au.com.integradev.delphi.file.DelphiFile.DelphiInputFile;
import au.com.integradev.delphi.preprocessor.CompilerSwitchRegistry;
import au.com.integradev.delphi.reporting.DelphiIssueBuilderImpl;
import java.util.List;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.directive.CompilerDirectiveParser;
import org.sonar.plugins.communitydelphi.api.reporting.DelphiIssueBuilder;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

public class DelphiCheckContextImpl implements DelphiCheckContext {
  private final DelphiCheck check;
  private final SensorContext sensorContext;
  private final DelphiInputFile delphiFile;
  private final CompilerDirectiveParser compilerDirectiveParser;
  private final MasterCheckRegistrar checkRegistrar;

  public DelphiCheckContextImpl(
      DelphiCheck check,
      SensorContext sensorContext,
      DelphiInputFile delphiFile,
      CompilerDirectiveParser compilerDirectiveParser,
      MasterCheckRegistrar checkRegistrar) {
    this.check = check;
    this.sensorContext = sensorContext;
    this.delphiFile = delphiFile;
    this.compilerDirectiveParser = compilerDirectiveParser;
    this.checkRegistrar = checkRegistrar;
  }

  @Override
  public DelphiAst getAst() {
    return delphiFile.getAst();
  }

  @Override
  public List<DelphiToken> getTokens() {
    return delphiFile.getTokens();
  }

  @Override
  public List<String> getFileLines() {
    return delphiFile.getSourceCodeFilesLines();
  }

  @Override
  public CompilerSwitchRegistry getCompilerSwitchRegistry() {
    return delphiFile.getCompilerSwitchRegistry();
  }

  @Override
  public CompilerDirectiveParser getCompilerDirectiveParser() {
    return compilerDirectiveParser;
  }

  @Override
  public TypeFactory getTypeFactory() {
    return delphiFile.getTypeFactory();
  }

  @Override
  public DelphiIssueBuilder newIssue() {
    return new DelphiIssueBuilderImpl(check, sensorContext, delphiFile, checkRegistrar);
  }
}
