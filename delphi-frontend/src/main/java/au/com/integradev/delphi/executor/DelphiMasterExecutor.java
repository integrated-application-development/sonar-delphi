/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package au.com.integradev.delphi.executor;

import au.com.integradev.delphi.file.DelphiFile.DelphiInputFile;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.communitydelphi.api.FatalAnalysisError;

public class DelphiMasterExecutor implements Executor {
  private static final Logger LOG = LoggerFactory.getLogger(DelphiMasterExecutor.class);
  private final List<Executor> executors;
  private final Set<Class<? extends Executor>> executed;

  public DelphiMasterExecutor(Executor... allExecutors) {
    executors = Arrays.asList(allExecutors);
    executed = new HashSet<>();
  }

  @Override
  public void setup() {
    for (Executor executor : executors) {
      executor.setup();
    }
  }

  @Override
  public void execute(Context context, DelphiInputFile file) {
    executed.clear();
    for (Executor executor : executors) {
      try {
        executeExecutor(executor, context, file);
      } catch (FatalAnalysisError e) {
        throw e;
      } catch (Exception e) {
        String executorName = executor.getClass().getSimpleName();
        String fileName = file.getSourceCodeFile().getName();
        LOG.error("Error occurred while running {} on file: {}", executorName, fileName, e);
        LOG.info("Continuing with next executor.");
      }
    }
  }

  @Override
  public void complete() {
    for (Executor executor : executors) {
      executor.complete();
    }
  }

  private void executeExecutor(Executor executor, Context context, DelphiInputFile file) {
    if (!executed.contains(executor.getClass())) {
      executeDependencies(executor, context, file);
      executor.execute(context, file);
      executed.add(executor.getClass());
    }
  }

  private void executeDependencies(Executor executor, Context context, DelphiInputFile file) {
    for (Class<? extends Executor> dependency : executor.dependencies()) {
      if (executed.contains(dependency)) {
        continue;
      }

      Executor dependencyExecutor =
          executors.stream()
              .filter(exec -> exec.getClass().equals(dependency))
              .findFirst()
              .orElseThrow(() -> new UnsatisfiedExecutorDependencyException(executor, dependency));

      try {
        executeExecutor(dependencyExecutor, context, file);
      } catch (FatalAnalysisError e) {
        throw e;
      } catch (Exception e) {
        throw new UnsatisfiedExecutorDependencyException(executor, dependency, e);
      }
    }
  }
}
