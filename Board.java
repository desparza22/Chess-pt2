import java.util.HashMap;

public class Board {
  private final int width = 8;
  private final int typesOfPieces = 6;
  private final int numColors = 2;
  private final int includeEmpty = 1;
  private final int squares = 64;
  private final int inputNodesNeeded = (typesOfPieces * numColors + includeEmpty) * squares;
  private InputLayer inputLayer = new InputLayer(inputNodesNeeded);
  private PhysicalBoard boardRepresentation = new PhysicalBoard();
  private BoardPositionTracker boardPositionTracker = new BoardPositionTracker();
  private boolean pawnPlayOccured = false;
  private boolean pieceWasTaken = false;
  private boolean castleOccured = false;
  private int timesThisPositionHasBeenReached = 0;

  public static final int EMPTYINDEx = 0;
  public static final int BPAWNINDEx = 1;
  public static final int BROOKINDEx = 2;
  public static final int BKNIGHTINDEx = 3;
  public static final int BBISHOPINDEx = 4;
  public static final int BQUEENINDEx = 5;
  public static final int BKINGINDEx = 6;
  public static final int WPAWNINDEx = 7;
  public static final int WROOKINDEx = 8;
  public static final int WKNIGHTINDEx = 9;
  public static final int WBISHOPINDEx = 10;
  public static final int WQUEENINDEx = 11;
  public static final int WKINGINDEx = 12;

  private int[] enpassantInfo = new int[2];
  private final int white = 0;
  private final int black = 1;
  private final int neither = 2;
  private int[] castleInfo = new int[4];
  private final int castle = 1;
  private final int noCastle = 0;



  int definingVariableTwiceSoItWontCompile = 1;
  // int definingVariableTwiceSoItWontCompile = 2;
  //TODO: implement communication of moves on part of DataProcess

  public Board() {
    logBoardPosition();
  }

  public void movePiece(int start, int end) {
    inputLayer.clearEnpassant();
    inputLayer.switchWhoseTurn();

    int activePiece = boardRepresentation.getPieceAt(start);
    int takenPiece = boardRepresentation.getPieceAt(end);
    updateBoardAndInputMove(activePiece, takenPiece, start, end);
    updatePawnPlayOccuredAndPieceWasTaken(activePiece, takenPiece);

    checkPawnPush(activePiece, start, end);
    if(thereIsAPawnPush()) {
      updateBoardAndInputEnpassant();
    } else {
      noPawnPushSoCheckCastle(activePiece, start, end);
      updateBoardAndInputCastle();
    }
    if(specialMove()) {
      clearBoardPositions();
      resetSpecialMoves();
    }
    logBoardPosition();
  }

  public int timesPositionWasReached() {
    return timesThisPositionHasBeenReached;
  }

  private void logBoardPosition() {
    long positionID = inputLayer.currentID();
    long[] positionArray = inputLayer();
    timesThisPositionHasBeenReached = boardPositionTracker.addPosition(positionID, positionArray);
  }

  private boolean specialMove() {
    return pawnPlayOccured || pieceWasTaken || castleOccured;
  }

  private void clearBoardPositions() {
    boardPositionTracker.clearPositions();
  }

  private void updatePawnPlayOccuredAndPieceWasTaken(int activePiece, int takenPiece) {
    pawnPlayOccured = activePiece == BPAWNINDEx || activePiece == WPAWNINDEx;
    pieceWasTaken = takenPiece != EMPTYINDEx;
  }

  private void resetSpecialMoves() {
    pawnPlayOccured = false;
    pieceWasTaken = false;
    castleOccured = false;
  }

  private boolean thereIsAPawnPush() {
    return enpassantInfo[0] != neither;
  }

  private void updateBoardAndInputEnpassant() {
    boardRepresentation.updateEnpassantInfo(enpassantInfo);
    inputLayer.updateEnpassantInfo(enpassantInfo);
  }

  private void noPawnPushSoCheckCastle(int activePiece, int start, int end) {
    checkCastle(activePiece, start, end);
    if(thereIsACastle()) {
      castleOccured = true;
      activePiece = castleInfo[1];
      int takenPiece = EMPTYINDEx;
      start = castleInfo[2];
      end = castleInfo[3];
      updateBoardAndInputMove(activePiece, takenPiece, start, end);
    }
  }

  private boolean thereIsACastle() {
    return castleInfo[0] == castle;
  }

  private void updateBoardAndInputCastle() {
    boardRepresentation.updateCastling();
    boolean[][] canCastleWhiteBlackLeftRight = boardRepresentation.getCastlingInfo();
    inputLayer.updateCastling(canCastleWhiteBlackLeftRight);
  }

