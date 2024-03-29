/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "CharacterToCharacterPointerCastRule", repositoryKey = "delph")
@Rule(key = "CharacterToCharacterPointerCast")
public class CharacterToCharacterPointerCastCheck extends AbstractCastCheck {
  @Override
  protected String getIssueMessage() {
    return "Remove this character -> character pointer cast.";
  }

  @Override
  protected boolean isViolation(Type originalType, Type castType) {
    return originalType.isChar()
        && castType.isPointer()
        && ((Type.PointerType) castType).dereferencedType().isChar();
  }
}
