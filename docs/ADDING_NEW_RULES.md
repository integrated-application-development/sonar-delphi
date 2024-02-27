# Adding new rules to SonarDelphi

*This page is about contributing to the core SonarDelphi plugin. For creating a custom rules plugin, see [Writing Custom Delphi Rules](CUSTOM_RULES.md).*

To implement a new rule in SonarDelphi:

1. Add a class inheriting from `DelphiCheck` in `delphi-checks/src/main/java/au/com/integradev/delphi/checks`
    * This class must be annotated with `@Rule(key = "xyz")`, where `xyz` is the rule key in PascalCase
    * This class must be named the rule key + `Check`, e.g. `UnusedImportCheck`
2. Add a test suite in `delphi-checks/src/test/java/au/com/integradev/delphi/checks`
    * Test names should follow the format `test...ShouldAddIssue` or `test...ShouldNotAddIssue`
3. Add the rule implementation class from step 1 to the `CheckList` in `delphi-checks/src/main/java/au/com/integradev/delphi/checks`
4. Add rule metadata in `delphi-checks/src/main/resources/org/sonar/l10n/delphi/rules/community-delphi`
    * JSON rule metadata must be named the rule key and [follow the expected metadata format](#json-metadata)
    * The rule description HTML must be named the rule key and [follow the expected description format](#html-description)

## JSON metadata

The structure the JSON metadata should have is reproduced below. In particular:

* The title should follow the [SonarSource rule title guidelines](https://docs.sonarsource.com/sonarqube/latest/extension-guide/adding-coding-rules/#titles)
* The attribute should be one of the fourteen [Clean Code Attributes](https://docs.sonarsource.com/sonarqube/latest/user-guide/clean-code/#clean-code-attributes)
* There must be at least one impact, there can be multiple impacts that are for different [software qualities](https://docs.sonarsource.com/sonarqube/latest/user-guide/clean-code/#software-qualities)
* The tags should be one or more of the [built-in rule tags](https://docs.sonarsource.com/sonarqube/latest/user-guide/rules/built-in-rule-tags/)

```json
{
  "title": "<Descriptive title of rule phrased using 'should'>",
  "type": "<CODE_SMELL | BUG | VULNERABILITY | SECURITY_HOTSPOT>",
  "status": "ready",
  "remediation": {
    "func": "Constant/Issue",
    "constantCost": "<Roughly how many minutes it should take to fix>min"
  },
  "code": {
    "attribute": "<Clean code attribute>",
    "impacts": {
      "<MAINTAINABILITY | SECURITY | RELIABILITY>": "<LOW | MEDIUM | HIGH>"
    }
  },
  "tags": [<tags>],
  "defaultSeverity": "<Minor | Major | Critical | Blocker>",
  "scope": "<ALL | MAIN | TEST>",
  "quickfix": "unknown"
}
```

## HTML description

The HTML description is inserted verbatim into a specifically designated `<div>` - there’s no need to add a containing
element yourself.

The rule description should follow the following format:

```html
<h2>Why is this an issue?</h2>
<!-- the reason why this is a rule, and why it's bad -->

<h2>How to fix it</h2>
<!-- concrete steps to fix the exact issue -->

<h2>Resources</h2>
<ul>
  <li><a href="<resource link>">Location name: Page title</a></li>
  <!-- any other resources useful for further reading... -->
</ul>
```

SonarQube has special handling for `<h2>`s with those exact headings. Only "Why is this an issue?" is required -
the others are optional and can be omitted.

Within these sections:

* Body text should be placed within `<p>` tags.
* Inline code should be placed within `<code>` tags.
* Block code should be placed within `<pre>` tags.
* HTML styling (bold, italic, lists, etc.) should be used as usual.

### Diff view

SonarQube supports a diff view that is enabled through adding attributes to pairs of `<pre>` tags:

* `data-diff-id` is the per-description unique ID of the code snippet (just start with "1" and go up for each new code snippet)
* `data-diff-type` is `"noncompliant"` for the "before" and `"compliant"` for the "after"

It will then display each `<pre>` as diffed relative to its counterpart with the same ID.