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
package au.com.integradev.delphi;

import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/** Parses {@code delphi.tokens} file and transforms it into a Java enum. */
@Mojo(
    name = "generate",
    defaultPhase = LifecyclePhase.GENERATE_SOURCES,
    requiresDependencyResolution = ResolutionScope.COMPILE,
    threadSafe = true)
public class DelphiTokensGeneratorMojo extends AbstractMojo {
  /** The directory where the ({@code protocol.xml}) files are located. */
  @Parameter(defaultValue = "${project.build.directory}/generated-sources/antlr3/Delphi.tokens")
  private File tokensFile;

  /**
   * The directory where the generated source files will be stored. The directory will be registered
   * as a compile source root of the project such that the generated files will participate in later
   * build phases like compiling and packaging.
   */
  @Parameter(
      defaultValue = "${project.build.directory}/generated-sources/delphi-tokens",
      required = true)
  private File outputDirectory;

  /** The current Maven project. */
  @Parameter(property = "project", required = true, readonly = true)
  protected MavenProject project;

  @Override
  public void execute() throws MojoFailureException {
    DelphiTokensGenerator generator = new DelphiTokensGenerator(tokensFile, outputDirectory);
    try {
      generator.generate();
    } catch (Exception e) {
      getLog().error(e.getMessage());
      throw new MojoFailureException(e.getMessage(), e);
    }
    project.addCompileSourceRoot(outputDirectory.getPath());
  }
}
