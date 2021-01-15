import java.util.Random;

public class PositionsSplitterAndShuffler {
  private String fileNameTraining = "finding-elo/paeTrain.dat";
  private String fileNameTesting = "finding-elo/paeTest.dat";
  //
  //
  //
  private PositionEvalWriter writeTrain = new PositionEvalWriter(fileNameTraining);
  private PositionEvalWriter writeTest = new PositionEvalWriter(fileNameTesting);
  //
  private PositionEvalReader readPositions = new PositionEvalReader();
  //
  //
  //
  private PositionAndEvaluation[] positionsTrain;
  private PositionAndEvaluation[] positionsTest;
  //
  //
  //
  private final int numPositions = 136242;
  //
  //
  //
  private Random r = new Random();

  public PositionsSplitterAndShuffler(double percentAsTraining) {
    initializePositionContainers(percentAsTraining);
    fillPositionContainers();
    shufflePositionContainers();
    writePositionContainers();
  }

  private void initializePositionContainers(double percentAsTraining) {
    int positionsToUseForTraining = (int) (percentAsTraining * numPositions);
    positionsTrain = new PositionAndEvaluation[positionsToUseForTraining];
    int positionsToUseForTesting = numPositions - positionsToUseForTraining;
    positionsTest = new PositionAndEvaluation[positionsToUseForTesting];
  }

  private void fillPositionContainers() {
    fillContainer(positionsTrain);
    fillContainer(positionsTest);
    checkThatAllPositionsHaveBeenRead();
  }

  private void fillContainer(PositionAndEvaluation[] container) {
    for(int i = 0; i < container.length; i++) {
      PositionAndEvaluation positionAndEvaluation = new PositionAndEvaluation();
      readPositions.next(positionAndEvaluation);
      container[i] = positionAndEvaluation;
    }
  }

  private void checkThatAllPositionsHaveBeenRead() {
    if(readPositions.hasNext()) {
      System.out.println("There are positions left over.");
      System.exit(0);
    } else {
      System.out.println("All positions were read. Data has been loaded.");
    }
  }

  private void shufflePositionContainers() {
    shuffle(positionsTrain);
    System.out.println("Training data has been shuffled.");
    shuffle(positionsTest);
    System.out.println("Testing data has been shuffled.");
  }

  private void shuffle(PositionAndEvaluation[] container) {
    for(int i = 0; i < container.length; i++) {
      int poolSize = container.length-i;
      int selection = r.nextInt(poolSize) + i;
      switchPositions(container, i, selection);
    }
  }

  private void switchPositions(PositionAndEvaluation[] container, int positionOne, int positionTwo) {
    PositionAndEvaluation temp = container[positionOne];
    container[positionOne] = container[positionTwo];
    container[positionTwo] = temp;
  }

  private void writePositionContainers() {
    writePositionContainer(positionsTrain, writeTrain);
    writePositionContainer(positionsTest, writeTest);
    System.out.println("Data has been written.");
    closeWriters();
  }

  private void closeWriters() {
    writeTrain.close();
    writeTest.close();
  }

  private void writePositionContainer(PositionAndEvaluation[] container, PositionEvalWriter writer) {
    for(int i = 0; i < container.length; i++) {
      writer.writePositionAndEvaluation(container[i]);
    }
  }

  public static void main(String[] args) {
    double percentOfDataTraining = .9;
    PositionsSplitterAndShuffler splitAndShuffleData = new PositionsSplitterAndShuffler(percentOfDataTraining);
  }
}
