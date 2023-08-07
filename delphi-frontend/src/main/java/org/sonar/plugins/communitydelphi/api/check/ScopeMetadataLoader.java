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
