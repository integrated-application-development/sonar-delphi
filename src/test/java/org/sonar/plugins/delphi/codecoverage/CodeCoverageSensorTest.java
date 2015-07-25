/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
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
package org.sonar.plugins.delphi.codecoverage;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.resources.Project;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.debug.DebugConfiguration;

public class CodeCoverageSensorTest {

    @Test
    public void shouldExecuteOnProjectTest() {
        Project project = mock(Project.class);
        FileSystem fs = mock(FileSystem.class);
        FilePredicates fp = mock(FilePredicates.class);
        when(fs.predicates()).thenReturn(fp);
        when(fs.hasFiles(org.mockito.Matchers.any(FilePredicate.class))).thenReturn(true);

        fs.hasFiles(fs.predicates().hasLanguage(DelphiLanguage.KEY));
        assertTrue(new CodeCoverageSensor(new DebugConfiguration(), fs).shouldExecuteOnProject(project));
    }
}
