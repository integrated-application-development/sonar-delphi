/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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
package au.com.integradev.delphi.checks;

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;

class ImplicitDefaultEncodingCheckTest {
  private DelphiTestUnitBuilder getSystem() {
    return new DelphiTestUnitBuilder()
        .unitName("System")
        .appendDecl("type")
        .appendDecl("  TObject = class")
        .appendDecl("  end;")
        .appendDecl("  IInterface = interface")
        .appendDecl("  end;")
        .appendDecl("  TClassHelperBase = class")
        .appendDecl("  end;")
        .appendDecl("  TVarRec = record")
        .appendDecl("  end;")
        .appendDecl("  RawByteString = type AnsiString($ffff);")
        .appendDecl("  TArray<T> = array of T;");
  }

  private DelphiTestUnitBuilder getSystemClasses() {
    return new DelphiTestUnitBuilder()
        .unitName("System.Classes")
        .appendDecl("uses System.SysUtils;")
        .appendDecl("type")
        .appendDecl("  TStream = class(TObject)")
        .appendDecl("  end;")
        .appendDecl("  TStrings = class(TObject)")
        .appendDecl("  public")
        .appendDecl("    procedure LoadFromFile(const FileName: string); overload; virtual;")
        .appendDecl("    procedure LoadFromFile(")
        .appendDecl("      const FileName: string; Encoding: TEncoding); overload; virtual;")
        .appendDecl("    procedure LoadFromStream(Stream: TStream); overload; virtual;")
        .appendDecl("    procedure LoadFromStream(")
        .appendDecl("      Stream: TStream; Encoding: TEncoding); overload; virtual;")
        .appendDecl("    procedure SaveToFile(const FileName: string); overload; virtual;")
        .appendDecl("    procedure SaveToFile(")
        .appendDecl("      const FileName: string; Encoding: TEncoding); overload; virtual;")
        .appendDecl("    procedure SaveToStream(Stream: TStream); overload; virtual;")
        .appendDecl("    procedure SaveToStream(")
        .appendDecl("      Stream: TStream; Encoding: TEncoding); overload; virtual;")
        .appendDecl("  end;")
        .appendDecl("  TBytesStream = class(TStream);")
        .appendDecl("  TStringStream = class(TBytesStream)")
        .appendDecl("  public")
        .appendDecl("    constructor Create; overload;")
        .appendDecl("    constructor Create(const AString: string); overload;")
        .appendDecl("    constructor Create(const AString: RawByteString); overload;")
        .appendDecl("    constructor Create(")
        .appendDecl("      const AString: string;")
        .appendDecl("      AEncoding: TEncoding;")
        .appendDecl("      AOwnsEncoding: Boolean = True")
        .appendDecl("    ); overload;")
        .appendDecl("    constructor Create(const AString: string; ACodePage: Integer); overload;")
        .appendDecl("    constructor Create(const ABytes: TBytes); overload;")
        .appendDecl("  end;")
        .appendDecl("  TStreamReader = class(TObject)")
        .appendDecl("  public")
        .appendDecl("    constructor Create(Stream: TStream); overload;")
        .appendDecl("    constructor Create(Stream: TStream; DetectBOM: Boolean); overload;")
        .appendDecl("    constructor Create(")
        .appendDecl("      Stream: TStream;")
        .appendDecl("      Encoding: TEncoding;")
        .appendDecl("      DetectBOM: Boolean = False;")
        .appendDecl("      BufferSize: Integer = 4096")
        .appendDecl("    ); overload;")
        .appendDecl("    constructor Create(const Filename: string); overload;")
        .appendDecl("    constructor Create(const Filename: string; DetectBOM: Boolean); overload;")
        .appendDecl("    constructor Create(const Filename: string; Encoding: TEncoding;")
        .appendDecl("    DetectBOM: Boolean = False; BufferSize: Integer = 4096); overload;")
        .appendDecl("  end;")
        .appendDecl("  TStreamWriter = class(TObject)")
        .appendDecl("  public")
        .appendDecl("    constructor Create(Stream: TStream); overload;")
        .appendDecl("    constructor Create(")
        .appendDecl("      Stream: TStream;")
        .appendDecl("      Encoding: TEncoding;")
        .appendDecl("      BufferSize: Integer = 4096")
        .appendDecl("    ); overload;")
        .appendDecl("    constructor Create(")
        .appendDecl("      const Filename: string;")
        .appendDecl("      Append: Boolean = False")
        .appendDecl("    ); overload;")
        .appendDecl("    constructor Create(")
        .appendDecl("      const Filename: string;")
        .appendDecl("      Append: Boolean;")
        .appendDecl("      Encoding: TEncoding;")
        .appendDecl("      BufferSize: Integer = 4096")
        .appendDecl("    ); overload;")
        .appendDecl("  end;");
  }

