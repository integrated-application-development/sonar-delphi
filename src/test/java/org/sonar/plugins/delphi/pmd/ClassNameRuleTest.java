/*
 * Sonar Delphi Plugin
 * Copyright (C) 2015 Fabricio Colombo
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
package org.sonar.plugins.delphi.pmd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

public class ClassNameRuleTest extends BasePmdRuleTest {

  @Test
  public void testAcceptTForClass() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TType = class(TObject)");
    builder.appendDecl("  end;");

    execute(builder);

    assertThat(stringifyIssues(), issues, is(empty()));
  }

  @Test
  public void testNotAcceptLowercaseTForClass() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  tomType = class(TObject)");
    builder.appendDecl("  end;");

    execute(builder);

    assertThat(stringifyIssues(), issues, hasSize(1));
  }

  @Test
  public void testNestedType() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TOuterClass = class");
    builder.appendDecl("  strict private");
    builder.appendDecl("    type");
    builder.appendDecl("      TInnerClass1 = class");
    builder.appendDecl("      end;");
    builder.appendDecl("      TInnerClass2 = class");
    builder.appendDecl("      end;");
    builder.appendDecl("  end;");

    execute(builder);

    assertThat(stringifyIssues(), issues, is(empty()));
  }

  @Test
  public void testAcceptTFormForExceptionClasses() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TFormMyForm = class(TForm)");
    builder.appendDecl("  end;");

    execute(builder);

    assertThat(issues, is(empty()));
  }

  @Test
  public void testNotAcceptLowercaseTFormForExceptionClasses() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  tformMyForm = class(TForm)");
    builder.appendDecl("  end;");

    execute(builder);

    assertThat(issues, hasSize(1));
  }

  @Test
  public void testAcceptEForExceptionClasses() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  EMyCustomException = class(Exception)");
    builder.appendDecl("  end;");

    execute(builder);

    assertThat(issues, is(empty()));
  }

  @Test
  public void testNotAcceptLowercaseEForExceptionClasses() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  eomMyCustomException = class(Exception)");
    builder.appendDecl("  end;");

    execute(builder);

    assertThat(issues, hasSize(1));
  }
}
