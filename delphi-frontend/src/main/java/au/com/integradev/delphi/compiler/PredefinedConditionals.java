/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package au.com.integradev.delphi.compiler;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class PredefinedConditionals {
  private static final CompilerVersion VERSION_6 = CompilerVersion.fromVersionNumber("14.0");
  private static final CompilerVersion VERSION_2006 = CompilerVersion.fromVersionNumber("18.0");
  private static final CompilerVersion VERSION_2007 = CompilerVersion.fromVersionNumber("18.5");
  private static final CompilerVersion VERSION_2009 = CompilerVersion.fromVersionNumber("20.0");
  private static final CompilerVersion VERSION_TOKYO = CompilerVersion.fromVersionNumber("32.0");
  private static final CompilerVersion VERSION_SYDNEY = CompilerVersion.fromVersionNumber("34.0");
  private static final CompilerVersion VERSION_ATHENS = CompilerVersion.fromVersionNumber("36.0");

  private final Toolchain toolchain;
  private final CompilerVersion compilerVersion;

  private PredefinedConditionals(Toolchain toolchain, CompilerVersion compilerVersion) {
    this.toolchain = toolchain;
    this.compilerVersion = compilerVersion;
  }

  private boolean checkToolchain(Toolchain... options) {
    return Set.of(options).contains(toolchain);
  }

  private <T> T selectByArchitecture(T x86, T x64) {
    switch (toolchain.architecture) {
      case X86:
        return x86;
      case X64:
        return x64;
      default:
        throw new AssertionError("Unhandled Architecture");
    }
  }

  private Set<String> getCompilerConditionalDefines() {
    Set<String> result = Sets.newHashSet("DCC", compilerVersion.symbol());

    if (compilerVersion.equals(VERSION_2007)) {
      // Delphi 2006 and 2007 are binary compatible, so ver180 is defined for both.
      result.add(VERSION_2006.symbol());
    }

    return result;
  }

  private Set<String> getPlatformConditionalDefines() {
    Set<String> result = new HashSet<>();

    switch (toolchain.platform) {
      case WINDOWS:
        result.add("MSWINDOWS");
        result.add(selectByArchitecture("WIN32", "WIN64"));
        break;
      case LINUX:
        result.add("LINUX");
        result.add(selectByArchitecture("LINUX32", "LINUX64"));
        break;
      case MACOS:
        result.add("OSX");
        if (toolchain.architecture == Architecture.X64) {
          result.add("OSX64");
        }
        break;
      case IOS:
        result.add("IOS");
        result.add(selectByArchitecture("IOS32", "IOS64"));
        break;
      case ANDROID:
        result.add("ANDROID");
        result.add(selectByArchitecture("ANDROID32", "ANDROID64"));
        break;
      default:
        // Do nothing
    }

    if (toolchain.platform == Platform.MACOS || toolchain.platform == Platform.IOS) {
      // Indicates the target platform is an Apple Darwin OS (macOS or iOS).
      // Note: This symbol existed before Apple changed the name of OS X to macOS.
      result.add("MACOS");
      result.add(selectByArchitecture("MACOS32", "MACOS64"));
    }

    if (toolchain.platform != Platform.WINDOWS) {
      result.add("POSIX");
      result.add(selectByArchitecture("POSIX32", "POSIX64"));
    }

    result.add("CONSOLE");
    result.add("NATIVECODE");

    return result;
  }

  private Set<String> getCPUConditionalDefines() {
    Set<String> result = new HashSet<>();
    if (checkToolchain(Toolchain.DCC32, Toolchain.DCCOSX, Toolchain.DCCOSX64, Toolchain.DCCIOS32)) {
      result.add("CPU386");
    }

    if (checkToolchain(Toolchain.DCC32, Toolchain.DCCOSX, Toolchain.DCCIOS32)) {
      result.add("CPUX86");
    }

    if (checkToolchain(Toolchain.DCC64, Toolchain.DCCOSX64, Toolchain.DCCLINUX64)) {
      result.add("CPUX64");
    }

    if (checkToolchain(
        Toolchain.DCCOSXARM64,
        Toolchain.DCCIOSARM,
        Toolchain.DCCIOSARM64,
        Toolchain.DCCIOSSIMARM64,
        Toolchain.DCCAARM,
        Toolchain.DCCAARM64)) {
      result.add("CPUARM");
      result.add(selectByArchitecture("CPUARM32", "CPUARM64"));
    }

    result.add(selectByArchitecture("CPU32BITS", "CPU64BITS"));

    return result;
  }

  private Set<String> getNextGenConditionalDefines() {
    boolean isArcCompiler =
        checkToolchain(
            Toolchain.DCCOSX64,
            Toolchain.DCCIOSARM,
            Toolchain.DCCIOSARM64,
            Toolchain.DCCIOS32,
            Toolchain.DCCAARM,
            Toolchain.DCCLINUX64);

    boolean beforeSydney = compilerVersion.compareTo(VERSION_SYDNEY) < 0;
    boolean linuxAfterTokyo =
        toolchain == Toolchain.DCCLINUX64 && compilerVersion.compareTo(VERSION_TOKYO) > 0;

    if (isArcCompiler && beforeSydney && !linuxAfterTokyo) {
      return Set.of("NEXTGEN", "AUTOREFCOUNT", "WEAKINSTREF");
    }

    return Collections.emptySet();
  }

  private Set<String> getLLVMConditionalDefines() {
    boolean basedOnLLVM =
        checkToolchain(
            Toolchain.DCCOSX64,
            Toolchain.DCCOSXARM64,
            Toolchain.DCCIOSARM,
            Toolchain.DCCIOSARM64,
            Toolchain.DCCIOSSIMARM64,
            Toolchain.DCCAARM,
            Toolchain.DCCAARM64,
            Toolchain.DCCLINUX64);

    if (basedOnLLVM) {
      Set<String> result = Sets.newHashSet("EXTERNALLINKER");
      if (compilerVersion.compareTo(VERSION_ATHENS) >= 0) {
        result.add("LLVM");
      }
      return result;
    } else {
      return Set.of("ASSEMBLER");
    }
  }

  private Set<String> getAvailabilityConditionalDefines() {
    Set<String> result = new HashSet<>();
    result.addAll(getNextGenConditionalDefines());
    result.addAll(getLLVMConditionalDefines());

    if (checkToolchain(Toolchain.DCCOSX, Toolchain.DCCIOS32)) {
      result.add("ALIGN_STACK");
    }

    if (compilerVersion.compareTo(VERSION_2009) >= 0) {
      result.add("UNICODE");
    }

    if (compilerVersion.compareTo(VERSION_6) >= 0) {
      result.add("CONDITIONALEXPRESSIONS");
    }

    if (toolchain.platform == Platform.LINUX) {
      result.add("ELF");
    }

    if (checkToolchain(Toolchain.DCCOSX, Toolchain.DCCIOS32)) {
      result.add("PC_MAPPED_EXCEPTIONS");
    }

    if (toolchain.platform != Platform.WINDOWS) {
      result.add("PIC");
    }

    if (checkToolchain(Toolchain.DCC32, Toolchain.DCCOSX, Toolchain.DCCIOS32)) {
      result.add("UNDERSCOREIMPORTNAME");
    }

    if (!checkToolchain(Toolchain.DCC32, Toolchain.DCC64, Toolchain.DCCOSX)) {
      result.add("WEAKREF");
      result.add("WEAKINTFREF");
    }

    if (checkToolchain(Toolchain.DCCIOSSIMARM64)) {
      result.add("IOSSIMULATOR");
    }

    return result;
  }

  private Set<String> getConditionalDefines() {
    Set<String> result = new HashSet<>();
    result.addAll(getCompilerConditionalDefines());
    result.addAll(getPlatformConditionalDefines());
    result.addAll(getCPUConditionalDefines());
    result.addAll(getAvailabilityConditionalDefines());
    return result;
  }

  /**
   * Returns the conditional symbols that should be predefined on this compiler toolchain and
   * version
   *
   * @param toolchain The compiler toolchain
   * @param compilerVersion The compiler version
   * @return set of conditional symbols that are defined on this compiler toolchain
   * @see <a href="http://bit.ly/delphi_predefined_conditionals">Predefined Conditionals</a>
   */
  public static Set<String> getConditionalDefines(
      Toolchain toolchain, CompilerVersion compilerVersion) {
    PredefinedConditionals predefined = new PredefinedConditionals(toolchain, compilerVersion);
    return predefined.getConditionalDefines();
  }
}
