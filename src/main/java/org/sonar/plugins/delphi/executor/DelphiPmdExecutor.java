package org.sonar.plugins.delphi.executor;

import static org.sonar.plugins.delphi.pmd.DelphiPmdConstants.REPOSITORY_KEY;
import static org.sonar.plugins.delphi.pmd.DelphiPmdConstants.TEMPLATE;

import com.google.common.annotations.VisibleForTesting;
import java.io.File;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageRegistry;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.DelphiFile;
import org.sonar.plugins.delphi.pmd.DelphiLanguageModule;
import org.sonar.plugins.delphi.pmd.DelphiPmdConfiguration;
import org.sonar.plugins.delphi.pmd.violation.DelphiPmdViolationRecorder;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleSet;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleSetHelper;

public class DelphiPmdExecutor implements Executor {
  private static final Logger LOG = Loggers.get(DelphiPmdExecutor.class);
  private static final String DYSFUNCTIONAL_RULE = "Removed misconfigured rule: %s  cause: %s";

  private final SensorContext context;
  private final ActiveRules rulesProfile;
  private final DelphiPmdConfiguration pmdConfiguration;
  private final DelphiPmdViolationRecorder violationRecorder;

  private Language language = LanguageRegistry.getLanguage(DelphiLanguageModule.LANGUAGE_NAME);
  private RuleContext ctx = new RuleContext();
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
      DelphiPmdViolationRecorder violationRecorder) {
    this.context = context;
    this.rulesProfile = rulesProfile;
    this.pmdConfiguration = pmdConfiguration;
    this.violationRecorder = violationRecorder;
  }

  @Override
  public void setup() {
    ruleSets = createRuleSets();
  }

  @Override
  public void execute(SensorContext context, DelphiFile delphiFile) {
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
    pmdConfiguration.dumpXmlReport(ctx.getReport());

    for (RuleViolation violation : ctx.getReport()) {
      violationRecorder.saveViolation(violation, context);
    }
  }

  private RuleSets createRuleSets() {
    RuleSets rulesets = new RuleSets();
    String rulesXml = dumpXml(rulesProfile);
    File ruleSetFile = pmdConfiguration.dumpXmlRuleSet(REPOSITORY_KEY, rulesXml);
    RuleSetFactory ruleSetFactory = new RuleSetFactory();

    try {
      RuleSet ruleSet = ruleSetFactory.createRuleSet(ruleSetFile.getAbsolutePath());
      removeTemplateRules(ruleSet);
      removeDysfunctionRules(ruleSet);
      removeUnusedRules(ruleSet);
      rulesets.addRuleSet(ruleSet);
      return rulesets;
    } catch (RuleSetNotFoundException e) {
      throw new IllegalStateException(e);
    }
  }

  private String dumpXml(ActiveRules rulesProfile) {
    final StringWriter writer = new StringWriter();
    final DelphiRuleSet ruleSet = DelphiRuleSetHelper.createFrom(rulesProfile, REPOSITORY_KEY);

    addBuiltinProperties(ruleSet);

    ruleSet.writeTo(writer);

    return writer.toString();
  }

  @VisibleForTesting
  void addBuiltinProperties(DelphiRuleSet ruleSet) {
    for (var rule : ruleSet.getRules()) {
      var definition = pmdConfiguration.getRuleDefinition(rule);
      boolean isCustomTemplateRule = definition.getName().equals(rule.getTemplateName());

      for (DelphiRuleProperty propertyDefinition : definition.getProperties()) {
        if (isCustomTemplateRule && propertyDefinition.isTemplateProperty()) {
          // For custom rules, we extract the properties from the template definition.
          // We don't want the TEMPLATE property on the custom rule.
          continue;
        }

        if (propertyDefinition.isBuiltinProperty()) {
          rule.addProperty(propertyDefinition);
        }
      }
    }
  }

  private static void removeTemplateRules(final RuleSet ruleSet) {
    ruleSet.getRules().removeIf(rule -> rule.getProperty(TEMPLATE));
  }

  private static void removeDysfunctionRules(final RuleSet ruleSet) {
    final Set<Rule> brokenRules = new HashSet<>();
    ruleSet.removeDysfunctionalRules(brokenRules);

    for (final Rule rule : brokenRules) {
      LOG.warn(String.format(DYSFUNCTIONAL_RULE, rule.getName(), rule.dysfunctionReason()));
    }
  }

  private void removeUnusedRules(final RuleSet ruleSet) {
    ruleSet
        .getRules()
        .removeIf(
            rule -> rulesProfile.findByInternalKey(REPOSITORY_KEY, rule.getRuleClass()) == null);
  }
}
