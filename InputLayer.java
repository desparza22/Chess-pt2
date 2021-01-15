/*
64 nodes per piece type, with binary activation value indicating that the piece
is at that location.

Should there be nodes to indicate empty spaces as well? this would technically
be redundant information, but it might help the network visualize the board.
*/

import java.lang.StringBuilder;

public class InputLayer {
  private InputArray inputArray;
  private final int width = 8;
  private final int bitsPerNumber = width*width;

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
  public static final int extraInfoIndex = WKINGINDEx + 1;
  public static final int extraInfoEnpassant = 16;
  public static final int extraInfoCastleOption = 4;
  public static final int extraInfoWhoseMove = 2;
  public static final int extraInfoBits = extraInfoEnpassant + extraInfoCastleOption + extraInfoWhoseMove;
  public static final int enpassantInfoStart = 0;
  public static final int castleInfoStart = enpassantInfoStart + extraInfoEnpassant; //white then black
  public static final int whoseMoveInfoStart = castleInfoStart + extraInfoCastleOption;

  public InputLayer(int inputNodesNeeded) {
    if(extraInfoBits <= bitsPerNumber) {
      inputNodesNeeded += extraInfoBits;
    } else {
      System.out.println("Structure of input layer not prepared to handle extra bits of information such as which pawn has just pushed, castling, and whose turn it is. Program terminating...");
      System.exit(0);
    }
    initializeInputLayer(inputNodesNeeded);
  }

  private void initializeInputLayer(int inputNodesNeeded) {
    int numLongsNeeded = divideAndRoundUp(inputNodesNeeded, bitsPerNumber);
    inputArray = new InputArray(numLongsNeeded);
    markAllSpacesEmpty();

    boolean white = true;
    setUpColor(!white);
    setUpColor(white);
    setCastlingAvailable();
    setWhitesTurn();
  }

  private void markAllSpacesEmpty() {
    long allOnes = -1;
    inputArray.setLongAtIndex(EMPTYINDEx, allOnes);
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

    removeSpacesRectangle(0, 8, 0+shift, 2+shift);

    placeOrRemovePieceAtLocation(BROOKINDEx+shift, 0, yBackRow);
    placeOrRemovePieceAtLocation(BROOKINDEx+shift, 7, yBackRow);
    placeOrRemovePieceAtLocation(BKNIGHTINDEx+shift, 1, yBackRow);
    placeOrRemovePieceAtLocation(BKNIGHTINDEx+shift, 6, yBackRow);
    placeOrRemovePieceAtLocation(BBISHOPINDEx+shift, 2, yBackRow);
    placeOrRemovePieceAtLocation(BBISHOPINDEx+shift, 5, yBackRow);
    placeOrRemovePieceAtLocation(BQUEENINDEx+shift, 3, yBackRow);
    placeOrRemovePieceAtLocation(BKINGINDEx+shift, 4, yBackRow);

    for(int i = 0; i < 8; i++) {
      placeOrRemovePieceAtLocation(BPAWNINDEx+shift, i, yPawnRow);
    }
  }

  private void setCastlingAvailable() {
    int indexOfLongToOperateOn = extraInfoIndex;
    int startBitToActivateInclusive = castleInfoStart;
    int endBitToActivateExclusive = castleInfoStart + extraInfoCastleOption;
    slowlyActivateBitsBetween(indexOfLongToOperateOn, startBitToActivateInclusive, endBitToActivateExclusive);
  }

  private void slowlyActivateBitsBetween(int indexOfLongToOperateOn, int startInclusive, int endExclusive) {
    for(int indexOfBitToActivate = startInclusive; indexOfBitToActivate < endExclusive; indexOfBitToActivate++) {
      inputArray.activateBit(indexOfLongToOperateOn, indexOfBitToActivate);
    }
  }

  private void removeSpacesRectangle(int xStart, int xEnd, int yStart, int yEnd) {
    for(int i = yStart; i < yEnd; i++) {
      for(int j = xStart; j < xEnd; j++) {
        placeOrRemovePieceAtLocation(EMPTYINDEx, j, i);
      }
    }
  }

  private int divideAndRoundUp(int numerator, int divisor) {
    return (numerator - 1) / divisor + 1;
  }

