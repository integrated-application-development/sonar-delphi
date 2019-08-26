/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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
package org.sonar.plugins.delphi.pmd.violation;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleViolation;
import org.sonar.plugins.delphi.pmd.FilePosition;
import org.sonar.plugins.delphi.pmd.rules.DelphiRule;

/** Delphi pmd rule violation */
public class DelphiRuleViolation implements RuleViolation {

  private final Rule rule;
  private String description;
  private String filename;

  private String className = "";
  private String methodName = "";
  private String packageName = "";

  private int beginLine = FilePosition.UNDEFINED_LINE;
  private int endLine = FilePosition.UNDEFINED_COLUMN;
  private int beginColumn = FilePosition.UNDEFINED_LINE;
  private int endColumn = FilePosition.UNDEFINED_COLUMN;

  private boolean suppressed;

  /**
   * C-tor used by DelphiRuleViolationBuilder
   *
   * @param rule DelphiLanguage rule
   * @param ctx Rule context
   */
  public DelphiRuleViolation(DelphiRule rule, RuleContext ctx) {
    this.rule = rule;
    this.filename = ctx.getSourceCodeFile().getAbsolutePath();
    this.description = rule.getMessage();
  }

  @Override
  public String getFilename() {
    return filename;
  }

  @Override
  public int getBeginLine() {
    return beginLine;
  }

  @Override
  public int getBeginColumn() {
    return beginColumn;
  }

  @Override
  public int getEndLine() {
    return endLine;
  }

  @Override
  public int getEndColumn() {
    return endColumn;
  }

  @Override
  public Rule getRule() {
    return rule;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String getPackageName() {
    return packageName;
  }

  @Override
  public String getMethodName() {
    return methodName;
  }

  @Override
  public String getClassName() {
    return className;
  }

  @Override
  public boolean isSuppressed() {
    return suppressed;
  }

  @Override
  public String getVariableName() {
    return "";
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public void setBeginLine(int beginLine) {
    this.beginLine = beginLine;
  }

  public void setEndLine(int endLine) {
    this.endLine = endLine;
  }

  public void setBeginColumn(int beginColumn) {
    this.beginColumn = beginColumn;
  }

  public void setEndColumn(int endColumn) {
    this.endColumn = endColumn;
  }

  public void setSuppressed(boolean suppressed) {
    this.suppressed = suppressed;
  }
}
