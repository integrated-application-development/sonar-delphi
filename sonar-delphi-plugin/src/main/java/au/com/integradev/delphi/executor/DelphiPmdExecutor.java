/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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

import static au.com.integradev.delphi.pmd.DelphiPmdConstants.REPOSITORY_KEY;
import static au.com.integradev.delphi.pmd.DelphiPmdConstants.SCOPE;
import static au.com.integradev.delphi.pmd.DelphiPmdConstants.TEMPLATE;
import static java.lang.Boolean.TRUE;

import au.com.integradev.delphi.file.DelphiFile.DelphiInputFile;
import au.com.integradev.delphi.pmd.DelphiLanguageModule;
import au.com.integradev.delphi.pmd.DelphiPmdConfiguration;
import au.com.integradev.delphi.pmd.profile.DelphiPmdRuleSetDefinitionProvider;
import au.com.integradev.delphi.pmd.violation.DelphiPmdViolationRecorder;
import au.com.integradev.delphi.pmd.violation.DelphiRuleViolation;
import au.com.integradev.delphi.pmd.xml.DelphiRuleProperty;
import au.com.integradev.delphi.pmd.xml.DelphiRuleSet;
import au.com.integradev.delphi.pmd.xml.DelphiRuleSetHelper;
import com.google.common.annotations.VisibleForTesting;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersion;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class DelphiPmdExecutor implements Executor {
  private static final Logger LOG = Loggers.get(DelphiPmdExecutor.class);
  private final SensorContext sensorContext;
  private final ActiveRules rulesProfile;
  private final DelphiPmdConfiguration pmdConfiguration;
  private final DelphiPmdViolationRecorder violationRecorder;
  private final Language language;
  private final RuleContext ctx;
  private final DelphiPmdRuleSetDefinitionProvider pmdRuleSetDefinitionProvider;
  private RuleSets ruleSets;

  /**
   * Constructor
   *
   * @param context Sensor context
   * @param rulesProfile Profile used to export active rules
   * @param pmdConfiguration Helper class for PMD report/ruleset configuration and manipulation
   * @param violationRecorder Saves PMD violations as sonar issues
   */
  public DelphiPmdExecutor(
      SensorContext context,
      ActiveRules rulesProfile,
      DelphiPmdConfiguration pmdConfiguration,
      DelphiPmdViolationRecorder violationRecorder,
      DelphiPmdRuleSetDefinitionProvider pmdRuleSetDefinitionProvider) {
    this.sensorContext = context;
    this.rulesProfile = rulesProfile;
    this.pmdConfiguration = pmdConfiguration;
    this.violationRecorder = violationRecorder;
    this.pmdRuleSetDefinitionProvider = pmdRuleSetDefinitionProvider;
    this.language = LanguageRegistry.getLanguage("Delphi");
    this.ctx = new RuleContext();
  }

  @Override
  public void setup() {
    ruleSets = createRuleSets();
    for (LanguageVersion version : language.getVersions()) {
      version.getLanguageVersionHandler().getXPathHandler().initialize();
    }
  }

  @Override
  public void execute(Context context, DelphiInputFile delphiFile) {
    ctx.setSourceCodeFile(delphiFile.getSourceCodeFile());
    ctx.setLanguageVersion(language.getDefaultVersion());

    if (ruleSets.applies(ctx.getSourceCodeFile())) {
      try {
        ruleSets.start(ctx);
        ruleSets.apply(Collections.singletonList(delphiFile.getAst()), ctx, language);
      } finally {
        ruleSets.end(ctx);
      }
    }
  }

  @Override
  public void complete() {
    Report report = ctx.getReport();
    pmdConfiguration.dumpXmlReport(report);

    try {
      LOG.info("{} violations found.", report.size());
      for (RuleViolation violation : report) {
        violationRecorder.saveViolation((DelphiRuleViolation) violation, sensorContext);
      }
    } catch (Exception e) {
      throw new FatalExecutorError("Failed to record violations", e);
    }
  }

  @Override
  public Set<Class<? extends Executor>> dependencies() {
    return Set.of(DelphiSymbolTableExecutor.class);
  }

  private RuleSets createRuleSets() {
    RuleSets rulesets = new RuleSets();
    String rulesXml = dumpXml(rulesProfile);
    File ruleSetFile = pmdConfiguration.dumpXmlRuleSet(REPOSITORY_KEY, rulesXml);
    RuleSetFactory ruleSetFactory = new RuleSetFactory();

    try {
      RuleSet ruleSet = ruleSetFactory.createRuleSet(ruleSetFile.getAbsolutePath());
      removeTemplateRules(ruleSet);
      removeDysfunctionalRules(ruleSet);
      removeUnusedRules(ruleSet);
      rulesets.addRuleSet(ruleSet);
      return rulesets;
    } catch (RuleSetNotFoundException e) {
      throw new IllegalStateException(e);
    }
  }

  private String dumpXml(ActiveRules rulesProfile) {
    final StringWriter writer = new StringWriter();
    final DelphiRuleSet ruleSet =
        DelphiRuleSetHelper.createFrom(
            rulesProfile,
            REPOSITORY_KEY,
            sensorContext.runtime().getProduct(),
            pmdRuleSetDefinitionProvider);

    addBuiltinProperties(ruleSet);

    ruleSet.writeTo(writer);

    return writer.toString();
  }

  @VisibleForTesting
  void addBuiltinProperties(DelphiRuleSet ruleSet) {
    for (var rule : ruleSet.getRules()) {
      var definition = pmdConfiguration.getRuleDefinition(rule);
      boolean isCustomTemplateRule = definition.getName().equals(rule.getTemplateName());
      List<DelphiRuleProperty> properties = new ArrayList<>(definition.getProperties());

      // For custom rules, we extract the properties from the template definition.
      if (isCustomTemplateRule) {
        // We don't want the TEMPLATE property on the custom rule.
        properties.removeIf(DelphiRuleProperty::isTemplateProperty);
        // We want to use the SCOPE property from the custom rule if it exists, since it's exposed
        // to the user via the SonarQube web interface.
        if (rule.getProperty(SCOPE.name()) != null) {
          properties.removeIf(DelphiRuleProperty::isScopeProperty);
        }
      }

      properties.stream()
          .filter(DelphiRuleProperty::isBuiltinProperty)
          .filter(property -> !(property.isTemplateProperty() && isCustomTemplateRule))
          .forEach(rule::addProperty);
    }
  }

  private static void removeTemplateRules(final RuleSet ruleSet) {
    ruleSet.getRules().removeIf(rule -> TRUE.equals(rule.getProperty(TEMPLATE)));
  }

  private static void removeDysfunctionalRules(final RuleSet ruleSet) {
    final Set<Rule> brokenRules = new HashSet<>();
    ruleSet.removeDysfunctionalRules(brokenRules);
    brokenRules.forEach(DelphiPmdExecutor::reportDysfunctionReason);
  }

  private static void reportDysfunctionReason(Rule rule) {
    LOG.warn("Removed misconfigured rule: {}  cause: {}", rule.getName(), rule.dysfunctionReason());
  }

  private void removeUnusedRules(final RuleSet ruleSet) {
    ruleSet
        .getRules()
        .removeIf(rule -> rulesProfile.find(RuleKey.of(REPOSITORY_KEY, rule.getName())) == null);
  }
}
