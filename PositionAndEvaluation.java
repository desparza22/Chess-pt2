
public class PositionAndEvaluation {
  private final int numLongsPerPosition = 14;
  private long[] positionContainer = new long[numLongsPerPosition];
  private double positionEvaluation;

  public PositionAndEvaluation() {

  }

  public long[] getPositionContainer() {
    return positionContainer;
  }

  public void setEvaluation(double value) {
    positionEvaluation = value;
  }

  public double getEvaluation() {
    return positionEvaluation;
  }
}
