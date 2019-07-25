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
package org.sonar.plugins.delphi.antlr.resolvers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.util.HashSet;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.pmd.DelphiTestUnitBuilder;

public class DefineResolverTest {

  private DefineResolver resolver;
  private SourceResolverResults results;

  @Before
  public void setup() {
    resolver = new DefineResolver(new HashSet<>());
  }

  @Test
  public void testDefinitionsIncludes() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("{$if defined(FPC) or (CompilerVersion >= 17)} //Delphi 2005 up");
    builder.appendDecl("  {$DEFINE HAVE_INLINE}");
    builder.appendDecl("{$ifend}");
    builder.appendDecl("{$IFDEF FPC}");
    builder.appendDecl("  {$MODE OBJFPC}{$H+}");
    builder.appendDecl("{$ENDIF}");
    builder.appendDecl("");
    builder.appendDecl("{$DEFINE SUPER_METHOD}");
    builder.appendDecl("{$DEFINE WINDOWSNT_COMPATIBILITY}");
    builder.appendDecl("{.$DEFINE DEBUG} // track memory leack");
    builder.appendDecl("");
    builder.appendDecl("");
    builder.appendDecl("{$if CompilerVersion >= 21} //DELPHI 2010 UP");
    builder.appendDecl("  {$define HAVE_RTTI}");
    builder.appendDecl("{$ifend}");
    builder.appendDecl("");
    builder.appendDecl("{$if CompilerVersion >= 22} //Delphi XE Up");
    builder.appendDecl("  {$define NEED_FORMATSETTINGS}");
    builder.appendDecl("{$ifend}");
    builder.appendDecl("");
    builder.appendDecl("{$if defined(FPC) and defined(VER2_6)}");
    builder.appendDecl("  {$define NEED_FORMATSETTINGS}");
    builder.appendDecl("{$ifend}");
    builder.appendDecl("");
    builder.appendDecl("{$OVERFLOWCHECKS OFF}");
    builder.appendDecl("{$RANGECHECKS OFF}");
    builder.appendDecl("");
    builder.appendDecl("unit superobject;");
    builder.appendDecl("");
    builder.appendDecl("interface");
    builder.appendDecl("uses");
    builder.appendDecl("  Classes, SuperObjectUtils");
    builder.appendDecl("{$IFDEF HAVE_RTTI}");
    builder.appendDecl("  , Generics.Collections, RTTI, TypInfo");
    builder.appendDecl("{$ENDIF}");
    builder.appendDecl("  ;");
    builder.appendDecl("");
    builder.appendDecl("type");
    builder.appendDecl("{$IFNDEF FPC}");
    builder.appendDecl("{$IFDEF CPUX64}");
    builder.appendDecl("  PtrInt = Int64;");
    builder.appendDecl("  PtrUInt = UInt64;");
    builder.appendDecl("{$ELSE}");
    builder.appendDecl("  PtrInt = longint;");
    builder.appendDecl("  PtrUInt = Longword;");
    builder.appendDecl("{$ENDIF}");
    builder.appendDecl("{$ENDIF}");
    builder.appendDecl("  SuperInt = Int64;");
    builder.appendDecl("");
    builder.appendDecl("{$if (sizeof(Char) = 1)}");
    builder.appendDecl("  SOChar = WideChar;");
    builder.appendDecl("  SOIChar = Word;");
    builder.appendDecl("  PSOChar = PWideChar;");
    builder.appendDecl("{$IFDEF FPC}");
    builder.appendDecl("  SOString = UnicodeString;");
    builder.appendDecl("{$ELSE}");
    builder.appendDecl("  SOString = WideString;");
    builder.appendDecl("{$ENDIF}");
    builder.appendDecl("{$else}");
    builder.appendDecl("  SOChar = Char;");
    builder.appendDecl("  SOIChar = Word;");
    builder.appendDecl("  PSOChar = PChar;");
    builder.appendDecl("  SOString = string;");
    builder.appendDecl("{$ifend}");

    results = new SourceResolverResults("", builder.getSourceCode());

    resolver.resolve(results);

    String resultSourceCode = results.getFileData().toString();
    System.out.println(resultSourceCode);

