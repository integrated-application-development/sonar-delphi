package org.sonar.plugins.delphi.pmd.xpath;

import java.util.List;
import org.jaxen.Context;
import org.jaxen.Function;

abstract class AbstractFunction implements Function {
  @SuppressWarnings("rawtypes")
  @Override
  public final Object call(final Context context, final List args) {
    return doCall(context, args);
  }

  protected abstract Object doCall(final Context context, final List<?> args);
}
