package DisplayBoardEmulation.emulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class DisplayBoard extends JPanel implements Display {
	private final static int PIXEL_WIDTH = 10;
	private final static int PIXEL_HEIGHT = 10;
	private final static int PIXEL_SPACING = 2;
	private int pixelRowMultiplier;
	private int pixelColMultiplier;

	public final static int ROWS = 44;
	public final static int COLS = 74;

	private Pixel[][] pixelArr;

	private JFrame containerFrame;

	private TreeSet<String> keys;

	private LinkedList<KeyRunnable> keyCallbacks;

	public DisplayBoard() {
		/*
		 * ROWS = rows; COLS = cols;
		 */
		pixelRowMultiplier = PIXEL_HEIGHT + PIXEL_SPACING;
		pixelColMultiplier = PIXEL_WIDTH + PIXEL_SPACING;
		pixelArr = new Pixel[ROWS][COLS];
		for (int r = 0; r < ROWS; r++) {
			for (int c = 0; c < COLS; c++) {
				int pixelX = c * pixelColMultiplier;
				int pixelY = r * pixelRowMultiplier;
				Rectangle pixelRect = new Rectangle(pixelX, pixelY, PIXEL_WIDTH, PIXEL_HEIGHT);
				pixelArr[r][c] = new Pixel(pixelRect, Color.BLACK);
			}
		}
		keys = new TreeSet<String>();
		this.addKeyListener(new panelKeyListener());
		keyCallbacks = new LinkedList<KeyRunnable>();
		setBackground(Color.GRAY);
		initFrame();
	}

	/**
	 * Ignore this method. It sets the size of the JPanel. You won't be using it.
	 */
	public Dimension getPreferredSize() {
		return new Dimension((pixelColMultiplier * COLS) - PIXEL_SPACING, (pixelRowMultiplier * ROWS) - PIXEL_SPACING);
	}

	/**
	 * Ignore this method. It paints the JPanel. You won't be using it.
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		for (int r = 0; r < ROWS; r++) {
			for (int c = 0; c < COLS; c++) {
				Pixel currentPixel = pixelArr[r][c];
				g2.setColor(currentPixel.getPixelColor());
				g2.fill(currentPixel.getPixelRect());
			}
		}
	}

	// Methods to use
	public void setPixel(int row, int col, int red, int green, int blue) {
		/*
		 * int rgb = red; rgb = (rgb<<8) + green; rgb = (rgb<<8) + blue;
		 */
		colorPixel(row, col, new Color(red, green, blue));
		repaint();
	}

	public void setPixel(int row, int col, Color c) {
		colorPixel(row, col, c);
		repaint();
	}

	public Color getPixel(int row, int col) {
		return pixelArr[row][col].getPixelColor();
	}

	private void colorPixel(int row, int col, Color c) {
		if (row < 0 || row >= ROWS || col < 0 || col >= COLS) {
			System.out.println("DisplayBoard: Invalid pixel coordinates " + row + ", " + col + " were inputted.");
			return;
		}
		pixelArr[row][col].setPixelColor(c);
	}

	public void colorRect(int row, int col, int width, int height, int r, int g, int b) {
		int finalRow = row + height;
		if (finalRow >= ROWS) {
			finalRow = ROWS - 1;
		}
		int finalCol = col + width;
		if (finalCol >= COLS) {
			finalCol = COLS - 1;
		}
		for (int rw = row; rw <= finalRow; rw++) {
			for (int cl = col; cl <= finalCol; cl++) {
				colorPixel(rw, cl, new Color(r, g, b));
			}
		}
		repaint();
	}

	public void addKeyCallback(KeyRunnable r) {
		keyCallbacks.add(r);
	}
	
	public boolean hasKeyCallback(KeyRunnable r) {
		return keyCallbacks.contains(r);
	}
	public void colorRect(int row, int col, int width, int height, Color c) {
		int finalRow = row + height;
		if (finalRow >= ROWS) {
			finalRow = ROWS - 1;
		}
		int finalCol = col + width;
		if (finalCol >= COLS) {
			finalCol = COLS - 1;
		}
		for (int rw = row; rw <= finalRow; rw++) {
			for (int cl = col; cl <= finalCol; cl++) {
				colorPixel(rw, cl, c);
			}
		}
		repaint();
	}

	public void colorRect(Rectangle rect, Color c) {
		colorRect(rect.y, rect.x, rect.width, rect.height, c);
	}

	public void colorRect(Rectangle rect, int r, int g, int b) {
		colorRect(rect.y, rect.x, rect.width, rect.height, r, g, b);
	}

	public void clear() {
		colorRect(0, 0, COLS, ROWS, Color.BLACK);
	}

	public boolean isCleared() {
		for (int r = 0; r < ROWS; r++) {
			for (int c = 0; c < COLS; c++) {
				if (!getPixel(r, c).equals(Color.BLACK)) {
					return false;
				}
			}
		}
		return true;
	}

	// Key handling
	public Set<String> getKeys() {
		return keys;
	}

	// JFrame Handling
	public void show() {
		containerFrame.setVisible(true);
		this.setFocusable(true);
		this.requestFocus();
	}

	private void initFrame() {
		containerFrame = new JFrame();
		// f.setUndecorated(true);
		// f.setBackground(new Color(0, 0, 0, 0));
		containerFrame.setTitle("Pixel Display");
		containerFrame.add(this);
		containerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		containerFrame.pack();
	}

	public void close() {
		containerFrame.setVisible(false);
		containerFrame.dispose();
	}

	private class panelKeyListener implements KeyListener {

		@Override
		public void keyPressed(KeyEvent arg0) {
			keys.add("" + arg0.getKeyChar());
			try {
				for(KeyRunnable run : keyCallbacks) {
					run.run(arg0);
				}
			} catch(ConcurrentModificationException e) {
				System.out.println("DisplayBoard: Concurrent Modification Exception in key press listeners!");
			
			}
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			keys.remove("" + arg0.getKeyChar());
			try {
				for(KeyRunnable run : keyCallbacks) {
					run.run(arg0);
				}
			} catch(ConcurrentModificationException e) {
				System.out.println("DisplayBoard: Concurrent Modification Exception in key press listeners!");
			}
		}

		@Override
		public void keyTyped(KeyEvent arg0) {
			// TODO Auto-generated method stub

		}
	}

	@Override
	/**
	 * drawString without n (for easier Java testing)
	 * 
	 * @param row
	 * @param col
	 * @param red
	 * @param green
	 * @param blue
	 * @param chars
	 */
	public void drawString(int row, int col, int red, int green, int blue, String chars) {
		char[][] charset = charSet.Cset(); // creating character set

		int extraSpacing = 0; // extra spacing between letters

		for (int i = 0; i < chars.length(); i++) { // for each character in "chars"...
			String letter = chars.substring(i, i + 1); // get corresponding letter

			char[] locs = charset[letter.hashCode()]; // array of hex codes for each row of pixels in the letter
			System.out.println("DisplayBoard: L: " + letter + "; HC: " + letter.hashCode());
			if (letter.hashCode() == 32)// if the character is a space (" ")...
			{
				extraSpacing -= 3;// reduce the spacing
			}
			for (int c = 0; c < locs.length; c++) // for each column...
			{
				int r = 0; // intialized row count
				for (int j = 1; j <= 256; j *= 2) // for each pixel/binary in the row...
				{
					if (((locs[c]) & j) != 0) // if the pixel should be on...
					{

						setPixel(r + row, c + col + extraSpacing, red, green, blue);// turn the pixel on
					}

					r++;// increase the row count
				}
			}
			extraSpacing += 6; // add spacing between letters
		}

	}

	@Override
	/**
	 * 
	 * @param n
	 *            - The number of characters in chars
	 * @param row
	 * @param col
	 * @param red
	 * @param green
	 * @param blue
	 * @param chars
	 */
	public void drawString(int n, int row, int col, int red, int green, int blue, String chars) {
		char[][] charset = charSet.Cset(); // creating character set
		int extraSpacing = 0; // extra spacing between letters

		for (int i = 0; i < n; i++) { // for each character in "chars"...
			String letter = chars.substring(i, i + 1); // get corresponding letter

			char[] locs = charset[letter.hashCode()]; // array of hex codes for each row of pixels in the letter
			System.out.println("DisplayBoard: L: " + letter + "; HC: " + letter.hashCode());
			if (letter.hashCode() == 32)// if the character is a space (" ")...
			{
				extraSpacing -= 3;// reduce the spacing
			}
			for (int c = 0; c < locs.length; c++) // for each column...
			{
				int r = 0; // intialized row count
				for (int j = 1; j <= 256; j *= 2) // for each pixel/binary in the row...
				{
					if (((locs[c]) & j) != 0) // if the pixel should be on...
					{

						setPixel(r + row, c + col + extraSpacing, red, green, blue);// turn the pixel on
					}

					r++;// increase the row count
				}
			}
			extraSpacing += 6; // add spacing between letters
		}

	}

	@Override
	/**
	 * 
	 * @param n
	 *            - The number of Characters in chars
	 * @param row
	 * @param col
	 * @param red
	 * @param green
	 * @param blue
	 * @param chars
	 * @param spacing
	 *            - Integer to customize spacing between characters
	 */
	public void drawString(int n, int row, int col, int red, int green, int blue, String chars, int spacing) {
		char[][] charset = charSet.Cset(); // creating character set
		
		int extraSpacing = 0; // extra spacing between letters

		for (int i = 0; i < n; i++) { // for each character in "chars"...
			String letter = chars.substring(i, i + 1); // get corresponding letter

			char[] locs = charset[letter.hashCode()]; // array of hex codes for each row of pixels in the letter
			System.out.println("DisplayBoard: L: " + letter + "; HC: " + letter.hashCode());
			if (letter.hashCode() == 32)// if the character is a space (" ")...
			{
				extraSpacing -= spacing / 2;// reduce the spacing
			}
			for (int c = 0; c < locs.length; c++) // for each column...
			{
				int r = 0; // intialized row count
				for (int j = 1; j <= 256; j *= 2) // for each pixel/binary in the row...
				{
					if (((locs[c]) & j) != 0) // if the pixel should be on...
					{

						setPixel(r + row, c + col + extraSpacing, red, green, blue);// turn the pixel on
					}

					r++;// increase the row count
				}
			}
			extraSpacing += spacing; // add spacing between letters
		}

	}
	
	@Override
	public void drawString(int row, int col, Color c, String chars) {
		drawString(row,col,c.getRed(),c.getGreen(),c.getBlue(),chars);
	}
	
	@Override
	public void drawString(int row, int col, Color c, String chars, int spacing) {
		drawString(row,col,c.getRed(),c.getGreen(),c.getBlue(),chars,spacing);
	}
	
	public int StringWidth(String chars) {
		char[] chararr = chars.toCharArray();
		int pixels = 0;
		for(char c : chararr) {
			if(("" + c).equals(" ")) {
				pixels += 3;
			} else {
				pixels += 6;
			}
		}
		return pixels;
	}
	
	public int StringWidth(String chars, int spacing) {
		char[] chararr = chars.toCharArray();
		int pixels = 0;
		for(char c : chararr) {
			if(("" + c).equals(" ")) {
				pixels += spacing/2;
			} else {
				pixels += spacing;
			}
		}
		return pixels;
	}

	@Override
	public void drawString(int row, int col, int red, int green, int blue, String chars, int spacing) {
		drawString(chars.length(),row,col,red,green,blue,chars,spacing);
	}

	@Override
	public void drawString(int n, int row, int col, Color c, String chars) {
		drawString(n,row,col,c.getRed(),c.getGreen(),c.getBlue(),chars);
	}

	@Override
	public void drawString(int n, int row, int col, Color c, String chars, int spacing) {
		drawString(n,row,col,c.getRed(),c.getGreen(),c.getBlue(),chars,spacing);
	}
}
