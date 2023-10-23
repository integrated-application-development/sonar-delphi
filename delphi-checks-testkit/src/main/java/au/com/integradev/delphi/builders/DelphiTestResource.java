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
package au.com.integradev.delphi.builders;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.apache.commons.io.FileUtils;
import org.sonar.api.batch.fs.InputFile;

final class DelphiTestResource extends AbstractDelphiTestFile<DelphiTestResource> {

  private final File resource;

  DelphiTestResource(File resource) {
    this.resource = resource;
  }

  @Override
  protected DelphiTestResource getThis() {
    return this;
  }

  @Override
  protected String getFileName() {
    return resource.getName();
  }

  @Override
  public String sourceCode() {
    try {
      return FileUtils.readFileToString(resource, UTF_8.name());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public InputFile inputFile() {
    return createInputFile(resource.getParentFile(), resource);
  }
}
