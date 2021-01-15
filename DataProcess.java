import java.util.Scanner;
import java.io.*;
import java.lang.Math;
import java.lang.InterruptedException;
import java.math.BigDecimal;

//NOTES
/*
Have a way to serailize board positions, exclude repeated positions. This will
exclude positions that are evaluated as 0 because of repitition.
The board will be a single object which will be modified move by move, in order
to save stack space. No need to implement equals(Object) method or hashCode()
method. Just an ID variable that changes with moves, but can revert back to
previous values with undoings of those moves.

Shuffling algorithm. The data will be loaded in with several similar positions
(games are just strings of positions that are one move apart).
*/

public class DataProcess {
  private GameIterator gameIt = new GameIterator();
  private MoveIterator moveIt = new MoveIterator();
  private GameEvalIterator gameEvalIt = new GameEvalIterator();
  private MoveEvalIterator moveEvalIt = new MoveEvalIterator();
  private int gamesRun = 0;

  private PositionEvalWriter positionEvalWriter = new PositionEvalWriter();

  public DataProcess() {

  }

  private void processGames() {
    while(gameIt.hasNext()) {
      runGame(gameIt.next(), gameEvalIt.next());
      gamesRun++;
      if(gamesRun % 1000 == 0) {
        System.out.println(gamesRun + " games run.");
      }
    }
    closeFileWriter();
  }

  private int iterations = 1;
  private void runGame(String gameString, String boardEvalString) {
    final int multiple = 30;
    Board board = new Board();
    moveIt.focusOnGame(gameString);
    moveEvalIt.focusOnGame(boardEvalString);
    int[] moveContainer = new int[3];
    while(moveIt.hasNext()) {
      moveIt.next(moveContainer);
      feedBoardMove(board, moveContainer);
      long[] inputLayer = board.inputLayer();
      int boardEval = moveEvalIt.next();
      if(iterations % multiple == 0 && boardEvaluationIsNotNAAndPositionIsNotRepeated(board, boardEval)) {
        filePositionAndEval(inputLayer, boardEval);
      }
      iterations++;
    }
  }

  private boolean boardEvaluationIsNotNAAndPositionIsNotRepeated(Board board, int boardEvaluation) {
    return boardEvaluationIsNotNA(boardEvaluation) && positionIsNotRepeated(board);
  }

  private boolean boardEvaluationIsNotNA(int boardEvaluation) {
    return !moveEvalIt.moveIsNA(boardEvaluation);
  }

  private boolean positionIsNotRepeated(Board board) {
    return board.timesPositionWasReached() == 0;
  }

  private static void feedBoardMove(Board board, int[] move) {
    int start = move[0];
    int end = move[1];
    int promotedPiece = move[2];
    board.movePiece(start, end);
    if(moveIsPromotion(promotedPiece)) {
      board.promotePiece(end, promotedPiece);
    }
  }

  private static boolean moveIsPromotion(int pieceIndex) {
    return pieceIndex != Board.EMPTYINDEx;
  }

  private void filePositionAndEval(long[] inputLayer, int boardEval) {
    double adjusted = adjustEval(boardEval);
    positionEvalWriter.writePositionAndEvaluation(inputLayer, adjusted);
  }

  private void closeFileWriter() {
    positionEvalWriter.close();
  }

  private static void sleep(int milliseconds) {
    try {
      Thread.sleep(milliseconds);
    } catch(InterruptedException ie) {
      ie.printStackTrace();
    }
  }

  private double adjustEval(int eval) {
    if(eval == 0) {
      return 0.5;
    }
    int mult = 1;
    if(eval < 0) {
      mult = -1;
      eval *= -1;
    }
    double e = Math.exp(1);
    double dEval = (double) eval;
    double plusE = dEval + e;
    double logE = log_e(plusE);
    double log10 = logBase(logE, 10);
    double toTheFifth = Math.pow(log10, 5);
    double scale57 = toTheFifth * 0.57d;
    double reconsiderSign = scale57 * mult;
    double shift5 = reconsiderSign + 0.5;
    return shift5;
  }

  private double log_e(double value) {
    return Math.log(value);
  }

  private double logBase(double value, double base) {
    return Math.log(value) / Math.log(base);
  }

