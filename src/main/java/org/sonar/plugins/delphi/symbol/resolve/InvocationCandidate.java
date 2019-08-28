package org.sonar.plugins.delphi.symbol.resolve;

/**
 * Stores information about an invocation candidate, used for overload resolution. Based directly
 * off of the tcandidate record from the FreePascal compiler
 *
 * @see <a href="https://github.com/graemeg/freepascal/blob/master/compiler/htypechk.pas#L50">
 *     tcandidate</a>
 */
public class InvocationCandidate {
  public static final int CONVERT_LEVELS = 6;

  private Invocable data;
  private int exactCount;
  private int equalCount;
  private int[] convertLevelCount = new int[CONVERT_LEVELS];
  private int convertOperatorCount;
  private double ordinalDistance;
  private boolean invalid;

  public InvocationCandidate(Invocable invocable) {
    this.data = invocable;
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

  public void setOrdinalDistance(double ordinalDistance) {
    this.ordinalDistance = ordinalDistance;
  }

  public void increaseOrdinalDistance(double ordinalDistance) {
    this.ordinalDistance += ordinalDistance;
  }

  public boolean isInvalid() {
    return invalid;
  }

  public void setInvalid() {
    this.invalid = true;
  }
}
