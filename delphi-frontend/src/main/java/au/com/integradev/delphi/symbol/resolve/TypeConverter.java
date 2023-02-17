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
package au.com.integradev.delphi.symbol.resolve;

import static au.com.integradev.delphi.symbol.resolve.EqualityType.INCOMPATIBLE_TYPES;

import au.com.integradev.delphi.symbol.resolve.TypeConverter.TypeConversion.Source;
import java.util.Set;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.StructType;

public final class TypeConverter {
  private TypeConverter() {
    // Utility class
  }

  public static TypeConversion convert(Type from, Type to) {
    Type fromConversionType = null;

    EqualityType fromConversionEquality = INCOMPATIBLE_TYPES;
    if (from.isStruct()) {
      Set<Type> implicit = ((StructType) from).typesWithImplicitConversionsFromThis();
      for (Type implicitConversion : implicit) {
        EqualityType convertEquality = TypeComparer.compare(implicitConversion, to);
        if (convertEquality.ordinal() > fromConversionEquality.ordinal()) {
          fromConversionEquality = convertEquality;
          fromConversionType = implicitConversion;
        }
      }
    }

    Type toConversionType = null;
    EqualityType toConversionEquality = INCOMPATIBLE_TYPES;
    if (to.isStruct()) {
      Set<Type> implicit = ((StructType) to).typesWithImplicitConversionsToThis();
      for (Type implicitConversion : implicit) {
        EqualityType convertEquality = TypeComparer.compare(from, implicitConversion);
        if (convertEquality.ordinal() > toConversionEquality.ordinal()) {
          toConversionEquality = convertEquality;
          toConversionType = implicitConversion;
        }
      }
    }

    EqualityType equality = INCOMPATIBLE_TYPES;
    Source source = TypeConversion.Source.NONE;

    if (fromConversionEquality.ordinal() > toConversionEquality.ordinal()) {
      equality = fromConversionEquality;
      from = fromConversionType;
      source = TypeConversion.Source.FROM;
    } else if (toConversionEquality != INCOMPATIBLE_TYPES) {
      equality = toConversionEquality;
      to = toConversionType;
      source = TypeConversion.Source.TO;
    }

    return new TypeConversion(from, to, equality, source);
  }

  public static class TypeConversion {
    public enum Source {
      NONE,
      FROM,
      TO
    }

    private final Type from;
    private final Type to;
    private final EqualityType equality;
    private final Source source;

    private TypeConversion(Type from, Type to, EqualityType equality, Source source) {
      this.from = from;
      this.to = to;
      this.equality = equality;
      this.source = source;
    }

    public Type getFrom() {
      return from;
    }

    public Type getTo() {
      return to;
    }

    public EqualityType getEquality() {
      return equality;
    }

    public Source getSource() {
      return source;
    }

    public boolean isSuccessful() {
      return equality != INCOMPATIBLE_TYPES;
    }
  }
}
