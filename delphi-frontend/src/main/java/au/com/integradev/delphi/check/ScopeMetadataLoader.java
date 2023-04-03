package au.com.integradev.delphi.check;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sonar.api.rule.RuleScope;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.utils.AnnotationUtils;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonarsource.api.sonarlint.SonarLintSide;

@ScannerSide
@SonarLintSide
public class ScopeMetadataLoader {
  private final MetadataResourcePathSupplier metadataResourcePathSupplier;
  private final Map<Class<? extends DelphiCheck>, RuleScope> checkClassToScope;

  public ScopeMetadataLoader(MetadataResourcePathSupplier metadataResourcePathSupplier) {
    this.metadataResourcePathSupplier = metadataResourcePathSupplier;
    checkClassToScope = new HashMap<>();
  }

  public RuleScope getScope(Class<? extends DelphiCheck> checkClass) {
    return checkClassToScope.computeIfAbsent(checkClass, this::readScope);
  }

  private RuleScope readScope(Class<? extends DelphiCheck> checkClass) {
    String ruleKey = getRuleKey(checkClass);
    URL url = getMetadataURL(ruleKey);

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

  private static String getRuleKey(Class<? extends DelphiCheck> checkClass) {
    Rule ruleAnnotation = AnnotationUtils.getAnnotation(checkClass, Rule.class);
    if (ruleAnnotation == null) {
      throw new IllegalStateException("No Rule annotation was found on " + checkClass.getName());
    }
    return ruleAnnotation.key();
  }

  private URL getMetadataURL(String ruleKey) {
    return Thread.currentThread()
        .getContextClassLoader()
        .getResource(metadataResourcePathSupplier.get() + "/" + ruleKey + ".json");
  }
}
