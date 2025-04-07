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

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DelphiTokensGeneratorTest {
  @Test
  void testSimpleTokens(@TempDir Path tempDir) {
    generate(
        tempDir,
        "FOO=4\n" //
            + "BAR=5\n"
            + "BAZ=6\n"
            + "'foo'=4\n"
            + "'bar'=5\n"
            + "'baz'=6\n");

    assertThat(getTokenFactory(tempDir))
        .isEqualTo(
            "package au.com.integradev.delphi.antlr.ast.token;\n"
                + "\n"
                + "import javax.annotation.processing.Generated;\n"
                + "import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;\n"
                + "\n"
                + "@Generated(\"au.com.integradev.delphi.DelphiTokensGenerator\")\n"
                + "public final class DelphiTokenTypeFactory {\n"
                + "  private DelphiTokenTypeFactory() {\n"
                + "    // utility class\n"
                + "  }\n"
                + "\n"
                + "  public static DelphiTokenType createTokenType(int value) {\n"
                + "    switch(value) {\n"
                + "      case -1:\n"
                + "        return DelphiTokenType.EOF;\n"
                + "      case 0:\n"
                + "        return DelphiTokenType.INVALID;\n"
                + "      case 4:\n"
                + "        return DelphiTokenType.FOO;\n"
                + "      case 5:\n"
                + "        return DelphiTokenType.BAR;\n"
                + "      case 6:\n"
                + "        return DelphiTokenType.BAZ;\n"
                + "      default:\n"
                + "        throw new IllegalArgumentException(\"Unknown value: \" + value);\n"
                + "    }\n"
                + "  }\n"
                + "\n"
                + "  public static int getValueFromTokenType(DelphiTokenType tokenType) {\n"
                + "    switch(tokenType) {\n"
                + "      case EOF:\n"
                + "        return -1;\n"
                + "      case INVALID:\n"
                + "        return 0;\n"
                + "      case FOO:\n"
                + "        return 4;\n"
                + "      case BAR:\n"
                + "        return 5;\n"
                + "      case BAZ:\n"
                + "        return 6;\n"
                + "      default:\n"
                + "        throw new IllegalArgumentException(\"Unknown type: \" + tokenType);\n"
                + "    }\n"
                + "  }\n"
                + "}\n");

    assertThat(getTokenEnum(tempDir))
        .isEqualTo(
            "package org.sonar.plugins.communitydelphi.api.token;\n"
                + "\n"
                + "import javax.annotation.processing.Generated;\n"
                + "\n"
                + "@Generated(\"au.com.integradev.delphi.DelphiTokensGenerator\")\n"
                + "public enum DelphiTokenType {\n"
                + "  EOF,\n"
                + "  INVALID,\n"
                + "  FOO,\n"
                + "  BAR,\n"
                + "  BAZ,\n"
                + "}\n");
  }

  @Test
  void testTokensRequiringNameProcessing(@TempDir Path tempDir) {
    generate(
        tempDir,
        "TkFoo=4\n" //
            + "Bar_Baz123Flarp=5\n");

    assertThat(getTokenFactory(tempDir))
        .isEqualTo(
            "package au.com.integradev.delphi.antlr.ast.token;\n"
                + "\n"
                + "import javax.annotation.processing.Generated;\n"
                + "import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;\n"
                + "\n"
                + "@Generated(\"au.com.integradev.delphi.DelphiTokensGenerator\")\n"
                + "public final class DelphiTokenTypeFactory {\n"
                + "  private DelphiTokenTypeFactory() {\n"
                + "    // utility class\n"
                + "  }\n"
                + "\n"
                + "  public static DelphiTokenType createTokenType(int value) {\n"
                + "    switch(value) {\n"
                + "      case -1:\n"
                + "        return DelphiTokenType.EOF;\n"
                + "      case 0:\n"
                + "        return DelphiTokenType.INVALID;\n"
                + "      case 4:\n"
                + "        return DelphiTokenType.FOO;\n"
                + "      case 5:\n"
                + "        return DelphiTokenType.BAR_BAZ123_FLARP;\n"
                + "      default:\n"
                + "        throw new IllegalArgumentException(\"Unknown value: \" + value);\n"
                + "    }\n"
                + "  }\n"
                + "\n"
                + "  public static int getValueFromTokenType(DelphiTokenType tokenType) {\n"
                + "    switch(tokenType) {\n"
                + "      case EOF:\n"
                + "        return -1;\n"
                + "      case INVALID:\n"
                + "        return 0;\n"
                + "      case FOO:\n"
                + "        return 4;\n"
                + "      case BAR_BAZ123_FLARP:\n"
                + "        return 5;\n"
                + "      default:\n"
                + "        throw new IllegalArgumentException(\"Unknown type: \" + tokenType);\n"
                + "    }\n"
                + "  }\n"
                + "}\n");

    assertThat(getTokenEnum(tempDir))
        .isEqualTo(
            "package org.sonar.plugins.communitydelphi.api.token;\n"
                + "\n"
                + "import javax.annotation.processing.Generated;\n"
                + "\n"
                + "@Generated(\"au.com.integradev.delphi.DelphiTokensGenerator\")\n"
                + "public enum DelphiTokenType {\n"
                + "  EOF,\n"
                + "  INVALID,\n"
                + "  FOO,\n"
                + "  BAR_BAZ123_FLARP,\n"
                + "}\n");
  }

  @Test
  void testDeprecatedTokens(@TempDir Path tempDir) {
    generate(
        tempDir,
        "CARET=4\n" //
            + "CARROT__deprecated=5\n"
            + "'^'=4\n"
            + "'\uD83E\uDD55'=5\n");

    assertThat(getTokenFactory(tempDir))
        .isEqualTo(
            "package au.com.integradev.delphi.antlr.ast.token;\n"
                + "\n"
                + "import javax.annotation.processing.Generated;\n"
                + "import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;\n"
                + "\n"
                + "@SuppressWarnings(\"removal\")\n"
                + "@Generated(\"au.com.integradev.delphi.DelphiTokensGenerator\")\n"
                + "public final class DelphiTokenTypeFactory {\n"
                + "  private DelphiTokenTypeFactory() {\n"
                + "    // utility class\n"
                + "  }\n"
                + "\n"
                + "  public static DelphiTokenType createTokenType(int value) {\n"
                + "    switch(value) {\n"
                + "      case -1:\n"
                + "        return DelphiTokenType.EOF;\n"
                + "      case 0:\n"
                + "        return DelphiTokenType.INVALID;\n"
                + "      case 4:\n"
                + "        return DelphiTokenType.CARET;\n"
                + "      case 5:\n"
                + "        return DelphiTokenType.CARROT;\n"
                + "      default:\n"
                + "        throw new IllegalArgumentException(\"Unknown value: \" + value);\n"
                + "    }\n"
                + "  }\n"
                + "\n"
                + "  public static int getValueFromTokenType(DelphiTokenType tokenType) {\n"
                + "    switch(tokenType) {\n"
                + "      case EOF:\n"
                + "        return -1;\n"
                + "      case INVALID:\n"
                + "        return 0;\n"
                + "      case CARET:\n"
                + "        return 4;\n"
                + "      case CARROT:\n"
                + "        return 5;\n"
                + "      default:\n"
                + "        throw new IllegalArgumentException(\"Unknown type: \" + tokenType);\n"
                + "    }\n"
                + "  }\n"
                + "}\n");

    assertThat(getTokenEnum(tempDir))
        .isEqualTo(
            "package org.sonar.plugins.communitydelphi.api.token;\n"
                + "\n"
                + "import javax.annotation.processing.Generated;\n"
                + "\n"
                + "@Generated(\"au.com.integradev.delphi.DelphiTokensGenerator\")\n"
                + "public enum DelphiTokenType {\n"
                + "  EOF,\n"
                + "  INVALID,\n"
                + "  CARET,\n"
                + "  @Deprecated(forRemoval = true)\n"
                + "  CARROT,\n"
                + "}\n");
  }

  @Test
  void testTokensFileDoesNotExist(@TempDir Path root) {
    File tokensFile = root.resolve("Delphi.tokens").toFile();
    File outputDirectory = root.resolve("out").toFile();

    DelphiTokensGenerator generator = new DelphiTokensGenerator(tokensFile, outputDirectory);

    assertThatThrownBy(generator::generate).isExactlyInstanceOf(FileNotFoundException.class);
  }

  @Test
  void testOutputDirectoryCannotBeCreated(@TempDir Path root) throws IOException {
    File tokensFile = Files.createFile(root.resolve("Delphi.tokens")).toFile();
    File outputDirectory = Files.createFile(root.resolve("out")).toFile();

    DelphiTokensGenerator generator = new DelphiTokensGenerator(tokensFile, outputDirectory);

    assertThatThrownBy(generator::generate)
        .isExactlyInstanceOf(IOException.class)
        .hasCauseInstanceOf(FileAlreadyExistsException.class);
  }

  private static void generate(Path root, String tokens) {
    File tokensFile = root.resolve("Delphi.tokens").toFile();
    File outputDirectory = root.resolve("out").toFile();

    DelphiTokensGenerator generator = new DelphiTokensGenerator(tokensFile, outputDirectory);
    try {
      FileUtils.writeStringToFile(tokensFile, tokens, StandardCharsets.UTF_8);
      generator.generate();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static String getTokenFactory(Path root) {
    try {
      return Files.readString(
          root.resolve("out/au/com/integradev/delphi/antlr/ast/token/DelphiTokenTypeFactory.java"));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static String getTokenEnum(Path root) {
    try {
      return Files.readString(
          root.resolve("out/org/sonar/plugins/communitydelphi/api/token/DelphiTokenType.java"));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
