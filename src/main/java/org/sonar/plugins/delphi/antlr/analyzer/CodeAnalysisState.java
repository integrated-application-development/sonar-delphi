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
package org.sonar.plugins.delphi.antlr.analyzer;

import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.UnitInterface;

/**
 * Class holding temporary analysis state
 */
public class CodeAnalysisState extends CodeAnalysisCacheResults {

  protected UnitInterface activeUnit = null;
  protected FunctionInterface activeFunction = null;
  protected ClassInterface activeClass = null;

  /**
   * File, Implementation or Interface section.
   */
  protected LexerMetrics parseStatus = LexerMetrics.FILE;

  /**
   * We start with public because whole classes in interface are public.
   */
  protected LexerMetrics parseVisibility = LexerMetrics.PUBLIC;

  /**
   * @return unit currently being processed
   */
  public UnitInterface getActiveUnit() {
    return activeUnit;
  }

  /**
   * set unit being processed
   * 
   * @param activeUnit unit
   */
  public void setActiveUnit(UnitInterface activeUnit) {
    this.activeUnit = activeUnit;
  }

  /**
   * sets function being currently processed
   * 
   * @param function function to set
   */
  public void setActiveFunction(FunctionInterface function) {
    this.activeFunction = function;
  }

  /**
   * get function being currently processed
   * 
   * @return function currently processed
   */
  public FunctionInterface getActiveFunction() {
    return activeFunction;
  }

  /**
   * sets current visibility scope
   * 
   * @param parseVisibility visibility scope
   */
  public void setParseVisibility(LexerMetrics parseVisibility) {
    this.parseVisibility = parseVisibility;
  }

  /**
   * @return current visibility scope
   */
  public LexerMetrics getParseVisibility() {
    return parseVisibility;
  }

  /**
   * sets active class begin processed
   * 
   * @param activeClass class
   */
  public void setActiveClass(ClassInterface activeClass) {
    this.activeClass = activeClass;
  }

  /**
   * @return active class being processed
   */
  public ClassInterface getActiveClass() {
    return activeClass;
  }

  /**
   * @return parse status
   */
  public LexerMetrics getParseStatus() {
    return parseStatus;
  }

  /**
   * sets parse status
   * 
   * @param status parse status
   */
  public void setParseStatus(LexerMetrics status) {
    parseStatus = status;
  }

}
