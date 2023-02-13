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
package au.com.integradev.delphi.pmd.rules;

import static au.com.integradev.delphi.utils.conditions.RuleKey.ruleKey;
import static au.com.integradev.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;
import static org.assertj.core.api.Assertions.assertThat;

import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.SetTypeNode;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class HelperNameRuleTest extends BasePmdRuleTest {

  private void setAllowedPrefixes(String prefix) {
    DelphiRuleProperty property =
        Objects.requireNonNull(
            getRule(HelperNameRule.class).getProperty(HelperNameRule.HELPER_PREFIXES.name()));
    property.setValue(prefix);
  }

  @Test
  void testCombinedPrefixesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendDecl("type")
        .appendDecl("  TFoo = class(TObject)")
        .appendDecl("  end;")
        .appendDecl("  TTFooHelper = class helper for TFoo")
        .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("HelperNameRule"));
  }

  @Test
  void testMismatchedPrefixesShouldNotAddIssue() {
    setAllowedPrefixes("T|Prefix");

    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendDecl("type")
        .appendDecl("  TFoo = class(TObject)")
        .appendDecl("  end;")
        .appendDecl("  PrefixFooHelper = class helper for TFoo")
        .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("HelperNameRule"));
  }

  @Test
  void testSinglePrefixShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendDecl("type")
        .appendDecl("  TFoo = class(TObject)")
        .appendDecl("  end;")
        .appendDecl("  TFooHelper = class helper for TFoo")
        .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("HelperNameRule"));
  }

  @Test
  void testNoPrefixShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendDecl("type")
        .appendDecl("  TFoo = class(TObject)")
        .appendDecl("  end;")
        .appendDecl("  FooHelper = class helper for TFoo")
        .appendDecl("  end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("HelperNameRule", builder.getOffsetDecl() + 4));
  }

  @Test
  void testArbitraryTextShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendDecl("type")
        .appendDecl("  TFoo = class(TObject)")
        .appendDecl("  end;")
        .appendDecl("  TabcFooHelper = class helper for TFoo")
        .appendDecl("  end;")
        .appendDecl("  TFoodefHelper = class helper for TFoo")
        .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("HelperNameRule", builder.getOffsetDecl() + 4))
        .areExactly(1, ruleKeyAtLine("HelperNameRule", builder.getOffsetDecl() + 6));
  }

  @Test
  void testNoSuffixShouldAddIssue() {
    setAllowedPrefixes("T|Prefix");

    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendDecl("type")
        .appendDecl("  TFoo = class(TObject)")
        .appendDecl("  end;")
        .appendDecl("  PrefixFoo = class helper for TFoo")
        .appendDecl("  end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("HelperNameRule", builder.getOffsetDecl() + 4));
  }

  @Test
  void testStringHelperShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendDecl("type")
        .appendDecl("  TStringHelper = record helper for string")
        .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("HelperNameRule"));
  }

  @Test
  void testFileHelperShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendDecl("type")
        .appendDecl("  TFileHelper = record helper for file")
        .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("HelperNameRule"));
  }

  @Test
  void testGetUnknownExtendedTypeSimpleName() {
    DelphiAst ast =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TMySet = set of string;")
            .parse();

    SetTypeNode setTypeNode = ast.getFirstChildOfType(SetTypeNode.class);
    assertThat(HelperNameRule.getExtendedTypeSimpleName(setTypeNode)).isNull();
  }
}
