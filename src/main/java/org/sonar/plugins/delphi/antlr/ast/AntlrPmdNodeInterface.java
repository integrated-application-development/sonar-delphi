/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General default
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General default License for more details.
 *
 * You should have received a copy of the GNU Lesser General default
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.delphi.antlr.ast;

import java.util.Iterator;
import java.util.List;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.ast.xpath.Attribute;
import net.sourceforge.pmd.lang.dfa.DataFlowNode;
import net.sourceforge.pmd.lang.symboltable.Scope;
import net.sourceforge.pmd.lang.symboltable.ScopedNode;
import org.w3c.dom.Document;

/**
 * Boilerplate interface to override all of the API methods that PMD wants implemented
 *
 * <p>An antlr implementation really doesn't use most of these methods, and it's better not to
 * pollute the DelphiNode class with 300 lines of unsupported operations.
 */
interface AntlrPmdNodeInterface extends ScopedNode {

  /** {@inheritDoc} */
  @Override
  default void jjtOpen() {
    throw new UnsupportedOperationException("Won't be needed on Antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default void jjtClose() {
    throw new UnsupportedOperationException("Won't be needed on Antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default void jjtSetParent(Node n) {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default Node jjtGetParent() {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default void jjtAddChild(Node n, int i) {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default Node jjtGetChild(int i) {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default void jjtSetChildIndex(int var1) {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default int jjtGetChildIndex() {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default int jjtGetNumChildren() {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default int jjtGetId() {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default Document getAsDocument() {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default Object getUserData() {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default String getImage() {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default void setImage(String image) {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default boolean hasImageEqualTo(String image) {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default DataFlowNode getDataFlowNode() {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default void setDataFlowNode(DataFlowNode dataFlowNode) {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default boolean isFindBoundary() {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default Node getNthParent(int n) {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default <T> T getFirstParentOfType(Class<T> parentType) {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default <T> List<T> getParentsOfType(Class<T> parentType) {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @SuppressWarnings("unchecked")
  @Override
  default <T> T getFirstParentOfAnyType(Class<? extends T>... classes) {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default <T> List<T> findChildrenOfType(Class<T> childType) {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default <T> List<T> findDescendantsOfType(Class<T> targetType) {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default <T> void findDescendantsOfType(
      Class<T> targetType, List<T> results, boolean crossFindBoundaries) {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default <T> T getFirstChildOfType(Class<T> childType) {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default <T> T getFirstDescendantOfType(Class<T> descendantType) {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default <T> boolean hasDescendantOfType(Class<T> type) {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default List<? extends Node> findChildNodesWithXPath(String xpathString) {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default boolean hasDescendantMatchingXPath(String xpathString) {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  /** {@inheritDoc} */
  @Override
  default void setUserData(Object userData) {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  @Override
  default Scope getScope() {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  @Override
  default void remove() {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  @Override
  default void removeChildAtIndex(int var1) {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  @Override
  default String getXPathNodeName() {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }

  @Override
  default Iterator<Attribute> getXPathAttributesIterator() {
    throw new UnsupportedOperationException("Out of scope for antlr implementation");
  }
}
