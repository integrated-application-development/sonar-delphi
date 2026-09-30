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
package au.com.integradev.delphi.antlr.ast.node;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.integradev.delphi.utils.files.DelphiFileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.plugins.communitydelphi.api.ast.Visibility.VisibilityType;
import org.sonar.plugins.communitydelphi.api.ast.VisibilityNode;

class VisibilityNodeImplTest {
  @ParameterizedTest(name = "[{index}] {0} should be {1}")
  @CsvSource({
    "published,PUBLISHED",
    "public,PUBLIC",
    "protected,PROTECTED",
    "strict protected,STRICT_PROTECTED",
    "private,PRIVATE",
    "strict private,STRICT_PRIVATE",
    "automated,AUTOMATED"
  })
  void testGetVisibility(String keyword, VisibilityType expected) {
    assertThat(parse(keyword).getVisibility()).isEqualTo(expected);
  }

  @ParameterizedTest(name = "[{index}] {0} public: {1}")
  @CsvSource({
    "public,true",
    "automated,true",
    "published,false",
    "protected,false",
    "private,false"
  })
  void testIsPublic(String keyword, boolean expected) {
    assertThat(parse(keyword).isPublic()).isEqualTo(expected);
  }

  @Test
  void testIsAutomated() {
    assertThat(parse("automated").isAutomated()).isTrue();
    assertThat(parse("public").isAutomated()).isFalse();
  }

  private static VisibilityNode parse(String visibility) {
    return DelphiFileUtils.parse(
            "unit Test;",
            "",
            "interface",
            "",
            "type",
            "  TFoo = class",
            "  " + visibility,
            "    procedure Bar;",
            "  end;",
            "",
            "implementation",
            "end.")
        .getAst()
        .getFirstDescendantOfType(VisibilityNode.class);
  }
}
