package org.sonar.plugins.delphi.pmd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.config.Configuration;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.pmd.profile.DelphiPmdRuleSetDefinitionProvider;
import org.sonar.plugins.delphi.pmd.xml.DelphiRule;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleSet;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class DelphiPmdExecutorTest {

  private static final String ROOT_PATH = "/org/sonar/plugins/delphi";
  private static final File ROOT_DIR = DelphiUtils.getResource(ROOT_PATH);
  private static final String BAD_SYNTAX = ROOT_PATH + "/projects/BadSyntaxProject/BadSyntax.Pas";

  private DelphiProjectHelper projectHelper;
  private DelphiPmdExecutor executor;
  private DelphiPmdConfiguration pmdConfiguration;

  @Rule public ExpectedException exceptionCatcher = ExpectedException.none();

  @Before
  public void setup() {
    projectHelper = mock(DelphiProjectHelper.class);
    when(projectHelper.getProjects()).thenReturn(Collections.emptyList());

    ActiveRules rulesProfile = mock(ActiveRules.class);

    DefaultFileSystem fileSystem = new DefaultFileSystem(ROOT_DIR).setWorkDir(ROOT_DIR.toPath());
    Configuration configuration = mock(Configuration.class);
    DelphiPmdRuleSetDefinitionProvider provider = new DelphiPmdRuleSetDefinitionProvider();

    pmdConfiguration = spy(new DelphiPmdConfiguration(fileSystem, configuration, provider));

    executor = spy(new DelphiPmdExecutor(projectHelper, rulesProfile, pmdConfiguration));
  }

  @Test
  public void testNonexistentRuleSetIllegalState() {
    File badFile = mock(File.class);
    when(badFile.getAbsolutePath()).thenReturn("does/not/exist.xml");
    when(pmdConfiguration.dumpXmlRuleSet(anyString(), anyString())).thenReturn(badFile);

    exceptionCatcher.expect(IllegalStateException.class);
    executor.execute();
  }

  @Test
  public void testAddBuiltinProperties() {
    // The SwallowedExceptionsRule has 3 builtin properties: BASE_EFFORT, SCOPE and TYPE
    DelphiRule rule = new DelphiRule();
    rule.setName("SwallowedExceptionsRule");
    rule.setClazz("org.sonar.plugins.delphi.pmd.rules.SwallowedExceptionsRule");
    rule.setPriority(2);

    DelphiRuleSet ruleSet = new DelphiRuleSet();
    ruleSet.addRule(rule);

    executor.addBuiltinProperties(ruleSet);

    assertThat(rule.getProperties(), hasSize(3));
    assertThat(rule.getProperty(DelphiPmdConstants.BASE_EFFORT), is(not(nullValue())));
    assertThat(rule.getProperty(DelphiPmdConstants.SCOPE), is(not(nullValue())));
    assertThat(rule.getProperty(DelphiPmdConstants.TYPE), is(not(nullValue())));
  }

  @Test
  public void testAddBuiltinPropertiesToCustomRule() {
    // A custom template rule, created via the SonarQube web interface
    // There's no plugin-side rule definition and no builtin properties to add.
    DelphiRule rule = new DelphiRule();
    rule.setName("SomeCustomXPathRule");
    rule.setClazz(DelphiPmdConstants.TEMPLATE_XPATH_CLASS);
    rule.setPriority(2);

    DelphiRuleSet ruleSet = new DelphiRuleSet();
    ruleSet.addRule(rule);

    executor.addBuiltinProperties(ruleSet);

    assertThat(rule.getProperties(), empty());
  }

  @Test
  public void testParsingExceptionShouldAddError() {
    DelphiProject project = new DelphiProject("Test");
    project.addFile(DelphiUtils.getResource(BAD_SYNTAX).getPath());

    when(projectHelper.getProjects()).thenReturn(Collections.singletonList(project));
    when(projectHelper.isExcluded(any(File.class))).thenReturn(false);

    executor.execute();

    assertThat(executor.getErrors(), hasSize(1));
  }
}
