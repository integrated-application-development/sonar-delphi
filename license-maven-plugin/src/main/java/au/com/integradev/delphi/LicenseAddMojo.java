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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Year;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/** Add missing license headers to source files. */
@Mojo(name = "add", defaultPhase = LifecyclePhase.PROCESS_SOURCES, threadSafe = true)
public class LicenseAddMojo extends AbstractMojo {
  private static final Pattern LICENSE_PATTERN =
      Pattern.compile("^/\\*(.(?!\\*/))*Copyright \\(C\\) \\d{4}.*$", Pattern.DOTALL);

  /** The current Maven project. */
  @Parameter(property = "project", required = true, readonly = true)
  protected MavenProject project;

  /** Fail the build if any license headers are missing. */
  @Parameter(property = "license.failIfMissing", defaultValue = "false")
  private boolean failIfMissing;

  private final List<Path> missingHeaderFiles = new ArrayList<>();

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    Path src = project.getBasedir().toPath().resolve("src");
    if (!Files.exists(src)) {
      return;
    }

    try (Stream<Path> files = Files.find(src, Integer.MAX_VALUE, LicenseAddMojo::isJavaFile)) {
      for (Path file : files.collect(Collectors.toList())) {
        addLicenseIfMissing(file);
      }
    } catch (IOException e) {
      getLog().error(e.getMessage());
      throw new MojoExecutionException(e.getMessage(), e);
    }

    if (failIfMissing && !missingHeaderFiles.isEmpty()) {
      throw new MojoFailureException(
          "Some files do not have a license header. Run license:add to add them."
              + System.lineSeparator()
              + missingHeaderFiles.stream()
                  .map(Path::toString)
                  .collect(Collectors.joining(System.lineSeparator())));
    }
  }

  private void addLicenseIfMissing(Path file) throws IOException {
    String content = Files.readString(file);
    if (!hasLicenseHeader(content)) {
      getLog().info("Adding missing license header: " + file);
      content = getLicenseHeader() + content;
      Files.writeString(file, content);
      missingHeaderFiles.add(file);
    }
  }

  private static boolean isJavaFile(Path path, BasicFileAttributes attributes) {
    return attributes.isRegularFile() && path.toString().endsWith(".java");
  }

  private static boolean hasLicenseHeader(String fileContent) {
    return LICENSE_PATTERN.matcher(fileContent).matches();
  }

  private static String getLicenseHeader() {
    return String.format(
        "/*%n"
            + " * Sonar Delphi Plugin%n"
            + " * Copyright (C) %d Integrated Application Development%n"
            + " *%n"
            + " * This program is free software; you can redistribute it and/or%n"
            + " * modify it under the terms of the GNU Lesser General Public%n"
            + " * License as published by the Free Software Foundation; either%n"
            + " * version 3 of the License, or (at your option) any later version.%n"
            + " *%n"
            + " * This program is distributed in the hope that it will be useful,%n"
            + " * but WITHOUT ANY WARRANTY; without even the implied warranty of%n"
            + " * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU%n"
            + " * Lesser General Public License for more details.%n"
            + " *%n"
            + " * You should have received a copy of the GNU Lesser General Public%n"
            + " * License along with this program; if not, write to the Free Software%n"
            + " * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02%n"
            + " */%n",
        Year.now(ZoneId.of("Australia/Melbourne")).getValue());
  }
}
