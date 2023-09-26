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
package au.com.integradev.delphi.executor;

import au.com.integradev.delphi.file.DelphiFile.DelphiInputFile;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;

public class DelphiNoSonarExecutor implements Executor {
  private static final Pattern PATTERN = Pattern.compile(".*\\bNOSONAR\\b.*");

  private final NoSonarFilter noSonarFilter;

  public DelphiNoSonarExecutor(NoSonarFilter noSonarFilter) {
    this.noSonarFilter = noSonarFilter;
  }

  @Override
  public void execute(Context context, DelphiInputFile delphiFile) {
    Set<Integer> noSonarLines = new HashSet<>();
    for (DelphiToken token : delphiFile.getComments()) {
      if (PATTERN.matcher(token.getImage()).matches()) {
        noSonarLines.add(token.getBeginLine());
      }
    }
    if (!noSonarLines.isEmpty()) {
      noSonarFilter.noSonarInFile(delphiFile.getInputFile(), noSonarLines);
    }
  }
}
