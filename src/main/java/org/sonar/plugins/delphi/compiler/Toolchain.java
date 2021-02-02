package org.sonar.plugins.delphi.compiler;

public enum Toolchain {
  DCC32(Architecture.X86, Platform.WINDOWS),
  DCC64(Architecture.X64, Platform.WINDOWS),
  DCCOSX(Architecture.X86, Platform.MACOS),
  DCCOSX64(Architecture.X64, Platform.MACOS),
  DCCIOSARM(Architecture.X86, Platform.IOS),
  DCCIOSARM64(Architecture.X64, Platform.IOS),
  DCCIOS32(Architecture.X86, Platform.IOS),
  DCCAARM(Architecture.X86, Platform.ANDROID),
  DCCAARM64(Architecture.X64, Platform.ANDROID),
  DCCLINUX64(Architecture.X64, Platform.LINUX);

  public final Architecture architecture;
  public final Platform platform;

  Toolchain(Architecture architecture, Platform platform) {
    this.architecture = architecture;
    this.platform = platform;
  }
}
