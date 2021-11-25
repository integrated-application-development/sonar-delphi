package org.sonar.plugins.delphi.coverage;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.coverage.delphicodecoveragetool.DelphiCodeCoverageToolParser;

class DelphiCoverageParserFactoryTest {
  private final DelphiCoverageParserFactory factory = new DelphiCoverageParserFactory();

  @Test
  void testGetNonExistentParser() {
    assertThat(factory.getParser(UUID.randomUUID().toString(), null)).isEmpty();
  }

  @Test
  void testGetDelphiCodeCoverageParser() {
    assertThat(factory.getParser("dcc", null))
        .get()
        .isInstanceOf(DelphiCodeCoverageToolParser.class);
  }
}