  public static void main(String[] args) {
    DataProcess dataProcess = new DataProcess();
    dataProcess.processGames();
    // final int millisecondsBetweenMoves = 700;
    // final int setupMilliseconds = 2000;
    //
    // ChessGUI chessGUI = new ChessGUI("Eggs");
    // chessGUI.displayGUI();
    // GameIterator gameIt = new GameIterator();
    // MoveIterator moveIt = new MoveIterator();
    // int[] moveContainer = new int[3];
    // while(gameIt.hasNext()) {
    //   Board board = new Board();
    //   chessGUI.displayBoard(board.getBoardRepresentation()); //board.inputLayer() to test long[]
    //   sleep(millisecondsBetweenMoves);
    //   String gameString = gameIt.next();
    //   moveIt.focusOnGame(gameString);
    //   while(moveIt.hasNext()) {
    //     moveIt.next(moveContainer);
    //     // System.out.println(moveContainer[0] + " " + moveContainer[1] + " " + moveContainer[2]);
    //     feedBoardMove(board, moveContainer);
    //     chessGUI.displayBoard(board.getBoardRepresentation());
    //     sleep(millisecondsBetweenMoves);
    //   }
    // }
    //NOTE: files board evaluation values onto separate lines,
    //making them easy to graph in R. Used R to figure out best function for
    //mapping evaluations to [0, 1]
    //NOTE: modified to file adjusted values, to compare with originals and make
    //sure that the logic behind the function was correct
    // GameEvalIterator gameIt = new GameEvalIterator();
    // MoveEvalIterator moveIt = new MoveEvalIterator();
    // try {
    //   File gameEvaluations = new File("gameEvaluations2.txt");
    //   gameEvaluations.createNewFile();
    // } catch(IOException e) {
    //   e.printStackTrace();
    // }
    // FileWriter writer = null;
    // try {
    //   writer = new FileWriter("gameEvaluations2.txt");
    // } catch(IOException e) {
    //   e.printStackTrace();
    // }
    //
    // // int games = 0;
    // while(gameIt.hasNext()) {
    //   // games++;
    //   String game = gameIt.next();
    //   moveIt.focusOnGame(game);
    //   while(moveIt.hasNext()) {
    //     try {
    //       int val = moveIt.next();
    //       double shifted = adjustEval(val);
    //       // System.out.println(val);
    //       writer.write(Double.toString(shifted));
    //       writer.write(System.lineSeparator());
    //     } catch(IOException e) {
    //       e.printStackTrace();
    //     }
    //   }
    // }
    // try {
    //   writer.close();
    // } catch(IOException e) {
    //   e.printStackTrace();
    // }
  }
}

class GameIterator {
  private Scanner scanGames;
  private String filePath = "finding-elo/data_uci.pgn";
  private String nextGame;
  private boolean hasNextGame = false;
  private int linesBeforeFirstGame = 10;
  private int linesBetweenGames = 11;
  private int gamesProcessed = 0;

  GameIterator() {
    initializeScanner();
    loadLines(linesBeforeFirstGame);
    loadGame();
  }

