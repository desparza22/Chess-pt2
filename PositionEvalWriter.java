// import java.io.DataOutputStream;
// import java.io.FileOutputStream;
// import java.io.IOException;

public class PositionEvalWriter {
  private String fileName = "finding-elo/positionsAndEvaluations.txt";
  private FastReaderWriter dos = null;

  public PositionEvalWriter() {
    initializeStreams();
  }

  public PositionEvalWriter(String alternateFile) {
    fileName = alternateFile;
    initializeStreams();
  }

  private void initializeStreams() {
    dos = new FastReaderWriter();
    dos.setWritingFile(fileName);
  }

  public void writePositionAndEvaluation(long[] positionInputLayer, double evaluation) {
    for(long positionLong : positionInputLayer) {
      write(positionLong);
    }
    write(evaluation);
  }

  public void writePositionAndEvaluation(PositionAndEvaluation positionAndEvaluation) {
    long[] positionInputLayer = positionAndEvaluation.getPositionContainer();
    double evaluation = positionAndEvaluation.getEvaluation();
    writePositionAndEvaluation(positionInputLayer, evaluation);
  }

  public void close() {
    dos.flushAndClose();
  }

  private void write(double value) {
    dos.writeDouble(value);
  }

  private void write(long value) {
    dos.writeLong(value);
  }
}
