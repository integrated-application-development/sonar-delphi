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
package au.com.integradev.delphi.executor;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.DelphiProperties;
import au.com.integradev.delphi.compiler.Platform;
import au.com.integradev.delphi.core.Delphi;
import au.com.integradev.delphi.file.DelphiFile.DelphiInputFile;
import au.com.integradev.delphi.file.DelphiFileConfig;
import au.com.integradev.delphi.preprocessor.DelphiPreprocessorFactory;
import au.com.integradev.delphi.preprocessor.search.SearchPath;
import au.com.integradev.delphi.symbol.SymbolTable;
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.IssueResolution;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

class DelphiIssueResolutionExecutorTest {
  private static final RuleKey RULE_ONE = RuleKey.of("community-delphi", "RuleOne");
  private static final RuleKey RULE_TWO = RuleKey.of("community-delphi", "RuleTwo");

  @TempDir private Path baseDir;

  private SensorContextTester sensorContext;
  private ExecutorContext context;
  private DelphiIssueResolutionExecutor executor;

  @BeforeEach
  void setup() {
    sensorContext = SensorContextTester.create(baseDir);
    context = new ExecutorContext(sensorContext, mock(SymbolTable.class));
    executor = new DelphiIssueResolutionExecutor();
    setActiveRules(RULE_ONE, RULE_TWO);
  }

  @Test
  void testLineCommentDirectivePublishesIssueResolution() {
    DelphiInputFile delphiFile =
        createDelphiFile(
            "SingleLine.pas",
            "unit SingleLine;\n"
                + "interface\n"
                + "implementation\n"
                + "// sonar-resolve [fp] community-delphi:RuleOne \"Reviewed false positive\"\n"
                + "end.");

    executor.execute(context, delphiFile);

    assertThat(issueResolutions())
        .singleElement()
        .satisfies(
            resolution -> {
              assertThat(resolution.status()).isEqualTo(IssueResolution.Status.FALSE_POSITIVE);
              assertThat(resolution.ruleKeys()).containsExactly(RULE_ONE);
              assertThat(resolution.comment()).isEqualTo("Reviewed false positive");
              assertThat(resolution.inputFile()).isEqualTo(delphiFile.getInputFile());
              assertThat(resolution.textRange().start().line()).isEqualTo(4);
              assertThat(resolution.textRange().end().line()).isEqualTo(4);
            });
  }

  @Test
  void testBraceCommentDirectivePublishesIssueResolution() {
    DelphiInputFile delphiFile =
        createDelphiFile(
            "BraceComment.pas",
            "unit BraceComment;\n"
                + "interface\n"
                + "implementation\n"
                + "{ sonar-resolve [fp] community-delphi:RuleOne \"Reviewed false positive\" }\n"
                + "end.");

    executor.execute(context, delphiFile);

    assertThat(issueResolutions())
        .singleElement()
        .satisfies(
            resolution -> {
              assertThat(resolution.status()).isEqualTo(IssueResolution.Status.FALSE_POSITIVE);
              assertThat(resolution.ruleKeys()).containsExactly(RULE_ONE);
              assertThat(resolution.comment()).isEqualTo("Reviewed false positive");
              assertThat(resolution.textRange().start().line()).isEqualTo(4);
              assertThat(resolution.textRange().end().line()).isEqualTo(4);
            });
  }

  @Test
  void testMultilineBlockDirectiveUsesKeywordLine() {
    DelphiInputFile delphiFile =
        createDelphiFile(
            "MultiLine.pas",
            "unit MultiLine;\n"
                + "interface\n"
                + "implementation\n"
                + "(*\n"
                + "  sonar-resolve [accept] community-delphi:RuleOne,\n"
                + "  community-delphi:RuleTwo\n"
                + "  \"Reviewed manually\"\n"
                + "*)\n"
                + "end.");

    executor.execute(context, delphiFile);

    assertThat(issueResolutions())
        .singleElement()
        .satisfies(
            resolution -> {
              assertThat(resolution.status()).isEqualTo(IssueResolution.Status.DEFAULT);
              assertThat(resolution.ruleKeys()).containsExactlyInAnyOrder(RULE_ONE, RULE_TWO);
              assertThat(resolution.comment()).isEqualTo("Reviewed manually");
              assertThat(resolution.textRange().start().line()).isEqualTo(5);
              assertThat(resolution.textRange().end().line()).isEqualTo(5);
            });
  }

