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
