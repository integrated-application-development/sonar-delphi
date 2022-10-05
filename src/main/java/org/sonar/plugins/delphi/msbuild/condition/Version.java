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
package org.sonar.plugins.delphi.msbuild.condition;

import com.google.common.collect.ComparisonChain;
import java.util.Objects;

public final class Version implements Comparable<Version> {
  private final int major;
  private final int minor;
  private final int build;
  private final int revision;

  public Version(int major, int minor, int build, int revision) {
    this.major = major;
    this.minor = minor;
    this.build = build;
    this.revision = revision;
  }

  public Version(int major, int minor, int build) {
    this.major = major;
    this.minor = minor;
    this.build = build;
    this.revision = -1;
  }

  public Version(int major, int minor) {
    this.major = major;
    this.minor = minor;
    this.build = -1;
    this.revision = -1;
  }

  public int getMajor() {
    return major;
  }

  public int getMinor() {
    return minor;
  }

  public int getBuild() {
    return build;
  }

  public int getRevision() {
    return revision;
  }

  @Override
  public int compareTo(Version other) {
    return ComparisonChain.start()
        .compare(major, other.major)
        .compare(minor, other.minor)
        .compare(build, other.build)
        .compare(revision, other.revision)
        .result();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Version version = (Version) o;

    if (major != version.getMajor()) {
      return false;
    }
    if (minor != version.getMinor()) {
      return false;
    }
    if (build != version.getBuild()) {
      return false;
    }
    return revision == version.getRevision();
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.major, this.minor, this.build, this.revision);
  }
}
