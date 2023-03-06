package com.nikolamarincic.fpscaler;

import java.nio.file.Path;
import java.nio.file.Paths;

import processing.core.PApplet;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class FPScaler extends PApplet {

	public final static Path imagesLoadPath = Paths.get("../../data/floorplan-project/dataset_02_reference/plan").toAbsolutePath().normalize();
	public final static Path measurementsLoadPath = Paths.get("../../data/floorplan-project/measurements/").toAbsolutePath().normalize();
	
	Controller controller;

	int mainWindowWidth = 1920;
	int mainWindowHeight = 1080;
	int numWindows = 1;
	boolean isFullScreen = false;
	
	public void settings() {
		if (isFullScreen) {
			fullScreen();
		} else {
			size(mainWindowWidth, mainWindowHeight);
		}
	}

	public void setup() {
		frameRate(30);
		controller = new Controller(this);
		FrameCoords[] coords = controller.getCoordinates(numWindows, 10, 50, 40, 2050);
		for (int i = 0; i < numWindows; i++) {
			controller.addWindow(coords[i], Utils.DEFAULT_SCALE, i);
		}
		controller.loadImages(imagesLoadPath);
	}


	public void draw() {
		background(255);
		controller.drawViews();
	}

	public void mouseWheel(MouseEvent event) {
		controller.mouseWheel(event);
	}

	public void mousePressed(MouseEvent event) {
		controller.mousePressed(event);
	}

	public void mouseReleased(MouseEvent event) {
		controller.mouseReleased(event);
	}

	public void mouseMoved(MouseEvent event) {
		controller.mouseMoved(event);
	}

	public void mouseDragged(MouseEvent event) {
		controller.mouseDragged(event);
	}

	public void keyPressed(KeyEvent event) {
		controller.keyPressed(event);
	}
	
	public static void main(String[] args) {
		PApplet.main("com.nikolamarincic.fpscaler.FPScaler");
	}
}
