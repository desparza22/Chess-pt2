import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.io.IOException;

import java.lang.InterruptedException;


public class ChessGUI {

  private final JPanel gui = new JPanel(new BorderLayout(3, 3)); // horizontal gap, vertical gap between components
  private JButton[][] chessBoardSquares = new JButton[8][8];
  private JPanel chessBoard;
  private JLabel message;
  private JFrame frame;
  private static final String cols = "ABCDEFGH";
  // private ImageIcon[][] chessPieces = new ImageIcon[8][8];
  // private char WHITE = 'W';
  // private char BLACK = 'B';

  public ChessGUI(String name) {
    message = new JLabel(name + " is ready");
    frame = new JFrame(name);
    initializeGUI();
  }

  private void initializeGUI() {
    //sets up the main GUI
    this.gui.setBorder(new EmptyBorder(5, 5, 5, 5));
    JToolBar tools = new JToolBar();
    tools.setFloatable(false);
    this.gui.add(tools, BorderLayout.PAGE_START); // PAGE_START is a predefined position corresponding to NORTH
    tools.add(new JButton("New Game"));
    tools.add(new JButton("Resign"));
    tools.addSeparator();
    tools.add(this.message);

    this.gui.add(new JLabel("Add stuff later"), BorderLayout.LINE_START);

    this.chessBoard = new JPanel(new GridLayout(0, 9)); // 0 rows and 9 columns. 0 indicates any number of objects can be added to that dimension
    this.chessBoard.setBorder(new LineBorder(Color.BLACK));
    this.gui.add(chessBoard);

    //create board squares
    Insets buttonMargin = new Insets(0, 0, 0, 0); // border layout or something?
    // Color black = new Color(0, 0, 0);
    // Color white = new Color(255, 255, 255);
    for(int i = 0; i < chessBoardSquares.length; i++) {
      for(int j = 0; j < chessBoardSquares[i].length; j++) {
        JButton b = new JButton();
        b.setMargin(buttonMargin);
        // ImageIcon icon = this.chessPieces[j][i];
        // BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
                                             // BufferedImage.TYPE_INT_ARGB);
        // Graphics2D g = bi.createGraphics();

        //determine color of graphic and jbutton background
        boolean whiteSquare = (j + i) % 2 == 0;
        if(whiteSquare) {
          b.setBackground(Color.WHITE);
          // g.setColor(Color.WHITE);
        } else {
          b.setBackground(Color.BLACK);
          // g.setColor(Color.BLACK);
        }
        // g.fillRect(0, 0, bi.getWidth(), bi.getHeight());

        // icon.paintIcon(null, g, 0, 0);
        // g.dispose();
        b.setOpaque(true);
        // b.setIcon(icon);

        chessBoardSquares[j][i] = b;
      }
    }

    //fill the board
    chessBoard.add(new JLabel(""));

    for(int i = 0; i < 8; i++) {
      chessBoard.add(
        new JLabel(cols.substring(i, i + 1),
        SwingConstants.CENTER));
    }

    for(int i = 0; i < 8; i++) {
      for(int j = 0; j < 8; j++) {
        switch(j) {
          case 0:
            chessBoard.add(new JLabel("" + (i + 1),
              SwingConstants.CENTER));
          default:
            chessBoard.add(chessBoardSquares[j][i]);
        }
      }
    }
  }

  private final JComponent getChessBoard() {
    return this.chessBoard;
  }

  private final JComponent getGUI() {
    return this.gui;
  }

  public void displayBoard(long[] inputLayer) {
    final int lastPiecesIndex = inputLayer.length-2;
    for(int i = 0; i <= lastPiecesIndex; i++) {
      displayPieceFromLong(i, inputLayer[i]);
    }
  }

  private void displayPieceFromLong(int pieceIndex, long positionBitMap) {
    long mask = 1L<<63;
    for(int i = 0; i < 64; i++) {
      if((positionBitMap & mask) != 0) {
        int x = i % 8;
        int y = i / 8;
        setPieceAt(x, y, pieceIndex);
      }
      mask = mask >>> 1;
    }
  }

  public void displayBoard(int[] board) {
    for(int i = 0; i < board.length; i++) {
      int x = i % 8;
      int y = i / 8;
      setPieceAt(x, y, board[i]);
    }
  }

  public void displayBoard(int[][] board) {
    for(int x = 0; x < board.length; x++) {
      for(int y = 0; y < board[0].length; y++) {
        setPieceAt(x, y, board[x][y]);
      }
    }
  }

  private void setPieceAt(int x, int y, int pieceImageIndex) {
    JButton buttonToSetImageFor = chessBoardSquares[x][y];
    ImageIcon iconAtSquare = chooseImage(pieceImageIndex);
    buttonToSetImageFor.setIcon(iconAtSquare);
  }

  private ImageIcon[] pieceImages = new ImageIcon[] {
    new ImageIcon(new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB)),
    new ImageIcon("ChessPiece/Chess_pdt60.png"),
    new ImageIcon("ChessPiece/Chess_rdt60.png"),
    new ImageIcon("ChessPiece/Chess_ndt60.png"),
    new ImageIcon("ChessPiece/Chess_bdt60.png"),
    new ImageIcon("ChessPiece/Chess_qdt60.png"),
    new ImageIcon("ChessPiece/Chess_kdt60.png"),
    new ImageIcon("ChessPiece/Chess_plt60.png"),
    new ImageIcon("ChessPiece/Chess_rlt60.png"),
    new ImageIcon("ChessPiece/Chess_nlt60.png"),
    new ImageIcon("ChessPiece/Chess_blt60.png"),
    new ImageIcon("ChessPiece/Chess_qlt60.png"),
    new ImageIcon("ChessPiece/Chess_klt60.png")
  };
  private ImageIcon chooseImage(int pieceIndex) {
    return pieceImages[pieceIndex];
  }

  public void displayGUI() {
    frame.add(getGUI());
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.setLocationByPlatform(true);
    frame.pack();
    frame.setSize(800, 600);
    frame.setMinimumSize(frame.getSize());
    frame.setVisible(true);
  }

  public void closeGUI() {
    this.frame.dispose();
  }

  public static void main(String[] args) {
    ChessGUI testGUI = new ChessGUI("BIGBOY");
    testGUI.displayGUI();
    Board testBoard = new Board();
    // int[] boardRepresentation = testBoard.getBoardRepresentation();
    long[] boardRepresentation = testBoard.inputLayer();
    testGUI.displayBoard(boardRepresentation);

    try {
      Thread.sleep(5000);
    } catch(InterruptedException ie) {
      ie.printStackTrace();
    }
    testGUI.closeGUI();
  }
}
