package org.sonar.plugins.delphi.antlr.filestream;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Used to configure the encoding and resolver behavior in the DelphiFileStream class
 *
 * @see DelphiFileStream
 */
public class DelphiFileStreamConfig {
  private String encoding;
  private final List<File> includedDirs;
  private final Set<String> definitions;
  private final boolean extendIncludes;

  /**
   * Constructor which only sets encoding
   *
   * @param encoding The expected file encoding
   */
  public DelphiFileStreamConfig(String encoding) {
    this.encoding = null;
    this.includedDirs = new ArrayList<>();
    this.definitions = new HashSet<>();
    this.extendIncludes = true;
    this.encoding = encoding;
  }

  /**
   * Constructor
   *
   * @param encoding The expected file encoding
   * @param includedDirs Included directories, used by the Include Resolver
   * @param definitions Compiler definitions, used by the Define Resolver
   * @param extendIncludes Whether the Include Resolver should insert includes into files
   */
  public DelphiFileStreamConfig(
      String encoding, List<File> includedDirs, List<String> definitions, boolean extendIncludes) {
    this.encoding = encoding;
    this.includedDirs = includedDirs;
    this.definitions = new HashSet<>(definitions);
    this.extendIncludes = extendIncludes;
  }

  public String getEncoding() {
    return encoding;
  }

  public List<File> getIncludedDirs() {
    return includedDirs;
  }

  public Set<String> getDefinitions() {
    return definitions;
  }

  public boolean getExtendIncludes() {
    return extendIncludes;
  }
}
