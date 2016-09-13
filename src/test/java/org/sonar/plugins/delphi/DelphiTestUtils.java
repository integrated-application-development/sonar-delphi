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
package org.sonar.plugins.delphi;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;

import java.io.File;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DelphiTestUtils {

  public static DelphiProjectHelper mockProjectHelper() {
    DelphiProjectHelper mock = mock(DelphiProjectHelper.class);
    when(mock.shouldExecuteOnProject()).thenReturn(true);

    when(mock.getFile(any(File.class))).thenAnswer(new Answer<InputFile>() {

      @Override
      public InputFile answer(InvocationOnMock invocation) throws Throwable {
        File file = (File) invocation.getArguments()[0];
        InputFile inputFile = new DefaultInputFile("ROOT_KEY_CHANGE_AT_SONARAPI_5",file.getAbsolutePath());

        return inputFile;
      }
    });

    return mock;
  }

  public static void mockGetFileFromString(DelphiProjectHelper mock) {
    when(mock.getFile(any(String.class))).thenAnswer(new Answer<InputFile>() {
      @Override
      public InputFile answer(InvocationOnMock invocation) throws Throwable {
        String fileName = (String) invocation.getArguments()[0];

        File file = new File(fileName);
        InputFile inputFile = new DefaultInputFile("ROOT_KEY_CHANGE_AT_SONARAPI_5",file.getPath());

        return inputFile;
      }
    });
  }

}
