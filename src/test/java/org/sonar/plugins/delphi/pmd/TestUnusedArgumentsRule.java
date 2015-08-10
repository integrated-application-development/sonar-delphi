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
package org.sonar.plugins.delphi.pmd;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.resources.Project;
import org.sonar.plugins.delphi.DelphiTestUtils;
import org.sonar.plugins.delphi.StubIssueBuilder;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.debug.DebugSensorContext;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class TestUnusedArgumentsRule {

    private static final String ROOT_NAME = "/org/sonar/plugins/delphi/PMDTest";
    private static final String TEST_FILE = "/org/sonar/plugins/delphi/PMDTest/UnusedARgumentRule.pas";

    private Project project;
    private DelphiPmdSensor sensor;
    private ResourcePerspectives perspectives;
    private DelphiProjectHelper delphiProjectHelper;
    private Issuable issuable;
    private List<Issue> issues = new LinkedList<Issue>();

    @Before
    public void init() {
        project = mock(Project.class);
        perspectives = mock(ResourcePerspectives.class);
        delphiProjectHelper = DelphiTestUtils.mockProjectHelper();

        // Don't pollute current working directory
        when(delphiProjectHelper.workDir()).thenReturn(new File("target"));

        File baseDir = DelphiUtils.getResource(ROOT_NAME);

        File srcFile = DelphiUtils.getResource(TEST_FILE);

        InputFile inputFile = new DefaultInputFile(ROOT_NAME)
                .setFile(srcFile);

        DelphiProject delphiProject = new DelphiProject("Default Project");
        delphiProject.setSourceFiles(Arrays.asList(inputFile));

        issuable = mock(Issuable.class);

        when(delphiProjectHelper.getWorkgroupProjects()).thenReturn(Arrays.asList(delphiProject));
        when(delphiProjectHelper.getFile(anyString())).thenAnswer(new Answer<InputFile>() {
            @Override
            public InputFile answer(InvocationOnMock invocation) throws Throwable {
                InputFile inputFile = new DefaultInputFile(ROOT_NAME).setFile(new File((String) invocation
                        .getArguments()[0]));

                when(perspectives.as(Issuable.class, inputFile)).thenReturn(issuable);

                when(issuable.newIssueBuilder()).thenReturn(new StubIssueBuilder());

                return inputFile;
            }
        });

        when(issuable.addIssue(Matchers.any(Issue.class))).then(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                Issue issue = (Issue) invocation.getArguments()[0];
                issues.add(issue);
                return Boolean.TRUE;
            }
        });

        sensor = new DelphiPmdSensor(delphiProjectHelper, perspectives);
    }

    @Test
    public void testUnusedArguments() {
        // TODO Create one test per violation

        DebugSensorContext debugContext = new DebugSensorContext();
        sensor.analyse(project, debugContext);

        // all expected rule violations and their lines
        RuleData ruleData[] = {
                new RuleData("Unused Arguments Rule", 31)
        };

        // Sort the violations by line number, so we don't have to add
        // violations order
        Arrays.sort(ruleData, RuleData.getComparator());

        assertThat("number of issues", issues, hasSize(1));

        for (int i = 0; i < issues.size(); ++i) {
            Issue issue = issues.get(i);

            System.out.println(issue.ruleKey().rule() + ":" + issue.line());

            assertThat(ruleData[i].toString(), ruleData[i].getName(), is(issue.ruleKey().rule()));
            assertThat(ruleData[i].toString(), ruleData[i].getLine(), is(issue.line()));
        }
    }
}
