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

import java.io.File;
import org.antlr.runtime.tree.Tree;
import org.w3c.dom.Document;

/**
 * AST Tree interface, used by AbstractAnalyser.
 * 
 */
public interface ASTTree extends Tree {

  /**
   * Gets file name associated with the AST tree
   * 
   * @return File name
   */
  String getFileName();

  /**
   * Checks if there were errors during file parsing
   * 
   * @return True if they were, false otherwise
   */
  boolean isError();

  /**
   * Generates an XML document from current node
   * 
   * @return XML document
   */
  Document generateDocument();

  /**
   * Generates and saves AST tree to XML file
   * 
   * @param fileName The target file to generate the XML
   * @return Generated XML file
   */
  File generateXML(String fileName);

  /**
   * @return Gets the file source
   */
  String getFileSource();

  /**
   * Gets the source file at line
   * 
   * @param lineNr Line number, starting from 1
   * @return Gets the source file at line
   */
  String getFileSourceLine(int lineNr);

}
