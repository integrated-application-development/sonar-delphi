/*
 * Sonar Delphi Plugin
 * Copyright (C) 2024 Integrated Application Development
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
package au.com.integradev.delphi.utils.format;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

abstract class FormatArgumentCollector<T> {
  private final List<FormatSpecifier> formatSpecifiers;
  private List<Set<T>> collected;
  private int currentIndex;

  protected FormatArgumentCollector(List<FormatSpecifier> formatSpecifiers) {
    this.formatSpecifiers = formatSpecifiers;
    this.collected = new ArrayList<>();
  }

  protected abstract Optional<T> constructWidth();

  protected abstract Optional<T> constructPrecision();

  protected abstract Optional<T> constructValue(FormatSpecifier specifier);

  private void addToCollected(T obj) {
    if (currentIndex >= collected.size()) {
      int setsToAdd = 1 + (currentIndex - collected.size());

      for (int i = 0; i < setsToAdd; i++) {
        collected.add(new HashSet<>());
      }
    }

    collected.get(currentIndex).add(obj);
  }

  public List<Set<T>> collect() {
    collected = new ArrayList<>();
    currentIndex = -1;

    for (FormatSpecifier specifier : formatSpecifiers) {
      currentIndex = specifier.getIndex().orElse(currentIndex + 1);

      Optional<NumberOrWildcard> width = specifier.getWidth();
      if (width.isPresent() && width.get().isWildcard()) {
        constructWidth().ifPresent(this::addToCollected);
        currentIndex++;
      }

      Optional<NumberOrWildcard> precision = specifier.getPrecision();
      if (precision.isPresent() && precision.get().isWildcard()) {
        constructPrecision().ifPresent(this::addToCollected);
        currentIndex++;
      }

      constructValue(specifier).ifPresent(this::addToCollected);
    }

    return collected;
  }
}