  private DelphiTestUnitBuilder getSystemSysUtils() {
    return new DelphiTestUnitBuilder()
        .unitName("System.SysUtils")
        .appendDecl("type")
        .appendDecl("  TBytes = TArray<Byte>;")
        .appendDecl("  TEncoding = class(TObject)")
        .appendDecl("  end;");
  }

  private DelphiTestUnitBuilder getVclOutline() {
    return new DelphiTestUnitBuilder()
        .unitName("Vcl.Outline")
        .appendDecl("uses System.SysUtils, System.Classes;")
        .appendDecl("type")
        .appendDecl("  TOutlineNode = class(TObject)")
        .appendDecl("  public")
        .appendDecl("     procedure WriteNode(")
        .appendDecl("       Buffer: TBytes;")
        .appendDecl("       Stream: TStream")
        .appendDecl("     ); overload; deprecated;")
        .appendDecl("     procedure WriteNode(")
        .appendDecl("       Buffer: PChar;")
        .appendDecl("       Stream: TStream")
        .appendDecl("     ); overload; deprecated;")
        .appendDecl("     procedure WriteNode(Stream: TStream; Encoding: TEncoding); overload;")
        .appendDecl("  end;")
        .appendDecl("  TCustomOutline = class(TObject)")
        .appendDecl("  public")
        .appendDecl("    procedure LoadFromFile(const FileName: string); overload;")
        .appendDecl("    procedure LoadFromFile(")
        .appendDecl("      const FileName: string;")
        .appendDecl("      Encoding: TEncoding")
        .appendDecl("    ); overload;")
        .appendDecl("    procedure LoadFromStream(Stream: TStream); overload;")
        .appendDecl("    procedure LoadFromStream(Stream: TStream; Encoding: TEncoding); overload;")
        .appendDecl("    procedure SaveToFile(const FileName: string); overload;")
        .appendDecl("    procedure SaveToFile(")
        .appendDecl("      const FileName: string;")
        .appendDecl("      Encoding: TEncoding")
        .appendDecl("    ); overload;")
        .appendDecl("    procedure SaveToFile(")
        .appendDecl("      const FileName: string;")
        .appendDecl("      Encoding: TEncoding")
        .appendDecl("    ); overload;")
        .appendDecl("    procedure SaveToStream(Stream: TStream); overload;")
        .appendDecl("    procedure SaveToStream(Stream: TStream; Encoding: TEncoding); overload;")
        .appendDecl("  end; ");
  }

