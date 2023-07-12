package au.com.integradev.delphi;

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
  private static final Pattern TOKENS_LINE_PATTERN =
      Pattern.compile("(?i)([a-z_]+[a-z_\\d])=(\\d+)");

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
    if (!tokensFile.exists()) {
      throw new MojoFailureException("Tokens file does not exist");
    }

    if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
      throw new MojoFailureException("Failed to create output directory");
    }

    try {
      List<TokenTypeRecord> tokenTypes = readTokenTypes();
      generateEnum(tokenTypes);
      generateEnumFactory(tokenTypes);
    } catch (Exception e) {
      getLog().error(e.getMessage());
      throw new MojoFailureException(e.getMessage(), e);
    }

    project.addCompileSourceRoot(outputDirectory.getPath());
  }

  private List<TokenTypeRecord> readTokenTypes() throws IOException {
    ImmutableList.Builder<TokenTypeRecord> result = ImmutableList.builder();

    result.add(new TokenTypeRecord("INVALID", 0));

    Files.readAllLines(tokensFile.toPath()).stream()
        .map(DelphiTokensGeneratorMojo::createTokenType)
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
            .append("public enum DelphiTokenType {\n");

    for (int i = 0; i < tokenTypes.size(); ++i) {
      builder.append("  ").append(tokenTypes.get(i).getName()).append(",\n");
    }

    builder.append("}\n");

    Files.writeString(outputPath.resolve("DelphiTokenType.java"), builder);
  }

  private void generateEnumFactory(List<TokenTypeRecord> tokenTypes) throws IOException {
    Path outputPath = outputDirectory.toPath().resolve("au/com/integradev/delphi/antlr/ast/token");

    Files.createDirectories(outputPath);

    StringBuilder builder =
        new StringBuilder()
            .append("package au.com.integradev.delphi.antlr.ast.token;\n\n")
            .append("import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;\n\n")
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
        .append("      default:")
        .append("        throw new IllegalArgumentException(\"Unknown token type: \" + value);")
        .append("    }\n")
        .append("  }\n")
        .append("}\n");

    Files.writeString(outputPath.resolve("DelphiTokenTypeFactory.java"), builder);
  }

  private static TokenTypeRecord createTokenType(String line) {
    Matcher matcher = TOKENS_LINE_PATTERN.matcher(line);
    if (matcher.matches()) {
      return new TokenTypeRecord(
          antlrNameToEnumName(matcher.group(1)), Integer.parseInt(matcher.group(2)));
    }
    return null;
  }

  private static String antlrNameToEnumName(String antlrName) {
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

    public TokenTypeRecord(String name, int value) {
      this.name = name;
      this.value = value;
    }

    public String getName() {
      return name;
    }

    public int getValue() {
      return value;
    }
  }
}
