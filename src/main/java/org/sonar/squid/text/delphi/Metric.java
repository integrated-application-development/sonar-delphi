/*
 * SSLR Squid Bridge
 * Copyright (C) 2010-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.squid.text.delphi;


public enum Metric implements MetricDef {

  PACKAGES, CLASSES, ANONYMOUS_INNER_CLASSES, FILES, METHODS, CONSTRUCTORS, STATEMENTS, LINES(false), BLANK_LINES(false),
  COMMENT_LINES(false), HEADER_COMMENT_LINES(false), COMMENTED_OUT_CODE_LINES(false), BRANCHES,
  PUBLIC_API, PUBLIC_DOC_API, ACCESSORS,
  COMMENT_BLANK_LINES(false), LINES_OF_CODE(false),
  COMPLEXITY, INTERFACES, ABSTRACT_CLASSES,
  ;

  private boolean aggregateIfThereIsAlreadyAValue = true;

  Metric() {
  }

  Metric(boolean aggregateIfThereIsAlreadyAValue) {
    this.aggregateIfThereIsAlreadyAValue = aggregateIfThereIsAlreadyAValue;
  }

  @Override
  public String getName() {
    return name();
  }

  @Override
  public boolean aggregateIfThereIsAlreadyAValue() {
    return aggregateIfThereIsAlreadyAValue;
  }

}
