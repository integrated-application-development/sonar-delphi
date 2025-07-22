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
package au.com.integradev.delphi.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.plugins.communitydelphi.api.type.CodePages;

class CharsetUtilsTest {
  @Test
  void testAcpCodePage() {
    String originalNativeEncoding = System.getProperty("native.encoding");
    try {
      System.setProperty("native.encoding", StandardCharsets.ISO_8859_1.name());
      assertThat(CharsetUtils.charsetForCodePage(CodePages.CP_ACP))
          .isEqualTo(StandardCharsets.ISO_8859_1);
    } finally {
      if (originalNativeEncoding != null) {
        System.setProperty("native.encoding", originalNativeEncoding);
      } else {
        System.clearProperty("native.encoding");
      }
    }
  }

  @ParameterizedTest
  @CsvSource({
    "37,IBM037",
    "437,IBM437",
    "708,ASMO-708",
    "874,x-windows-874",
    "862,IBM862",
    "866,IBM866",
    "932,windows-31j",
    "936,GBK",
    "949,x-windows-949",
    "950,Big5",
    "1047,IBM1047",
    "1140,IBM01140",
    "1200,UTF-16LE",
    "1201,UTF-16BE",
    "10000,x-MacRoman",
    "10004,x-MacArabic",
    "10029,x-MacCentralEurope",
    "12000,UTF-32LE",
    "12001,UTF-32BE",
    "20127,US-ASCII",
    "20273,IBM273",
    "20420,IBM420",
    "20866,KOI8-R",
    "20838,IBM-Thai",
    "21866,KOI8-U",
    "21025,cp1025",
    "28591,ISO-8859-1",
    "50225,ISO-2022-KR",
    "51932,EUC-JP",
    "51936,GB2312",
    "51949,EUC-KR",
    "54936,GB18030",
    "1252,windows-1252",
    "1361,x-Johab"
  })
  void testCodePagesResolveExpectedCharsets(int codePage, String expectedCharsetName) {
    assertThat(CharsetUtils.charsetForCodePage(codePage))
        .isEqualTo(Charset.forName(expectedCharsetName));
  }

  @ParameterizedTest
  @ValueSource(ints = {720, 10001, 29001, 65000})
  void testMappedButUnsupportedCodePageThrows(int codePage) {
    assertThatThrownBy(() -> CharsetUtils.charsetForCodePage(codePage))
        .isInstanceOf(CharsetUtils.UnsupportedCodePageException.class)
        .hasMessage("Unsupported code page: " + codePage);
  }

  @ParameterizedTest
  @ValueSource(ints = {709, 710, 21027, 50229, 51950})
  void testCodePageOutsideTableThrows(int codePage) {
    assertThatThrownBy(() -> CharsetUtils.charsetForCodePage(codePage))
        .isInstanceOf(CharsetUtils.UnsupportedCodePageException.class)
        .hasMessage("Unsupported code page: " + codePage);
  }
}