  public void movePiece(int activePieceIndex, int replacingPieceIndex, int previousLocation, int newPosition) {
    //move active piece
    placeOrRemovePieceAtLocation(activePieceIndex, previousLocation);
    placeOrRemovePieceAtLocation(activePieceIndex, newPosition);
    //replace captured piece
    placeOrRemovePieceAtLocation(replacingPieceIndex, newPosition);
    //mark old square empty
    placeOrRemovePieceAtLocation(EMPTYINDEx, previousLocation);
  }

  public void promote(int promotedPieceIndex, int position, int correctPawnIndex) {
    placeOrRemovePieceAtLocation(promotedPieceIndex, position);
    placeOrRemovePieceAtLocation(correctPawnIndex, position);
  }

  public void updateEnpassantInfo(int[] enpassantInfo) {
    final int color0IfWhite1IfBlack = enpassantInfo[0];
    final int column = enpassantInfo[1];
    final int blackShift = 8;
    final int shift = color0IfWhite1IfBlack * blackShift + enpassantInfoStart;

    inputArray.activateBit(extraInfoIndex, shift + column);
  }

  public void clearEnpassant() {
    inputArray.resetEnpassantBits();
  }

  public void updateCastling(boolean[][] canCastleWhiteBlackLeftRight) {
    final int numColors = 2;
    final int numSides = 2;
    for(int color = 0; color < numColors; color++) {
      for(int side = 0; side < numSides; side++) {
        boolean canCastleColorAndSide = canCastleWhiteBlackLeftRight[color][side];
        if(!canCastleColorAndSide) {
          int infoIndex = castleInfoIndex(color, side);
          inputArray.deactivateBit(extraInfoIndex, infoIndex);
        }
      }
    }
  }

  private int castleInfoIndex(int color, int side) {
    return color * 2 + side;
  }

  public void switchWhoseTurn() {
    final int whiteToPlayIndex = whoseMoveInfoStart;
    final int blackToPlayIndex = whiteToPlayIndex + 1;
    inputArray.flipBit(extraInfoIndex, whiteToPlayIndex);
    inputArray.flipBit(extraInfoIndex, blackToPlayIndex);
  }

  private void setWhitesTurn() {
    final int whiteToPlayIndex = whoseMoveInfoStart;
    inputArray.activateBit(extraInfoIndex, whiteToPlayIndex);
  }

  private void placeOrRemovePieceAtLocation(int pieceIndex, int x, int y) {
    int index = coordToIndex(x, y);
    placeOrRemovePieceAtLocation(pieceIndex, index);
  }

  private void placeOrRemovePieceAtLocation(int pieceIndex, int position) {
    inputArray.flipBit(pieceIndex, position);
  }

  public long[] currentState() {
    final int inputArraySize = inputArray.size();
    long[] inputArrayCopy = new long[inputArraySize];
    for(int i = 0; i < inputArraySize; i++) {
      inputArrayCopy[i] = inputArray.getLongAtIndexPreRotation(i);
    }
    return inputArrayCopy;
  }

  public long currentID() {
    return inputArray.currentID();
  }

  public boolean checkValidity() {
    return checkValidityOfPieceLocations() && checkValidityOfExtraInfo();
  }

  private String[] pieceNames = new String[] {
    "Empty", "BPawn", "BRook", "BKnight", "BBishop", "BQueen", "BKing",
    "BPawn", "WRook", "WKnight", "WBishop", "WQueen", "WKing"};
  private boolean checkValidityOfPieceLocations() {
    long xorAllPieceLocations = 0;
    int indexOfLastLongBeforeExtraInfo = inputArray.size()-2;
    for(int i = 0; i <= indexOfLastLongBeforeExtraInfo; i++) {
      long longContainingInfoAboutPieceLocations = inputArray.getLongAtIndexPreRotation(i);
      xorAllPieceLocations = xorAllPieceLocations ^ longContainingInfoAboutPieceLocations;
    }
    return xorAllPieceLocations == -1;
  }

  private void printPositionLong(int pieceIndex, long position) {
    System.out.println(pieceNames[pieceIndex] + " " + longToBinaryString(position));
  }

  public static String longToBinaryString(long number) {
    StringBuilder binaryString = new StringBuilder(64);
    long mask = 1L<<63;
    for(int i = 0; i < 64; i++) {
      if((mask & number) != 0) {
        binaryString.append('1');
      } else {
        binaryString.append('0');
      }
      mask = mask >>> 1;
    }
    return binaryString.toString();
  }

