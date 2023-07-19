package au.com.integradev.delphi.executor;

import au.com.integradev.delphi.file.DelphiFile.DelphiInputFile;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;

public class DelphiNoSonarExecutor implements Executor {
  private final NoSonarFilter noSonarFilter;
  private static final Pattern PATTERN = Pattern.compile(".*\\bNOSONAR\\b.*");

  public DelphiNoSonarExecutor(NoSonarFilter noSonarFilter) {
    this.noSonarFilter = noSonarFilter;
  }

  @Override
  public void execute(Context context, DelphiInputFile delphiFile) {
    Set<Integer> noSonarLines = new HashSet<>();
    for (DelphiToken token : delphiFile.getComments()) {
      if (PATTERN.matcher(token.getImage()).matches()) {
        noSonarLines.add(token.getBeginLine());
      }
    }
    if (!noSonarLines.isEmpty()) {
      noSonarFilter.noSonarInFile(delphiFile.getInputFile(), noSonarLines);
    }
  }
}
