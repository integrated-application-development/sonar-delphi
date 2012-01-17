/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
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
package org.sonar.plugins.delphi.debug;

import java.util.Collection;

import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleQuery;

/**
 * Debug rule finder used in unit tests
 * 
 */
public class DebugRuleFinder implements org.sonar.api.rules.RuleFinder {

  /**
   * Default ctor
   */
  public DebugRuleFinder() {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Rule find(RuleQuery query) {
    Rule rule = Rule.create(query.getRepositoryKey(), query.getConfigKey(), query.getKey());
    return rule;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Rule findById(int ruleId) {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Rule findByKey(String repositoryKey, String key) {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<Rule> findAll(RuleQuery query) {
    return null;
  }
}
