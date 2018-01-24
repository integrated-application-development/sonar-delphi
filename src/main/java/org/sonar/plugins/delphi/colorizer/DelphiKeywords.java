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
package org.sonar.plugins.delphi.colorizer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * DelphiLanguage keywords used for code colorization.
 */
public final class DelphiKeywords {

  private static final Set<String> DELPHI_KEYWORDS = new HashSet<>();
  static {
    Collections.addAll(DELPHI_KEYWORDS, "private", "public", "protected", "and", "array", "as", "begin", "case",
      "class", "const",
      "constructor", "destructor", "div", "do", "downto", "else", "end", "except", "file", "finally", "for",
      "goto", "if",
      "implementation", "in", "inherited", "system", "interface", "is", "mod", "not", "system", "object",
      "of", "on", "or", "packed",
      "procedure", "function", "program", "property", "raise", "record", "repeat", "set", "shl", "shr",
      "then", "threadvar", "to", "try",
      "type", "unit", "until", "uses", "var", "while", "with", "xor", "Private", "Public", "Protected",
      "And", "Array", "As", "Begin",
      "Case", "Class", "Const", "Constructor", "Destructor", "Div", "Do", "DownTo", "Else", "End", "Except",
      "File", "Finally", "For",
      "Goto", "If", "Implementation", "In", "Inherited", "System", "Interface", "Is", "Mod", "Not", "System",
      "Object", "Of", "On", "Or",
      "Packed", "Procedure", "Function", "Program", "Property", "Raise", "Record", "Repeat", "Set", "Shl",
      "Shr", "Then", "ThreadVar",
      "To", "Try", "Type", "Unit", "Until", "Uses", "Var", "While", "With", "Xor", "PRIVATE", "PUBLIC",
      "PROTECTED", "AND", "ARRAY",
      "AS", "BEGIN", "CASE", "CLASS", "CONST", "CONSTRUCTOR", "DESTRUCTOR", "DIV", "DO", "DOWNTO", "ELSE",
      "END", "EXCEPT", "FILE",
      "FINALLY", "FOR", "GOTO", "IF", "IMPLEMENTATION", "IN", "INHERITED", "SYSTEM", "INTERFACE", "IS",
      "MOD", "NOT", "SYSTEM", "OBJECT",
      "OF", "ON", "OR", "PACKED", "PROCEDURE", "FUNCTION", "PROGRAM", "PROPERTY", "RAISE", "RECORD",
      "REPEAT", "SET", "SHL", "SHR",
      "THEN", "THREADVAR", "TO", "TRY", "TYPE", "UNIT", "UNTIL", "USES", "VAR", "WHILE", "WITH", "XOR",
      "out", "OUT", "Out",
      "initialization", "Initialization", "INITIALIZATION", "finalization", "Finalization", "FINALIZATION");
  }

  private DelphiKeywords() {
  }

  /**
   * @return set of Delphi language keywords
   */
  public static Set<String> get() {
    return Collections.unmodifiableSet(DELPHI_KEYWORDS);
  }

}
