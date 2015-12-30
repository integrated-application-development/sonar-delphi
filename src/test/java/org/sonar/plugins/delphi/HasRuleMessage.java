/*
 * Sonar Delphi Plugin
 * Copyright (C) 2015 Fabricio Colombo
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
package org.sonar.plugins.delphi;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.sonar.api.issue.Issue;

public class HasRuleMessage<T extends Issue> extends TypeSafeMatcher<T> {

  private final String message;

  public HasRuleMessage(String message) {
    this.message = message;
  }

  @Override
  protected boolean matchesSafely(T item) {
    return message.equals(item.message());
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("message ").appendValue(message);
  }

  @Override
  protected void describeMismatchSafely(T item, Description mismatchDescription) {
    mismatchDescription.appendText("was ").appendValue(item.message());
  }

  public static <T extends Issue> Matcher<T> hasRuleMessage(String message) {
    return new HasRuleMessage<T>(message);
  }

}
