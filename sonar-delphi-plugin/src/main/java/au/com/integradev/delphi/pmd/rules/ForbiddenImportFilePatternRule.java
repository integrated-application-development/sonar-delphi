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
package au.com.integradev.delphi.pmd.rules;

import au.com.integradev.delphi.antlr.ast.node.UnitImportNode;
import au.com.integradev.delphi.symbol.declaration.UnitNameDeclaration;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class ForbiddenImportFilePatternRule extends AbstractDelphiRule {
  private static final Logger LOG = Loggers.get(ForbiddenImportFilePatternRule.class);

  public static final PropertyDescriptor<String> FORBIDDEN_IMPORT_PATTERN =
      PropertyFactory.stringProperty("forbiddenImportPattern")
          .desc("The path pattern for files which should not be imported.")
          .defaultValue("")
          .build();

  public static final PropertyDescriptor<String> FORBIDDEN_IMPORT_SYNTAX =
      PropertyFactory.stringProperty("forbiddenImportSyntax")
          .desc("The syntax of forbiddenImportPattern. Options are: 'GLOB', 'REGEX'")
          .defaultValue("GLOB")
          .build();

  public static final PropertyDescriptor<String> WHITELIST_PATTERN =
      PropertyFactory.stringProperty("whitelistPattern")
          .desc("The path pattern for whitelisted files which may import these forbidden imports.")
          .defaultValue("")
          .build();

  public static final PropertyDescriptor<String> WHITELIST_SYNTAX =
      PropertyFactory.stringProperty("whitelistSyntax")
          .desc("The syntax of whitelistPattern. Options are: 'GLOB', 'REGEX'")
          .defaultValue("GLOB")
          .build();

  private static final PropertyDescriptor<String> MESSAGE =
      PropertyFactory.stringProperty("message").desc("The issue message").defaultValue("").build();

  private boolean init;
  private PathMatcher forbiddenMatcher;
  private PathMatcher whitelistMatcher;
  private String dysfunctionReason;

  public ForbiddenImportFilePatternRule() {
    definePropertyDescriptor(FORBIDDEN_IMPORT_PATTERN);
    definePropertyDescriptor(FORBIDDEN_IMPORT_SYNTAX);
    definePropertyDescriptor(WHITELIST_PATTERN);
    definePropertyDescriptor(WHITELIST_SYNTAX);
    definePropertyDescriptor(MESSAGE);
  }

  @Override
  public void start(RuleContext ctx) {
    if (!init) {
      init = true;
      String forbiddenImportSyntax = getProperty(FORBIDDEN_IMPORT_SYNTAX);
      String forbiddenImportPattern = getProperty(FORBIDDEN_IMPORT_PATTERN);
      forbiddenMatcher = makePathMatcher(forbiddenImportSyntax, forbiddenImportPattern);

      String whitelistSyntax = getProperty(WHITELIST_SYNTAX);
      String whitelistPattern = getProperty(WHITELIST_PATTERN);
      if (!whitelistPattern.isBlank()) {
        whitelistMatcher = makePathMatcher(whitelistSyntax, whitelistPattern);
      }
    }
  }

  @Override
  public RuleContext visit(UnitImportNode unitImport, RuleContext data) {
    if (isViolation(unitImport)) {
      addViolation(data, unitImport);
    }
    return data;
  }

  private boolean isViolation(UnitImportNode node) {
    UnitNameDeclaration importUnit = node.getImportNameDeclaration().getOriginalDeclaration();
    Path currentPath = node.getASTTree().getDelphiFile().getSourceCodeFile().toPath();
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
      String problem = "Unable to compile path pattern \"" + patternAndSyntax + "\"";
      LOG.debug(problem, e);
      dysfunctionReason = problem + ": " + e.getMessage();
    }
    return null;
  }

  @Override
  public String dysfunctionReason() {
    start(null);
    return dysfunctionReason;
  }
}
