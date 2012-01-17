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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: sg0501095 Date: 1/5/12 Time: 9:49 AM To change this template use File | Settings | File Templates.
 */
public class AQTimeCoverageDao {

  private static final String TABLE1 = "LIGHT_COVERAGE_PROFILER_LINES";
  private static final String TABLE2 = "LIGHT_COVERAGE_PROFILER_SOURCE_FILES_DATA";
  
  private Map<String, String> jdbcProps;
  private String databasePrefix = "CC.dbo.";
  private JdbcTemplate jdbcTemplate;

  public void setJdbcProps(Map<String, String> jdbcProps) {
    this.jdbcProps = jdbcProps;
  }

  public JdbcTemplate getJdbcTemplate() {
    if (jdbcTemplate == null) {
      jdbcTemplate = new JdbcTemplate(new DatabaseProperties(jdbcProps));
    }
    return jdbcTemplate;
  }

  public List<AQTimeCodeCoverage> readAQTimeCodeCoverage() {
    return getJdbcTemplate().query(
        "SELECT l.ID, COL_FILE_NAME, l.COL_MARK, l.COL_SOURCE_LINE, f.COL____COVERED FROM " + databasePrefix + TABLE1 + " l inner join "
            + databasePrefix + TABLE2 + " f on l.PARENT_ID = f.ID " + "ORDER BY COL_FILE_NAME", new RowMapper<AQTimeCodeCoverage>() {

          @Override
          public AQTimeCodeCoverage mapRow(ResultSet resultSet) throws SQLException {
            AQTimeCodeCoverage result = new AQTimeCodeCoverage();
            result.setCoveredFileName(resultSet.getString("COL_FILE_NAME"));
            result.setLineNumber(resultSet.getInt("COL_SOURCE_LINE"));
            result.setLineHits(resultSet.getInt("COL_MARK"));
            return result;
          }
        });

  }

  public void setDatabasePrefix(String databasePrefix) {
    this.databasePrefix = databasePrefix;
  }
}
