/*
 * Sonar Delphi Plugin
 * Copyright (C) 2025 Integrated Application Development
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
package au.com.integradev.delphi.cfg.checker;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;

public class ElementChecker {
  private int blockId;
  private int elementId;
  private final List<BiConsumer<ElementChecker, DelphiNode>> checkers = new ArrayList<>();

  public static <T extends DelphiNode> ElementChecker element(Class<T> elementClass) {
    return new ElementChecker(
        (elementChecker, node) -> assertElementIsType(elementChecker, elementClass, node));
  }

  public static <T extends DelphiNode> ElementChecker element(Class<T> elementClass, String image) {
    return new ElementChecker(
        (elementChecker, node) -> {
          assertElementIsType(elementChecker, elementClass, node);
          assertThat(node.getImage())
              .as(elementChecker.getElementId() + " is expected to have image " + image)
              .isEqualTo(image);
        });
  }

  private static <T extends DelphiNode> void assertElementIsType(
      ElementChecker elementChecker, Class<T> elementClass, DelphiNode node) {
    assertThat(node)
        .as(
            elementChecker.getElementId()
                + " is expected to be of type "
                + elementClass.getTypeName())
        .isInstanceOf(elementClass);
  }

  private String getElementId() {
    return "B" + blockId + ":E" + elementId;
  }

  private ElementChecker(BiConsumer<ElementChecker, DelphiNode> checker) {
    this.checkers.add(checker);
  }

  public ElementChecker withCheck(Consumer<DelphiNode> checker) {
    return withCheck((elementChecker, node) -> checker.accept(node));
  }

  public ElementChecker withCheck(BiConsumer<ElementChecker, DelphiNode> checker) {
    this.checkers.add(checker);
    return this;
  }

  protected ElementChecker withBlockId(int blockId, int elementId) {
    this.blockId = blockId;
    this.elementId = elementId;
    return this;
  }

  public void check(final DelphiNode element) {
    this.checkers.forEach(checker -> checker.accept(this, element));
  }
}
