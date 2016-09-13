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

import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.delphi.core.DelphiLanguage;

import java.io.InputStreamReader;
import java.io.Reader;

/**
 * default Delphi rules profile
 */
public class DefaultDelphiProfile extends ProfileDefinition {

  private DelphiPmdProfileImporter importer;

  /**
   * ctor
   * 
   * @param importer delphi pmd profile importer provided by Sonar
   */
  public DefaultDelphiProfile(DelphiPmdProfileImporter importer) {
    this.importer = importer;
  }

  @Override
  public RulesProfile createProfile(ValidationMessages messages) {
    Reader reader = new InputStreamReader(getClass().getResourceAsStream(
      "/org/sonar/plugins/delphi/pmd/default-delphi-profile.xml"));
    RulesProfile profile = importer.importProfile(reader, messages);
    profile.setLanguage(DelphiLanguage.KEY);
    profile.setName("Sonar way");
    profile.setDefaultProfile(Boolean.TRUE);
    return profile;
  }

}