  private boolean checkValidityOfExtraInfo() {
    long extraInfo = inputArray.getLongAtIndexPreRotation(extraInfoIndex);
    return enpassantHasFewerThanTwoBitsSet(extraInfo) && whoseMoveHasOneBitSet(extraInfo);
  }

  private boolean enpassantHasFewerThanTwoBitsSet(long extraInfo) {
    int bitsSet = bitsSetBetween(extraInfo, enpassantInfoStart, enpassantInfoStart + extraInfoEnpassant);
    boolean enpassantHasFewerThanTwoBitsSet = bitsSet < 2;
    if(!enpassantHasFewerThanTwoBitsSet) {
      System.out.println("Input array failed enpassantHasFewerThanTwoBitsSet with long " + longToBinaryString(extraInfo));
    }
    return enpassantHasFewerThanTwoBitsSet;
  }

  private boolean whoseMoveHasOneBitSet(long extraInfo) {
    int bitsSet = bitsSetBetween(extraInfo, whoseMoveInfoStart, whoseMoveInfoStart + extraInfoWhoseMove);
    boolean whoseMoveHasOneBitSet = bitsSet == 1;
    if(!whoseMoveHasOneBitSet) {
      System.out.println("Input array failed whoseMoveHasOneBitSet with long " + longToBinaryString(extraInfo));
    }
    return whoseMoveHasOneBitSet;
  }

  private int bitsSetBetween(long number, int startInclusive, int endExclusive) {
    int bitsSet = 0;
    for(int i = startInclusive; i < endExclusive; i++) {
      bitsSet += returnOneIfBitSet(number, i);
    }
    return bitsSet;
  }

  private int returnOneIfBitSet(long number, int index) {
    long mask = 1L<<index;
    long bitAtIndex = number & mask;
    return (int)(bitAtIndex>>>index);
  }

  private int coordToIndex(int x, int y) {
    return y*width + x;
  }

  public static void main(String[] args) {
    Board b = new Board();
    long[] inputArray = b.inputLayer();

    // long toOperateOn = 0;
    // toOperateOn += 1L<<50;
    // toOperateOn += 1L<<10;
    // toOperateOn += 1L<<8;
    // toOperateOn += 1L<<6;
    // System.out.println("original string: " + longToBinaryString(toOperateOn));
    //
    // RotateBits rotateBits = new RotateBits();
    // int[] intParameters = new int[1];
    // long[] longParameters = new long[1];
    // int intIgnore = 0;
    // long longIgnore = 0;
    //
    // int rotateDistance = 42;
    // intParameters[0] = rotateDistance;
    // longParameters[0] = toOperateOn;
    // long result = rotateBits.operateOnLong(longIgnore, intIgnore, intParameters, longParameters);
    //
    // System.out.println("rotated string: " + longToBinaryString(result));
  }
}


class InputArray {
  private long[] inputArray;
  private long id = 0;
  private int[] idRotations;
  private final int bitsInALong = 64;
  private final int bitsPerNumber = bitsInALong;

  private FlipBit flipBit = new FlipBit();
  private ActivateBit activateBit = new ActivateBit();
  private DeactivateBit deactivateBit = new DeactivateBit();
  private ResetEnpassantBits resetEnpassantBits = new ResetEnpassantBits();
  private RotateBits rotateBits = new RotateBits();

  private final int maxParameters = 1;
  private int[] intParameters = new int[maxParameters];
  private long[] longParameters = new long[maxParameters];

  private long placeholderLongDoesNothing = 0;
  private int placeholderIntDoesNothing = 0;

  private int indexOfExtraInformation;

  public InputArray(int numLongsNeeded) {
    this.inputArray = new long[numLongsNeeded];
    indexOfExtraInformation = lastIndex(inputArray);
    initializeIDRotations();
    completeResetEnpassantBitsInitialization();
  }

  private int lastIndex(long[] array) {
    return array.length-1;
  }

  public int size() {
    return inputArray.length;
  }

  public long getLongAtIndexPostRotation(int index) {
    long rotatedLong = inputArray[index];
    return rotatedLong;
  }

