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
package org.sonar.plugins.communitydelphi.api.type;

/**
 * Constants defining Windows code pages, commonly called <i>ANSI code pages</i>.
 *
 * @see <a href="http://docwiki.embarcadero.com/RADStudio/en/Character_Sets#Single-byte_Characters">
 *     Character Sets: Single-byte Characters</a>
 * @see <a href="https://docs.microsoft.com/en-us/windows/win32/intl/code-page-identifiers">Code
 *     Page Identifiers</a>
 */
public final class CodePages {
  /** The system default codepage, used for AnsiString without codepage specified */
  public static final int CP_ACP = 0;

  /**
   * windows-1252 codepage, used by default in the legacy components of Microsoft Windows for
   * English and many European languages including Spanish, French, and German.
   */
  public static final int CP_1252 = 1252;

  /** UTF8 codepage, used for System.UTF8String */
  public static final int CP_UTF8 = 65001;

  /** No codepage, used for System.RawByteString */
  public static final int CP_NONE = 65535;

  private CodePages() {
    // constants class
  }
}
