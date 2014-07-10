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
package org.sonar.plugins.delphi.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.*;

import org.hamcrest.collection.IsMapContaining;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SonarIndex;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.resources.Resource;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.sonar.plugins.delphi.DelphiSensor;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import com.google.common.collect.Maps;

public class DelphiFileTest {

	private static final String ROOT_NAME = "/org/sonar/plugins/delphi/SimpleDelphiProject";
	private static final java.io.File BASE_DIR = DelphiUtils
			.getResource(ROOT_NAME);
	private static final java.io.File FILE = DelphiUtils.getResource(ROOT_NAME
			+ "/Globals.pas");
	private final List<java.io.File> sourceDirs = Collections
			.unmodifiableList(Arrays.asList(BASE_DIR));
	
	private Project project;

	@Before
	public void setup(){
		project = mock(Project.class);
		ProjectFileSystem pfs = mock(ProjectFileSystem.class);

	    when(project.getFileSystem()).thenReturn(pfs);

	    when(pfs.getBasedir()).thenReturn(BASE_DIR);
	}
	
	private File createFile() {
		return File.fromIOFile(FILE, project);
	}

	private DelphiFile createDelphiFile() {
		return DelphiFile.fromAbsolutePath(FILE.getAbsolutePath(), sourceDirs,
				false);
	}

	@Test
	public void testFullFileName(){
		assertThat(FILE.getAbsolutePath(), equalTo(createDelphiFile().getAbsolutePath()));
	}
	
	@Test
	public void testSameHashCode() {
		DelphiFile delphiFile = createDelphiFile();
		File file = createFile();

		assertNotNull("delphiFile", delphiFile);
		assertNotNull("file", file);

		assertEquals("hashcode", file.hashCode(), delphiFile.hashCode());
	}

	@Test
	public void testToFile() {
		DelphiFile delphiFile = createDelphiFile();
		File file = createFile();

		File newFile = delphiFile.toFile(project);
		
		assertEquals("hashcode", file.hashCode(), newFile.hashCode());
		assertThat(file, equalTo(newFile));
	}

	
	@Test
	public void testHashMapDelphiFile() {
		Map<Resource, String> map = Maps.newHashMap();
		map.put(createFile(), "file");

		assertTrue("file", map.containsKey(createFile()));
		assertTrue("delphiFile", map.containsKey(createDelphiFile().toFile(project)));
	}
}
