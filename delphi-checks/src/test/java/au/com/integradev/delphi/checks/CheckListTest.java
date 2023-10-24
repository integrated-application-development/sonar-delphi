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

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClass.Predicates;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;

class CheckListTest {
  private static final JavaClasses CHECKS_PACKAGE =
      new ClassFileImporter().importPackages("au.com.integradev.delphi.checks");

  private static final String CHECKS_METADATA_PATH =
      "/org/sonar/l10n/delphi/rules/community-delphi/";

  private static final DescribedPredicate<JavaClass> ARE_DELPHI_CHECKS =
      DescribedPredicate.describe("extend DelphiCheck", Predicates.assignableTo(DelphiCheck.class));

  private static final ArchCondition<JavaClass> BE_IN_THE_CHECK_LIST =
      new ArchCondition<JavaClass>("be in the check list") {
        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
          if (javaClass.getDirectDependenciesToSelf().stream()
              .noneMatch(
                  access ->
                      access
                          .getSourceCodeLocation()
                          .getSourceClass()
                          .isEquivalentTo(CheckList.class))) {
            events.add(
                SimpleConditionEvent.violated(
                    javaClass,
                    String.format("%s is not in the check list", javaClass.getSimpleName())));
          }
        }
      };

  private static final ArchCondition<JavaClass> HAVE_A_RULE_DESCRIPTION =
      new ArchCondition<JavaClass>("have a rule description") {
        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
          Rule rule = javaClass.getAnnotationOfType(Rule.class);
          if (rule == null) {
            events.add(
                SimpleConditionEvent.violated(
                    javaClass,
                    String.format("%s is not annotated with @Rule", javaClass.getSimpleName())));
            return;
          }

          String descHtmlFile = rule.key() + ".html";

          if (missingMetadata(descHtmlFile)) {
            events.add(
                SimpleConditionEvent.violated(
                    javaClass,
                    String.format(
                        "%s does not have an associated %s",
                        javaClass.getSimpleName(), descHtmlFile)));
          }
        }
      };

  private static final ArchCondition<JavaClass> HAVE_RULE_METADATA =
      new ArchCondition<JavaClass>("have rule metadata") {
        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
          Rule rule = javaClass.getAnnotationOfType(Rule.class);
          if (rule == null) {
            events.add(
                SimpleConditionEvent.violated(
                    javaClass,
                    String.format("%s is not annotated with @Rule", javaClass.getSimpleName())));
            return;
          }

          String descJsonFile = rule.key() + ".json";

          if (missingMetadata(descJsonFile)) {
            events.add(
                SimpleConditionEvent.violated(
                    javaClass,
                    String.format(
                        "%s does not have an associated %s",
                        javaClass.getSimpleName(), descJsonFile)));
          }
        }
      };

  private static boolean missingMetadata(String resourceName) {
    return CheckListTest.class.getResource(CHECKS_METADATA_PATH + resourceName) == null;
  }

  @Test
  void testAbstractChecksShouldBeNamedAbstract() {
    classes()
        .that(ARE_DELPHI_CHECKS)
        .and()
        .haveModifier(JavaModifier.ABSTRACT)
        .should()
        .haveSimpleNameStartingWith("Abstract")
        .check(CHECKS_PACKAGE);
  }

  @Test
  void testChecksShouldBeInCheckList() {
    classes()
        .that(ARE_DELPHI_CHECKS)
        .and()
        .doNotHaveModifier(JavaModifier.ABSTRACT)
        .should(BE_IN_THE_CHECK_LIST)
        .check(CHECKS_PACKAGE);
  }

  @Test
  void testChecksShouldHaveMetadata() {
    classes()
        .that(ARE_DELPHI_CHECKS)
        .and()
        .doNotHaveModifier(JavaModifier.ABSTRACT)
        .should(HAVE_A_RULE_DESCRIPTION)
        .andShould(HAVE_RULE_METADATA)
        .check(CHECKS_PACKAGE);
  }

  @Test
  void testChecksShouldHaveRuleAnnotation() {
    classes()
        .that(ARE_DELPHI_CHECKS)
        .and()
        .doNotHaveModifier(JavaModifier.ABSTRACT)
        .should()
        .beAnnotatedWith(Rule.class);
  }

  @Test
  void testCheckListShouldBeSortedAlphabetically() {
    List<String> checkListNames =
        CheckList.getChecks().stream()
            .map(Class::getSimpleName)
            .collect(Collectors.toUnmodifiableList());

    assertThat(checkListNames).isSorted();
  }
}
