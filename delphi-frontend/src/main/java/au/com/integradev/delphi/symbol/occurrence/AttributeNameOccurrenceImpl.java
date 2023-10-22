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
package au.com.integradev.delphi.symbol.occurrence;

import java.util.Objects;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;

public final class AttributeNameOccurrenceImpl extends NameOccurrenceImpl {
  private NameOccurrence implicitConstructorNameOccurrence;

  public AttributeNameOccurrenceImpl(DelphiNode concreteNode) {
    super(concreteNode);
  }

  @Override
  public boolean isAttributeReference() {
    return true;
  }

  public void setImplicitConstructorNameOccurrence(NameOccurrence occurrence) {
    this.implicitConstructorNameOccurrence = occurrence;
  }

  public NameOccurrence getImplicitConstructorNameOccurrence() {
    return implicitConstructorNameOccurrence;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    AttributeNameOccurrenceImpl that = (AttributeNameOccurrenceImpl) o;
    return Objects.equals(
        implicitConstructorNameOccurrence, that.implicitConstructorNameOccurrence);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), implicitConstructorNameOccurrence);
  }
}
