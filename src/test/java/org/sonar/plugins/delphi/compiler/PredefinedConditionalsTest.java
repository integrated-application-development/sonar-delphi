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
package org.sonar.plugins.delphi.compiler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

class PredefinedConditionalsTest {
  private static final CompilerVersion VERSION_5 = CompilerVersion.fromVersionNumber("13.0");
  private static final CompilerVersion VERSION_2007 = CompilerVersion.fromVersionNumber("18.5");
  private static final CompilerVersion VERSION_TOKYO = CompilerVersion.fromVersionNumber("32.0");
  private static final CompilerVersion VERSION_RIO = CompilerVersion.fromVersionNumber("33.0");
  private static final CompilerVersion VERSION_SYDNEY = CompilerVersion.fromVersionNumber("34.0");

  private static final Set<String> NEXT_GEN_FEATURES =
      Set.of("NEXTGEN", "AUTOREFCOUNT", "WEAKINSTREF");

  @Test
  void testDDC32() {
    assertThat(PredefinedConditionals.getConditionalDefines(Toolchain.DCC32, VERSION_RIO))
        .containsExactlyInAnyOrder(
            "DCC",
            "VER330",
            "CONSOLE",
            "NATIVECODE",
            "MSWINDOWS",
            "WIN32",
            "CPU386",
            "CPUX86",
            "CPU32BITS",
            "ASSEMBLER",
            "UNICODE",
            "CONDITIONALEXPRESSIONS",
            "UNDERSCOREIMPORTNAME");
  }

  @Test
  void testDDC64() {
    assertThat(PredefinedConditionals.getConditionalDefines(Toolchain.DCC64, VERSION_RIO))
        .containsExactlyInAnyOrder(
            "DCC",
            "VER330",
            "CONSOLE",
            "NATIVECODE",
            "MSWINDOWS",
            "WIN64",
            "CPUX64",
            "CPU64BITS",
            "ASSEMBLER",
            "UNICODE",
            "CONDITIONALEXPRESSIONS");
  }

  @Test
  void testDDCOSX() {
    assertThat(PredefinedConditionals.getConditionalDefines(Toolchain.DCCOSX, VERSION_RIO))
        .containsExactlyInAnyOrder(
            "DCC",
            "VER330",
            "CONSOLE",
            "NATIVECODE",
            "MACOS",
            "MACOS32",
            "POSIX",
            "POSIX32",
            "CPU386",
            "CPUX86",
            "CPU32BITS",
            "ALIGN_STACK",
            "ASSEMBLER",
            "UNICODE",
            "CONDITIONALEXPRESSIONS",
            "PC_MAPPED_EXCEPTIONS",
            "PIC",
            "UNDERSCOREIMPORTNAME");
  }

  @Test
  void testDDCOSX64() {
    assertThat(PredefinedConditionals.getConditionalDefines(Toolchain.DCCOSX64, VERSION_RIO))
        .containsExactlyInAnyOrder(
            "DCC",
            "VER330",
            "CONSOLE",
            "NATIVECODE",
            "MACOS",
            "MACOS64",
            "POSIX",
            "POSIX64",
            "CPU386",
            "CPUX64",
            "CPU64BITS",
            "AUTOREFCOUNT",
            "EXTERNALLINKER",
            "UNICODE",
            "CONDITIONALEXPRESSIONS",
            "NEXTGEN",
            "PIC",
            "WEAKREF",
            "WEAKINSTREF",
            "WEAKINTFREF");
  }

  @Test
  void testDDCIOSARM() {
    assertThat(PredefinedConditionals.getConditionalDefines(Toolchain.DCCIOSARM, VERSION_RIO))
        .containsExactlyInAnyOrder(
            "DCC",
            "VER330",
            "CONSOLE",
            "NATIVECODE",
            "IOS",
            "IOS32",
            "POSIX",
            "POSIX32",
            "CPU32BITS",
            "CPUARM",
            "CPUARM32",
            "AUTOREFCOUNT",
            "EXTERNALLINKER",
            "UNICODE",
            "CONDITIONALEXPRESSIONS",
            "NEXTGEN",
            "PIC",
            "WEAKREF",
            "WEAKINSTREF",
            "WEAKINTFREF");
  }

  @Test
  void testDDCIOS32() {
    assertThat(PredefinedConditionals.getConditionalDefines(Toolchain.DCCIOS32, VERSION_RIO))
        .containsExactlyInAnyOrder(
            "DCC",
            "VER330",
            "CONSOLE",
            "NATIVECODE",
            "IOS",
            "IOS32",
            "POSIX",
            "POSIX32",
            "CPU386",
            "CPUX86",
            "CPU32BITS",
            "ALIGN_STACK",
            "ASSEMBLER",
            "AUTOREFCOUNT",
            "UNICODE",
            "CONDITIONALEXPRESSIONS",
            "NEXTGEN",
            "PC_MAPPED_EXCEPTIONS",
            "PIC",
            "UNDERSCOREIMPORTNAME",
            "WEAKREF",
            "WEAKINSTREF",
            "WEAKINTFREF");
  }

