package org.sonar.plugins.delphi.symbol.resolve;

import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.Math.nextAfter;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores information about an invocation candidate, used for overload resolution. Based directly
 * off of the tcandidate record from the FreePascal compiler
 *
 * @see <a href="https://github.com/graemeg/freepascal/blob/master/compiler/htypechk.pas#L50">
 *     tcandidate</a>
 */
public class InvocationCandidate {
  public static final int CONVERT_LEVELS = 6;

  private final Invocable data;
  private int exactCount;
  private int equalCount;
  private final int[] convertLevelCount;
  private int convertOperatorCount;
  private double ordinalDistance;
  private int proceduralDistance;
  private final List<VariantConversionType> variantConversions;
  private boolean invalid;

  public InvocationCandidate(Invocable invocable) {
    this.data = invocable;
    this.convertLevelCount = new int[CONVERT_LEVELS];
    this.variantConversions = new ArrayList<>(invocable.getParametersCount());
  }

  public Invocable getData() {
    return data;
  }

  public int getExactCount() {
    return exactCount;
  }

  public void incrementExactCount() {
    ++this.exactCount;
  }

  public int getEqualCount() {
    return equalCount;
  }

  public void incrementEqualCount() {
    ++this.equalCount;
  }

  public int getConvertLevelCount(int convertLevel) {
    return convertLevelCount[convertLevel - 1];
  }

  public void incrementConvertLevelCount(int convertLevel) {
    ++this.convertLevelCount[convertLevel - 1];
  }

  public int getConvertOperatorCount() {
    return convertOperatorCount;
  }

  public void incrementConvertOperatorCount() {
    ++this.convertOperatorCount;
  }

  public double getOrdinalDistance() {
    return ordinalDistance;
  }

  public void increaseOrdinalDistance(double ordinalDistance) {
    this.ordinalDistance += ordinalDistance;
  }

  public void bumpOrdinalDistance() {
    this.ordinalDistance = nextAfter(this.ordinalDistance, POSITIVE_INFINITY);
  }

  public void increaseProceduralDistance(int proceduralDistance) {
    this.proceduralDistance += proceduralDistance;
  }

  public int getProceduralDistance() {
    return proceduralDistance;
  }

  public void addVariantConversion(VariantConversionType variantConversionType) {
    variantConversions.add(variantConversionType);
  }

  public VariantConversionType getVariantConversionType(int argumentIndex) {
    return variantConversions.get(argumentIndex);
  }

  public boolean isInvalid() {
    return invalid;
  }

  public void setInvalid() {
    this.invalid = true;
  }
}
