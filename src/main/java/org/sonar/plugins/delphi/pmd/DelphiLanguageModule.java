/*
 * Sonar Delphi Plugin
 * Author(s):
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

import net.sourceforge.pmd.lang.BaseLanguageModule;
import org.sonar.plugins.delphi.core.DelphiLanguage;

public class DelphiLanguageModule extends BaseLanguageModule {

  public static final String LANGUAGE_NAME = "Delphi";
  private static final String TERSE_NAME = DelphiLanguage.KEY;

  public DelphiLanguageModule() {
    super(LANGUAGE_NAME, null, TERSE_NAME, DelphiRuleChainVisitor.class, "pas", "dpr", "dpk");
    // Delphi 10.2
    addVersion("32", new DelphiLanguageHandler(), true);
  }
}
