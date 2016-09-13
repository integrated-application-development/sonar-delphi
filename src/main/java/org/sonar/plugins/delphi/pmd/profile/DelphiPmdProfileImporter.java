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
package org.sonar.plugins.delphi.pmd.profile;

import org.apache.commons.io.IOUtils;
import org.sonar.api.profiles.ProfileImporter;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;
import org.sonar.plugins.delphi.pmd.xml.DelphiRulesUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * imports Delphi rules profile from Sonar
 */
public class DelphiPmdProfileImporter extends ProfileImporter {

  /**
   * ctor
   */
  public DelphiPmdProfileImporter() {
    super(DelphiPmdConstants.REPOSITORY_KEY, DelphiPmdConstants.REPOSITORY_NAME);
    setSupportedLanguages(DelphiLanguage.KEY);
  }

  @Override
  public RulesProfile importProfile(Reader reader, ValidationMessages messages) {
    RulesProfile profile = RulesProfile.create();
    try {
      List<ActiveRule> activeRules = DelphiRulesUtils.importConfiguration(IOUtils.toString(reader),
        DelphiRulesUtils.getInitialReferential());
      profile.setActiveRules(activeRules);
    } catch (IOException e) {
      messages.addErrorText(e.getMessage());
    }
    return profile;
  }
}