  @Test
  void testDCCAARM() {
    assertThat(PredefinedConditionals.getConditionalDefines(Toolchain.DCCAARM, VERSION_RIO))
        .containsExactlyInAnyOrder(
            "DCC",
            "VER330",
            "CONSOLE",
            "NATIVECODE",
            "ANDROID",
            "ANDROID32",
            "POSIX",
            "POSIX32",
            "CPU32BITS",
            "CPUARM",
            "CPUARM32",
            "AUTOREFCOUNT",
            "EXTERNALLINKER",
            "UNICODE",
            "CONDITIONALEXPRESSIONS",
            "NEXTGEN",
            "PIC",
            "WEAKREF",
            "WEAKINSTREF",
            "WEAKINTFREF");
  }

  @Test
  void testDCCIOSARM64() {
    assertThat(PredefinedConditionals.getConditionalDefines(Toolchain.DCCIOSARM64, VERSION_RIO))
        .containsExactlyInAnyOrder(
            "DCC",
            "VER330",
            "CONSOLE",
            "NATIVECODE",
            "IOS",
            "IOS64",
            "POSIX",
            "POSIX64",
            "CPU64BITS",
            "CPUARM",
            "CPUARM64",
            "AUTOREFCOUNT",
            "EXTERNALLINKER",
            "UNICODE",
            "CONDITIONALEXPRESSIONS",
            "NEXTGEN",
            "PIC",
            "WEAKREF",
            "WEAKINSTREF",
            "WEAKINTFREF");
  }

  @Test
  void testDCCLINUX64() {
    assertThat(PredefinedConditionals.getConditionalDefines(Toolchain.DCCLINUX64, VERSION_RIO))
        .containsExactlyInAnyOrder(
            "DCC",
            "VER330",
            "CONSOLE",
            "NATIVECODE",
            "LINUX",
            "LINUX64",
            "POSIX",
            "POSIX64",
            "CPUX64",
            "CPU64BITS",
            "EXTERNALLINKER",
            "UNICODE",
            "CONDITIONALEXPRESSIONS",
            "ELF",
            "PIC",
            "WEAKREF",
            "WEAKINTFREF");
  }

  @Test
  void testDCCAARM64() {
    assertThat(PredefinedConditionals.getConditionalDefines(Toolchain.DCCAARM64, VERSION_RIO))
        .containsExactlyInAnyOrder(
            "DCC",
            "VER330",
            "CONSOLE",
            "NATIVECODE",
            "ANDROID",
            "ANDROID64",
            "POSIX",
            "POSIX64",
            "CPU64BITS",
            "CPUARM",
            "CPUARM64",
            "EXTERNALLINKER",
            "UNICODE",
            "CONDITIONALEXPRESSIONS",
            "PIC",
            "WEAKREF",
            "WEAKINTFREF");
  }

  @Test
  void testDelphi2007ShouldBeBinaryCompatibleWithDelphi2006() {
    assertThat(PredefinedConditionals.getConditionalDefines(Toolchain.DCC32, VERSION_2007))
        .contains("VER180", "VER185");
  }

  @Test
  void testVersionsBeforeDelphi6ShouldNotSupportConditionalExpressions() {
    assertThat(PredefinedConditionals.getConditionalDefines(Toolchain.DCC32, VERSION_5))
        .doesNotContain("CONDITIONALEXPRESSIONS");
  }

  @Test
  void testVersionsBeforeDelphi2009ShouldNotSupportUnicode() {
    assertThat(PredefinedConditionals.getConditionalDefines(Toolchain.DCC32, VERSION_2007))
        .doesNotContain("UNICODE");
  }

  @Test
  void testVersionsAfterDelphiRioShouldNotSupportNextGenFeatures() {
    assertThat(PredefinedConditionals.getConditionalDefines(Toolchain.DCC32, VERSION_SYDNEY))
        .doesNotContainAnyElementsOf(NEXT_GEN_FEATURES);
  }

  @Test
  void testLinuxOnlySupportsNextGenFeaturesBeforeDelphiRio() {
    assertThat(PredefinedConditionals.getConditionalDefines(Toolchain.DCCLINUX64, VERSION_TOKYO))
        .containsAll(NEXT_GEN_FEATURES);

    assertThat(PredefinedConditionals.getConditionalDefines(Toolchain.DCCLINUX64, VERSION_RIO))
        .doesNotContainAnyElementsOf(NEXT_GEN_FEATURES);
  }
}
