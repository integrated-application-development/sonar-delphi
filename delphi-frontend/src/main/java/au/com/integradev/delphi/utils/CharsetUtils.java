/*
 * Sonar Delphi Plugin
 * Copyright (C) 2026 Integrated Application Development
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

import com.google.common.collect.ImmutableMap;
import java.nio.charset.Charset;
import java.util.Map;
import org.sonar.plugins.communitydelphi.api.type.CodePages;

public final class CharsetUtils {
  private static final String NATIVE_ENCODING_PROPERTY = "native.encoding";
  private static final Map<Integer, String> CODE_PAGE_CHARSET_NAMES =
      ImmutableMap.<Integer, String>builder()
          .put(37, "IBM037")
          .put(437, "IBM437")
          .put(500, "IBM500")
          .put(708, "ASMO-708")
          .put(720, "x-IBM720")
          .put(737, "x-IBM737")
          .put(775, "IBM775")
          .put(850, "IBM850")
          .put(852, "IBM852")
          .put(855, "IBM855")
          .put(857, "IBM857")
          .put(858, "IBM00858")
          .put(860, "IBM860")
          .put(861, "IBM861")
          .put(862, "IBM862")
          .put(863, "IBM863")
          .put(864, "IBM864")
          .put(865, "IBM865")
          .put(866, "IBM866")
          .put(869, "IBM869")
          .put(870, "IBM870")
          .put(874, "x-windows-874")
          .put(875, "x-IBM875")
          .put(932, "windows-31j")
          .put(936, "GBK")
          .put(949, "x-windows-949")
          .put(950, "Big5")
          .put(1026, "IBM1026")
          .put(1047, "IBM1047")
          .put(1140, "IBM01140")
          .put(1141, "IBM01141")
          .put(1142, "IBM01142")
          .put(1143, "IBM01143")
          .put(1144, "IBM01144")
          .put(1145, "IBM01145")
          .put(1146, "IBM01146")
          .put(1147, "IBM01147")
          .put(1148, "IBM01148")
          .put(1149, "IBM01149")
          .put(1200, "UTF-16LE")
          .put(1201, "UTF-16BE")
          .put(1250, "windows-1250")
          .put(1251, "windows-1251")
          .put(1252, "windows-1252")
          .put(1253, "windows-1253")
          .put(1254, "windows-1254")
          .put(1255, "windows-1255")
          .put(1256, "windows-1256")
          .put(1257, "windows-1257")
          .put(1258, "windows-1258")
          .put(1361, "x-Johab")
          .put(10000, "x-MacRoman")
          .put(10001, "x-MacJapanese") // Unsupported on standard JVMs
          .put(10002, "x-MacChineseTrad") // Unsupported on standard JVMs
          .put(10003, "x-MacKorean") // Unsupported on standard JVMs
          .put(10004, "x-MacArabic")
          .put(10005, "x-MacHebrew")
          .put(10006, "x-MacGreek")
          .put(10007, "x-MacCyrillic")
          .put(10008, "x-MacChineseSimp") // Unsupported on standard JVMs
          .put(10010, "x-MacRomania")
          .put(10017, "x-MacUkraine")
          .put(10021, "x-MacThai")
          .put(10029, "x-MacCentralEurope")
          .put(10079, "x-MacIceland")
          .put(10081, "x-MacTurkish")
          .put(10082, "x-MacCroatian")
          .put(12000, "UTF-32LE")
          .put(12001, "UTF-32BE")
          .put(20000, "x-EUC-TW")
          .put(20001, "x-cp20001") // Unsupported on standard JVMs (Traditional Chinese TCA Taiwan)
          .put(20002, "x-cp20002") // Unsupported on standard JVMs (Traditional Chinese Eten)
          .put(20003, "x-cp20003") // Unsupported on standard JVMs (IBM EBCDIC Korea Extended)
          .put(20004, "x-cp20004") // Unsupported on standard JVMs (IBM EBCDIC Japan)
          .put(20005, "x-cp20005") // Unsupported on standard JVMs (IBM EBCDIC Japan variant)
          .put(20105, "US-ASCII") // Equivalent mapping (x-IA5)
          .put(20106, "x-IA5-German") // Unsupported on standard JVMs
          .put(20107, "x-IA5-Swedish") // Unsupported on standard JVMs
          .put(20108, "x-IA5-Norwegian") // Unsupported on standard JVMs
          .put(20127, "US-ASCII")
          .put(20261, "x-cp20261") // Unsupported on standard JVMs (ITU T.61)
          .put(20269, "x-cp20269") // Unsupported on standard JVMs (ITU T.51)
          .put(20273, "IBM273")
          .put(20277, "IBM277")
          .put(20278, "IBM278")
          .put(20280, "IBM280")
          .put(20284, "IBM284")
          .put(20285, "IBM285")
          .put(20290, "IBM290")
          .put(20297, "IBM297")
          .put(20420, "IBM420")
          .put(20423, "IBM423") // Supported on IBM JVMs
          .put(20424, "IBM424")
          .put(20833, "x-EBCDIC-KoreanExtended") // Unsupported on standard JVMs
          .put(20838, "IBM-Thai")
          .put(20866, "KOI8-R")
          .put(20871, "IBM871")
          .put(20880, "IBM880") // Supported on IBM JVMs
          .put(20905, "IBM905") // Supported on IBM JVMs
          .put(20924, "IBM00924") // Supported on IBM JVMs
          .put(20932, "EUC-JP")
          .put(20936, "GB2312")
          .put(20949, "EUC-KR")
          .put(21025, "x-IBM1025")
          .put(21866, "KOI8-U")
          .put(28591, "ISO-8859-1")
          .put(28592, "ISO-8859-2")
          .put(28593, "ISO-8859-3")
          .put(28594, "ISO-8859-4")
          .put(28595, "ISO-8859-5")
          .put(28596, "ISO-8859-6")
          .put(28597, "ISO-8859-7")
          .put(28598, "ISO-8859-8")
          .put(28599, "ISO-8859-9")
          .put(28603, "ISO-8859-13")
          .put(28605, "ISO-8859-15")
          .put(29001, "x-Europa") // Unsupported on standard JVMs
          .put(38598, "ISO-8859-8")
          .put(50220, "ISO-2022-JP")
          .put(50221, "x-windows-50221")
          .put(50222, "x-windows-50221")
          .put(50225, "ISO-2022-KR")
          .put(50227, "x-ISO-2022-CN-GB")
          .put(51932, "EUC-JP")
          .put(51936, "EUC-CN")
          .put(51949, "EUC-KR")
          .put(52936, "hz-gb-2312") // Unsupported on standard JVMs
          .put(54936, "GB18030")
          .put(57002, "x-ISCII91") // We map all ISCII codepages to the generic x-ISCII91 charset
          .put(57003, "x-ISCII91")
          .put(57004, "x-ISCII91")
          .put(57005, "x-ISCII91")
          .put(57006, "x-ISCII91")
          .put(57007, "x-ISCII91")
          .put(57008, "x-ISCII91")
          .put(57009, "x-ISCII91")
          .put(57010, "x-ISCII91")
          .put(57011, "x-ISCII91")
          .put(65000, "UTF-7") // Unsupported on all JVMs
          .put(65001, "UTF-8")
          .buildOrThrow();

  private CharsetUtils() {
    // Utility class
  }

  public static final class UnsupportedCodePageException extends IllegalArgumentException {
    UnsupportedCodePageException(int codePage) {
      super("Unsupported code page: " + codePage);
    }
  }

  public static Charset nativeCharset() {
    String charsetName = System.getProperty(NATIVE_ENCODING_PROPERTY);

    if (charsetName != null && !charsetName.isBlank() && Charset.isSupported(charsetName)) {
      return Charset.forName(charsetName);
    }

    return Charset.defaultCharset();
  }

  public static Charset charsetForCodePage(int codePage) {
    if (codePage == CodePages.CP_ACP) {
      return nativeCharset();
    }

    String charsetName = CODE_PAGE_CHARSET_NAMES.get(codePage);
    if (charsetName == null || !Charset.isSupported(charsetName)) {
      throw new UnsupportedCodePageException(codePage);
    }

    return Charset.forName(charsetName);
  }
}
