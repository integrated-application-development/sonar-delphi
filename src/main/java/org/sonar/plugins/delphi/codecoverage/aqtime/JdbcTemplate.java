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

package org.sonar.plugins.delphi.codecoverage.aqtime;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.sonar.plugins.delphi.utils.DelphiUtils;

/**
 * JdbcTemplate
 */
public class JdbcTemplate {

  private Connection connection = null;
  DatabaseProperties databaseProperties = null;

  public JdbcTemplate(DatabaseProperties databaseProperties) {
    this.databaseProperties = databaseProperties;
  }

  public Connection getConnection() {
    try {
      Class.forName(databaseProperties.getDriverName());
      connection = DriverManager.getConnection(databaseProperties.getHost(), databaseProperties.getConnectionProperties());
    } catch (Exception e) {
      DelphiUtils.LOG.error(e.getMessage());
      DelphiUtils.getDebugLog().println(e.getMessage());
      return null;
    }
    return connection;
  }

  public <T> List<T> query(String queryString, RowMapper<T> rowMapper) {
    List<T> result = new ArrayList<T>();
    try {
      Statement statement = getConnection().createStatement();
      ResultSet resultSet = statement.executeQuery(queryString);
      while (resultSet.next()) {
        result.add(rowMapper.mapRow(resultSet));
      }
      statement.close();
    } catch (SQLException e) {
      DelphiUtils.LOG.error("AQTime SQL error: " + e.getMessage());
      DelphiUtils.getDebugLog().println("AQTime SQL error: " + e.getMessage());

    }

    return result;
  }

}
