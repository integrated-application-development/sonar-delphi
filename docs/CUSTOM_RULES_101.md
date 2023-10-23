Writing Custom Delphi Rules 101
==========

If you are using SonarQube along with the SonarDelphi Analyzer to analyze your projects, you might
find that certain specific requirements of your company cannot be addressed by the existing rules.
In such cases, the most suitable option would be to develop your own custom Delphi rules.

This document introduces custom rule writing for the SonarDelphi Analyzer.
It will cover all the main concepts of static analysis required to understand and develop effective
rules, relying on the API provided by the SonarDelphi Analyzer.

## Content

* [Getting Started](#getting-started)
    * [Looking at the pom](#looking-at-the-pom)
* [Writing a rule](#writing-a-rule)
    * [Two files to forge a rule](#two-files-to-forge-a-rule)
    * [A specification to make it right](#a-specification-to-make-it-right)
    * [A test class to rule them all](#a-test-class-to-rule-them-all)
    * [First version: Using AST Nodes and API basics](#first-version-using-ast-nodes-and-api-basics)
    * [Second version: Using semantic API](#second-version-using-semantic-api)
    * [What you can use, and what you can't](#what-you-can-use-and-what-you-cant)
* [Registering the rule in the custom plugin](#registering-the-rule-in-the-custom-plugin)
    * [Rule Metadata](#rule-metadata)
    * [Rule Activation](#rule-activation)
    * [Rule Registrar](#rule-registrar)
* [Testing a custom plugin](#testing-a-custom-plugin)
    * [How to define rule parameters](#how-to-define-rule-parameters)
* [References](#references)

## Getting started

The rules you develop will be delivered using a dedicated, custom plugin, relying on the
**SonarDelphi Analyzer**. In order to start working efficiently, we provide a template Maven project
that you will fill in while following this tutorial.

Grab the template project by:

* cloning [this repository](https://github.com/integrated-application-development/sonar-delphi)
* importing the
[delphi-custom-rules-example](https://github.com/integrated-application-development/sonar-delphi/tree/master/docs/delphi-custom-rules-example)
sub-module in your IDE

This project already contains examples of custom rules. Our goal will be to add an extra rule!

### Looking at the POM

A custom plugin is a Maven project, and before diving into code, it is important to notice a few
relevant lines related to the configuration of your soon-to-be-released custom plugin.
The root of a Maven project is a file named `pom.xml`.

Tags such as `<groupId>`, `<artifactId>`, `<version>`, `<name>`, and `<description>` can be freely
modified.

```xml
<groupId>au.com.integradev.samples</groupId>
<artifactId>delphi-custom-rules-example</artifactId>
<version>1.0.0-SNAPSHOT</version>
<name>SonarDelphi :: Documentation :: Custom Rules Example</name>
<description>Delphi Custom Rules Example for SonarQube</description>
```

In the code snippet below, there's a couple of important configuration properties to note:
* `<pluginClass>` provides the **entry point of the plugin**. You must change this configuration if
you rename or move the class implementing `org.sonar.api.Plugin`.
* `<pluginApiMinVersion>` guarantees compatibility with the plugin API version you target.

```xml
<plugin>
  <groupId>org.sonarsource.sonar-packaging-maven-plugin</groupId>
  <artifactId>sonar-packaging-maven-plugin</artifactId>
  <version>${sonar.packaging.plugin.version}</version>
  <extensions>true</extensions>
  <configuration>
    <pluginKey>customdelphi</pluginKey>
    <pluginName>Delphi Custom Rules</pluginName>
    <pluginClass>au.com.integradev.samples.delphi.MyDelphiRulesPlugin</pluginClass>
    <skipDependenciesPackaging>true</skipDependenciesPackaging>
    <pluginApiMinVersion>9.14.0.375</pluginApiMinVersion>
    <requirePlugins>communitydelphi:${sonar.delphi.version}</requirePlugins>
  </configuration>
</plugin>
```

> [!NOTE]
> `9.14.0.375` is the latest plugin API version supported by SonarQube 9.9.

## Writing a rule

In this section, we will write a custom rule from scratch.
To do so, we will use
a [Test Driven Development](https://en.wikipedia.org/wiki/Test-driven_development) (TDD) approach,
relying on writing some test cases first, followed by the implementation of a solution.

### Two files to forge a rule

When implementing a rule, there is always a minimum of two distinct files to create:

1. A test class, which contains the rule's unit tests.
2. A rule class, which contains the implementation of the rule.

To create our first custom rule (usually called a "*check*"), let's start by creating these two
files in the template project, as described below:

1. In package `au.com.integradev.samples.delphi.checks` of `/src/test/java`, create a new test class
   called `MyFirstCustomCheckTest` and copy-paste the content of the following code snippet.

```java
package au.com.integradev.samples.delphi.checks;

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;

class MyFirstCustomCheckTest {
  // TODO: tests
}
```

2. In package `au.com.integradev.samples.delphi.checks` of `/src/main/java`, create a new class
   called `MyFirstCustomCheck` extending
   class `org.sonar.plugins.communitydelphi.api.check.DelphiCheck` provided by the Delphi Plugin
   API.
   This file will be described when dealing with the implementation of the rule!

```java
package au.com.integradev.samples.delphi.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;

@Rule(key = "MyFirstCustomRule")
public class MyFirstCustomCheck extends DelphiCheck {

}
```

>
> Question: **More files...**
>
> It is also possible to use external files to describe rule metadata, such as a description in HTML
> format.
> This will be described in other topics of this documentation.
>

### A specification to make it right

Of course, before going any further, we need a key element in rule writing: a specification!

For the sake of the exercise, let's consider the following quote from a famous Guru as being the
specification of our custom rule, as it is of course absolutely correct and incontrovertible.

>
> **Gandalf - Why Program When Magic Rulez (WPWMR, p.42)**
>
> *“For a method having a single parameter, the types of its return value and its parameter should
never be the same.”*
>

### A test class to rule them all

Because we chose a TDD approach, the first thing to do is to write examples of the code our rule
will target.
In these examples, we consider numerous cases that our rule may encounter during an analysis.

In the test file `MyFirstCustomCheckTest.java` created earlier, copy-paste the following code:

```java
package au.com.integradev.samples.delphi.checks;

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;

class MyFirstCustomCheckTest {

  @Test
  void testConstructorShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MyFirstCustomCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("    constructor Create(Foo: TFoo);")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testFunctionWithoutParameterShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MyFirstCustomCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("    function Bar: TFoo; ")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testProcedureShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MyFirstCustomCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("    procedure Bar(Foo: TFoo); ")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testFunctionReturningSameTypeAsParameterShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MyFirstCustomCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("    function Bar(Foo: TFoo): TFoo; // Noncompliant")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testFunctionReturningDifferentTypeFromParameterShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MyFirstCustomCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("    function Bar(Foo: TFoo): Integer; ")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testFunctionWithMultipleParametersShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MyFirstCustomCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("    function Bar(Foo: TFoo; Baz: Integer): TFoo; ")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }
}
```

The test file now contains the following test cases, the purpose of which is to verify the behavior of the rule we
are going to implement:

* A constructor
* A function without parameter
* A procedure
* A function returning the same type as its parameter, which will be noncompliant
* A function with a single parameter, but a different return type
* A method with more than 1 parameter

To do so, it relies on the usage of the `CheckVerifier` class, provided by the Delphi Analyzer
rule-testing API.
This `CheckVerifier` class provides useful methods to validate rule implementations, allowing us to
totally abstract all the mechanisms related to analyzer initialization.

Now, let's proceed to the next step of TDD: make the tests fail!

To do so, simply execute the tests using JUnit.
A test should **fail** with the error message "**At least one issue expected**", as shown in the
code snippet below.
Since our check is not yet implemented, no issue can be raised yet, so that's the expected behavior.

```
java.lang.AssertionError: Issues were expected at [7]
	at au.com.integradev.delphi.checks.verifier.CheckVerifierImpl.verifyIssuesOnLinesInternal(CheckVerifierImpl.java:195)
	at au.com.integradev.samples.delphi.checks.MyFirstCustomCheck.testFunctionReturningSameTypeAsParameterShouldAddIssue(MyFirstCustomCheckTest.java:57)
    ...
```

### First version: Using AST Nodes and API basics

Before we start with the implementation of the rule itself, a little background is needed.

Prior to running any rule, the SonarDelphi Analyzer parses a given Delphi code file and produces an
equivalent data structure: the **Abstract Syntax Tree**.
Each construction of the Delphi language can be represented with a specific kind of AST node,
detailing each of its particularities.
For instance, the node associated with the declaration of a method is defined by
the `org.sonar.plugins.communitydelphi.api.ast.MethodDeclarationNode` interface.

When creating a new rule class, we extend the `DelphiCheck` class from the API.
The `DelphiCheck` class exposes a `visit` method for every node that can appear in the AST.

Now it's finally time to jump into the implementation of our first rule!
Go back to the `MyFirstCustomCheck` class, and override the `visit` method
for `MethodDeclarationNode`.

```java
@Override
public DelphiCheckContext visit(MethodDeclarationNode method, DelphiCheckContext context) {

}
```

Now, let's narrow the focus of the rule by checking that the method has a single parameter. We'll
raise an issue in that case.

```java
@Override
public DelphiCheckContext visit(MethodDeclarationNode method, DelphiCheckContext context) {
  if (method.getParameters().size() == 1) {
    reportIssue(context, method.getMethodNameNode(), "Never do that!");
  }
  return super.visit(method, context);
}
```

The method `reportIssue(DelphiCheckContext context, DelphiNode node, String message)`
from `DelphiCheck` allows reporting an issue on a given node with a specific message.
In this case, we chose to report the issue at a precise location, which will be the name of the
method.

Now, let's test our implementation by executing our tests again.

```
java.lang.AssertionError: No issues expected but got 1 issues:
--> 'Never do that!' in Test.pas:7
	at au.com.integradev.delphi.checks.verifier.CheckVerifierImpl.verifyNoIssues(CheckVerifierImpl.java:169)
	at au.com.integradev.samples.delphi.checks.MyFirstCustomCheck.testProcedureShouldNotAddIssue(MyFirstCustomCheckTest.java:44)
    ...
```

The `CheckVerifier` reported that line 7 raised an unexpected issue, as visible in the stack trace
above.

By looking back at our test case, it's easy to figure out that raising an issue here is wrong
because the method has no return type, meaning it cannot match the parameter type.
Raising these issues is correct according to our implementation, as we didn't check for the types of
the parameter and return type.

To handle types, however, we will need to rely on more than what we can achieve using only knowledge
of the AST.
This time, we will need to use the semantic API!

### Second version: Using semantic API

Up to now, our rule implementation only relied on the data provided directly by the AST that
resulted from the parsing of the code.
However, the SonarDelphi Analyzer provides a lot more regarding the code being analyzed, because it
also constructs a ***semantic model*** of the code.
This semantic model provides information related to each ***symbol*** being manipulated.
For a method, for instance, the semantic API will provide useful data such as a method's declaring
type, its usages, the types of its parameters and its return type, etc.

But now, let's go back to our implementation and take advantage of the semantics.

Once we know that our method has a single parameter, let's start by getting the type of the method's
first parameter using `MethodDeclarationNode::getParameterTypes`. (You may have to
import `org.sonar.plugins.communitydelphi.api.type.Type`)

```java
@Override
public DelphiCheckContext visit(MethodDeclarationNode method, DelphiCheckContext context) {
  if (method.getParameters().size() == 1) {
    Type parameterType = method.getParameterTypes().get(0);
    reportIssue(context, method.getMethodNameNode(), "Never do that!");
  }
  return super.visit(method, context);
}
```

Next, let's get the return type of the method using `MethodDeclarationNode::getReturnType`.

```java
@Override
public DelphiCheckContext visit(MethodDeclarationNode method, DelphiCheckContext context) {
  if (method.getParameters().size() == 1) {
    Type parameterType = method.getParameterTypes().get(0);
    Type returnType = method.getReturnType();
    reportIssue(context, method.getMethodNameNode(), "Never do that!");
  }
  return super.visit(method, context);
}
```

Since the rule should only raise an issue when these two types are the same, we then simply test if
the return type is the same as the type of the first parameter using the `Type.is(Type type)`
method, before raising the issue.

```java
@Override
public DelphiCheckContext visit(MethodDeclarationNode method, DelphiCheckContext context) {
  if (method.getParameters().size() == 1) {
    Type parameterType = method.getParameterTypes().get(0);
    Type returnType = method.getReturnType();
    if (parameterType.is(returnType)) {
      reportIssue(context, method.getMethodNameNode(), "Never do that!");
    }
  }
  return super.visit(method, context);
}
```

Now, run the tests again.

Test passed? If not, then check if you missed a step.

If it passed...

>
> :tada: **Congratulations!** :confetti_ball:
>
> *You implemented your first custom rule for the SonarDelphi Analyzer!*
>

### What you can use, and what you can't

When writing custom Delphi rules, you can only use classes from
package [org.sonar.plugins.communitydelphi.api](https://github.com/integrated-application-development/sonar-delphi/tree/master/delphi-frontend/src/main/java/org/sonar/plugins/communitydelphi/api).

When browsing the existing rules from the SonarDelphi Analyzer, you will sometime notice the use of
some other utility classes which are not part of the API.
While these classes could be useful in your context, **these classes are not available at runtime**
for custom rule plugins.
It means that, while your unit tests are still going to pass when building your plugin, your rules
will most likely make analysis **crash at analysis time**.

Feel free to reach out through GitHub Discussions to suggest features and API improvements!

## Registering the rule in the custom plugin

You are probably quite happy at this point, as our first rule is running as expected.

However, we are not really done yet.
Before running our rule against any real projects, we have to register it within the custom plugin.

### Rule Metadata

The first thing to do is provide our rule with all the metadata which will allow us to register it
properly in the SonarQube platform.
There are two ways to add metadata for your rule:

* annotations
* static documentation

While annotations provide a handy way to document the rule, static documentation offers the
possibility for richer information.
Incidentally, static documentation is also the way rules in the core `sonar-delphi` plugin are
described.

To provide metadata for your rule, you need to create:

* an HTML file (where you can provide an extended textual description of the rule)
* a JSON file (with the actual metadata)

In the case of `MyFirstCustomRule`, you will head to
the `src/main/resources/org/sonar/l10n/delphi/rules/mycompany-delphi/` folder to
create `MyFirstCustomRule.html` and `MyFirstCustomRule.json`.

We first need to populate the HTML file with some information that will help developers fix the
issue.

```html
<p>For a method having a single parameter, the types of its return value and its parameter should
  never be the same.</p>

<h2>Noncompliant Code Example</h2>
<pre>
type
  TFoo = class
    function Bar(Foo: TFoo): TFoo; // Noncompliant
  end;
</pre>

<h2>Compliant Solution</h2>
<pre>
type
  TFoo = class
    function Bar(): TFoo; // Compliant
    function Baz(Foo: TFoo): TBaz; // Compliant
  end;
</pre>
```

We can now add metadata
to `src/main/resources/org/sonar/l10n/delphi/rules/mycompany-delphi/MyFirstCustomRule.json`:

```json
{
  "title": "Return type and parameter of a method should not be the same",
  "type": "BUG",
  "status": "ready",
  "tags": [
    "bugs",
    "gandalf",
    "magic"
  ],
  "defaultSeverity": "Critical",
  "scope": "MAIN"
}
```

With this example, we have a concise but descriptive `title` for our rule, the `type` of an issue it
highlights, its `status` (ready or deprecated), the `tags` that should bring it up in a search,
the `severity` of the issue, and the `scope` of the rule.

### Rule Activation

The second thing to do is to activate the rule within the plugin.
To do so, open the class `RulesList` (`au.com.integradev.samples.delphi.RulesList`).

In this class, you will notice the `getChecks()` method.
This method is used to register our rules alongside the rules of the core SonarDelphi plugin.

To register the rule, simply add the rule class to the list, as in the following code snippet:

```java
private static final List<Class<?extends DelphiCheck>> ALL_CHECKS =
    List.of(
    // other rules...
    MyFirstCustomCheck.class);
```

### Rule Registrar

Because your rules are relying on the Delphi Plugin API, you also need to tell the core SonarDelphi
plugin that some new rules have to be retrieved.

If you are using the template custom plugin for this tutorial, you should have everything
done already, but feel free to have a look at the `MyDelphiFileCheckRegistrar.java` class, which
connects the dots.

Finally, be sure that this registrar class is also correctly added as an extension in your Plugin
definition class (`MyDelphiRulesPlugin.java`).

```java
/**
 * Provide the "checks" (implementations of rules) classes that are going to be executed during
 * source code analysis.
 *
 * <p>This class is a batch extension by implementing the {@link
 * org.sonar.plugins.communitydelphi.api.check.CheckRegistrar} interface.
 */
public class MyDelphiFileCheckRegistrar implements CheckRegistrar {
  private final MetadataResourcePath metadataResourcePath;

  public MyDelphiFileCheckRegistrar(MetadataResourcePath metadataResourcePath) {
    this.metadataResourcePath = metadataResourcePath;
  }

  /** Register the classes that will be used to instantiate checks during analysis. */
  @Override
  public void register(RegistrarContext registrarContext) {
    // The core plugin needs to know the scope for each rule (ALL, MAIN, TEST)
    // The ScopeMetadataLoader class can load the rule scope from the JSON rule metadata.
    ScopeMetadataLoader scopeMetadataLoader =
        new ScopeMetadataLoader(metadataResourcePath, getClass().getClassLoader());

    // Associate the classes with the correct repository key and scope.
    registrarContext.registerClassesForRepository(
        RulesList.REPOSITORY_KEY, RulesList.getChecks(), scopeMetadataLoader::getScope);
  }
}
```

### Rules repository

With the actions taken above, your rule is activated, registered, and should be ready to test.
But before doing so, you may want to customize the repository name your rule belongs to.

This repository's key and name are defined in `MyDelphiRulesDefinition.java` and can be customized
to suit your needs.

>
> :warning: **Note**
>
> When you change the repository key, you must also change the metadata resource path accordingly.
>
> For example, if you changed the repository key to `fellowship-inc`, then your metadata path would
> become `org/sonar/l10n/delphi/rules/fellowship-inc`.
>

```java
public class MyDelphiRulesDefinition implements RulesDefinition {
  // ...
  public static final String REPOSITORY_KEY = "fellowship-inc";

  public static final String REPOSITORY_NAME = "The Fellowship's custom rules";
  // ...
}
```

## Testing a custom plugin

>
> :exclamation: **Prerequisite**
>
> For this chapter, you will need a local instance of SonarQube.
> If you don't have a SonarQube platform installed on your machine, you can download the latest
> version from [here](https://www.sonarqube.org/downloads/),
>

At this point, we've completed the implementation of the first custom rule and registered it into
the custom plugin.
The last remaining step is to test it directly with the SonarQube platform and try to analyze a
project!

Start by building the project using Maven.

```
$ pwd
/home/gandalf/workspace/sonar-delphi/docs/delphi-custom-rules-example

$ mvn clean install
[INFO] Scanning for projects...
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Building SonarDelphi :: Documentation :: Custom Rules Example 1.0.0-SNAPSHOT
[INFO] ------------------------------------------------------------------------

...

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  12.151 s
[INFO] Finished at: 2023-08-15T10:58:51+10:00
[INFO] ------------------------------------------------------------------------
```

Then, grab the jar file `delphi-custom-rules-example-1.0.0-SNAPSHOT.jar` from the `target` folder of
the project.

Move it to the extensions folder of your SonarQube instance, which will be located
at `$SONAR_HOME/extensions/plugins`.

Now, (re-)start your SonarQube instance, log in as admin, and navigate to the ***Rules*** tab.

From there, under the language section, select "**Delphi**", and then "**The Fellowship's custom
rules**" (or "**MyCompany Custom Repository**" if you did not change it) under the repository
section.
Your rule should now be visible (with all the other sample rules).

Once activated
(see [Quality profiles](https://docs.sonarqube.org/latest/instance-administration/quality-profiles/)),
the only step remaining is to analyze one of your projects!

When encountering a method returning the same type as its parameter, the custom rule will now raise
an issue.

### How to define rule parameters

You have to add a `@RuleProperty` to your Rule.

Check this
example: [StringInMethodNameCheck.java](https://github.com/integrated-application-development/sonar-delphi/tree/master/docs/delphi-custom-rules-example/src/main/java/au/com/integradev/samples/delphi/checks/StringInMethodNameCheck.java)

## References

* [SonarQube Platform](https://www.sonarsource.com/products/sonarqube/)
* [SonarDelphi GitHub Repository](https://github.com/integrated-application-development/sonar-delphi)
* [SonarDelphi Custom Rules Example](https://github.com/integrated-application-development/sonar-delphi/docs/delphi-custom-rules-example)
