/*
 * Sonar Delphi Plugin
 * Copyright (C) 2024 Integrated Application Development
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

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMember;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates;
import com.tngtech.archunit.core.domain.properties.HasName;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;

class CheckTestNameTest {

  private static final JavaClasses CHECKS_PACKAGE =
      new ClassFileImporter().importPackages("au.com.integradev.delphi.checks");

  private static final DescribedPredicate<JavaMethod> VERIFY_ISSUES =
      callCheckVerifierMethod("verifyIssues")
          .or(callCheckVerifierMethod("verifyIssueOnFile"))
          .or(callCheckVerifierMethod("verifyIssueOnProject"));
  private static final DescribedPredicate<JavaMethod> VERIFY_NO_ISSUES =
      callMethod("au.com.integradev.delphi.checks.verifier.CheckVerifier.verifyNoIssues");
  private static final DescribedPredicate<JavaMethod> CALL_ASSERT_THROW_BY =
      callMethod("org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy");
  private static final DescribedPredicate<JavaMethod> CALL_ASSERT_NO_EXCEPTION =
      callMethod("org.assertj.core.api.AssertionsForClassTypes.assertThatNoException");
  private static final String IMPLEMENTATION_DETAIL_PREFIX = "testImplementationDetail";
  private static final DescribedPredicate<HasName> TESTING_IMPLEMENTATION_DETAILS =
      HasName.Predicates.nameStartingWith(IMPLEMENTATION_DETAIL_PREFIX);

  private static final List<Class<?>> METATEST_CLASSES =
      List.of(CheckListTest.class, CheckMetadataTest.class, CheckTestNameTest.class);
  private static final DescribedPredicate<JavaMember> DECLARED_IN_METATESTS =
      new DescribedPredicate<>("declared in meta test classes") {
        @Override
        public boolean test(JavaMember member) {
          return METATEST_CLASSES.contains(member.getOwner().reflect());
        }
      };

  private static final ArchCondition<JavaClass> HAVE_ASSOCIATED_CHECK =
      new ArchCondition<>("have an associated check") {
        @Override
        public void check(JavaClass item, ConditionEvents events) {
          String subjectName = item.getName().replaceAll("Test$", "");
          boolean hasSubject =
              CheckList.getChecks().stream().map(Class::getName).anyMatch(subjectName::equals);

          if (!hasSubject) {
            String message =
                String.format(
                    "%s does not have an associated subject %s", item.getFullName(), subjectName);
            events.add(SimpleConditionEvent.violated(item, message));
          }
        }
      };
  private static final ArchCondition<JavaClass> HAVE_ASSOCIATED_TEST =
      new ArchCondition<>("have an associated test") {
        @Override
        public void check(JavaClass item, ConditionEvents events) {
          String testName = item.getName() + "Test";
          boolean hasTest =
              item.getConstructorCallsToSelf().stream()
                  .anyMatch(
                      constructorCall ->
                          constructorCall.getOriginOwner().getName().equals(testName));

          if (!hasTest) {
            String message =
                String.format(
                    "%s does not have an associated test %s", item.getFullName(), testName);
            events.add(SimpleConditionEvent.violated(item, message));
          }
        }
      };

  static DescribedPredicate<JavaMethod> callMethod(String methodName) {
    return new DescribedPredicate<>("call " + methodName) {
      @Override
      public boolean test(JavaMethod method) {
        String methodPrefix = String.format("%s(", methodName);
        return method.getCallsFromSelf().stream()
            .anyMatch(call -> call.getTarget().getFullName().startsWith(methodPrefix));
      }
    };
  }

  static DescribedPredicate<JavaMethod> callCheckVerifierMethod(String methodName) {
    return callMethod(
        String.format("au.com.integradev.delphi.checks.verifier.CheckVerifier.%s", methodName));
  }

  @Test
  void testCheckTestsVerifyingIssuesAreNamedCorrectly() {
    methods()
        .that(VERIFY_ISSUES)
        .and(not(TESTING_IMPLEMENTATION_DETAILS))
        .and(
            DescribedPredicate.or(
                Predicates.annotatedWith(Test.class),
                Predicates.annotatedWith(ParameterizedTest.class)))
        .should()
        .haveNameMatching(".*ShouldAdd(Issues?|QuickFix(es)?)$")
        .allowEmptyShould(true)
        .check(CHECKS_PACKAGE);
  }

  @Test
  void testCheckTestsVerifyingNoIssuesAreNamedCorrectly() {
    methods()
        .that(VERIFY_NO_ISSUES)
        .and(not(TESTING_IMPLEMENTATION_DETAILS))
        .and(not(CALL_ASSERT_THROW_BY))
        .and(
            DescribedPredicate.or(
                Predicates.annotatedWith(Test.class),
                Predicates.annotatedWith(ParameterizedTest.class)))
        .should()
        .haveNameMatching(".*ShouldNotAddIssues?$")
        .allowEmptyShould(true)
        .check(CHECKS_PACKAGE);
  }

  @Test
  void testCheckTestsShouldThrowAreNamedCorrectly() {
    methods()
        .that(CALL_ASSERT_THROW_BY)
        .and(not(TESTING_IMPLEMENTATION_DETAILS))
        .should()
        .haveNameMatching(".*ShouldThrow$")
        .allowEmptyShould(true)
        .check(CHECKS_PACKAGE);
  }

  @Test
  void testCheckTestsShouldNotThrowAreNamedCorrectly() {
    methods()
        .that(CALL_ASSERT_NO_EXCEPTION)
        .and(not(TESTING_IMPLEMENTATION_DETAILS))
        .should()
        .haveNameMatching(".*ShouldNotThrow$")
        .allowEmptyShould(true)
        .check(CHECKS_PACKAGE);
  }

  @Test
  void testCheckTestsShouldBeNamedCorrectly() {
    methods()
        .that(
            DescribedPredicate.or(
                Predicates.annotatedWith(Test.class),
                Predicates.annotatedWith(ParameterizedTest.class)))
        .and(not(DECLARED_IN_METATESTS))
        .should()
        .haveNameMatching(".*Should((Not)?(Throw|Add(Issues?|QuickFix(es)?)))")
        .orShould()
        .haveNameMatching(IMPLEMENTATION_DETAIL_PREFIX + ".*")
        .allowEmptyShould(true)
        .check(CHECKS_PACKAGE);
  }

  @Test
  void testCheckTestsHaveAnAssociatedCheck() {
    classes()
        .that()
        .haveSimpleNameEndingWith("Test")
        .and()
        .doNotBelongToAnyOf(CheckListTest.class, CheckMetadataTest.class, CheckTestNameTest.class)
        .should(HAVE_ASSOCIATED_CHECK)
        .allowEmptyShould(true)
        .check(CHECKS_PACKAGE);
  }

  @Test
  void testChecksHaveAnAssociatedTest() {
    classes()
        .that()
        .areAssignableTo(DelphiCheck.class)
        .and()
        .doNotHaveModifier(JavaModifier.ABSTRACT)
        .should(HAVE_ASSOCIATED_TEST)
        .allowEmptyShould(true)
        .check(CHECKS_PACKAGE);
  }
}