  private void updateBoardAndInputMove(int activePiece, int takenPiece, int start, int end) {
    inputLayer.movePiece(activePiece, takenPiece, start, end);
    boardRepresentation.setPosition(EMPTYINDEx, start);
    boardRepresentation.setPosition(activePiece, end);
  }

  public void promotePiece(int location, int promotedTo) {
    final int shift = 6;
    int pawnIndex = BPAWNINDEx;
    if(promotionLocationIsWhite(location)) {
      pawnIndex += shift;
      promotedTo += shift;
    }
    updateBoardAndInputPromote(location, promotedTo, pawnIndex);
  }

  private void updateBoardAndInputPromote(int location, int promotedTo, int genericPawnIndex) {
    inputLayer.promote(promotedTo, location, genericPawnIndex);
    boardRepresentation.setPosition(promotedTo, location);
  }

  private boolean promotionLocationIsWhite(int location) {
    return location < width;
  }

  private void checkPawnPush(int activePiece, int start, int end) {
    final int pushDistance = 2*width;
    if(activePiece == WPAWNINDEx) {
      if(start - end == pushDistance) {
        enpassantInfo[0] = white;
        enpassantInfo[1] = column(start);
      } else {
        enpassantInfo[0] = neither;
      }
    } else if(activePiece == BPAWNINDEx) {
      if(end - start == pushDistance) {
        enpassantInfo[0] = black;
        enpassantInfo[1] = column(start);
      } else {
        enpassantInfo[0] = neither;
      }
    } else {
      enpassantInfo[0] = neither;
    }
  }

  private int column(int positionIndex) {
    return positionIndex % width;
  }

  private void checkCastle(int activePiece, int start, int end) {
    final int blackKingRow = 0;
    final int whiteKingRow = 7;
    if(activePiece == BKINGINDEx) {
      checkCastleHelp(start, end, blackKingRow, BROOKINDEx);
    } else if(activePiece == WKINGINDEx) {
      checkCastleHelp(start, end, whiteKingRow, WROOKINDEx);
    } else {
      castleInfo[0] = noCastle;
    }
  }

  private void checkCastleHelp(int start, int end, int kingRow, int rookIndex) {
    int moveDistance;
    int rookColumnStart;
    int rookColumnEnd;
    if(start < end) {
      moveDistance = end - start;
      rookColumnStart = 7;
      rookColumnEnd = rookColumnStart - 2;
    } else {
      moveDistance = start - end;
      rookColumnStart = 0;
      rookColumnEnd = rookColumnStart + 3;
    }
    if(moveDistance == 2) {
      castleInfo[0] = castle;
      castleInfo[1] = rookIndex;
      castleInfo[2] = coordToPosition(rookColumnStart, kingRow);
      castleInfo[3] = coordToPosition(rookColumnEnd, kingRow);
    } else {
      castleInfo[0] = noCastle;
    }
  }

  public long[] inputLayer() {
    if(!inputLayer.checkValidity()) {
      System.out.println("invalid input layer");
    }
    return inputLayer.currentState();
  }

  public int[] getBoardRepresentation() {
    return boardRepresentation.boardArray();
  }

  private int coordToPosition(int x, int y) {
    return y*width + x;
  }
}

class PhysicalBoard {
  private final int width = 8;
  private int[] boardRepresentation = new int[width*width];
  private int moveCount = 0;
  private final int numPawns = width;
  private int[][] whenPawnFirstMoved = new int[2][numPawns];
  private boolean[][] canCastleWhiteBlackLeftRight = new boolean[2][2];
  private int[][] castlePositionsAndExpectedPieces;


  PhysicalBoard() {
    setCastlePositions();
    setUpBoard();
    setCanCastleTrue();
    resetPawnFirstMoved();
    moveCount = 0;
  }

  public boolean[][] getCastlingInfo() {
    return canCastleWhiteBlackLeftRight;
  }

  private void setCastlePositions() {
    int blackRook1 = coordToPosition(0, 0);
    int blackRook2 = coordToPosition(7, 0);
    int blackKing = coordToPosition(4, 0);
    int whiteRook1 = coordToPosition(0, 7);
    int whiteRook2 = coordToPosition(7, 7);
    int whiteKing = coordToPosition(4, 7);
    int[] positions = new int[] {whiteRook1, whiteRook2, blackRook1, blackRook2, whiteKing, blackKing};
    int[] expectedPieces = new int[] {Board.WROOKINDEx, Board.WROOKINDEx, Board.BROOKINDEx, Board.BROOKINDEx, Board.WKINGINDEx, Board.BKINGINDEx};
    castlePositionsAndExpectedPieces = new int[2][];
    castlePositionsAndExpectedPieces[0] = positions;
    castlePositionsAndExpectedPieces[1] = expectedPieces;
  }

