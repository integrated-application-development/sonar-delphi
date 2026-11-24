/*
 * Sonar Delphi Plugin
 * Copyright (C) 2026 Integrated Application Development
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
package au.com.integradev.delphi;

import au.com.integradev.delphi.check.MetadataResourcePathImpl;
import au.com.integradev.delphi.checks.CheckList;
import au.com.integradev.delphi.core.DelphiFeatures;
import au.com.integradev.delphi.core.DelphiFeatures.Feature;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.api.Plugin;
import org.sonar.api.rule.RuleKey;
import org.sonarsource.analyzer.commons.BuiltInQualityProfileJsonLoader;

final class DelphiIntegration {
  private static final String API_CLASS_NAME =
      "com.sonarsource.plugins.dre.api.v1.SonarDelphiExtensionApi";

  private DelphiIntegration() {}

  static DelphiFeatures integrate(Plugin.Context context, ClassLoader classLoader) {
    try {
      var apiClass = Class.forName(API_CLASS_NAME, false, classLoader);
      var method = apiClass.getMethod("integrate", Plugin.Context.class, Collection.class);
      if (!Modifier.isStatic(method.getModifiers()) || method.getReturnType() != Set.class) {
        throw new NoSuchMethodException(
            "Expected public static Set integrate(Context, Collection)");
      }

      Object result = method.invoke(null, context, defaultRuleKeys());
      if (!(result instanceof Set<?>)) {
        throw new IllegalStateException(
            "The Sonar Delphi integration API returned an invalid result");
      }

      Set<Feature> disabledFeatures = EnumSet.noneOf(Feature.class);
      for (Object value : (Set<?>) result) {
        // Older DRE versions return NOSONAR. Community checks need the marks before DRE runs.
        if (!"NOSONAR".equals(value)) {
          disabledFeatures.add(toFeature(value));
        }
      }
      return new DelphiFeatures(disabledFeatures);
    } catch (ClassNotFoundException e) {
      return new DelphiFeatures(Set.of());
    } catch (InvocationTargetException e) {
      throw new IllegalStateException(
          "The Sonar Delphi integration API rejected the integration", e.getCause());
    } catch (ReflectiveOperationException | LinkageError | SecurityException e) {
      throw new IllegalStateException("Cannot call the Sonar Delphi integration API", e);
    }
  }

  private static Feature toFeature(Object value) {
    if (!(value instanceof String)) {
      throw new IllegalStateException(
          "The Sonar Delphi integration API returned a non-string feature");
    }
    String name = (String) value;
    try {
      return Feature.valueOf(name);
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException("Unknown Sonar Delphi feature: " + name, e);
    }
  }

  private static Set<RuleKey> defaultRuleKeys() {
    var resourcePath = new DelphiSonarWayResourcePath(new MetadataResourcePathImpl());
    return BuiltInQualityProfileJsonLoader.loadActiveKeysFromJsonProfile(resourcePath.get())
        .stream()
        // The commercial rule takes precedence in the joint profile.
        .filter(key -> !key.equals("CognitiveComplexityRoutine"))
        .map(key -> RuleKey.of(CheckList.REPOSITORY_KEY, key))
        .collect(Collectors.toUnmodifiableSet());
  }
}
