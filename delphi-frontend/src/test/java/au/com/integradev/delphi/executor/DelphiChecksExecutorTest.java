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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.check.MasterCheckRegistrar;
import au.com.integradev.delphi.compiler.Toolchain;
import au.com.integradev.delphi.file.DelphiFile.DelphiInputFile;
import au.com.integradev.delphi.msbuild.DelphiProjectHelper;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.SonarProduct;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.rule.RuleScope;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;

class DelphiChecksExecutorTest {
  private DelphiChecksExecutor executor;
  private MasterCheckRegistrar checkRegistrar;

  @BeforeEach
  void setup() {
    DelphiProjectHelper delphiProjectHelper = mock();
    when(delphiProjectHelper.getToolchain()).thenReturn(Toolchain.DCC32);

    checkRegistrar = mock();

    SonarRuntime sonarRuntime = mock();
    when(sonarRuntime.getProduct()).thenReturn(SonarProduct.SONARQUBE);

    executor = new DelphiChecksExecutor(delphiProjectHelper, checkRegistrar, mock(), sonarRuntime);
    executor.setup();
  }

  @Test
  void testExecute() {
    Executor.Context context = mock();

    DelphiCheck mainCheck = mockDelphiCheck(RuleScope.MAIN);
    DelphiCheck testCheck = mockDelphiCheck(RuleScope.TEST);
    DelphiCheck allCheck = mockDelphiCheck(RuleScope.ALL);

    DelphiInputFile mainFile = mockDelphiFile(InputFile.Type.MAIN);
    DelphiInputFile testFile = mockDelphiFile(InputFile.Type.TEST);

    executor.execute(context, mainFile);
    executor.execute(context, testFile);

    verify(mainCheck, times(1)).visit(eq(mainFile.getAst()), any());
    verify(mainCheck, never()).visit(eq(testFile.getAst()), any());

    verify(testCheck, never()).visit(eq(mainFile.getAst()), any());
    verify(testCheck, times(1)).visit(eq(testFile.getAst()), any());

    verify(allCheck, times(1)).visit(eq(mainFile.getAst()), any());
    verify(allCheck, times(1)).visit(eq(testFile.getAst()), any());
  }

  private DelphiCheck mockDelphiCheck(RuleScope scope) {
    DelphiCheck check = mock();
    when(checkRegistrar.getChecks(scope)).thenReturn(Set.of(check));
    return check;
  }

  private static DelphiInputFile mockDelphiFile(InputFile.Type inputFileType) {
    DelphiAst ast = mock();

    InputFile inputFile = mock();
    when(inputFile.type()).thenReturn(inputFileType);

    DelphiInputFile file = mock();
    when(file.getAst()).thenReturn(ast);
    when(file.getInputFile()).thenReturn(inputFile);

    return file;
  }
}
