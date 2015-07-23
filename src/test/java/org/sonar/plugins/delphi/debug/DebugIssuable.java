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
package org.sonar.plugins.delphi.debug;

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.component.Component;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;

public class DebugIssuable implements Issuable {

	private List<Issue> issues = new ArrayList<Issue>();

	@Override
	public Component component() {
		return null;
	}

	@Override
	public IssueBuilder newIssueBuilder() {
		return null;
	}

	@Override
	public boolean addIssue(Issue issue) {
		return this.issues.add(issue);
	}

	@Override
	public List<Issue> issues() {
		return this.issues ;
	}

	@Override
	public List<Issue> resolvedIssues() {
		return this.issues;
	}
	

}
