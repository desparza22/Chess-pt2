import java.util.HashMap;
import java.util.LinkedList;

public class BoardPositionTracker {
  private final int maxExpectedMoves = 300;
  private HashMap<Long, LinkedList<PositionAndCount>> seenPosition = new HashMap<Long, LinkedList<PositionAndCount>>(maxExpectedMoves);

  public BoardPositionTracker() {
  }

  public int addPosition(long positionID, long[] position) {
    LinkedList<PositionAndCount> positionsAtKey = seenPosition.getOrDefault(positionID, newList());
    seenPosition.put(positionID, positionsAtKey);
    return checkForPositionOrAdd(positionsAtKey, position);
  }

  private int checkForPositionOrAdd(LinkedList<PositionAndCount> positionsAtKey, long[] position) {
    int numPositionAtKey = positionsAtKey.size();
    PositionAndCount checkedPosition = null;
    for(int i = 0; i < numPositionAtKey; i++) {
      PositionAndCount positionToCompare = positionsAtKey.removeFirst();
      if(positionToCompare.isSameAsOtherPosition(position)) {
        positionsAtKey.add(positionToCompare);
        checkedPosition = positionToCompare;
        break;
      } else {
        positionsAtKey.add(positionToCompare);
      }
    }
    if(checkedPosition == null) {
      checkedPosition = new PositionAndCount(position);
      positionsAtKey.add(checkedPosition);
    }
    return checkedPosition.incrementCount();
  }

  public void clearPositions() {
    seenPosition.clear();
  }

  private LinkedList<PositionAndCount> newList() {
    return new LinkedList<PositionAndCount>();
  }
}

class PositionAndCount {
  private int count = 0;
  private long[] position;

  PositionAndCount(long[] position) {
    this.position = position;
  }

  int incrementCount() { //returns value before incrementing
    return this.count++;
  }

  int getCount() {
    return count;
  }

  long[] getPosition() {
    return position;
  }

  boolean isSameAsOtherPosition(long[] other) {
    boolean isSame = true;
    for(int i = 0; i < position.length && isSame; i++) {
      isSame = position[i] == other[i];
    }
    return isSame;
  }
}
