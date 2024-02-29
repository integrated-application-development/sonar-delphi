/*
 * Sonar Delphi Plugin
 * Copyright (C) 2024 Integrated Application Development
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

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class DelphiTokensGenerator {
  private static final Pattern TOKENS_LINE_PATTERN =
      Pattern.compile("(?i)([a-z_][a-z_\\d]+)=(\\d+)");
  private static final String DEPRECATED_SUFFIX = "__deprecated";

  private final File tokensFile;
  private final File outputDirectory;

  public DelphiTokensGenerator(File tokensFile, File outputDirectory) {
    this.tokensFile = tokensFile;
    this.outputDirectory = outputDirectory;
  }

  public void generate() throws IOException {
    if (!tokensFile.isFile()) {
      throw new FileNotFoundException("Tokens file does not exist");
    }

    if (!outputDirectory.isDirectory()) {
      try {
        Files.createDirectories(outputDirectory.toPath());
      } catch (IOException e) {
        throw new IOException("Failed to create output directory", e);
      }
    }

    List<TokenTypeRecord> tokenTypes = readTokenTypes();
    generateEnum(tokenTypes);
    generateEnumFactory(tokenTypes);
  }

  private List<TokenTypeRecord> readTokenTypes() throws IOException {
    ImmutableList.Builder<TokenTypeRecord> result = ImmutableList.builder();

    result.add(new TokenTypeRecord("EOF", -1, false));
    result.add(new TokenTypeRecord("INVALID", 0, false));

    Files.readAllLines(tokensFile.toPath()).stream()
        .map(DelphiTokensGenerator::createTokenType)
        .filter(Objects::nonNull)
        .forEach(result::add);

    return result.build();
  }

  private void generateEnum(List<TokenTypeRecord> tokenTypes) throws IOException {
    Path outputPath = outputDirectory.toPath().resolve("org/sonar/plugins/delphi/api/token");

    Files.createDirectories(outputPath);

    StringBuilder builder =
        new StringBuilder()
            .append("package org.sonar.plugins.communitydelphi.api.token;\n\n")
            .append("import javax.annotation.processing.Generated;\n\n")
            .append("@Generated(\"")
            .append(DelphiTokensGenerator.class.getName())
            .append("\")\n")
            .append("public enum DelphiTokenType {\n");

    for (TokenTypeRecord tokenType : tokenTypes) {
      if (tokenType.isDeprecated()) {
        builder.append("  @Deprecated(forRemoval = true)\n");
      }
      builder.append("  ").append(tokenType.getName()).append(",\n");
    }

    builder.append("}\n");

    Files.writeString(outputPath.resolve("DelphiTokenType.java"), builder);
  }

  private void generateEnumFactory(List<TokenTypeRecord> tokenTypes) throws IOException {
    Path outputPath = outputDirectory.toPath().resolve("au/com/integradev/delphi/antlr/ast/token");

    Files.createDirectories(outputPath);

    var builder =
        new StringBuilder()
            .append("package au.com.integradev.delphi.antlr.ast.token;\n\n")
            .append("import javax.annotation.processing.Generated;\n")
            .append("import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;\n\n");

    if (tokenTypes.stream().anyMatch(TokenTypeRecord::isDeprecated)) {
      builder.append("@SuppressWarnings(\"removal\")\n");
    }

    builder
        .append("@Generated(\"")
        .append(DelphiTokensGenerator.class.getName())
        .append("\")\n")
        .append("public final class DelphiTokenTypeFactory {\n")
        .append("  private DelphiTokenTypeFactory() {\n")
        .append("    // utility class\n")
        .append("  }\n\n")
        .append("  public static DelphiTokenType createTokenType(int value) {\n")
        .append("    switch(value) {\n");

    for (TokenTypeRecord tokenType : tokenTypes) {
      builder.append("      case ").append(tokenType.getValue()).append(":\n");
      builder.append("        return DelphiTokenType.").append(tokenType.getName()).append(";\n");
    }

    builder
        .append("      default:\n")
        .append("        throw new IllegalArgumentException(\"Unknown value: \" + value);\n")
        .append("    }\n")
        .append("  }\n\n")
        .append("  public static int getValueFromTokenType(DelphiTokenType tokenType) {\n")
        .append("    switch(tokenType) {\n");

    for (TokenTypeRecord tokenType : tokenTypes) {
      builder.append("      case ").append(tokenType.getName()).append(":\n");
      builder.append("        return ").append(tokenType.getValue()).append(";\n");
    }

    builder
        .append("      default:\n")
        .append("        throw new IllegalArgumentException(\"Unknown type: \" + tokenType);\n")
        .append("    }\n")
        .append("  }\n")
        .append("}\n");

    Files.writeString(outputPath.resolve("DelphiTokenTypeFactory.java"), builder);
  }

  private static TokenTypeRecord createTokenType(String line) {
    Matcher matcher = TOKENS_LINE_PATTERN.matcher(line);
    if (matcher.matches()) {
      String antlrName = matcher.group(1);
      String value = matcher.group(2);
      return new TokenTypeRecord(
          antlrNameToEnumName(antlrName),
          Integer.parseInt(value),
          antlrName.endsWith(DEPRECATED_SUFFIX));
    }
    return null;
  }

  private static String antlrNameToEnumName(String antlrName) {
    antlrName = StringUtils.removeStart(antlrName, "Tk");
    antlrName = StringUtils.removeEnd(antlrName, DEPRECATED_SUFFIX);

    StringBuilder result = new StringBuilder();
    boolean nextCapitalIsNewWord = true;

    for (int i = 0; i < antlrName.length(); ++i) {
      char c = antlrName.charAt(i);
      if (Character.isUpperCase(c)) {
        if (nextCapitalIsNewWord
            && result.length() > 0
            && result.charAt(result.length() - 1) != '_') {
          result.append('_');
        }
        nextCapitalIsNewWord = false;
      } else if (!Character.isDigit(c)) {
        nextCapitalIsNewWord = true;
      }
      result.append(Character.toUpperCase(c));
    }

    return result.toString();
  }

  private static class TokenTypeRecord {
    private final String name;
    private final int value;
    private final boolean deprecated;

    public TokenTypeRecord(String name, int value, boolean deprecated) {
      this.name = name;
      this.value = value;
      this.deprecated = deprecated;
    }

    public String getName() {
      return name;
    }

    public int getValue() {
      return value;
    }

    public boolean isDeprecated() {
      return deprecated;
    }
  }
}
