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
package org.sonar.plugins.delphi.utils;

import java.io.PrintWriter;

import org.hsqldb.Server;
import org.hsqldb.ServerConfiguration;
import org.hsqldb.persist.HsqlProperties;

/**
 * Utility to start the HSQL server.
 * 
 * @author Denis Pavlov
 * @since 1.0.0
 * 
 */
public final class HSQLServerUtil {

  private static final HSQLServerUtil UTIL = new HSQLServerUtil();
  private Server hsqlServer;

  private HSQLServerUtil() {
    // prevent instantiation
  }

  /**
   * @return utility instance.
   */
  public static HSQLServerUtil getInstance() {
    return UTIL;
  }

  private void doStart(final HsqlProperties props) {

    ServerConfiguration.translateDefaultDatabaseProperty(props);

    hsqlServer = new Server();
    hsqlServer.setLogWriter(new PrintWriter(DelphiUtils.getDebugLog()));
    hsqlServer.setErrWriter(new PrintWriter(DelphiUtils.getDebugLog()));
    hsqlServer.setRestartOnShutdown(false);
    hsqlServer.setNoSystemExit(true);
    hsqlServer.setProperties(props);

    DelphiUtils.LOG.info("Configured the HSQLDB server...");
    hsqlServer.start();
    DelphiUtils.LOG.info("HSQLDB server started on port " + hsqlServer.getPort() + "...");
  }

  /**
   * start the server with a database configuration.
   * 
   * @param dbName
   *          the name of database
   * @param port
   *          port to listen to
   */
  public void start(final String dbName, final int port) {
    HsqlProperties props = new HsqlProperties();
    props.setProperty("server.port", port);
    props.setProperty("server.database.0", dbName);
    props.setProperty("server.dbname.0", dbName);
    doStart(props);
  }

  /**
   * start the server with a database configuration.
   * 
   * @param dbName
   *          the name of database
   */
  public void start(final String dbName) {
    HsqlProperties props = new HsqlProperties();
    props.setProperty("server.database.0", dbName);
    props.setProperty("server.dbname.0", dbName);
    doStart(props);
  }

  /**
   * shutdown the started instance.
   */
  public void stop() {
    DelphiUtils.LOG.info("HSQLDB server shutting down...");
    hsqlServer.stop();
    DelphiUtils.LOG.info("HSQLDB server shutting down... done");
  }

}