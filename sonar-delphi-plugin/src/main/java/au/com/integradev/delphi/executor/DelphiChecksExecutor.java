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
package au.com.integradev.delphi.executor;

import au.com.integradev.delphi.check.DelphiCheckContextImpl;
import au.com.integradev.delphi.check.MasterCheckRegistrar;
import au.com.integradev.delphi.check.ScopeMetadataLoader;
import au.com.integradev.delphi.file.DelphiFile.DelphiInputFile;
import au.com.integradev.delphi.msbuild.DelphiProjectHelper;
import au.com.integradev.delphi.preprocessor.directive.CompilerDirectiveParserImpl;
import java.util.Set;
import org.sonar.api.batch.fs.InputFile.Type;

import org.sonar.api.rule.RuleScope;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;

public class DelphiChecksExecutor implements Executor {
  private final DelphiProjectHelper delphiProjectHelper;
  private final MasterCheckRegistrar checkRegistrar;
  private final ScopeMetadataLoader scopeMetadataLoader;

  public DelphiChecksExecutor(
      DelphiProjectHelper delphiProjectHelper,
      MasterCheckRegistrar checkRegistrar,
      ScopeMetadataLoader scopeMetadataLoader) {
    this.delphiProjectHelper = delphiProjectHelper;
    this.checkRegistrar = checkRegistrar;
    this.scopeMetadataLoader = scopeMetadataLoader;
  }

  @Override
  public void execute(Context context, DelphiInputFile delphiFile) {
    DelphiCheckContext checkContext =
        new DelphiCheckContextImpl(
            context.sensorContext(),
            delphiFile,
            new CompilerDirectiveParserImpl(delphiProjectHelper.getToolchain().platform),
            checkRegistrar,
            scopeMetadataLoader);

    runChecks(checkContext, RuleScope.ALL);

    runChecks(
        checkContext,
        delphiFile.getInputFile().type() == Type.MAIN ? RuleScope.MAIN : RuleScope.TEST);
  }

  private void runChecks(DelphiCheckContext checkContext, RuleScope scope) {
    checkRegistrar
        .getChecks(scope)
        .forEach(check -> check.visit(checkContext.getAst(), checkContext));
  }

  @Override
  public Set<Class<? extends Executor>> dependencies() {
    return Set.of(DelphiSymbolTableExecutor.class);
  }
}
