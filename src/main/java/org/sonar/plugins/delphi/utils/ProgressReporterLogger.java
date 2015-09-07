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
package org.sonar.plugins.delphi.utils;

import org.slf4j.Logger;

/**
 * Logger used to report progress from ProgressReporter
 */
public class ProgressReporterLogger
{
  private Logger log = null;

  /**
   * Default ctor, no logging
   */
  public ProgressReporterLogger() {
  }

  /**
   * Ctor, specify your own logger to write to
   * 
   * @param log logger to write to
   */
  public ProgressReporterLogger(Logger log) {
    this.log = log;
  }

  /**
   * log a message
   * 
   * @param msg message
   */
  public void log(String msg) {
    if (log != null) {
      log.info(msg);
    }
  }

}