  public long getLongAtIndexPreRotation(int index) {
    long rotatedLong = inputArray[index];
    int rotationDistance = idRotations[index];
    int distanceToUndoRotation = bitsInALong - rotationDistance;
    intParameters[0] = distanceToUndoRotation;
    longParameters[0] = rotatedLong;
    long unrotatedLong = rotateBits.operateOnLong(placeholderLongDoesNothing, placeholderIntDoesNothing, intParameters, longParameters);
    return unrotatedLong;
  }

  private void completeResetEnpassantBitsInitialization() {
    resetEnpassantBits.completeInitialization(idRotations);
  }

  private void initializeIDRotations() {
    int numberOfBoardIdentifyingNumbers = inputArray.length;
    int rotationDistanceToKeepNumbersSpreadOut = bitsPerNumber / (numberOfBoardIdentifyingNumbers + 1);
    idRotations = new int[numberOfBoardIdentifyingNumbers];
    fillArrayWithMultiplesOfNStartingAtK(idRotations, rotationDistanceToKeepNumbersSpreadOut, 0);
  }

  private void fillArrayWithMultiplesOfNStartingAtK(int[] array, int n, int k) {
    for(int i = 0; i < array.length; i++) {
      array[i] = k + (n * i);
    }
  }

  long currentID() {
    return id;
  }

  public void flipBit(int indexOfLongToOperateOn, int indexOfBit) {
    intParameters[0] = indexOfBit;
    operateAndUpdateSerial(indexOfLongToOperateOn, flipBit, intParameters, longParameters);
  }

  public void activateBit(int indexOfLongToOperateOn, int indexOfBit) {
    intParameters[0] = indexOfBit;
    operateAndUpdateSerial(indexOfLongToOperateOn, activateBit, intParameters, longParameters);
  }

  public void deactivateBit(int indexOfLongToOperateOn, int indexOfBit) {
    intParameters[0] = indexOfBit;
    operateAndUpdateSerial(indexOfLongToOperateOn, deactivateBit, intParameters, longParameters);
  }

  public void resetEnpassantBits() {
    operateAndUpdateSerial(indexOfExtraInformation, resetEnpassantBits, intParameters, longParameters);
  }

  public void setLongAtIndex(int indexOfLongToOperateOn, long numberToSetAtIndex) {
    int rotationDistance = idRotations[indexOfLongToOperateOn];
    intParameters[0] = rotationDistance;
    longParameters[0] = numberToSetAtIndex;
    operateAndUpdateSerial(indexOfLongToOperateOn, rotateBits, intParameters, longParameters);
  }

  private void operateAndUpdateSerial(int indexOfLongToOperateOn, LongFunction functionToPerformOnLong, int[] intParameters, long[] longParameters) {
    long longToOperateOn = inputArray[indexOfLongToOperateOn];
    addRemoveLongToID(longToOperateOn);
    int rotationForward = idRotations[indexOfLongToOperateOn];
    longToOperateOn = functionToPerformOnLong.operateOnLong(longToOperateOn, rotationForward, intParameters, longParameters);
    addRemoveLongToID(longToOperateOn);
    inputArray[indexOfLongToOperateOn] = longToOperateOn;
  }

  private void addRemoveLongToID(long longToRemove) {
    id ^= longToRemove;
  }
}

abstract class LongFunction {
  protected final int bitsInALong = 64;
  abstract long operateOnLong(long number, int rotationForward, int[] extraIntParameters, long[] extraLongParameters);
}

class FlipBit extends LongFunction {
  FlipBit() {

  }

  long operateOnLong(long number, int rotationForward, int[] extraIntParameters, long[] extraLongParameters) {
    int bitToBeFlipped = extraIntParameters[0];
    bitToBeFlipped = (bitToBeFlipped + rotationForward) % 64;
    return flipBit(number, bitToBeFlipped);
  }

  private long flipBit(long number, int bitIndex) {
    long mask = 1L<<bitIndex;
    long numberWithBitFlipped = number ^ mask;
    return number ^ mask;
  }
}

class ActivateBit extends LongFunction {
  ActivateBit() {

  }

  long operateOnLong(long number, int rotationForward, int[] extraIntParameters, long[] extraLongParameters) {
    int bitToBeActivated = extraIntParameters[0];
    bitToBeActivated = (bitToBeActivated + rotationForward) % bitsInALong;
    return activateBit(number, bitToBeActivated);
  }