  @Test
  void testUnrelatedFunctionInvocationShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ImplicitDefaultEncodingCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Unrelated; begin end;")
                .appendImpl("procedure Unrelated(Str: String); begin end;")
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  Unrelated;")
                .appendImpl("  Unrelated('abc');")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testPermittedOverloadsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ImplicitDefaultEncodingCheck())
        .withStandardLibraryUnit(getSystem())
        .withStandardLibraryUnit(getSystemSysUtils())
        .withStandardLibraryUnit(getSystemClasses())
        .withStandardLibraryUnit(getVclOutline())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils, System.Classes, Vcl.Controls;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Node: TOutlineNode;")
                .appendImpl("  Outline: TCustomOutline;")
                .appendImpl("  Strings: TStrings;")
                .appendImpl("  StringStream: TStringStream;")
                .appendImpl("  StreamReader: TStreamReader;")
                .appendImpl("  StreamWriter: TStreamWriter;")
                .appendImpl("  StringArg: String;")
                .appendImpl("  IntegerArg: Integer;")
                .appendImpl("  BooleanArg: Boolean;")
                .appendImpl("  Encoding: TEncoding;")
                .appendImpl("begin")
                .appendImpl("  Node.WriteNode(StreamArg, Encoding);")
                .appendImpl("  Outline.LoadFromFile(StringArg, Encoding);")
                .appendImpl("  Outline.LoadFromStream(StreamArg, Encoding);")
                .appendImpl("  Outline.SaveToFile(StringArg, Encoding);")
                .appendImpl("  Outline.SaveToStream(StreamArg, Encoding);")
                .appendImpl("  Strings.LoadFromFile(StringArg, Encoding);")
                .appendImpl("  Strings.LoadFromStream(StreamArg, Encoding);")
                .appendImpl("  Strings.SaveToFile(StringArg, Encoding);")
                .appendImpl("  Strings.SaveToStream(StreamArg, Encoding);")
                .appendImpl("  StringStream.Create(StringArg, Encoding);")
                .appendImpl("  StringStream.Create(StringArg, Encoding, BooleanArg);")
                .appendImpl("  StringStream.Create(StringArg, nil);")
                .appendImpl("  StringStream.Create(StringArg, nil, BooleanArg);")
                .appendImpl("  StringStream.Create(StringArg, IntegerArg);")
                .appendImpl("  StreamReader.Create(StreamArg, Encoding);")
                .appendImpl("  StreamReader.Create(StringArg, Encoding);")
                .appendImpl("  StreamReader.Create(StreamArg, Encoding, BooleanArg);")
                .appendImpl("  StreamReader.Create(StringArg, Encoding, BooleanArg);")
                .appendImpl("  StreamReader.Create(StreamArg, Encoding, BooleanArg, IntegerArg);")
                .appendImpl("  StreamReader.Create(StringArg, Encoding, BooleanArg, IntegerArg);")
                .appendImpl("  StreamWriter.Create(StreamArg, Encoding);")
                .appendImpl("  StreamWriter.Create(StreamArg, Encoding, IntegerArg);")
                .appendImpl("  StreamWriter.Create(StringArg, BooleanArg, Encoding);")
                .appendImpl("  StreamWriter.Create(StringArg, BooleanArg, Encoding, IntegerArg);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testForbiddenOverloadsShouldAddIssues() {
    CheckVerifier.newVerifier()
        .withCheck(new ImplicitDefaultEncodingCheck())
        .withStandardLibraryUnit(getSystem())
        .withStandardLibraryUnit(getSystemSysUtils())
        .withStandardLibraryUnit(getSystemClasses())
        .withStandardLibraryUnit(getVclOutline())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils, System.Classes, Vcl.Outline;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Node: TOutlineNode;")
                .appendImpl("  Outline: TCustomOutline;")
                .appendImpl("  Strings: TStrings;")
                .appendImpl("  StringStream: TStringStream;")
                .appendImpl("  StreamReader: TStreamReader;")
                .appendImpl("  StreamWriter: TStreamWriter;")
                .appendImpl("  StringArg: String;")
                .appendImpl("  PCharArg: PChar;")
                .appendImpl("  StreamArg: TStream;")
                .appendImpl("  RawByteStringArg: RawByteString;")
                .appendImpl("  BytesArg: TBytes;")
                .appendImpl("  BooleanArg: Boolean;")
                .appendImpl("begin")
                .appendImpl("  Node.WriteNode(BytesArg, StreamArg); // Noncompliant")
                .appendImpl("  Node.WriteNode(PCharArg, StreamArg); // Noncompliant")
                .appendImpl("  Outline.LoadFromFile(StringArg); // Noncompliant")
                .appendImpl("  Outline.LoadFromStream(StreamArg); // Noncompliant")
                .appendImpl("  Outline.SaveToFile(StringArg); // Noncompliant")
                .appendImpl("  Outline.SaveToStream(StreamArg); // Noncompliant")
                .appendImpl("  Strings.LoadFromFile(StringArg); // Noncompliant")
                .appendImpl("  Strings.LoadFromStream(StreamArg); // Noncompliant")
                .appendImpl("  Strings.SaveToFile(StringArg); // Noncompliant")
                .appendImpl("  Strings.SaveToStream(StreamArg); // Noncompliant")
                .appendImpl("  StringStream.Create; // Noncompliant")
                .appendImpl("  StringStream.Create(StringArg); // Noncompliant")
                .appendImpl("  StringStream.Create(RawByteStringArg); // Noncompliant")
                .appendImpl("  StringStream.Create(BytesArg); // Noncompliant")
                .appendImpl("  StreamReader.Create(StreamArg); // Noncompliant")
                .appendImpl("  StreamReader.Create(StreamArg, BooleanArg); // Noncompliant")
                .appendImpl("  StreamWriter.Create(StreamArg); // Noncompliant")
                .appendImpl("  StreamWriter.Create(StringArg); // Noncompliant")
                .appendImpl("  StreamWriter.Create(StringArg, BooleanArg); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }
}
