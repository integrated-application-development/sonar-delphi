package org.sonar.plugins.delphi.utils.builders;

public class DelphiTestProgramBuilder extends DelphiTestFileBuilder<DelphiTestProgramBuilder> {
  private String programName = "omTestProgram";

  @Override
  protected DelphiTestProgramBuilder getThis() {
    return this;
  }

  @Override
  public int getOffsetDecl() {
    return 2;
  }

  @Override
  public int getOffset() {
    int offset = getDeclCount() + getOffsetDecl() + 1;
    if (!getDeclaration().isEmpty()) {
      ++offset;
    }

    return offset;
  }

  @Override
  public DelphiTestProgramBuilder appendImpl(String value) {
    // Just adding a tab for pretty-printing.
    return super.appendImpl("  " + value);
  }

  @Override
  protected StringBuilder generateSourceCode() {
    StringBuilder source = new StringBuilder();
    source.append(String.format("program %s;\n", this.programName));
    source.append("\n");

    if (!getDeclaration().isEmpty()) {
      source.append(getDeclaration());
      source.append("\n");
    }

    source.append("begin\n");

    if (!getImplementation().isEmpty()) {
      source.append(this.getImplementation());
    }

    source.append("end.\n");

    return source;
  }

  @Override
  protected String getFilenamePrefix() {
    return "program";
  }

  @Override
  protected String getFileExtension() {
    return "dpr";
  }

  public DelphiTestProgramBuilder programName(String programName) {
    this.programName = programName;
    return getThis();
  }
}