  @Test
  void testMultilineBraceDirectiveUsesKeywordLine() {
    DelphiInputFile delphiFile =
        createDelphiFile(
            "MultiLineBrace.pas",
            "unit MultiLineBrace;\n"
                + "interface\n"
                + "implementation\n"
                + "{\n"
                + "  sonar-resolve [accept] community-delphi:RuleOne,\n"
                + "  community-delphi:RuleTwo\n"
                + "  \"Reviewed manually\"\n"
                + "}\n"
                + "end.");

    executor.execute(context, delphiFile);

    assertThat(issueResolutions())
        .singleElement()
        .satisfies(
            resolution -> {
              assertThat(resolution.status()).isEqualTo(IssueResolution.Status.DEFAULT);
              assertThat(resolution.ruleKeys()).containsExactlyInAnyOrder(RULE_ONE, RULE_TWO);
              assertThat(resolution.comment()).isEqualTo("Reviewed manually");
              assertThat(resolution.textRange().start().line()).isEqualTo(5);
              assertThat(resolution.textRange().end().line()).isEqualTo(5);
            });
  }

  @Test
  void testMultipleDirectivesInOneCommentPublishEachResolution() {
    DelphiInputFile delphiFile =
        createDelphiFile(
            "MultipleDirectives.pas",
            "unit MultipleDirectives;\n"
                + "interface\n"
                + "implementation\n"
                + "(*\n"
                + "  sonar-resolve [fp] community-delphi:RuleOne \"First\"\n"
                + "  sonar-resolve [accept] community-delphi:RuleTwo \"Second\"\n"
                + "*)\n"
                + "end.");

    executor.execute(context, delphiFile);

    assertThat(issueResolutions())
        .extracting(IssueResolution::ruleKeys, resolution -> resolution.textRange().start().line())
        .containsExactlyInAnyOrder(tuple(Set.of(RULE_ONE), 5), tuple(Set.of(RULE_TWO), 6));
  }

  @Test
  void testDirectiveSpanningConsecutiveLineCommentsPublishesIssueResolution() {
    DelphiInputFile delphiFile =
        createDelphiFile(
            "LineCommentRun.pas",
            "unit LineCommentRun;\n"
                + "interface\n"
                + "implementation\n"
                + "// sonar-resolve [accept] community-delphi:RuleOne,\n"
                + "//   community-delphi:RuleTwo\n"
                + "//   \"Reviewed by team\"\n"
                + "end.");

    executor.execute(context, delphiFile);

    assertThat(issueResolutions())
        .singleElement()
        .satisfies(
            resolution -> {
              assertThat(resolution.status()).isEqualTo(IssueResolution.Status.DEFAULT);
              assertThat(resolution.ruleKeys()).containsExactlyInAnyOrder(RULE_ONE, RULE_TWO);
              assertThat(resolution.comment()).isEqualTo("Reviewed by team");
              assertThat(resolution.textRange().start().line()).isEqualTo(4);
              assertThat(resolution.textRange().end().line()).isEqualTo(4);
            });
  }

  @Test
  void testConsecutiveDirectiveLineCommentsPublishEachResolution() {
    DelphiInputFile delphiFile =
        createDelphiFile(
            "ConsecutiveDirectives.pas",
            "unit ConsecutiveDirectives;\n"
                + "interface\n"
                + "implementation\n"
                + "// sonar-resolve [fp] community-delphi:RuleOne \"First\"\n"
                + "// sonar-resolve [fp] community-delphi:RuleTwo \"Second\"\n"
                + "end.");

    executor.execute(context, delphiFile);

    assertThat(issueResolutions())
        .extracting(IssueResolution::ruleKeys, resolution -> resolution.textRange().start().line())
        .containsExactlyInAnyOrder(tuple(Set.of(RULE_ONE), 4), tuple(Set.of(RULE_TWO), 5));
  }

  @Test
  void testNonAdjacentLineCommentsAreNotJoined() {
    DelphiInputFile delphiFile =
        createDelphiFile(
            "NonAdjacent.pas",
            "unit NonAdjacent;\n"
                + "interface\n"
                + "implementation\n"
                + "// sonar-resolve [accept] community-delphi:RuleOne,\n"
                + "\n"
                + "// community-delphi:RuleTwo \"Reviewed by team\"\n"
                + "end.");

    executor.execute(context, delphiFile);

    assertThat(issueResolutions()).isEmpty();
  }

