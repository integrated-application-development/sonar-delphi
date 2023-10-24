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
package au.com.integradev.delphi.checks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

import au.com.integradev.delphi.check.MetadataResourcePathImpl;
import au.com.integradev.delphi.core.Delphi;
import au.com.integradev.delphi.utils.DelphiUtils;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.Test;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.rule.RuleStatus;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.utils.AnnotationUtils;
import org.sonar.api.utils.Version;
import org.sonar.check.Rule;
import org.sonarsource.analyzer.commons.BuiltInQualityProfileJsonLoader;
import org.sonarsource.analyzer.commons.RuleMetadataLoader;

class CheckMetadataTest {
  private static final Set<String> RULE_KEYS =
      CheckList.getChecks().stream()
          .map(ruleClass -> AnnotationUtils.getAnnotation(ruleClass, Rule.class))
          .filter(Objects::nonNull)
          .map(Rule::key)
          .collect(Collectors.toUnmodifiableSet());

  private static final Set<String> ALLOWED_TAGS =
      Set.of(
          "brain-overload",
          "bad-practice",
          "clumsy",
          "confusing",
          "convention",
          "design",
          "lock-in",
          "pitfall",
          "suspicious",
          "unpredictable",
          "unused",
          "user-experience",
          "tests");

  @Test
  void testJsonMetadataIsNotUnused() {
    assertThat(listJsonMetadata().stream().map(Path::getFileName))
        .isNotEmpty()
        .allSatisfy(
            path ->
                assertThat(RULE_KEYS)
                    .overridingErrorMessage("Unused json metadata: " + path)
                    .contains(FilenameUtils.getBaseName(path.toString())));
  }

  @Test
  void testHtmlMetadataIsNotUnused() {
    assertThat(listHtmlMetadata().stream().map(Path::getFileName))
        .isNotEmpty()
        .allSatisfy(
            path ->
                assertThat(RULE_KEYS)
                    .overridingErrorMessage("Unused html metadata: " + path)
                    .contains(FilenameUtils.getBaseName(path.toString())));
  }

  @Test
  void testRulesShouldHaveTags() throws Exception {
    for (String key : RULE_KEYS) {
      Map<String, Object> metadata = getMetadataForRuleKey(key);

      String status = ((String) metadata.get("status")).toUpperCase();

      if (status.equals("DEPRECATED")) {
        // deprecated rules shouldn't have tags
        continue;
      }

      @SuppressWarnings("unchecked")
      var tags = (List<String>) metadata.get("tags");
      assertThat(tags).as("Rule " + key + " has no tags.").isNotEmpty();
    }
  }

  @Test
  void testRulesShouldOnlyHaveAllowedTags() throws Exception {
    for (String key : RULE_KEYS) {
      Map<String, Object> metadata = getMetadataForRuleKey(key);

      String status = (String) metadata.get("status");
      RuleStatus ruleStatus = EnumUtils.getEnumIgnoreCase(RuleStatus.class, status);

      if (ruleStatus == RuleStatus.DEPRECATED) {
        // deprecated rules shouldn't have tags
        continue;
      }

      @SuppressWarnings("unchecked")
      var tags = (List<String>) metadata.get("tags");
      var unknownTags =
          tags.stream().filter(tag -> !ALLOWED_TAGS.contains(tag)).collect(Collectors.toList());

      assertThat(unknownTags)
          .as("Rule " + key + " has unknown tags (" + StringUtils.join(unknownTags) + ").")
          .isEmpty();
    }
  }

