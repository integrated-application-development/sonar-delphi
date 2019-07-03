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
package org.sonar.plugins.delphi.pmd;

import java.util.List;
import net.sourceforge.pmd.RuleViolation;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.plugins.delphi.core.DelphiLanguage;

/**
 * PMD sensor
 */
public class DelphiPmdSensor implements Sensor {

  private final DelphiPmdExecutor executor;
  private final DelphiPmdViolationRecorder violationRecorder;

  /**
   * C-tor
   *
   * @param executor Does all of the work!
   * @param violationRecorder Saves PMD violations as sonar issues
   */
  public DelphiPmdSensor(DelphiPmdExecutor executor, DelphiPmdViolationRecorder violationRecorder) {
    this.executor = executor;
    this.violationRecorder = violationRecorder;
  }

  /**
   * Populate {@link SensorDescriptor} of this sensor.
   */
  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(DelphiLanguage.KEY)
        .name("DelphiPmdSensor");
  }

  /**
   * The actual sensor code.
   */
  @Override
  public void execute(@NonNull SensorContext context) {
    for (RuleViolation violation : executor.execute()) {
      violationRecorder.saveViolation(violation, context);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  public List<String> getErrors() {
    return executor.getErrors();
  }
}
