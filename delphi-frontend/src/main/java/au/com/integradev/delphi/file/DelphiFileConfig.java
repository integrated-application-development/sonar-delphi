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
package au.com.integradev.delphi.file;

import au.com.integradev.delphi.preprocessor.DelphiPreprocessorFactory;
import au.com.integradev.delphi.preprocessor.search.SearchPath;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

public interface DelphiFileConfig {
  /**
   * Returns the encoding that the source file is expected to be
   *
   * @return Name of encoding
   */
  @Nullable
  String getEncoding();

  /**
   * Returns the preprocessor factory, which can be used to create a preprocessor for this platform
   *
   * @return Preprocessor factory
   */
  DelphiPreprocessorFactory getPreprocessorFactory();

  /**
   * Returns the type factory, which can be used to create different types
   *
   * @return Type factory
   */
  TypeFactory getTypeFactory();

  /**
   * Returns the search path where include files can be found and imports can be resolved
   *
   * @return Search path
   */
  SearchPath getSearchPath();

  /**
   * Returns a list of symbol definitions used in conditional compiler directives
   *
   * @return Symbol definitions
   */
  Set<String> getDefinitions();

  /**
   * Returns whether the implementation section can be skipped when lexing the file.
   *
   * <p>NOTE: In some circumstances, this will not be logically possible. For example, if the
   * implementation section is nested inside of a branching conditional directive then the
   * implementation section will not be skipped.
   *
   * @return true if the implementation should be skipped when parsing the file
   */
  boolean shouldSkipImplementation();
}
