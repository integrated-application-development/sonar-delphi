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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
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
    String resourcePath =
        metadataResourcePath.forRepository(engineKey.repository())
            + "/"
            + engineKey.rule()
            + ".json";

    try {
      URL url = classLoader.getResource(resourcePath);
      if (url == null) {
        throw new IOException("Can't read resource: " + resourcePath);
      }
      String data = Resources.toString(url, StandardCharsets.UTF_8);
      JsonObject metadata = JsonParser.parseString(data).getAsJsonObject();
      return Optional.ofNullable(metadata.get("scope"))
          .filter(JsonPrimitive.class::isInstance)
          .map(JsonPrimitive.class::cast)
          .filter(JsonPrimitive::isString)
          .map(JsonElement::getAsString)
          .map(value -> value.toUpperCase(Locale.ROOT))
          .map(scope -> "TESTS".equals(scope) ? "TEST" : scope)
          .map(RuleScope::valueOf)
          .orElse(RuleScope.MAIN);
    } catch (IOException e) {
      throw new IllegalArgumentException("Metadata is missing for check \"" + engineKey + "\"", e);
    } catch (JsonSyntaxException e) {
      throw new IllegalArgumentException("Could not parse JSON", e);
    }
  }
}
