package au.com.integradev.delphi;

import au.com.integradev.delphi.checks.CheckList;
import org.sonar.plugins.communitydelphi.api.check.CheckRegistrar;

public class DelphiCheckRegistrar implements CheckRegistrar {
  @Override
  public void register(RegistrarContext registrarContext) {
    registrarContext.registerClassesForRepository(CheckList.REPOSITORY_KEY, CheckList.getChecks());
  }
}
