package org.sonar.plugins.delphi.operator;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.errorprone.annotations.Immutable;
import java.util.Arrays;
import java.util.Set;
import org.sonar.plugins.delphi.symbol.resolve.Invocable;

@Immutable
class OperatorData {
  private final int tokenType;
  private final ImmutableSet<IntrinsicOperatorSignature> intrinsicOperatorSignatures;
  private final ImmutableSet<String> operatorNames;

  OperatorData(int tokenType, IntrinsicOperatorSignature... intrinsicOperatorSignatures) {
    this.tokenType = tokenType;
    this.intrinsicOperatorSignatures =
        Arrays.stream(intrinsicOperatorSignatures).collect(ImmutableSet.toImmutableSet());
    this.operatorNames =
        this.intrinsicOperatorSignatures.stream()
            .map(Invocable::getName)
            .collect(ImmutableSortedSet.toImmutableSortedSet(String.CASE_INSENSITIVE_ORDER));
  }

  public int getTokenType() {
    return tokenType;
  }

  public Set<String> getOperatorNames() {
    return operatorNames;
  }

  public Set<IntrinsicOperatorSignature> getIntrinsicOperatorSignatures() {
    return intrinsicOperatorSignatures;
  }
}
