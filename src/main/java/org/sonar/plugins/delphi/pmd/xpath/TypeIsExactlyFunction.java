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
package org.sonar.plugins.delphi.pmd.xpath;

import java.util.List;
import net.sourceforge.pmd.lang.ast.Node;
import org.jaxen.Context;
import org.jaxen.SimpleFunctionContext;
import org.jaxen.XPathFunctionContext;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Typed;

/**
 * A custom XPath function that operates on Typed nodes. Returns true if the type is an exact match
 * of the specified type.
 *
 * <p>Based directly on the TypeIsExactlyFunction from the pmd-java plugin.
 *
 * @see <a href="bit.ly/TypeIsExactlyFunction">TypeIsExactlyFunction.java</a>
 */
public class TypeIsExactlyFunction extends AbstractFunction {

  public static void registerSelfInSimpleContext() {
    ((SimpleFunctionContext) XPathFunctionContext.getInstance())
        .registerFunction(null, "typeIsExactly", new TypeIsExactlyFunction());
  }

  @Override
  public Object doCall(final Context context, final List<?> args) {
    if (args.size() != 1) {
      throw new IllegalArgumentException(
          "typeIsExactly function takes a single String argument with a fully qualified type "
              + "name.");
    }
    final String fullTypeName = (String) args.get(0);
    final Node node = (Node) context.getNodeSet().get(0);

    return typeIsExactly(node, fullTypeName);
  }

  /**
   * Example XPath 1.0: {@code //TypeDeclarationNode[typeIsExactly('myUnit.Foo')]}
   *
   * <p>Example XPath 2.0: {@code //TypeDeclarationNode[pmd-delph:typeIsExactly('myUnit.Foo')]}
   *
   * @param node The node on which to check for types
   * @param image The image of the type or any supertype
   * @return True if the type of the node matches, false otherwise.
   */
  private static boolean typeIsExactly(final Node node, final String image) {
    if (node instanceof Typed) {
      Type type = ((Typed) node).getType();
      return type.is(image);
    } else {
      throw new IllegalArgumentException(
          "typeIsExactly function may only be called on a Typed node.");
    }
  }
}