  private void initializeScanner() {
    try {
      scanGames = new Scanner(new File(filePath));
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  private void loadLines(int numLines) {
    for(int i = 0; i < numLines && scanGames.hasNextLine(); i++) {
      scanGames.nextLine();
    }
  }

  private void loadGame() {
    hasNextGame = scanGames.hasNextLine();
    if(hasNextGame) {
      nextGame = scanGames.nextLine();
    }
    gamesProcessed++;
    loadLines(linesBetweenGames());
  }

  private int linesBetweenGames() {
    if(gamesProcessed < 25000) {
      return 11;
    }
    return 9;
  }

  boolean hasNext() {
    return hasNextGame;
  }

  String next() {
    String gameToReturn = nextGame;
    loadGame();
    return gameToReturn;
  }
}

class MoveIterator {
  private final int width = 8;
  private char[] game;
  private int index;
  private int end;
  private boolean hasMove = false;
  private int[] moveContainer = new int[3]; //start, end, promotion piece

  MoveIterator() {

  }

  void focusOnGame(String game) {
    this.game = game.toCharArray();
    index = 0;
    end = lastSpace();
    loadMove();
  }

  private void loadMove() {
    hasMove = index < end;
    if(hasMove) {
      fillMoveContainer();
    }
  }

  private void fillMoveContainer() {
    int startColumn = letterToNumber(game[index++]);
    int startRow = numberToNumber(game[index++]);
    int endColumn = letterToNumber(game[index++]);
    int endRow = numberToNumber(game[index++]);
    moveContainer[0] = position(startColumn, startRow);
    moveContainer[1] = position(endColumn, endRow);
    moveContainer[2] = promoted(game[index++]);
    if(moveContainer[2] != 0) {
      index++;
    }
  }

  private int promoted(char piece) {
    switch(piece) {
      case ' ':
        return 0;
      case 'Q':
        return Board.BQUEENINDEx;
      case 'N':
        return Board.BKNIGHTINDEx;
      case 'R':
        return Board.BROOKINDEx;
      case 'B':
        return Board.BBISHOPINDEx;
      default:
        System.out.println("Invalid promotion piece " + piece);
        System.out.println("game string was " + new String(game));
        return -1;
    }
  }

  private int letterToNumber(char letter) {
    return letter - 97;
  }

  private int numberToNumber(char number) {
    return number - 49;
  }

  private int position(int x, int y) {
    return y*width + x;
  }

  private int lastSpace() {
    int index = game.length-1;
    while(index >= 0 && game[index] != ' ') {
      index--;
    }
    return index;
  }

  int[] next() {
    int[] moveToReturn = new int[moveContainer.length];
    next(moveToReturn);
    return moveToReturn;
  }

  void next(int[] tempContainer) {
    copy(tempContainer, moveContainer);
    loadMove();
  }

  private void copy(int[] copyInto, int[] copyFrom) {
    for(int i = 0; i < copyFrom.length; i++) {
      copyInto[i] = copyFrom[i];
    }
  }

  boolean hasNext() {
    return hasMove;
  }
}

class GameEvalIterator {
  private Scanner scanGameEvaluations;
  private String filePath = "finding-elo/stockfish.csv";
  private boolean hasNextGame = false;
  private String nextGame;
  private int linesBeforeFirstGame = 1;

  GameEvalIterator() {
    initializeScanner();
    loadLines(linesBeforeFirstGame);
    loadGame();
  }

  private void initializeScanner() {
    try {
      scanGameEvaluations = new Scanner(new File(filePath));
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  private void loadLines(int numLines) {
    for(int i = 0; i < numLines && scanGameEvaluations.hasNextLine(); i++) {
      scanGameEvaluations.nextLine();
    }
  }

  private void loadGame() {
    hasNextGame = scanGameEvaluations.hasNextLine();
    if(hasNextGame) {
      nextGame = scanGameEvaluations.nextLine();
    }
  }

  boolean hasNext() {
    return hasNextGame;
  }

  String next() {
    String toReturn = nextGame;
    loadGame();
    return toReturn;
  }
}

class MoveEvalIterator {
  private char[] game;
  private int index;
  private boolean hasMoveEval = false;
  private int moveEval;
  private char minusSign = '-';
  private char space = ' ';
  private char firstLetterOfNA = 'N';
  private final int NAid = 20000;
  private int lengthOfNASequence = 2;

  MoveEvalIterator() {

  }

  void focusOnGame(String gameString) {
    game = gameString.toCharArray();
    index = firstComma()+1;
    loadEval();
  }

  void loadEval() {
    hasMoveEval = index < game.length;
    if(hasMoveEval) {
      int mult = 1;
      char first = game[index++];
      if(first == firstLetterOfNA) {
        moveEval = NAid;
        return;
      }

      if(first == minusSign) {
        mult = -1;
        first = game[index++];
      }
      int number = numberToNumber(first);
      while(index < game.length && game[index] != space) {
        number *= 10;
        number += numberToNumber(game[index++]);
      }
      moveEval = number * mult;
      index++;
    }
  }

  private int firstComma() {
    int index = 0;
    while(index < game.length && game[index] != ',') {
      index++;
    }
    return index;
  }

  private int numberToNumber(char number) {
    return number - 48;
  }

  boolean hasNext() {
    return hasMoveEval;
  }

  int next() {
    int evalToReturn = moveEval;
    loadEval();
    return evalToReturn;
  }

  boolean moveIsNA(int move) {
    return move == NAid;
  }
}