  private long activateBit(long number, int bitIndex) {
    long bitToSetToOne = 1L<<bitIndex;
    long numberWithBitSetToOne = number | bitToSetToOne;
    return numberWithBitSetToOne;
  }
}

class DeactivateBit extends LongFunction {
  DeactivateBit() {

  }

  long operateOnLong(long number, int rotationForward, int[] extraIntParameters, long[] extraLongParameters) {
    int bitToBeDeactivated = extraIntParameters[0];
    bitToBeDeactivated = (bitToBeDeactivated + rotationForward) % bitsInALong;
    return deactivateBit(number, bitToBeDeactivated);
  }

  private long deactivateBit(long number, int bitIndex) {
    long allBitsSetToOne = -1;
    long bitToSetToZero = 1L<<bitIndex;
    long bitToSetToZeroComplement = allBitsSetToOne ^ bitToSetToZero;
    long numberWithBitSetToZero = number & bitToSetToZeroComplement;
    return numberWithBitSetToZero;
  }
}

class ResetEnpassantBits extends LongFunction {
  private long resetEnpassantMask;
  private LongFunction deactivateBit = new DeactivateBit();

  ResetEnpassantBits() {
  }

  public void completeInitialization(int[] rotations) {
    int extraInfoRotations = rotations[InputLayer.extraInfoIndex];
    resetEnpassantMask = initializeResetEnpassantMask(extraInfoRotations, InputLayer.enpassantInfoStart, InputLayer.extraInfoEnpassant);
  }

  public long operateOnLong(long number, int rotationForward, int[] extraIntParameters, long[] extraLongParameters) {
    return number & resetEnpassantMask;
  }

  private long initializeResetEnpassantMask(int extraInfoRotations, int enpassantInfoStartsAt, int numberOfEnpassantInfoBits) {
    long allOnes = -1;
    int enpassantInfoEndsBy = enpassantInfoStartsAt + numberOfEnpassantInfoBits;
    long enpassantMask = deactivateBetween(allOnes, enpassantInfoStartsAt + extraInfoRotations, enpassantInfoEndsBy + extraInfoRotations);
    return enpassantMask;
  }

  private long deactivateBetween(long number, int start, int end) {
    final int rotationForward = 0;
    int[] intParameters = new int[1];
    long[] nullLongParameters = null;
    for(int i = start; i < end; i++) {
      int bitToDeactivate = i%bitsInALong;
      intParameters[0] = bitToDeactivate;
      number = deactivateBit.operateOnLong(number, rotationForward, intParameters, nullLongParameters);
    }
    return number;
  }
}

class RotateBits extends LongFunction {
  RotateBits() {

  }

  long operateOnLong(long longIgnore, int intIgnore, int[] intParameters, long[] longParameters) {
    long number = longParameters[0];
    int distanceToRotate = intParameters[0];
    return rotateLongForward(number, distanceToRotate);
  }

  private long rotateLongForward(long number, int distanceToRotate) {
    distanceToRotate %= bitsInALong;
    long rightmostRotateBits = rightmostBits(number, bitsInALong - distanceToRotate);
    long leftmostRotateBits = leftmostBits(number, distanceToRotate);
    return (rightmostRotateBits << distanceToRotate) + leftmostRotateBits;
  }

  // private long rotateLongForward(long number, int distanceToRotate) {
  //   long rightmostRotateBits = rightmostBits(number, bitsInALong - distanceToRotate);
  //   long leftmostRotateBits = leftmostBits(number, distanceToRotate);
  //   return (leftmostRotateBits << (bitsInALong - distanceToRotate)) + rightmostRotateBits;
  // }

  private long rightmostBits(long number, int numBits) {
    if(numBits <= 0) {
      return 0;
    }
    long allOnes = -1;
    long allOnesIncluded = allOnes >>> (bitsInALong - numBits);
    return number & allOnesIncluded;
  }

  private long leftmostBits(long number, int numBits) {
    if(numBits <= 0) {
      return 0;
    }
    long allOnes = -1;
    long allOnesIncluded = allOnes << (bitsInALong - numBits);
    long leftmostBitsPreShift = number & allOnesIncluded;
    return leftmostBitsPreShift >>> (bitsInALong - numBits);
  }
}
