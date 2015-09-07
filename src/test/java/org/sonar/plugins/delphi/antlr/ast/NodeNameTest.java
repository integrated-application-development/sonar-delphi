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
package org.sonar.plugins.delphi.antlr.ast;

import org.junit.Test;
import org.sonar.plugins.delphi.antlr.ast.exceptions.NodeNameForCodeDoesNotExistException;

import static org.junit.Assert.*;
import static org.sonar.plugins.delphi.antlr.ast.NodeName.*;

public class NodeNameTest {

  @Test
  public void testUnknownShouldPassWhenCodeIsEmptySymbol() {
    assertTrue(UNKNOWN.matchesCode(""));
  }

  @Test
  public void testUnknownShouldFailWhenCodeIsNotEmptySymbol() {
    assertFalse(UNKNOWN.matchesCode("."));
    assertFalse(UNKNOWN.matchesCode(" abc"));
    assertFalse(UNKNOWN.matchesCode("ab c"));
    assertFalse(UNKNOWN.matchesCode("abc "));
  }

  @Test
  public void testSemicoloShouldPassWhenCodeIsSemicolonSymbol() {
    assertTrue(SEMI.matchesCode(";"));
  }

  @Test
  public void testSemicoloShouldFailWhenCodeIsNotSemicolonSymbol() {
    assertFalse(SEMI.matchesCode(";;"));
    assertFalse(SEMI.matchesCode(" ;"));
    assertFalse(SEMI.matchesCode("; "));
  }

  @Test
  public void testDashShouldPassWhenCodeIsSemicolonSymbol() {
    assertTrue(DASH.matchesCode(","));
  }

  @Test
  public void testDashShouldFailWhenCodeIsNotSemicolonSymbol() {
    assertFalse(DASH.matchesCode(",,"));
    assertFalse(DASH.matchesCode(" ,"));
    assertFalse(DASH.matchesCode(", "));
  }

  @Test
  public void testDotShouldPassWhenCodeIsDotSymbol() {
    assertTrue(DOT.matchesCode("."));
  }

  @Test
  public void testDotShouldFailWhenCodeIsNotDotSymbol() {
    assertFalse(DOT.matchesCode("\\}"));
    assertFalse(DOT.matchesCode("abc."));
    assertFalse(DOT.matchesCode(".abc"));
    assertFalse(DOT.matchesCode("ab.c"));
    assertFalse(DOT.matchesCode("x"));
    assertFalse(DOT.matchesCode(".."));
    assertFalse(DOT.matchesCode("\\."));
  }

  @Test
  public void testColonShouldSucceedWhenCodeIsExactlyColonOrParenthesisSymbol() {
    assertTrue(COLON.matchesCode(":"));
    assertTrue(COLON.matchesCode("("));
    assertTrue(COLON.matchesCode(")"));
  }

  @Test
  public void testColonShouldFailWhenCodeIsExactlyNotColonOrParenthesisSymbol() {
    assertFalse(COLON.matchesCode("(:"));
    assertFalse(COLON.matchesCode("()"));
    assertFalse(COLON.matchesCode("):"));
    assertFalse(COLON.matchesCode("abcd:"));
    assertFalse(COLON.matchesCode("::"));
    assertFalse(COLON.matchesCode("(("));
    assertFalse(COLON.matchesCode("))"));
    assertFalse(COLON.matchesCode("|"));
  }

  @Test
  public void testGuidIdentifierShouldPassWhenCodeStartsWithGuidIdentifierSymbol() {
    assertTrue(GUID_IDENT.matchesCode("'{"));
    assertTrue(GUID_IDENT.matchesCode("'{51cdd3ad-824a-4b5f-9728-23f8ff137998}'"));
  }

  @Test
  public void testGuidIdentifierShouldFailWhenCodeDoesNotStartWithGuidIdentifierSymbol() {
    assertFalse(GUID_IDENT.matchesCode("hello'{"));
  }

  @Test
  public void testDashPointerShouldPassWhenCodeIsExactlyDashPointerSymbol() {
    assertTrue(DASH_POINTER.matchesCode("^"));
  }

  @Test
  public void testDashPointerShouldFailWhenCodeIsNotExactlyDashPointerSymbol() {
    assertFalse(DASH_POINTER.matchesCode("'^"));
    assertFalse(DASH_POINTER.matchesCode("^^"));
  }

  @Test
  public void testFindNodeNameByCodeShouldReturnValidNode() throws Exception {
    NodeName guidIdentifier = NodeName.findByCode("'{");
    assertNotNull(guidIdentifier);
    assertEquals(GUID_IDENT, guidIdentifier);
  }

  @Test(expected = NodeNameForCodeDoesNotExistException.class)
  public void testFindNodeNameByCodeShouldThrowAnExceptionWhenNodeNameIsNotFound() {
    NodeName.findByCode("BogusCode");
  }

  @Test
  public void testGetNameOfDOTNodeNameShouldReturnProperName() {
    assertEquals("dot", DOT.getName());
  }

}
