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
package au.com.integradev.delphi.reporting;

import au.com.integradev.delphi.DelphiProperties;
import java.util.Optional;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.sonar.api.config.Configuration;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.TypeScope;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ScopedType;
import org.sonar.plugins.communitydelphi.api.type.Type.StructType;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

public class TestCodeDetector {
  private final String testTypeName;
  private final String testAttributeName;

  public TestCodeDetector(Configuration config) {
    this.testTypeName = config.get(DelphiProperties.TEST_TYPE_KEY).orElse(null);
    this.testAttributeName = config.get(DelphiProperties.TEST_ATTRIBUTE_KEY).orElse(null);
  }

  public boolean isInTestCode(DelphiAst ast, FilePosition position) {
    Type type = findEnclosingType(ast, position);
    return isTestType(type) || isNestedInsideTestType(type);
  }

  public boolean isInTestCode(DelphiNode node) {
    Type type = findEnclosingType(node.getAst(), FilePosition.from(node));
    return isTestType(type) || isNestedInsideTestType(type);
  }

  private boolean isTestType(Type type) {
    return isTestTypeByAncestry(type) || isTestTypeByAttribute(type);
  }

  private boolean isTestTypeByAncestry(Type type) {
    return type.is(testTypeName) || type.isSubTypeOf(testTypeName);
  }

  private boolean isTestTypeByAttribute(Type type) {
    if (!(type instanceof StructType)) {
      return false;
    }

    return ((StructType) type)
        .attributeTypes().stream().anyMatch(attribute -> attribute.is(testAttributeName));
  }

  private boolean isNestedInsideTestType(Type type) {
    if (type instanceof ScopedType) {
      DelphiScope scope = ((ScopedType) type).typeScope();
      while ((scope = scope.getParent()) instanceof TypeScope) {
        if (isTestType(((TypeScope) scope).getType())) {
          return true;
        }
      }
    }
    return false;
  }

  private static Type findEnclosingType(DelphiAst ast, FilePosition filePosition) {
    Optional<TypeDeclarationNode> typeDeclarationNode =
        findNodeEnclosingFilePosition(ast, filePosition, TypeDeclarationNode.class);

    if (typeDeclarationNode.isPresent()) {
      return typeDeclarationNode.get().getType();
    }

    Optional<MethodImplementationNode> methodImplementationNode =
        findNodeEnclosingFilePosition(ast, filePosition, MethodImplementationNode.class);

    if (methodImplementationNode.isPresent()) {
      TypeNameDeclaration typeDeclaration = methodImplementationNode.get().getTypeDeclaration();
      if (typeDeclaration != null) {
        return typeDeclaration.getType();
      }
    }

    return TypeFactory.unknownType();
  }

  private static <T extends DelphiNode> Optional<T> findNodeEnclosingFilePosition(
      DelphiAst ast, FilePosition filePosition, Class<T> nodeClass) {
    if (filePosition != null) {
      return ast.findDescendantsOfType(nodeClass).stream()
          .filter(node -> nodeEnclosesFilePosition(node, filePosition))
          .max(
              (a, b) ->
                  new CompareToBuilder()
                      .append(a.getBeginLine(), b.getBeginLine())
                      .append(a.getBeginColumn(), b.getBeginColumn())
                      .toComparison());
    }
    return Optional.empty();
  }

  private static boolean nodeEnclosesFilePosition(DelphiNode node, FilePosition position) {
    return nodeStartsBeforeFilePosition(node, position)
        && nodeEndsAfterFilePosition(node, position);
  }

  private static boolean nodeStartsBeforeFilePosition(DelphiNode node, FilePosition position) {
    return node.getBeginLine() <= position.getBeginLine()
        && (node.getBeginLine() != position.getBeginLine()
            || node.getBeginColumn() <= position.getBeginColumn());
  }

  private static boolean nodeEndsAfterFilePosition(DelphiNode node, FilePosition position) {
    return node.getEndLine() >= position.getEndLine()
        && (node.getEndLine() != position.getEndLine()
            || node.getEndColumn() >= position.getEndColumn());
  }
}
