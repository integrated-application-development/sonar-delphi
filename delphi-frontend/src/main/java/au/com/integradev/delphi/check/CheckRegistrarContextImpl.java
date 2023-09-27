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
package au.com.integradev.delphi.check;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rule.RuleScope;
import org.sonar.plugins.communitydelphi.api.check.CheckRegistrar.RegistrarContext;

/** Context for checks registration. */
public class CheckRegistrarContextImpl implements RegistrarContext {
  private String repositoryKey;
  private List<Class<?>> checkClasses;
  private Function<RuleKey, RuleScope> scopeFunction;

  @Override
  public void registerClassesForRepository(
      String repositoryKey,
      Iterable<Class<?>> checkClasses,
      Function<RuleKey, RuleScope> scopeFunction) {
    Preconditions.checkArgument(
        StringUtils.isNotBlank(repositoryKey), "Please specify a valid repository key");
    this.repositoryKey = repositoryKey;
    this.checkClasses = ImmutableList.copyOf(checkClasses);
    this.scopeFunction = scopeFunction;
  }

  /**
   * Returns the repository key.
   *
   * @return repository key
   */
  public String repositoryKey() {
    return repositoryKey;
  }

  /**
   * Returns the registered check classes.
   *
   * @return registered check classes
   */
  public List<Class<?>> checkClasses() {
    return checkClasses;
  }

  /**
   * Returns a function that returns a {@code RuleScope} for a given rule's engine key.
   *
   * @return scope function
   */
  public Function<RuleKey, RuleScope> getScopeFunction() {
    return scopeFunction;
  }
}