  private void setUpBoard() {
    boolean white = true;
    setUpColor(white);
    setUpColor(!white);
  }

  private void setUpColor(boolean white) {
    int shift = 0;
    int yBackRow = 0;
    int yPawnRow = 1;
    if(white) {
      shift = 6;
      yBackRow = 7;
      yPawnRow = 6;
    }
    setUpValuePieces(yBackRow, shift);
    setUpPawns(yPawnRow, shift);
  }

  private void setUpValuePieces(int yBackRow, int shift) {
    setPosition(Board.BROOKINDEx+shift, 0, yBackRow);
    setPosition(Board.BROOKINDEx+shift, 7, yBackRow);
    setPosition(Board.BKNIGHTINDEx+shift, 1, yBackRow);
    setPosition(Board.BKNIGHTINDEx+shift, 6, yBackRow);
    setPosition(Board.BBISHOPINDEx+shift, 2, yBackRow);
    setPosition(Board.BBISHOPINDEx+shift, 5, yBackRow);
    setPosition(Board.BQUEENINDEx+shift, 3, yBackRow);
    setPosition(Board.BKINGINDEx+shift, 4, yBackRow);
  }

  private void setUpPawns(int yPawnRow, int shift) {
    for(int i = 0; i < 8; i++) {
      setPosition(Board.BPAWNINDEx+shift, i, yPawnRow);
    }
  }

  public void setPosition(int pieceIndex, int index) {
    moveCount++;
    boardRepresentation[index] = pieceIndex;
  }

  public void setPosition(int pieceIndex, int x, int y) {
    int index = coordToPosition(x, y);
    setPosition(pieceIndex, index);
  }

  private void setCanCastleTrue() {
    for(int color = 0; color < 2; color++) {
      for(int side = 0; side < 2; side++) {
        canCastleWhiteBlackLeftRight[color][side] = true;
      }
    }
  }

  private void resetPawnFirstMoved() {
    for(int color = 0; color < 2; color++) {
      for(int pawn = 0; pawn < numPawns; pawn++) {
        whenPawnFirstMoved[color][pawn] = 0;
      }
    }
  }

  public void updateEnpassantInfo(int[] enpassantInfo) {
    int color = enpassantInfo[0];
    int position = enpassantInfo[1];
    whenPawnFirstMoved[color][position] = moveCount;
  }

  public void updateCastling() {
    updateRooksCastling();
    updateKingsCastling();
  }

  private void updateRooksCastling() {
    final int rooksStartAtIndex = 0;
    final int numRooks = 4;
    for(int rook = rooksStartAtIndex; rook < numRooks + rooksStartAtIndex; rook++) {
      int position = castlePositionsAndExpectedPieces[0][rook];
      int expectedPiece = castlePositionsAndExpectedPieces[1][rook];
      int color = (rook - rooksStartAtIndex) / 2;
      int side = (rook - rooksStartAtIndex) % 2;
      boolean canCastleNow = getPieceAt(position) == expectedPiece;
      boolean couldCastleBefore = canCastleWhiteBlackLeftRight[color][side];
      canCastleWhiteBlackLeftRight[color][side] = canCastleNow && couldCastleBefore;
    }
  }

  private void updateKingsCastling() {
    final int kingsStartAtIndex = 4;
    final int numKings = 2;
    final int left = 0;
    final int right = 1;
    for(int king = kingsStartAtIndex; king < numKings + kingsStartAtIndex; king++) {
      int position = castlePositionsAndExpectedPieces[0][king];
      int expectedPiece = castlePositionsAndExpectedPieces[1][king];
      int color = (king - kingsStartAtIndex) / 1;
      boolean canCastleNow = getPieceAt(position) == expectedPiece;
      boolean couldCastleBeforeLeft = canCastleWhiteBlackLeftRight[color][left];
      canCastleWhiteBlackLeftRight[color][left] = canCastleNow && couldCastleBeforeLeft;
      boolean couldCastleBeforeRight = canCastleWhiteBlackLeftRight[color][right];
      canCastleWhiteBlackLeftRight[color][right] = canCastleNow && couldCastleBeforeRight;
    }
  }

  public int getPieceAt(int index) {
    return boardRepresentation[index];
  }

  public int getPieceAt(int x, int y) {
    int index = coordToPosition(x, y);
    return getPieceAt(index);
  }

  private int coordToPosition(int x, int y) {
    return y*width + x;
  }

  public int[] boardArray() {
    return boardRepresentation;
  }
}
