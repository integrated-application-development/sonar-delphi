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
package au.com.integradev.delphi.pmd.rules;

import org.sonar.plugins.communitydelphi.api.token.DelphiToken;
import au.com.integradev.delphi.pmd.DelphiPmdConstants;
import java.util.regex.Pattern;
import net.sourceforge.pmd.RuleContext;

public class NoSonarRule extends AbstractDelphiRule {
  private static final Pattern PATTERN =
      Pattern.compile(String.format(".*\\b%s\\b.*", DelphiPmdConstants.SUPPRESSION_TAG));

  @Override
  public void visitToken(DelphiToken token, RuleContext data) {
    if (token.isComment() && PATTERN.matcher(token.getImage()).matches()) {
      addViolation(data, token);
    }
  }

  @Override
  public String getMessage() {
    return "Is //NOSONAR used to exclude false-positives or to hide real quality flaw?";
  }

  @Override
  public boolean isSuppressedLine(int line) {
    return false;
  }
}