  @Test
  void testUnterminatedJustificationDoesNotConsumeNextDirective() {
    DelphiInputFile delphiFile =
        createDelphiFile(
            "Unterminated.pas",
            "unit Unterminated;\n"
                + "interface\n"
                + "implementation\n"
                + "{\n"
                + "  sonar-resolve [fp] community-delphi:RuleOne \"Unterminated\n"
                + "  sonar-resolve [fp] community-delphi:RuleTwo \"Legit\"\n"
                + "}\n"
                + "end.");

    executor.execute(context, delphiFile);

    assertThat(issueResolutions())
        .singleElement()
        .satisfies(
            resolution -> {
              assertThat(resolution.ruleKeys()).containsExactly(RULE_TWO);
              assertThat(resolution.comment()).isEqualTo("Legit");
              assertThat(resolution.textRange().start().line()).isEqualTo(6);
            });
  }

  @Test
  void testInvalidDirectiveIsIgnored() {
    DelphiInputFile delphiFile =
        createDelphiFile(
            "Invalid.pas",
            "unit Invalid;\n"
                + "interface\n"
                + "implementation\n"
                + "// sonar-resolve community-delphi:RuleOne\n"
                + "end.");

    executor.execute(context, delphiFile);

    assertThat(issueResolutions()).isEmpty();
  }

  @Test
  void testKeywordEmbeddedInWordIsIgnored() {
    DelphiInputFile delphiFile =
        createDelphiFile(
            "EmbeddedKeyword.pas",
            "unit EmbeddedKeyword;\n"
                + "interface\n"
                + "implementation\n"
                + "// xsonar-resolve [fp] community-delphi:RuleOne \"Ignored\"\n"
                + "end.");

    executor.execute(context, delphiFile);

    assertThat(issueResolutions()).isEmpty();
  }

  @Test
  void testProseMentionOfKeywordIsIgnored() {
    DelphiInputFile delphiFile =
        createDelphiFile(
            "ProseMention.pas",
            "unit ProseMention;\n"
                + "interface\n"
                + "implementation\n"
                + "// see the sonar-resolve feature for details\n"
                + "end.");

    executor.execute(context, delphiFile);

    assertThat(issueResolutions()).isEmpty();
  }

  private void setActiveRules(RuleKey... ruleKeys) {
    ActiveRulesBuilder builder = new ActiveRulesBuilder();
    for (RuleKey ruleKey : ruleKeys) {
      builder.addRule(
          new NewActiveRule.Builder()
              .setRuleKey(ruleKey)
              .setName(ruleKey.toString())
              .setSeverity("MAJOR")
              .setLanguage(Delphi.KEY)
              .build());
    }
    sensorContext.setActiveRules(builder.build());
  }

  private List<IssueResolution> issueResolutions() {
    return sensorContext.getIssueResolutions().values().stream()
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  private DelphiInputFile createDelphiFile(String fileName, String contents) {
    Path filePath;
    try {
      filePath = Files.writeString(baseDir.resolve(fileName), contents, UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    InputFile inputFile =
        TestInputFileBuilder.create("moduleKey", baseDir.toFile(), filePath.toFile())
            .setContents(contents)
            .setLanguage(Delphi.KEY)
            .setType(InputFile.Type.MAIN)
            .build();
    sensorContext.fileSystem().add(inputFile);
    return DelphiInputFile.from(inputFile, fileConfig());
  }

  private static DelphiFileConfig fileConfig() {
    TypeFactory typeFactory =
        new TypeFactoryImpl(
            DelphiProperties.COMPILER_TOOLCHAIN_DEFAULT, DelphiProperties.COMPILER_VERSION_DEFAULT);
    DelphiFileConfig config = mock(DelphiFileConfig.class);
    when(config.getCharset()).thenReturn(UTF_8);
    when(config.getPreprocessorFactory())
        .thenReturn(
            new DelphiPreprocessorFactory(
                DelphiProperties.COMPILER_VERSION_DEFAULT, Platform.WINDOWS));
    when(config.getTypeFactory()).thenReturn(typeFactory);
    when(config.getSearchPath()).thenReturn(SearchPath.create(Collections.emptyList()));
    when(config.getDefinitions()).thenReturn(Collections.emptySet());
    return config;
  }
}
