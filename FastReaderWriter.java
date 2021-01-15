import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class FastReaderWriter {
  //
  // File format:
  // number of bytes
  // byte array
  //
  private int bytesWritten = 0;
  private LinkedList<byte[]> listOfBytes = new LinkedList<byte[]>();

  private byte[] readArray = null;
  private int readIndex = 0;

  private BufferedOutputStream bos = null;
  private BufferedInputStream bis = null;

  private byte[] integerContainer = new byte[4];
  private byte[] doubleContainer = new byte[8];
  private byte[] longContainer = new byte[8];


  public FastReaderWriter() {

  }

  public boolean hasNext() {
    return readArray != null && readIndex < readArray.length;
  }

  public void setWritingFile(String fileName) {
    initOutput(fileName);
  }

  public void setReadingFile(String fileName) {
    initReadingInformation(fileName);
  }

  private void initReadingInformation(String fileName) {
    initInput(fileName);
    int numBytes = readStartingInteger();
    createAndFillReadArray(numBytes);
  }

  public void writeLong(long value) {
    addBytesToList(longToBytes(value));
  }

  private byte[] longToBytes(long value) {
    byte[] holdLong = new byte[8];
    ByteBuffer wrapper = ByteBuffer.wrap(holdLong);
    wrapper.putLong(value);
    return holdLong;
  }

  public long readLong() {
    fillContainer(longContainer);
    return bytesToLong(longContainer);
  }

  public void writeDouble(double value) {
    addBytesToList(doubleToBytes(value));
  }

  private byte[] doubleToBytes(double value) {
    byte[] holdDouble = new byte[8];
    ByteBuffer wrapper = ByteBuffer.wrap(holdDouble);
    wrapper.putDouble(value);
    return holdDouble;
  }

  public double readDouble() {
    fillContainer(doubleContainer);
    return bytesToDouble(doubleContainer);
  }

  public void writeInteger(int value) {
    addBytesToList(integerToBytes(value));
  }

  private byte[] integerToBytes(int value) {
    byte[] holdInteger = new byte[4];
    ByteBuffer wrapper = ByteBuffer.wrap(holdInteger);
    wrapper.putInt(value);
    return holdInteger;
  }

  public int readInteger() {
    fillContainer(integerContainer);
    return bytesToInt(integerContainer);
  }

  private void addBytesToList(byte[] holdsValue) {
    bytesWritten += holdsValue.length;
    listOfBytes.add(holdsValue);
  }

  public void flushAndClose() {
    flush();
    closeOutput();
    resetOutputInformation();
  }

  private void initOutput(String fileName) {
    try {
      bos = new BufferedOutputStream(new FileOutputStream(fileName));
    } catch(IOException ioe) {
      ioe.printStackTrace();
    }
  }

  private void initInput(String fileName) {
    try {
      bis = new BufferedInputStream(new FileInputStream(fileName));
    } catch(IOException ioe) {
      ioe.printStackTrace();
    }
  }

  private void flush() {
    int bytesToIndicateNumberOfBytes = 4;
    int bytesToWrite = bytesToIndicateNumberOfBytes + bytesWritten;
    byte[] holdAllBytes = new byte[bytesToWrite];
    int copyIndex = writeStartingInteger(holdAllBytes, bytesWritten);
    moveBytes(holdAllBytes, copyIndex);
    writeBytes(holdAllBytes);
  }

  private int writeStartingInteger(byte[] holdAllBytes, int integerToWrite) {
    byte[] bytesOfInteger = integerToBytes(integerToWrite);
    int copyIndex = 0;
    copyIndex = moveBytes(holdAllBytes, bytesOfInteger, copyIndex);
    return copyIndex;
  }

  private int readStartingInteger() {
    int offset = 0;
    int length = 4;
    read(integerContainer, offset, length);
    return bytesToInt(integerContainer);
  }

  private void createAndFillReadArray(int readArrayLength) {
    readArray = new byte[readArrayLength];
    int offset = 0;
    int length = readArrayLength;
    read(readArray, offset, length);
  }

  private void moveBytes(byte[] holdAllBytes, int copyIndex) {
    while(listOfBytes.size() != 0) {
      byte[] byteArray = listOfBytes.removeFirst();
      copyIndex = moveBytes(holdAllBytes, byteArray, copyIndex);
    }
  }

  private int moveBytes(byte[] holdAllBytes, byte[] byteArray, int copyIndex) {
    for(int i = 0; i < byteArray.length; i++) {
      holdAllBytes[copyIndex++] = byteArray[i];
    }
    return copyIndex;
  }

  private void writeBytes(byte[] bytesToWrite) {
    int offset = 0;
    int length = bytesToWrite.length;
    write(bytesToWrite, offset, length);
  }

  private void write(byte[] bytesToWrite, int offset, int length) {
    try {
      bos.write(bytesToWrite, offset, length);
    } catch(IOException ioe) {
      ioe.printStackTrace();
    }
  }

  private void read(byte[] arrayToReadInto, int offset, int length) {
    try {
      bis.read(arrayToReadInto, offset, length);
    } catch(IOException ioe) {
      ioe.printStackTrace();
    }
  }

  private void closeOutput() {
    try {
      bos.close();
    } catch(IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public void closeInput() {
    try {
      bis.close();
    } catch(IOException ioe) {
      ioe.printStackTrace();
    }
    readArray = null;
    readIndex = 0;
  }

  private void resetOutputInformation() {
    bos = null;
    bytesWritten = 0;
    listOfBytes = new LinkedList<byte[]>();
  }

  private void fillContainer(byte[] container) {
    for(int i = 0; i < container.length; i++) {
      container[i] = readArray[readIndex++];
    }
  }

  private int bytesToInt(byte[] integerBytes) {
    ByteBuffer wrapper = ByteBuffer.wrap(integerBytes);
    return wrapper.getInt();
  }

  private double bytesToDouble(byte[] doubleBytes) {
    ByteBuffer wrapper = ByteBuffer.wrap(doubleBytes);
    return wrapper.getDouble();
  }

  private long bytesToLong(byte[] longBytes) {
    ByteBuffer wrapper = ByteBuffer.wrap(longBytes);
    return wrapper.getLong();
  }

  public static void main(String[] args) {
    String testFile = "testFile.txt";
    FastReaderWriter fastReaderWriter = new FastReaderWriter();
    fastReaderWriter.setWritingFile(testFile);
    fastReaderWriter.writeInteger(5);
    fastReaderWriter.writeDouble(5.5);
    fastReaderWriter.writeLong(3402L);
    fastReaderWriter.writeDouble(2.999);
    fastReaderWriter.writeInteger(700);

    fastReaderWriter.flushAndClose();

    fastReaderWriter.setReadingFile(testFile);
    System.out.println(fastReaderWriter.readInteger());
    System.out.println(fastReaderWriter.readDouble());
    System.out.println(fastReaderWriter.readLong());
    System.out.println(fastReaderWriter.readDouble());
    System.out.println(fastReaderWriter.readInteger());

    fastReaderWriter.closeInput();
  }
}
