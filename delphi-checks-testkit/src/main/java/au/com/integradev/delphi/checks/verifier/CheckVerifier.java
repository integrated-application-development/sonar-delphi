package au.com.integradev.delphi.checks.verifier;

import au.com.integradev.delphi.builders.DelphiTestFileBuilder;
import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;

public interface CheckVerifier {
  static CheckVerifier newVerifier() {
    return new CheckVerifierImpl();
  }

  CheckVerifier withCheck(DelphiCheck check);

  CheckVerifier withUnitScopeName(String unitScope);

  CheckVerifier withUnitAlias(String alias, String unitName);

  CheckVerifier withSearchPathUnit(DelphiTestUnitBuilder builder);

  CheckVerifier onFile(DelphiTestFileBuilder<?> builder);

  void verifyIssueOnLine(int... lines);

  void verifyIssueOnFile();

  void verifyIssueOnProject();

  void verifyNoIssues();
}
