// import java.io.DataInputStream;
// import java.io.FileInputStream;
// import java.io.IOException;

public class PositionEvalReader {
  private String fileName = "finding-elo/positionsAndEvaluations.txt";
  private FastReaderWriter dis = null;

  private long[] nextInputLayer;
  private double nextEvaluation;

  public PositionEvalReader() {
    initializeStreams();
  }

  public PositionEvalReader(String alternateFile) {
    fileName = alternateFile;
    initializeStreams();
  }

  public void next(PositionAndEvaluation container) {
    fillPosition(container);
    fillEvaluation(container);
  }

  public PositionAndEvaluation next() {
    PositionAndEvaluation container = new PositionAndEvaluation();
    next(container);
    return container;
  }

  private void fillPosition(PositionAndEvaluation container) {
    long[] positionContainer = container.getPositionContainer();
    for(int i = 0; i < positionContainer.length; i++) {
      positionContainer[i] = dis.readLong();
    }
  }

  private void fillEvaluation(PositionAndEvaluation container) {
    double positionEvaluation = dis.readDouble();
    container.setEvaluation(positionEvaluation);
  }

  public boolean hasNext() {
    return dis.hasNext();
  }

  private void initializeStreams() {
    dis = new FastReaderWriter();
    dis.setReadingFile(fileName);
  }

  public void close() {
    dis.closeInput();
  }

  public static void main(String[] args) {
    int positions = 0;
    PositionEvalReader positionEvalReader = new PositionEvalReader();
    PositionAndEvaluation positionAndEvaluation = new PositionAndEvaluation();
    while(positionEvalReader.hasNext()) {
      positionEvalReader.next(positionAndEvaluation);
      positions++;
    }
    positionEvalReader.close();
    System.out.println("Across 50,000 games, there are " + positions + " positions");
  }
}
