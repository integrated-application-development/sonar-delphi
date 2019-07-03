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

import java.io.Writer;
import org.sonar.api.profiles.ProfileExporter;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.server.ServerSide;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleSet;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleSetHelper;

/**
 * ServerSide component that is able to export all currently active PMD rules as XML.
 */
@ServerSide
public class DelphiPmdProfileExporter extends ProfileExporter {

  private static final String CONTENT_TYPE_APPLICATION_XML = "application/xml";

  public DelphiPmdProfileExporter() {
    super(DelphiPmdConstants.REPOSITORY_KEY, DelphiPmdConstants.REPOSITORY_NAME);
    setSupportedLanguages(DelphiLanguage.KEY);
    setMimeType(CONTENT_TYPE_APPLICATION_XML);
  }

  @Override
  public void exportProfile(RulesProfile profile, Writer writer) {
    DelphiRuleSet tree = DelphiRuleSetHelper.createFrom(profile, DelphiPmdConstants.REPOSITORY_KEY);

    try {
      tree.writeTo(writer);
    } catch (IllegalStateException e) {
      throw new IllegalStateException("An exception occurred while generating the PMD "
          + "configuration file from profile: " + profile.getName(), e);
    }
  }
}
