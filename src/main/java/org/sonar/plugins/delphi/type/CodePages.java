package org.sonar.plugins.delphi.type;

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
