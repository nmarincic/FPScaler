package com.nikolamarincic.fpscaler;


import controlP5.ControlP5;
import controlP5.Textfield;
import processing.core.PApplet;
import processing.core.PFont;
import controlP5.ControlListener;

public class MainWindow {

	int wid, hei;
	int offsetTop = 10, offsetSide = 10;
	private FrameCoords coords;
	private PApplet applet;
	private ControlP5 cp5;
	private Textfield tf;
	
	// Stats
	private int numberOfImages;
	private int numberOfMeasured;

	public MainWindow(PApplet applet, ControlP5 cp5) {
		this.applet = applet;
		this.cp5 = cp5;
		this.wid = this.applet.width;
		this.hei = this.applet.height;
		initFrame();
		System.out.println("Main window created with size: " + wid + "x" + hei + "px");
		addGUI();
	}

	public void addListener(ControlListener listener) {
		cp5.addListener(listener);
	}

	public void removeListener(ControlListener listener) {
		cp5.removeListener(listener);
	}
	
	public FrameCoords[] getCoordinates(int noDivisions, int frameWid, int topOffset, int bottomOffset, int sizeY) {
		
		int totalWid = coords.sizeX - (noDivisions - 1) * frameWid;
		int sizeX = (int) ((float) totalWid / noDivisions);
		if (sizeY > coords.sizeY - topOffset - bottomOffset) {
			sizeY = coords.sizeY - topOffset - bottomOffset;
			System.out.println("Frame's vertical size is larger than the main windows. Resizing it to: " + sizeY);
		}
		FrameCoords[] coordinates = new FrameCoords[noDivisions];

		for (int i = 0; i < noDivisions; i++) {
			int posX = -1;
			if (i == 0) {
				posX = coords.posX;
			} else {
				posX = coords.posX + (frameWid + sizeX) * i;
			}
			coordinates[i] = new FrameCoords(posX, coords.posY + topOffset, sizeX, sizeY);
		}
		return coordinates;
	}

	
	private void initFrame() {
		int posx = offsetSide;
		int posy = offsetTop;
		int sizex = wid - (2 * offsetSide);
		int sizey = hei - (2 * offsetTop);
		coords = new FrameCoords(posx, posy, sizex, sizey);
	}

	public void setOffset(int offsetSide, int offsetTop) {
		this.offsetTop = offsetTop;
		this.offsetSide = offsetSide;
		initFrame();
	}

	public void addGUI() {
		PFont font = applet.createFont("arial", 18);
		applet.textFont(font);
		applet.textSize(14); 
		
		int offset = addLoadButton(0, 100,19, 5);
		offset = addSaveButton(offset, 100,19, 5);
		offset = addLoadMeasurementsButton(offset,100,19,5);
		
		
		tf = cp5.addTextfield("userInput")
			      .setPosition(0,0)
			      .setSize(100, 30)
			      .setFocus(false)
			      .setLabel("")
			      .setColorBackground(Utils.GREEN_MEDIUM)
			      .setColorActive(Utils.WHITE)
			      .setFont(font)
			      .setVisible(false)
			      ;
		
		
	}
	
	public int addLoadButton(int posx, int buttonWidth, int buttonHeight, int offsetRight) {
		cp5.addButton("load_images")
		.setLabel("load images")
		.setColorBackground(applet.color(200, 0, 0))
		.setColorForeground(applet.color(255, 0, 0)).setColorActive(applet.color(150, 0, 0))
		.setPosition(offsetSide+posx, offsetTop)
		.setSize(buttonWidth, buttonHeight);
		return offsetSide + posx + buttonWidth + offsetRight;
	}
	
	public int addSaveButton(int posx, int buttonWidth, int buttonHeight, int offsetRight) {
		cp5.addButton("save_metadata")
		.setLabel("save metadata")
		.setColorBackground(applet.color(200, 0, 0))
		.setColorForeground(applet.color(255, 0, 0)).setColorActive(applet.color(150, 0, 0))
		.setPosition(posx, offsetTop)
		.setSize(buttonWidth, buttonHeight);
		return posx + buttonWidth + offsetRight;
	}
	
	public int addLoadMeasurementsButton(int posx, int buttonWidth, int buttonHeight, int offsetRight) {
		cp5.addButton("load_measurements")
		.setLabel("load measurements")
		.setColorBackground(applet.color(200, 0, 0))
		.setColorForeground(applet.color(255, 0, 0)).setColorActive(applet.color(150, 0, 0))
		.setPosition(posx, offsetTop)
		.setSize(buttonWidth, buttonHeight);
		return posx + buttonWidth + offsetRight;
	}
	
	public void drawStatistics() {
		applet.pushStyle();
		applet.fill(0);
		applet.text("scaled: "+numberOfMeasured+"/"+numberOfImages, offsetSide+5, hei-offsetTop-25);
		applet.popStyle();
	}

	public void draw() {
		drawStatistics();
		cp5.draw();
		
	}
	
	public Textfield getTextField() {
		return tf;
	}

	public void hideTextField() {
		tf.setVisible(false);
		tf.keepFocus(false);
		tf.setFocus(false);
		tf.bringToFront();
		
	}
	
	public void bringTextField() {
		tf.setPosition(applet.mouseX+10, applet.mouseY-10-tf.getHeight());
		tf.setVisible(true);
		tf.keepFocus(true);
	}
	
	public void updateStatistics(int numImages, int numMeasured) {
		this.numberOfImages = numImages;
		this.numberOfMeasured = numMeasured;
		
	}
}