    assertThat(resultSourceCode, containsString("(*{$IFDEF"));
    assertThat(resultSourceCode, containsString("$ENDIF}*)"));
    assertThat(resultSourceCode, containsString("(*{$if"));
    assertThat(resultSourceCode, containsString("(*{$ifend}*)"));
  }

  @Test
  public void testIfUndefinedWithElse() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("unit superobject;");
    builder.appendDecl("");
    builder.appendDecl("interface");
    builder.appendDecl("uses");
    builder.appendDecl("  Classes, SuperObjectUtils;");
    builder.appendDecl("");
    builder.appendDecl("type");
    builder.appendDecl("  SuperInt = Int64;");
    builder.appendDecl("");
    builder.appendDecl("{$if (sizeof(Char) = 1)}");
    builder.appendDecl("  SOChar = WideChar;");
    builder.appendDecl("  SOIChar = Word;");
    builder.appendDecl("  PSOChar = PWideChar;");
    builder.appendDecl("{$else}");
    builder.appendDecl("  SOChar = Char;");
    builder.appendDecl("  SOIChar = Word;");
    builder.appendDecl("  PSOChar = PChar;");
    builder.appendDecl("  SOString = string;");
    builder.appendDecl("{$ifend}");

    results = new SourceResolverResults("", builder.getSourceCode());

    resolver.resolve(results);

    String resultSourceCode = results.getFileData().toString();
    System.out.println(resultSourceCode);
    assertThat(resultSourceCode, containsString("(*{$if"));
    assertThat(resultSourceCode, containsString("$else}*)"));
    assertThat(resultSourceCode, containsString("(*{$ifend}*)"));
  }

  @Test
  public void testIfdefUndefinedWithElse() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("unit superobject;");
    builder.appendDecl("");
    builder.appendDecl("interface");
    builder.appendDecl("uses");
    builder.appendDecl("  Classes, SuperObjectUtils;");
    builder.appendDecl("");
    builder.appendDecl("type");
    builder.appendDecl("  SuperInt = Int64;");
    builder.appendDecl("");
    builder.appendDecl("{$IFDEF FPC}");
    builder.appendDecl("  SOString = UnicodeString;");
    builder.appendDecl("{$ELSE}");
    builder.appendDecl("  SOString = WideString;");
    builder.appendDecl("{$ENDIF}");

    results = new SourceResolverResults("", builder.getSourceCode());

    resolver.resolve(results);

    String resultSourceCode = results.getFileData().toString();
    System.out.println(resultSourceCode);
    assertThat(resultSourceCode, containsString("(*{$IFDEF"));
    assertThat(resultSourceCode, containsString("$ELSE}*)"));
    assertThat(resultSourceCode, containsString("(*{$ENDIF}*)"));
  }

  @Test
  public void testIfdefDefined() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("unit superobject;");
    builder.appendDecl("");
    builder.appendDecl("interface");
    builder.appendDecl("{$DEFINE FPC}");
    builder.appendDecl("{$IFDEF FPC}");
    builder.appendDecl("  SOString = UnicodeString;");
    builder.appendDecl("{$ENDIF}");

    results = new SourceResolverResults("", builder.getSourceCode());

    resolver.resolve(results);

    String resultSourceCode = results.getFileData().toString();
    System.out.println(resultSourceCode);
    assertThat(resultSourceCode, containsString("(*{$IFDEF FPC}*)"));
    assertThat(resultSourceCode, containsString("(*{$ENDIF}*)"));
    assertThat(resultSourceCode, containsString("SOString = UnicodeString;"));
  }

  @Test
  public void testIfdefUndefined() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("unit superobject;");
    builder.appendDecl("");
    builder.appendDecl("interface");
    builder.appendDecl("{$IFDEF FPC}");
    builder.appendDecl("  SOString = UnicodeString;");
    builder.appendDecl("{$ENDIF}");

    results = new SourceResolverResults("", builder.getSourceCode());

    resolver.resolve(results);

    String resultSourceCode = results.getFileData().toString();
    System.out.println(resultSourceCode);
    assertThat(resultSourceCode, containsString("(*{$IFDEF FPC}"));
    assertThat(resultSourceCode, containsString("{$ENDIF}*)"));
  }

  @Test
  public void testIfdefDefinedWithElse() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("{$DEFINE FPC}");
    builder.appendDecl("{$IFDEF FPC}");
    builder.appendDecl("  SOString = UnicodeString;");
    builder.appendDecl("{$ELSE}");
    builder.appendDecl("  SOString = WideString;");
    builder.appendDecl("{$ENDIF}");

    results = new SourceResolverResults("", builder.getSourceCode());

    resolver.resolve(results);

    String resultSourceCode = results.getFileData().toString();
    System.out.println(resultSourceCode);
    assertThat(resultSourceCode, containsString("(*{$IFDEF FPC}"));
    assertThat(resultSourceCode, containsString("(*{$ELSE"));
    assertThat(resultSourceCode, containsString("$ENDIF}*)"));
    assertThat(resultSourceCode, containsString("SOString = UnicodeString;"));
  }

  @Test
  public void testSkipAlreadyCommentBlock() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("unit superobject;");
    builder.appendDecl("");
    builder.appendDecl("interface");
    builder.appendDecl("uses");
    builder.appendDecl("  Classes, SuperObjectUtils;");
    builder.appendDecl("");
    builder.appendDecl("type");
    builder.appendDecl("  SuperInt = Int64;");
    builder.appendDecl("");
    builder.appendDecl("{$IFDEF FPC}");
    builder.appendDecl("  SOString = UnicodeString; (* unicode *) ");
    builder.appendDecl("{$ENDIF}");
    builder.appendDecl("{$if TEST}");
    builder.appendDecl("  (* comment *)");
    builder.appendDecl("{$ifend}");

    results = new SourceResolverResults("", builder.getSourceCode());

    resolver.resolve(results);

    String resultSourceCode = results.getFileData().toString();
    System.out.println(resultSourceCode);
    assertThat(resultSourceCode, containsString("(*{$IFDEF FPC}"));
    assertThat(resultSourceCode, containsString("$ENDIF}*)"));
    assertThat(resultSourceCode, containsString("(* unicode  )"));
    assertThat(resultSourceCode, containsString("(*{$if TEST}"));
    assertThat(resultSourceCode, containsString("{$ifend}*)"));
    assertThat(resultSourceCode, containsString("(* comment  )"));
  }

  @Test
  public void testIfndefUndefined() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("{$IFDEF FPC}");
    builder.appendImpl("{$ELSE}");
    builder.appendImpl("  try");
    builder.appendImpl("{$ENDIF} ");
    builder.appendImpl("{$IFNDEF FPC}");
    builder.appendImpl("  finally");
    builder.appendImpl("  end;");
    builder.appendImpl("{$ENDIF}");

    results = new SourceResolverResults("", builder.getSourceCode());

    resolver.resolve(results);

    String resultSourceCode = results.getFileData().toString();
    System.out.println(resultSourceCode);
    assertThat(resultSourceCode, containsString("(*{$IFDEF FPC}"));
    assertThat(resultSourceCode, containsString("{$ELSE}*)"));
    assertThat(resultSourceCode, containsString("(*{$IFNDEF FPC}*)"));
  }
}
