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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.WildcardPattern;

/**
 * Class representing "package", that is a folder with source files
 */
public class DelphiPackage extends Resource {

  public static final String DEFAULT_PACKAGE_NAME = "[default]";

  /**
   * ctor
   * 
   * @param key
   *          package key
   */
  public DelphiPackage(String key) {
    super();
    setKey(StringUtils.defaultIfEmpty(StringUtils.trim(key), DEFAULT_PACKAGE_NAME));
  }

  @Override
  public boolean matchFilePattern(String antPattern) {
    String patternWithoutFileSuffix = StringUtils.substringBeforeLast(antPattern, ".");
    WildcardPattern matcher = WildcardPattern.create(patternWithoutFileSuffix, ".");
    return matcher.match(getKey());
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public String getScope() {
    return Resource.SCOPE_SPACE;
  }

  @Override
  public String getQualifier() {
    return Resource.QUALIFIER_PACKAGE;
  }

  @Override
  public String getName() {
    return getKey();
  }

  @Override
  public Resource<?> getParent() {
    return null;
  }

  @Override
  public String getLongName() {
    return null;
  }

  @Override
  public Language getLanguage() {
    return DelphiLanguage.instance;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("key", getKey()).toString();
  }

}