  @Test
  void testRulesTargetingTestsShouldHaveTestsTag() throws Exception {
    for (String key : RULE_KEYS) {
      Map<String, Object> metadata = getMetadataForRuleKey(key);

      String status = (String) metadata.get("status");
      RuleStatus ruleStatus = EnumUtils.getEnumIgnoreCase(RuleStatus.class, status);

      if (ruleStatus == RuleStatus.DEPRECATED) {
        // deprecated rules shouldn't have tags
        continue;
      }

      @SuppressWarnings("unchecked")
      var tags = (List<String>) metadata.get("tags");
      var scope = ((String) metadata.get("scope")).toUpperCase();

      if (Set.of("TEST", "TESTS").contains(scope)) {
        assertThat(tags)
            .as("Rule " + key + " is targeting tests sources and should contain the 'tests' tag.")
            .contains("tests");
      } else {
        assertThat(tags)
            .as("Rule " + key + " is targeting main sources and shouldn't contain the 'tests' tag.")
            .doesNotContain("tests");
      }
    }
  }

  @Test
  void testJsonMetadataHasValidFormat() {
    String resourceFolder = new MetadataResourcePathImpl().forRepository(CheckList.REPOSITORY_KEY);
    SonarRuntime runtime = SonarRuntimeImpl.forSonarLint(Version.create(10, 2));

    var loader = new RuleMetadataLoader(resourceFolder, runtime);
    var context = new RulesDefinition.Context();
    NewRepository newRepository = context.createRepository(CheckList.REPOSITORY_KEY, Delphi.KEY);

    assertThatCode(() -> loader.addRulesByAnnotatedClass(newRepository, CheckList.getChecks()))
        .doesNotThrowAnyException();
  }

  @Test
  void testSonarWayHasValidFormat() {
    assertThatCode(
            () ->
                BuiltInQualityProfileJsonLoader.load(
                    mock(),
                    CheckList.REPOSITORY_KEY,
                    "/"
                        + new MetadataResourcePathImpl().forRepository(CheckList.REPOSITORY_KEY)
                        + "/Sonar_way_profile.json"))
        .doesNotThrowAnyException();
  }

  @Test
  void testSonarWayContainsValidRuleKeys() throws Exception {
    assertThat(getSonarWayRuleKeys())
        .isNotEmpty()
        .allSatisfy(
            ruleKey ->
                assertThat(RULE_KEYS)
                    .overridingErrorMessage("'" + ruleKey + "' was not found in CheckList.")
                    .contains(ruleKey));
  }

  @Test
  void testSonarWayShouldBeSortedAlphabetically() throws Exception {
    assertThat(getSonarWayRuleKeys()).isSorted();
  }

  private static List<Path> listJsonMetadata() {
    Path sonarWay = sonarWayMetadata();
    return listMetadata(".json").stream()
        .filter(path -> !path.equals(sonarWay))
        .collect(Collectors.toList());
  }

  private static List<Path> listHtmlMetadata() {
    return listMetadata(".html");
  }

  private static Path sonarWayMetadata() {
    return listMetadata(".json").stream()
        .filter(path -> path.getFileName().toString().equals("Sonar_way_profile.json"))
        .findFirst()
        .orElseThrow();
  }

  private static List<Path> listMetadata(String extension) {
    String path = "/" + new MetadataResourcePathImpl().forRepository(CheckList.REPOSITORY_KEY);
    return Stream.of(
            DelphiUtils.getResource(path)
                .listFiles((dir, name) -> name.toLowerCase().endsWith(extension)))
        .map(File::toPath)
        .collect(Collectors.toUnmodifiableList());
  }

  private static Map<String, Object> getMetadataForRuleKey(String ruleKey) throws Exception {
    Path metadataPath =
        listJsonMetadata().stream()
            .filter(path -> FilenameUtils.getBaseName(path.toString()).equals(ruleKey))
            .findFirst()
            .orElseThrow();

    String data = Files.readString(metadataPath);

    @SuppressWarnings("unchecked")
    var metadata = (Map<String, Object>) new JSONParser().parse(data);

    return metadata;
  }

  private static List<String> getSonarWayRuleKeys() throws Exception {
    Path sonarWayPath = sonarWayMetadata();

    String data = Files.readString(sonarWayPath);

    @SuppressWarnings("unchecked")
    var metadata = (Map<String, Object>) new JSONParser().parse(data);

    @SuppressWarnings("unchecked")
    var ruleKeys = (List<String>) metadata.get("ruleKeys");

    return ruleKeys;
  }
}
