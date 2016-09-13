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

import org.hsqldb.Server;
import org.hsqldb.ServerConfiguration;
import org.hsqldb.persist.HsqlProperties;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Utility to start the HSQL server.
 *
 * @author Denis Pavlov
 * @since 1.0.0
 */
public final class HSQLServerUtil {

  private static final HSQLServerUtil INSTANCE = new HSQLServerUtil();
  private Server hsqlServer;

  private HSQLServerUtil() {
    // prevent instantiation
  }

  /**
   * @return utility instance.
   */
  public static HSQLServerUtil getInstance() {
    return INSTANCE;
  }

  private void doStart(final HsqlProperties props) {

    ServerConfiguration.translateDefaultDatabaseProperty(props);

    hsqlServer = new Server();
    try {
      File outputFile = File.createTempFile("HSQLServerUtil", ".temp");
      outputFile.deleteOnExit();
      hsqlServer.setLogWriter(new PrintWriter(outputFile));
      hsqlServer.setErrWriter(new PrintWriter(outputFile));
    } catch (IOException e) {
      DelphiUtils.LOG
        .info("Could not create temporary output file for HSQLServerUtil class; output redirected to stdout");
    }

    hsqlServer.setRestartOnShutdown(false);
    hsqlServer.setNoSystemExit(true);
    hsqlServer.setProperties(props);
    hsqlServer.start();
  }

  /**
   * start the server with a database configuration.
   *
   * @param dbName the name of database
   * @param port port to listen to
   */
  public void start(final String dbName, final int port) {
    HsqlProperties props = new HsqlProperties();
    props.setProperty("server.port", port);
    // Usage of prefix "target/" is important in order to not pollute
    // working directory
    props.setProperty("server.database.0", "target/" + dbName);
    props.setProperty("server.dbname.0", dbName);
    doStart(props);
  }

  /**
   * start the server with a database configuration.
   *
   * @param dbName the name of database
   */
  public void start(final String dbName) {
    HsqlProperties props = new HsqlProperties();
    // Usage of prefix "target/" is important in order to not pollute
    // working directory
    props.setProperty("server.database.0", "target/" + dbName);
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
