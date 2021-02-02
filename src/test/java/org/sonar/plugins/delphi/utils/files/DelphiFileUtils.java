package org.sonar.plugins.delphi.utils.files;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.sonar.plugins.delphi.file.DelphiFileConfig;
import org.sonar.plugins.delphi.preprocessor.search.SearchPath;
import org.sonar.plugins.delphi.utils.types.TypeFactoryUtils;

public final class DelphiFileUtils {
  private DelphiFileUtils() {
    // Utility class
  }

  public static DelphiFileConfig mockConfig() {
    DelphiFileConfig mock = mock(DelphiFileConfig.class);
    when(mock.getEncoding()).thenReturn(StandardCharsets.UTF_8.name());
    when(mock.getTypeFactory()).thenReturn(TypeFactoryUtils.defaultFactory());
    when(mock.getSearchPath()).thenReturn(SearchPath.create(Collections.emptyList()));
    when(mock.getDefinitions()).thenReturn(Collections.emptySet());
    return mock;
  }
}
