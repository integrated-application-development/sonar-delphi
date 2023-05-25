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
package au.com.integradev.delphi.checks;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.communitydelphi.api.ast.UnitImportNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.IllegalRuleParameterError;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitNameDeclaration;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "ForbiddenImportFilePatternRule", repositoryKey = "delph")
@Rule(key = "ForbiddenImportFilePattern")
public class ForbiddenImportFilePatternCheck extends DelphiCheck {
  private static final String DEFAULT_MESSAGE = "Remove usage of this forbidden unit.";
  private static final String DEFAULT_SYNTAX = "GLOB";

  @RuleProperty(
      key = "forbiddenImportPattern",
      description = "The path pattern for files which should not be imported")
  public String forbiddenImportPattern;

  @RuleProperty(
      key = "forbiddenImportSyntax",
      description = "The syntax of forbiddenImportPattern. Options are: 'GLOB', 'REGEX'",
      defaultValue = DEFAULT_SYNTAX)
  public String forbiddenImportSyntax = DEFAULT_SYNTAX;

  @RuleProperty(
      key = "whitelistPattern",
      description =
          "The path pattern for whitelisted files which may import these forbidden imports")
  public String whitelistPattern;

  @RuleProperty(
      key = "whitelistSyntax",
      description = "The syntax of whitelistPattern. Options are: 'GLOB', 'REGEX'",
      defaultValue = DEFAULT_SYNTAX)
  public String whitelistSyntax = DEFAULT_SYNTAX;

  @RuleProperty(key = "message", description = "The issue message", defaultValue = DEFAULT_MESSAGE)
  public String message = DEFAULT_MESSAGE;

  private PathMatcher forbiddenMatcher;
  private PathMatcher whitelistMatcher;

  @Override
  public void start(DelphiCheckContext context) {
    forbiddenMatcher = makePathMatcher(forbiddenImportSyntax, forbiddenImportPattern);
    if (!whitelistPattern.isBlank()) {
      whitelistMatcher = makePathMatcher(whitelistSyntax, whitelistPattern);
    }
  }

  @Override
  public DelphiCheckContext visit(UnitImportNode unitImport, DelphiCheckContext context) {
    if (isViolation(unitImport)) {
      reportIssue(context, unitImport, message);
    }
    return context;
  }

  private boolean isViolation(UnitImportNode node) {
    UnitNameDeclaration importUnit = node.getImportNameDeclaration().getOriginalDeclaration();
    Path currentPath = node.getAst().getDelphiFile().getSourceCodeFile().toPath();
    return importUnit != null
        && isForbiddenImport(importUnit.getPath())
        && !isWhitelisted(currentPath);
  }

  private boolean isForbiddenImport(Path path) {
    return forbiddenMatcher.matches(path.toAbsolutePath());
  }

  private boolean isWhitelisted(Path path) {
    return whitelistMatcher != null && whitelistMatcher.matches(path.toAbsolutePath());
  }

  private PathMatcher makePathMatcher(String syntax, String pattern) {
    String patternAndSyntax = syntax.toLowerCase() + ":" + pattern;
    try {
      return FileSystems.getDefault().getPathMatcher(patternAndSyntax);
    } catch (Exception e) {
      throw new IllegalRuleParameterError(
          "Unable to compile path pattern \"" + patternAndSyntax + "\"", e);
    }
  }
}
