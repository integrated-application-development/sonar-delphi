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
import au.com.integradev.delphi.compiler.Platform;
import au.com.integradev.delphi.file.DelphiFile.DelphiInputFile;
import au.com.integradev.delphi.msbuild.DelphiProjectHelper;
import au.com.integradev.delphi.preprocessor.directive.CompilerDirectiveParserImpl;
import java.util.Set;
import java.util.function.Function;
import org.sonar.api.SonarProduct;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.rule.RuleScope;
import org.sonar.api.utils.AnnotationUtils;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.SonarLintUnsupported;
import org.sonar.plugins.communitydelphi.api.directive.CompilerDirectiveParser;

public class DelphiChecksExecutor implements Executor {
  private final DelphiProjectHelper delphiProjectHelper;
  private final MasterCheckRegistrar checkRegistrar;
  private final ScopeMetadataLoader scopeMetadataLoader;
  private final SonarRuntime sonarRuntime;

  public DelphiChecksExecutor(
      DelphiProjectHelper delphiProjectHelper,
      MasterCheckRegistrar checkRegistrar,
      ScopeMetadataLoader scopeMetadataLoader,
      SonarRuntime sonarRuntime) {
    this.delphiProjectHelper = delphiProjectHelper;
    this.checkRegistrar = checkRegistrar;
    this.scopeMetadataLoader = scopeMetadataLoader;
    this.sonarRuntime = sonarRuntime;
  }

  @Override
  public void execute(Context context, DelphiInputFile delphiFile) {
    Platform platform = delphiProjectHelper.getToolchain().platform;
    CompilerDirectiveParser compilerDirectiveParser = new CompilerDirectiveParserImpl(platform);
    Function<DelphiCheck, DelphiCheckContext> createCheckContext =
        check ->
            new DelphiCheckContextImpl(
                check,
                context.sensorContext(),
                delphiFile,
                compilerDirectiveParser,
                checkRegistrar,
                scopeMetadataLoader);

    runChecks(RuleScope.ALL, createCheckContext);
    // Main files may contain test code.
    runChecks(RuleScope.TEST, createCheckContext);
    // Test files do not contain main code.
    if (delphiFile.getInputFile().type() != InputFile.Type.TEST) {
      runChecks(RuleScope.MAIN, createCheckContext);
    }
  }

  private void runChecks(
      RuleScope scope, Function<DelphiCheck, DelphiCheckContext> createCheckContext) {
    checkRegistrar.getChecks(scope).stream()
        .filter(this::isCheckSupportedOnPlatform)
        .forEach(
            check -> {
              DelphiCheckContext context = createCheckContext.apply(check);
              check.start(context);
              check.visit(context.getAst(), context);
              check.end(context);
            });
  }

  private boolean isCheckSupportedOnPlatform(DelphiCheck check) {
    return sonarRuntime.getProduct() == SonarProduct.SONARQUBE
        || AnnotationUtils.getAnnotation(check, SonarLintUnsupported.class) == null;
  }

  @Override
  public Set<Class<? extends Executor>> dependencies() {
    return Set.of(DelphiSymbolTableExecutor.class, DelphiNoSonarExecutor.class);
  }
}
