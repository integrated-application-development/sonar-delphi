package org.sonar.plugins.delphi.pmd.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleSet;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleSetHelper;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class DelphiPmdRulesDefinitionTest {

  @Rule public ExpectedException exceptionCatcher = ExpectedException.none();

  @Test
  public void testShouldDefineRules() {
    var provider = mock(DelphiPmdRuleSetDefinitionProvider.class);
    when(provider.getDefinition()).thenReturn(getRuleSet("import_simple.xml"));

    DelphiPmdRulesDefinition definition = new DelphiPmdRulesDefinition(provider);
    RulesDefinition.Context context = new RulesDefinition.Context();
    definition.define(context);
    RulesDefinition.Repository repository = context.repository(DelphiPmdConstants.REPOSITORY_KEY);

    assertThat(repository).isNotNull();
    assertThat(repository.rules()).hasSize(3);
    assertThat(repository.rule("InterfaceNameRule")).isNotNull();
    assertThat(repository.rule("TooManyArgumentsRule")).isNotNull();
    assertThat(repository.rule("TooManyVariablesRule")).isNotNull();
  }

  @Test
  public void testShouldAbortExportOnWriterException() {
    exceptionCatcher.expect(IllegalArgumentException.class);
    exceptionCatcher.expectMessage(
        String.format(DelphiPmdRulesDefinition.UNDEFINED_BASE_EFFORT, "InterfaceNameRule"));

    var provider = mock(DelphiPmdRuleSetDefinitionProvider.class);
    when(provider.getDefinition())
        .thenReturn(getRuleSet("definition_missing_required_property.xml"));

    DelphiPmdRulesDefinition definition = new DelphiPmdRulesDefinition(provider);
    RulesDefinition.Context context = new RulesDefinition.Context();
    definition.define(context);
  }

  private static DelphiRuleSet getRuleSet(String fileName) {
    try {
      File xmlFile = DelphiUtils.getResource("/org/sonar/plugins/delphi/pmd/xml/" + fileName);
      return DelphiRuleSetHelper.createFrom(new FileReader(xmlFile, StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
