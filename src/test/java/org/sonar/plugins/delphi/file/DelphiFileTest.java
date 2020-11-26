package org.sonar.plugins.delphi.file;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.io.File;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.file.DelphiFile.DelphiFileConstructionException;
import org.sonar.plugins.delphi.utils.DelphiUtils;

class DelphiFileTest {
  @Test
  void testEmptyFileShouldThrowException() {
    File sourceFile = DelphiUtils.getResource("/org/sonar/plugins/delphi/file/Empty.pas");
    DelphiFileConfig config = DelphiFile.createConfig(UTF_8.name());

    assertThatThrownBy(() -> DelphiFile.from(sourceFile, config))
        .isInstanceOf(DelphiFileConstructionException.class)
        .hasCauseInstanceOf(RuntimeException.class);
  }
}
