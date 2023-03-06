package com.nikolamarincic.fpscaler;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import controlP5.ControlEvent;
import controlP5.ControlListener;
import controlP5.ControlP5;
import controlP5.Textfield;
import controlP5.Button;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class Controller implements ViewListener, ControlListener {

	private PApplet applet;
	private Model model;
	private ArrayList<ImageView> imgViews;
	private MainWindow mainWindow;
	private ControlP5 cp5;
	public static Path currentLoadPath = FPScaler.imagesLoadPath;


	public Controller(PApplet applet) {
		this.applet = applet;
		model = new Model();
		imgViews = new ArrayList<ImageView>();
		cp5 = new ControlP5(applet);
		cp5.setAutoDraw(false);
		mainWindow = new MainWindow(applet, cp5);
		mainWindow.addListener(this);
	}

	// =========================== WINDOWS =====================================

	public void addWindow(FrameCoords coords, float scale, int ID) {
		ImageView view = new ImageView(this.applet, this.cp5, coords.posX, coords.posY, coords.sizeX, coords.sizeY, scale, ID);
		view.addListerner(this);
		imgViews.add(view);
	}

	public ImageView getActiveWindow() {
		for (ImageView iv : imgViews) {
			if (iv.isActive()) {
				return iv;
			}
		}
		return null;
	}

	public ImageView getWindowByID(int ID) {
		for (ImageView iv : imgViews) {
			if (iv.getID() == ID) {
				return iv;
			}
		}
		return null;
	}

	// =========================== COORDINATES =====================================

	public FrameCoords[] getCoordinates(int noDivisions, int frameWid, int topOffset, int bottomOffset, int sizeY) {
		return mainWindow.getCoordinates(noDivisions, frameWid, topOffset, bottomOffset, sizeY);
	}

	// =========================== DRAW =====================================

	public void drawViews() {
		for (ImageView v : imgViews) {
			v.draw();
		}
		mainWindow.draw();
	}

	// =================== IMAGEVIEW METHODS ===============================


	public void setLockMeasureButtons(boolean value) {
		for (int i = 0; i<imgViews.size(); i++) {
			String name_ref = "ref_measure"+i;
			String name_temp = "test_measure"+i;
			Button toggle1 = (Button) cp5.getController(name_ref);
			Button toggle2 = (Button) cp5.getController(name_temp);
			if (value==true) {
				toggle1.lock();
				toggle2.lock();
			} else {
				toggle1.unlock();
				toggle2.unlock();
			}
		}
	}


	// =================== IMAGEVIEW EVENTS ================================


	public void viewEvent(ViewEvent event) {
		String message = event.getMessage();
		int ID = event.getViewID();

		if (message == "POINT1_REF") {
			setLockMeasureButtons(true);


		} else if (message =="POINT2_REF") {			
			mainWindow.bringTextField();
			applet.cursor(PConstants.ARROW);

		} else if (message =="POINT2_TEST") {
			getWindowByID(ID).rescaleViews();
		}
	}

	// =================== CONTROL P5 METHODS ===============================


	public void changeIndex(ImageView iw, int sign) {
		if (model.getMaxIndex() != 0) {
			int maxIndex = model.getMaxIndex();
			int currentIndex = iw.getImageIndex();
			currentIndex += sign;
			if (currentIndex == -1) {
				currentIndex = maxIndex - 1;
			}
			currentIndex = currentIndex % maxIndex;
			Image image = model.getImage(currentIndex);
			iw.setImage(image);
			iw.setImageIndex(currentIndex);
			iw.resetTranslation();
			iw.resetRotation();
		}
		reloadImagesMeasurements();
		updateMainWindowStatistics();
	}

	public void getNextUnmeasured(ImageView iw, int direction) {
		ArrayList<Image> images = model.getImages();
		int maxIndex = model.getMaxIndex();
		int currentIndex = iw.getImageIndex();

		do {
			currentIndex += direction;
			if (currentIndex == -1) {
				currentIndex = maxIndex - 1;
			}
			currentIndex = currentIndex % maxIndex;
		} while (images.get(currentIndex).isMeasured());
		iw.setImage(images.get(currentIndex));
		iw.setImageIndex(currentIndex);
		updateMainWindowStatistics();
	}

	public void reloadImagesMeasurements() {
		for (ImageView iw: imgViews) {
			iw.reloadImageMeasurement();
			iw.updateScaleTextField(iw.getImage().getCorrectScale());
		}
	}

	public void switchReferenceMeasureMode(ImageView iw, ControlEvent theEvent) {
		controlP5.Button toggle = (Button) theEvent.getController();
		if (toggle.isOn()) {
			iw.setReferenceMeasureMode(true);
			iw.resetMeasurePoints();
			iw.setButtonLock("test_measure", 1);
			iw.setButtonLock("zoom_measure", 1);
			setLockOthers(iw, 1);
			applet.noCursor();
		} else {
			iw.setReferenceMeasureMode(false);
			iw.setButtonLock("test_measure", 0);
			iw.setButtonLock("zoom_measure", 0);
			setLockOthers(iw, 0);
			applet.cursor(PConstants.ARROW);
		}
	}

	public void setLockOthers(ImageView iw, int value) {
		iw.setButtonLock("previous_unmeasured", value);
		iw.setButtonLock("next_unmeasured", value);
		iw.setButtonLock("scale_up", value);
		iw.setButtonLock("scale_down", value);
		iw.setButtonLock("next", value);
		iw.setButtonLock("previous", value);
		iw.setButtonLock("reset", value);
	}

	public void switchTestMeasureMode(ImageView iw, ControlEvent theEvent) {
		controlP5.Button toggle = (Button) theEvent.getController();
		if (toggle.isOn()) {
			iw.setTestMeasureMode(true);
			iw.setButtonLock("ref_measure", 1);
			iw.setButtonLock("zoom_measure", 1);
			setLockOthers(iw, 1);
			applet.noCursor();
		} else {
			iw.setTestMeasureMode(false);
			iw.setButtonLock("ref_measure", 0);
			iw.setButtonLock("zoom_measure", 0);
			setLockOthers(iw, 0);
			applet.cursor(PConstants.ARROW);
		}
	}

	public void lockView(ImageView iw, ControlEvent theEvent) {
		Button button = (Button) theEvent.getController();
		if (button.isOn()) {
			iw.setLock(true);
		} else {
			iw.setLock(false);
		}
	}

	public void resetViewAndImage(ImageView iw) {
		iw.resetView();
		iw.resetCurrentImage();
		updateMainWindowStatistics();
	}

	public void parseUserInput(ControlEvent theEvent) {
		if (theEvent.getName()=="userInput") {
			String stringValue = theEvent.getStringValue();
			float converted = Utils.convertInput(stringValue);
			mainWindow.hideTextField();
			for (ImageView iw: imgViews) {
				if (iw.isTypeMode()) {
					iw.setTypeMode(false);
					Image img = iw.getImage();
					Measurement refMeasurement = img.getReferenceMeasure();
					refMeasurement.setMeterDistance(converted);
					img.computeScaling();
					iw.setCorrectScale();
					iw.rescaleViews();
					iw.setToggle("ref_measure", 0); // turn off reference measure button
					iw.setButtonLock("test_measure", 0); // unlock test measure
				}
				iw.setButtonLock("ref_measure", 0);
			}
			reloadImagesMeasurements();
			updateMainWindowStatistics();
		}
	}

	public void saveMetadata(ControlEvent theEvent) {
		if (theEvent.getName()=="save_metadata") {
			model.saveMetadata();
		}
	}

	public void nudgeScale(ImageView iw, int amount) {
		Image currentImage = iw.getImage();
		float currentScale = currentImage.getCorrectScale();
		if (currentScale != 0) {
			float val = currentScale + 0.01f*amount;
			currentImage.setCorrectScale(val);
			iw.updateScaleTextField(val);
			currentImage.setFinalWidth(Math.round(currentImage.getOriginalWidth() * val));
			currentImage.setFinalHeight(Math.round(currentImage.getOriginalHeight()* val));
			if (iw.isLock()) {
				reloadImagesMeasurements();
			}
			iw.rescaleViews();

		}
	}

	public void zoomMeasure(ImageView iw) {
		float viewScale = iw.getViewScale();
		Image img = iw.getImage();
		int removeIndex = -1;
		ArrayList<Measurement> measurements = img.getMeasurements();
		for (int i=0; i<measurements.size(); i++) {
			Measurement m = measurements.get(i);
			if (m.getType().contentEquals(Utils.REFERENCE)) {
				removeIndex = i;
			}
		}
		if (removeIndex != -1) {
			measurements.remove(removeIndex);
		}
		img.setCorrectScale(viewScale);
		img.setMeasured(true);
		img.setFinalWidth(Math.round(img.getOriginalWidth() * viewScale));
		img.setFinalHeight(Math.round(img.getOriginalHeight()* viewScale));
		iw.rescaleViews();
		reloadImagesMeasurements();
		updateMainWindowStatistics();
	}

	
	
	public void updateMainWindowStatistics() {
		int count = 0;
		for (Image img: model.getImages()) {
			if (img.isMeasured())
				count++;
		}
		
		mainWindow.updateStatistics(model.getImages().size(), count);
	}
		
	

	public void textFieldToScale(ImageView iw, ControlEvent event) {
		Textfield textField = (Textfield) event.getController();
		String textValue = textField.getText();
		float newScale = Utils.textFieldToFloat(textValue);
		if (newScale != 0f) {
			Image img = iw.getImage();
			img.setCorrectScale(newScale);
			img.setMeasured(true);
			iw.rescaleViews();
			img.setFinalWidth(Math.round(img.getOriginalWidth() * newScale));
			img.setFinalHeight(Math.round(img.getOriginalHeight()* newScale));
			reloadImagesMeasurements();
			updateMainWindowStatistics();
			textField.setFocus(false);
		}
	}
	
	public void cropMode(ImageView iw, ControlEvent event) {
		boolean isCropMode = iw.isCropMode();
		if (!isCropMode) {
			iw.setCropMode(true);
		} else {
			iw.setCropMode(false);
		}
	}


	// =================== CONTROL P5 EVENTS ================================

	public void controlEvent(ControlEvent theEvent) {
		String eventName = theEvent.getName();
		// Main Window actions
		launchDirectoryChooser(theEvent, FPScaler.imagesLoadPath);
		launchFileChooser(theEvent, FPScaler.measurementsLoadPath);
		parseUserInput(theEvent);
		saveMetadata(theEvent);

		// View Windows actions
		for (ImageView iw : imgViews) {

			int ID = iw.getID();
			String next = "next" + ID;
			String previous = "previous" + ID;
			String reset = "reset" + ID;
			String lock = "lock" + ID;
			String ref_measure = "ref_measure" + ID;
			String test_measure = "test_measure" + ID;
			String reload_scale = "reload_scale"+ ID;
			String scale_up = "scale_up" + ID;
			String scale_down = "scale_down" + ID;
			String zoom_measure = "zoom_measure" + ID;
			String next_unmeasured = "next_unmeasured" + ID;
			String previous_unmeasured = "previous_unmeasured" + ID;
			String scale = "scale" + ID;
			String cropMode = "crop_mode"+ID;

			if (eventName.contentEquals(next)) {
				changeIndex(iw, 1);

			} else if (eventName.contentEquals(previous)) {
				changeIndex(iw, -1);

			} else if (eventName.contentEquals(reset)) {
				resetViewAndImage(iw);

			} else if (eventName.contentEquals(lock)) {
				lockView(iw, theEvent);

			} else if (eventName.contentEquals(ref_measure)) {
				switchReferenceMeasureMode(iw, theEvent);

			} else if (eventName.contentEquals(test_measure)) {
				switchTestMeasureMode(iw, theEvent);

			} else if (eventName.contentEquals(reload_scale)) {
				iw.reloadCorrectScale();

			} else if (eventName.contentEquals(scale_up)) {
				nudgeScale(iw, 1);

			} else if (eventName.contentEquals(scale_down)) {
				nudgeScale(iw, -1);

			} else if (eventName.contentEquals(zoom_measure)) {
				zoomMeasure(iw);

			} else if (eventName.contentEquals(next_unmeasured)) {
				getNextUnmeasured(iw, 1);

			} else if (eventName.contentEquals(previous_unmeasured)) {
				getNextUnmeasured(iw, -1);

			} else if (eventName.contentEquals(scale)) {
				textFieldToScale(iw, theEvent);
				
			} else if (eventName.contentEquals(cropMode)) {
				cropMode(iw, theEvent);
			}

			for (int i=0; i<ImageViewUI.NUM_TAG_BUTTONS; i++) {
				char tag = ImageViewUI.tagNames[i];
				String tagLoad = "tagLoad"+tag+"_"+ID;
				String tagSet = "tagSet"+tag+"_"+ID;

				if (eventName.contentEquals(tagLoad)) {
					//System.out.println("VIEW: "+ID+" tagLoad: "+tag+" activated");
					loadTag(iw, tag);


				} else if (eventName.contentEquals(tagSet)) {
					//System.out.println("VIEW: "+ID+" tagSet: "+tag+" activated");
					tagImage(iw, tag);
				}
			}
		}
	}

	

	
	public void tagImage(ImageView iw, char tag) {
		Image imageToTag = iw.getImage();

		for (Image img: model.getImages()) {
			if (img == imageToTag) {
				img.setTag(tag);
			} else {
				if (img.getTag()==tag)
					img.setTag(' ');
			}
		}
	}

	public void loadTag(ImageView iw, char tag) {

		for (Image img: model.getImages()) {
			char imageTag = img.getTag();
			if (imageTag == tag) {
				iw.setImage(img);
			}
		}
	}


	// =================== LOAD IMAGES ================================

	public void launchDirectoryChooser(ControlEvent theEvent, final Path loadPath) {
		if (theEvent.getName()=="load_images") {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						JFileChooser chooser = new JFileChooser();
						chooser.setCurrentDirectory(loadPath.toFile());
						chooser.setDialogTitle("Choose an images folder");
						chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						chooser.setAcceptAllFileFilterUsed(false);

						int returnVal = chooser.showOpenDialog(null);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File file = chooser.getSelectedFile();
							//String name = file.getAbsolutePath();
							dirChosen(file.toPath());

						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
	
	public void launchFileChooser(ControlEvent theEvent, final Path loadPath) {
		if (theEvent.getName()=="load_measurements") {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						JFileChooser chooser = new JFileChooser();
						chooser.setCurrentDirectory(loadPath.toFile());
						chooser.setDialogTitle("Choose an images folder");
						chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
						chooser.setAcceptAllFileFilterUsed(false);

						int returnVal = chooser.showOpenDialog(null);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File file = chooser.getSelectedFile();
							//String name = file.getAbsolutePath();
							measurementsChosen(file.toPath());

						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	private void dirChosen(Path name) {
		System.out.println(name);
		currentLoadPath = name;
		loadImages(name);
		updateMainWindowStatistics();
	}
	
	private void measurementsChosen(Path path) {
		model.applyMeasurementsToImages(path.toFile());
		for (ImageView iw: imgViews) {
			iw.reloadCorrectScale();
		}
		updateMainWindowStatistics();
	}

	private void loadFirstImageIntoView() {
		if (model.getMaxIndex()!= 0) {
			for (ImageView iw : imgViews) {
				iw.setImageIndex(0);
				iw.setNumImages(model.getMaxIndex());
				Image image = model.getImage(0);
				iw.setImage(image);
				if (!image.isMeasured()) {
					iw.resetView();
				}
			}
		}
	}

	public void loadImages(Path loadPath) {
		model.addImagesPaths(loadPath);
		loadFirstImageIntoView();
	}
	
	public static String getFullPath(String path) {
		return currentLoadPath.resolve(path).toString();
	}
	

	// =================== MOUSE EVENTS ================================

	public void mouseWheel(MouseEvent event) {
		for (ImageView v : imgViews) {
			v.mouseWheel(event);
		}
	}

	public void mousePressed(MouseEvent event) {
		for (ImageView v : imgViews) {
			v.mousePressed(event);
		}
	}

	public void mouseReleased(MouseEvent event) {
		for (ImageView v : imgViews) {
			v.mouseReleased(event);
		}
	}

	public void mouseMoved(MouseEvent event) {
		for (ImageView v : imgViews) {
			v.checkActive();
			v.convertMouse();
		}
	}

	public void mouseDragged(MouseEvent event) {
		for (ImageView v : imgViews) {
			v.mouseDragged(event);
		}
	}

	public void keyPressed(KeyEvent event) {
		for (ImageView v : imgViews) {
			v.keyPressed(event);
		}
	}
	
}
