package org.sonar.plugins.delphi.file;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.io.File;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.file.DelphiFile.DelphiFileConstructionException;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonar.plugins.delphi.utils.files.DelphiFileUtils;

class DelphiFileTest {
  @Test
  void testEmptyFileShouldThrowException() {
    File sourceFile = DelphiUtils.getResource("/org/sonar/plugins/delphi/file/Empty.pas");
    DelphiFileConfig config = DelphiFileUtils.mockConfig();

    assertThatThrownBy(() -> DelphiFile.from(sourceFile, config))
        .isInstanceOf(DelphiFileConstructionException.class)
        .hasCauseInstanceOf(RuntimeException.class);
  }
}
