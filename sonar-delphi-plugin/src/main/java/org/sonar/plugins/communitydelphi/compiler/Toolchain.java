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
package org.sonar.plugins.communitydelphi.compiler;

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
