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
package org.sonar.plugins.delphi.antlr.sanitizer;

import org.sonar.plugins.delphi.antlr.sanitizer.resolvers.SourceResolverResults;

/**
 * Abstract class for source resolvers, applies chain-of-command design pattern
 */
public abstract class SourceResolver {

  SourceResolver next = null;

  /**
   * chain next resolver
   * 
   * @param successor resolver to chain
   * @return chained resolver
   */
  public SourceResolver chain(SourceResolver successor) {
    next = successor;
    return next;
  }

  /**
   * resolves
   * 
   * @param results Class to holding results for resolvers
   */
  public void resolve(SourceResolverResults results) {
    doResolve(results);
    if (next != null) {
      next.resolve(results);
    }
  }

  protected abstract void doResolve(SourceResolverResults results);
}
