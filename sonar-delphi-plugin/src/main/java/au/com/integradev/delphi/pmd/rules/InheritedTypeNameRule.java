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

import au.com.integradev.delphi.antlr.ast.node.TypeDeclarationNode;
import au.com.integradev.delphi.antlr.ast.node.TypeNode;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class InheritedTypeNameRule extends AbstractDelphiRule {
  private static final Logger LOG = Loggers.get(InheritedTypeNameRule.class);

  public static final PropertyDescriptor<String> NAME_REGEX =
      PropertyFactory.stringProperty("nameRegex")
          .desc("The regular expression used to define the naming convention")
          .defaultValue("(?!)")
          .build();

  public static final PropertyDescriptor<String> PARENT_REGEX =
      PropertyFactory.stringProperty("parentNameRegex")
          .desc("The regular expression used to match parent types")
          .defaultValue("(?!)")
          .build();

  private static final PropertyDescriptor<String> MESSAGE =
      PropertyFactory.stringProperty("message").desc("The issue message").defaultValue("").build();

  private Pattern namePattern;
  private Pattern parentPattern;

  public InheritedTypeNameRule() {
    definePropertyDescriptor(NAME_REGEX);
    definePropertyDescriptor(PARENT_REGEX);
    definePropertyDescriptor(MESSAGE);
  }

  @Override
  public void start(RuleContext ctx) {
    if (namePattern == null && parentPattern == null) {
      namePattern = tryCompilePattern(getProperty(NAME_REGEX));
      parentPattern = tryCompilePattern(getProperty(PARENT_REGEX));
    }
  }

  private Pattern tryCompilePattern(String regularExpression) {
    try {
      return Pattern.compile(regularExpression);
    } catch (PatternSyntaxException e) {
      LOG.debug("Unable to compile regular expression: " + regularExpression, e);
      return null;
    }
  }

  @Override
  public RuleContext visit(TypeDeclarationNode type, RuleContext data) {
    if (parentPattern != null && namePattern != null) {
      TypeNode typeDecl = type.getTypeNode();
      if (inheritsFromType(typeDecl) && !namePattern.matcher(type.simpleName()).matches()) {
        addViolation(data, type.getTypeNameNode());
      }
    }
    return super.visit(type, data);
  }

  private boolean inheritsFromType(TypeNode typeDecl) {
    return typeDecl.getParentTypeNodes().stream()
        .anyMatch(typeRef -> parentPattern.matcher(typeRef.fullyQualifiedName()).matches());
  }

  @Override
  public String dysfunctionReason() {
    start(null);
    if (parentPattern == null) {
      return "Unable to compile regular expression: " + getProperty(PARENT_REGEX);
    } else if (namePattern == null) {
      return "Unable to compile regular expression: " + getProperty(NAME_REGEX);
    }
    return null;
  }
}
