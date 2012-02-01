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
package org.sonar.plugins.delphi.debug;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.NotImplementedException;

public class DebugConfiguration implements Configuration {

  private Map<String, Object> values = new HashMap<String, Object>();

  public Configuration subset(String prefix) {
    throw new NotImplementedException("DebugConfiguration method not implemented.");
  }

  public boolean isEmpty() {
    return values.isEmpty();
  }

  public boolean containsKey(String key) {
    return values.containsKey(key);
  }

  public void addProperty(String key, Object value) {
    values.put(key, value);
  }

  public void setProperty(String key, Object value) {
    throw new NotImplementedException("DebugConfiguration method not implemented.");
  }

  public void clearProperty(String key) {
    throw new NotImplementedException("DebugConfiguration method not implemented.");
  }

  public void clear() {
    values.clear();
  }

  public Object getProperty(String key) {
    return values.get(key);
  }

  public Iterator getKeys(String prefix) {
    throw new NotImplementedException("DebugConfiguration method not implemented.");
  }

  public Iterator getKeys() {
    throw new NotImplementedException("DebugConfiguration method not implemented.");
  }

  public Properties getProperties(String key) {
    throw new NotImplementedException("DebugConfiguration method not implemented.");
  }

  public boolean getBoolean(String key) {
    return (Boolean) values.get(key);
  }

  public boolean getBoolean(String key, boolean defaultValue) {
    if (values.containsKey(key)) {
      return (Boolean) values.get(key);
    }
    return defaultValue;
  }

  public Boolean getBoolean(String key, Boolean defaultValue) {
    if (values.containsKey(key)) {
      return (Boolean) values.get(key);
    }
    return defaultValue;
  }

  public byte getByte(String key) {
    throw new NotImplementedException("DebugConfiguration method not implemented.");
  }

  public byte getByte(String key, byte defaultValue) {
    return defaultValue;
  }

  public Byte getByte(String key, Byte defaultValue) {
    return defaultValue;
  }

  public double getDouble(String key) {
    throw new NotImplementedException("DebugConfiguration method not implemented.");
  }

  public double getDouble(String key, double defaultValue) {
    return defaultValue;
  }

  public Double getDouble(String key, Double defaultValue) {
    return defaultValue;
  }

  public float getFloat(String key) {
    throw new NotImplementedException("DebugConfiguration method not implemented.");
  }

  public float getFloat(String key, float defaultValue) {
    throw new NotImplementedException("DebugConfiguration method not implemented.");
  }

  public Float getFloat(String key, Float defaultValue) {
    throw new NotImplementedException("DebugConfiguration method not implemented.");
  }

  public int getInt(String key) {
    throw new NotImplementedException("DebugConfiguration method not implemented.");
  }

  public int getInt(String key, int defaultValue) {
    String value = (String) values.get(key);
    if (value == null) {
      return defaultValue;
    }
    return Integer.valueOf(value);
  }

  public Integer getInteger(String key, Integer defaultValue) {
    return defaultValue;
  }

  public long getLong(String key) {
    throw new NotImplementedException("DebugConfiguration method not implemented.");
  }

  public long getLong(String key, long defaultValue) {
    return defaultValue;
  }

  public Long getLong(String key, Long defaultValue) {
    return defaultValue;
  }

  public short getShort(String key) {
    throw new NotImplementedException("DebugConfiguration method not implemented.");
  }

  public short getShort(String key, short defaultValue) {
    return defaultValue;
  }

  public Short getShort(String key, Short defaultValue) {
    return defaultValue;
  }

  public BigDecimal getBigDecimal(String key) {
    throw new NotImplementedException("DebugConfiguration method not implemented.");
  }

  public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
    return defaultValue;
  }

  public BigInteger getBigInteger(String key) {
    throw new NotImplementedException("DebugConfiguration method not implemented.");
  }

  public BigInteger getBigInteger(String key, BigInteger defaultValue) {
    return defaultValue;
  }

  public String getString(String key) {
    return (String) values.get(key);
  }

  public String getString(String key, String defaultValue) {
    if ( !values.containsKey(key)) {
      return defaultValue;
    }
    return (String) values.get(key);
  }

  public String[] getStringArray(String key) {
    String str = (String) values.get(key);
    if (str == null) {
      return new String[0];
    }
    return str.split(",");
  }

  public List getList(String key) {
    throw new NotImplementedException("DebugConfiguration method not implemented.");
  }

  public List getList(String key, List defaultValue) {
    throw new NotImplementedException("DebugConfiguration method not implemented.");
  }

}
