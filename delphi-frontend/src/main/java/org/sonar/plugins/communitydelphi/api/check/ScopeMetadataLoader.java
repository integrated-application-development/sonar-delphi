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
package org.sonar.plugins.communitydelphi.api.check;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rule.RuleScope;

public final class ScopeMetadataLoader {
  private final MetadataResourcePath metadataResourcePath;
  private final ClassLoader classLoader;

  public ScopeMetadataLoader(MetadataResourcePath metadataResourcePath, ClassLoader classLoader) {
    this.metadataResourcePath = metadataResourcePath;
    this.classLoader = classLoader;
  }

  public RuleScope getScope(RuleKey engineKey) {
    URL url = getMetadataURL(engineKey);

    if (url == null) {
      throw new IllegalArgumentException("Metadata is missing for check \"" + engineKey + "\"");
    }

    try {
      String data = Resources.toString(url, StandardCharsets.UTF_8);
      JSONParser parser = new JSONParser();

      @SuppressWarnings("unchecked")
      Map<String, Object> metadata = (Map<String, Object>) parser.parse(data);

      return Optional.ofNullable(metadata.get("scope"))
          .filter(String.class::isInstance)
          .map(value -> ((String) value).toUpperCase(Locale.ROOT))
          .map(scope -> "TESTS".equals(scope) ? "TEST" : scope)
          .map(RuleScope::valueOf)
          .orElse(RuleScope.MAIN);
    } catch (IOException e) {
      throw new IllegalArgumentException("Can't read resource: " + url, e);
    } catch (ParseException e) {
      throw new IllegalArgumentException("Could not parse JSON", e);
    }
  }

  private URL getMetadataURL(RuleKey engineKey) {
    return classLoader.getResource(
        metadataResourcePath.forRepository(engineKey.repository())
            + "/"
            + engineKey.rule()
            + ".json");
  }
}
