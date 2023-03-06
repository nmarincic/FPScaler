package com.nikolamarincic.fpscaler;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import controlP5.Button;
import controlP5.CallbackEvent;
import controlP5.CallbackListener;
import controlP5.ControlP5;
import controlP5.Textfield;
import processing.core.*;
import processing.event.*;

public class ImageView {


	// PApplet
	private PApplet applet;
	private PGraphics pg;
	private ControlP5 cp5;
	private DecimalFormat df = new DecimalFormat("####.##");

	// UI
	ImageViewUI userInterface;

	// Loading images
	private Image image;
	private PImage pimage;
	private int imageIndex;

	// Windows ID and information
	private int ID = -1;
	private boolean isActive = false;

	private int posX, posY = 0;
	private int sizeX, sizeY = 0;
	private float endX, endY = 0;
	private float scl = 1f;
	private int backgroundColor;

	// Translation
	private float transX;
	private float transY;
	private float transXold, transYold;

	// Dragging
	private boolean isDrag;

	// Rotation
	private float rotamt = 0;

	// Event listeners
	private List<ViewListener> listeners = new ArrayList<ViewListener>();


	// Locking
	private boolean isLock;

	// Converted Mouse
	private float mx, my;
	private float mxStart, myStart;

	// Measure
	private boolean referenceMeasureMode;
	private boolean testMeasureMode;
	private boolean typeMode;
	private MeasureAction testMeasureAction;
	private MeasureAction refMeasureAction;

	// Cursor
	public boolean isCursor = true;

	// Font
	PFont font;
	boolean firstDraw = true;

	// Stats
	private int numImages;

	// Callback
	CallbackListener cb;

	// copy paste
	float copyPasteValue = 0;

	// Saving indicator
	private boolean saving;

	// cropping
	private boolean cropMode;

	public ImageView(PApplet applet, ControlP5 cp5, int posx, int posy, int sizex, int sizey, float scale, int ID) {

		this.applet = applet;
		this.cp5 = cp5;
		this.posX = posx;
		this.posY = posy;
		this.sizeX = sizex;
		this.sizeY = sizey;
		this.ID = ID;
		this.scl = scale;
		backgroundColor = this.applet.color(255, 255, 255);
		endX = posX + sizeX;
		endY = posY + sizeY;
		resetTranslation();
		userInterface = new ImageViewUI(this);
		userInterface.addWindowUI();
		testMeasureAction = new MeasureAction();
		refMeasureAction = new MeasureAction();

		font = this.applet.createFont("arial", 14);
		this.pg = this.applet.createGraphics(sizex, sizey);
		this.pg.smooth(8);
		initCallback();
		if (this.ID==0) {
			isActive=true;
		}

	}
	// =================== CALLBACK ==============================

	public void initCallback() {
		cb = new CallbackListener() {
			public void controlEvent(CallbackEvent theEvent) {
				int action = theEvent.getAction();
				if (action == ControlP5.ACTION_CLICK) {
					Textfield tf = (Textfield) theEvent.getController();

					if (applet.keyPressed) {
						if (applet.keyCode==PApplet.SHIFT) {
							copyPasteValue = Float.valueOf(tf.getText());
							tf.setFocus(false);
						}
						if (applet.keyCode==PApplet.CONTROL) {
							if (copyPasteValue != 0) {
								tf.setText(String.valueOf(copyPasteValue));
								tf.submit();
								tf.setFocus(false);
							}
						}
					}
				}
			}
		};
		Textfield tf = (Textfield) this.cp5.getController("scale"+ID);
		tf.addCallback(cb);
	}


	// ===================  DRAWING  ================================


	public void draw() {
		begin();
		drawImage();
		//drawAxes(100);
		drawFinalFrame(30f);
		drawCropFrame(224f);
		drawMeasurePoints();
		end();
	}

	public void begin() {
		pg.beginDraw();
		if (firstDraw) {
			pg.textFont(font);
			firstDraw = false;
		}
		pg.background(backgroundColor);
		pg.pushMatrix();
		pg.translate(transX, transY);
		pg.rotate(PApplet.radians(rotamt));
	}

	public void end() {
		pg.popMatrix();
		drawFrame();
		drawDebugger();
		drawCursor(20);
		drawInfo(20);
		drawMeasuredIndicator(15,15);
		pg.endDraw();
		applet.image(pg, posX, posY);
	}


	public void drawFinalFrame(float noMeters) {
		if (!cropMode) {
			pg.pushStyle();
			pg.stroke(0,0,255);
			pg.strokeWeight(2f);
			pg.noFill();
			float correctScale = image.getCorrectScale();
			float inverseCorrectScale = 1 / correctScale;
			float targetPixels = Utils.PIXELS_PER_METER*noMeters;
			float pixelDistance =  targetPixels / correctScale;
			pg.rect(scl*-pixelDistance/2, scl*-pixelDistance/2, scl*pixelDistance, scl*pixelDistance);
			// TEXT
			pg.textAlign(PApplet.CENTER);
			pg.fill(0,0,255);
			pg.noStroke();
			pg.text("with target scale: "+noMeters+"m = "+targetPixels+"px", 0, -scl*pixelDistance/2-10);
			pg.text("with original scale: "+noMeters+"m = "+targetPixels*inverseCorrectScale+"px", 0, scl*pixelDistance/2+20);
			pg.popStyle();
		}
	}

	public void drawCropFrame(float targetSize) {
		if (cropMode) {
			pg.textAlign(PApplet.CENTER);
			pg.pushStyle();
			pg.stroke(255,0,0);
			pg.strokeWeight(2f);
			pg.noFill();
			float translateX = 0;
			float translateY = 0;
			float cropSize = 0;
			if (image !=null) {
				PVector cropCentre = image.getCropCentre();
				translateX = cropCentre.x;
				translateY = cropCentre.y;
				cropSize = cropCentre.z;
			}
			pg.rect(-cropSize/2+translateX, -cropSize/2+translateY, cropSize, cropSize);
			pg.line(translateX-10, translateY-10, translateX+10, translateY+10);
			pg.line(translateX-10, translateY+10, translateX+10, translateY-10);
			// TEXT
			pg.textAlign(PApplet.CENTER);
			pg.fill(255,0,0);
			pg.noStroke();
			pg.text("actual size: "+cropSize+"px", 0+translateX, cropSize/2+20+translateY);
			pg.text("scale to: "+targetSize+"px", 0+translateX, -cropSize/2-15+translateY);
			pg.popStyle();
		}
	}

	public void drawImage() {
		if (pimage != null) {

			float hei = pimage.height * this.scl;
			float wid = pimage.width * this.scl;
			applet.noTint();
			if (referenceMeasureMode || typeMode || testMeasureMode) {
				applet.tint(255,180);
			}

			// IMAGE 
			if (!cropMode) { // normal mode
				pg.image(pimage, -wid / 2, -hei / 2, wid, hei);
			} else { // crop mode
				pg.image(pimage, -pimage.width / 2, -pimage.height / 2, pimage.width, pimage.height);
			}

			// IMAGE GREY FRAME
			pg.stroke(100);
			pg.noFill();
			if (!cropMode) { // normal mode
				pg.rect(-wid / 2, -hei / 2, wid, hei);
			} else {
				pg.rect(-pimage.width / 2, -pimage.height / 2, pimage.width, pimage.height);
			}

			pg.noStroke();
			pg.fill(0);
			pg.textAlign(PApplet.CENTER);
			if (!cropMode) {
				pg.text("original width: "+image.getOriginalWidth()+"px", 0, hei/2+15);		
			} else {
				pg.text("original width: "+image.getOriginalWidth()+"px", 0, pimage.height/2+15);
			}
			pg.fill(Utils.GREEN_DARK);
			if (!cropMode) {
				pg.text("target width: "+image.getFinalWidth()+"px", 0, -hei/2-5);			
			} else {
				//pg.text("target width: "+image.getFinalWidth()+"px", 0, -pimage.height/2-5);
			}
			pg.pushMatrix();
			if (!cropMode) {
				pg.translate(-wid/2-5, 0);
			} else {
				pg.translate(-pimage.width/2-5, 0);
			}
			pg.rotate(PApplet.PI/-2);
			pg.fill(0);

			pg.text("original height: "+image.getOriginalHeight()+"px", 0, 0);
			if (!cropMode) {
				pg.translate(0, wid+20);				
			} else {
				pg.translate(0, pimage.width+20);
			}
			pg.fill(Utils.GREEN_DARK);
			if (!cropMode)
				pg.text("target height: "+image.getFinalHeight()+"px", 0, 0);
			pg.popMatrix();
		}

	}

	public void drawMeasuredIndicator(int size, int offset) {
		if (image != null) {
			pg.pushStyle();
			pg.strokeWeight(0.5f);
			if (image.isMeasured()) {
				pg.fill(Utils.GREEN_MEDIUM);
			} else {
				pg.stroke(0);
				pg.noFill();
			}
			pg.ellipse(offset ,sizeY-10, size, size);


			if (!image.isPlan()) {
				pg.stroke(255,0,0);
				pg.strokeWeight(5f);
				pg.line(0, sizeY, sizeX, 0);
			}
			pg.popStyle();
		}
	}


	public void drawAxes(int len) {
		pg.pushStyle();
		pg.strokeWeight(1f);
		float lenscaled = len * scl;
		pg.noFill();
		pg.stroke(255, 0, 0);
		pg.line(0, 0, lenscaled, 0);
		pg.stroke(0, 0, 255);
		pg.line(0, 0, 0, lenscaled);
		pg.popStyle();
	}

	public void drawInfo(int offsetY) {
		if (image != null) {
			pg.pushStyle();

			pg.strokeWeight(0.5f);
			pg.fill(255,180);
			pg.stroke(0);
			pg.rect(0,sizeY-offsetY, sizeX-0.5f,offsetY-0.5f);

			pg.noStroke();
			pg.fill(50);
			pg.textAlign(PApplet.LEFT);
			pg.text(image.getPath(), 35,sizeY-5);
			pg.textAlign(PApplet.RIGHT);
			pg.text(imageIndex+"/"+numImages, sizeX-3,sizeY-5);
			pg.popStyle();
		}
	}

	public void drawMeasurePoints() {
		if (image != null) {
			ArrayList<Measurement> measurements = image.getMeasurements();
			pg.textAlign(PApplet.LEFT);
			for (Measurement m: measurements) {
				PVector p1 = m.getP1();
				PVector p2 = m.getP2();
				float meterDistance = m.getMeterDistance();
				if (p1 != null) {
					pg.pushStyle();
					if (m.getType().contentEquals(Utils.REFERENCE)) {
						pg.stroke(Utils.GREEN_MEDIUM);	
					} else {
						pg.stroke(Utils.BLUISH);
					}

					pg.strokeWeight(2f);
					if (p2 == null) {
						PVector temp = new PVector(mx,my);
						if (isActive) {
							pg.line(p1.x*scl, p1.y*scl, temp.x*scl, temp.y*scl);
						}
					} else {
						if (!cropMode) {
							pg.line(p1.x*scl, p1.y*scl, p2.x*scl, p2.y*scl);
						} else {
							pg.line(p1.x, p1.y, p2.x, p2.y);
						}
						pg.fill(Utils.GREEN_DARK, 255);
						pg.noStroke();
						if (m.getType().contentEquals(Utils.REFERENCE)) {
							pg.fill(Utils.GREEN_MEDIUM,200);
						} else {
							pg.fill(Utils.BLUISH,200);
						}
						if (!cropMode) {
							pg.rect(p1.x*scl+5, p1.y*scl+2, 40, -12);
						} else {
							pg.rect(p1.x+5, p1.y+2, 40, -12);
						}
						if (!isReferenceMeasureMode() && !typeMode) {
							pg.fill(255);
							if (!cropMode) {
								pg.text(meterDistance, p1.x*scl, p1.y*scl);							
							} else {
								pg.text(meterDistance, p1.x, p1.y);
							}
						}
						pg.popStyle();

					}
				}
			}
		}
	}

	public void drawCursor(int size) {
		if (referenceMeasureMode || testMeasureMode) {
			applet.pushStyle();
			applet.stroke(0);
			applet.strokeWeight(1.5f);
			applet.line((applet.mouseX-size), applet.mouseY, (applet.mouseX+size), applet.mouseY);
			applet.line(applet.mouseX, (applet.mouseY-size), applet.mouseX, (applet.mouseY+size));
			applet.popStyle();
		}
	}


	public void drawFrame() {
		pg.pushStyle();
		pg.strokeWeight(1f);
		pg.noFill();
		if (isActive) {
			pg.stroke(50);
		} else {
			pg.stroke(150);
		}
		if (referenceMeasureMode || typeMode) {
			pg.strokeWeight(4f);
		}
		if (saving) {
			pg.strokeWeight(50f);
			pg.stroke(255,0,0);
			saving = false;
		}

		pg.rect(0, 0, sizeX-1, sizeY-1);
		pg.popStyle();
	}

	public void drawDebugger() {
		if (isActive) {
			int offsetX = 4;
			int offsetY = 4;
			pg.textAlign(PApplet.LEFT);
			pg.pushStyle();
			pg.fill(50);
			pg.noStroke();
			pg.text("ID: " + ID, offsetX, offsetY + 10);
			pg.text("mx: " + df.format(mx) + " my: " + df.format(my), offsetX, offsetY + 25);
			pg.text("view scale: " + df.format(scl), offsetX, offsetY + 40);
			pg.text("rotation: " + df.format(rotamt), offsetX, offsetY + 55);
			pg.text("isDrag: " + isDrag, offsetX, offsetY + 70);
			pg.text("transX: " + df.format(transX) + " transY: " + df.format(transY), offsetX, offsetY + 85);
			pg.text("crop mode: "+ cropMode, offsetX, offsetY + 220);

			if (image!=null) {
				float correctScale = image.getCorrectScale();
				pg.text("correctScale: "+correctScale, offsetX, offsetY+100);
				pg.text("image index: " + imageIndex, offsetX, offsetY + 115);
				pg.text("referenceMeasureMode: " + referenceMeasureMode, offsetX, offsetY + 130);
				pg.text("isLock: " + isLock, offsetX, offsetY + 145);
				pg.text("isReferenceMeasured: " + image.isMeasured(), offsetX, offsetY + 160);
				pg.text("testMeasureMode: " + testMeasureMode, offsetX, offsetY + 175);
				pg.text("tag: "+image.getTag(), offsetX, offsetY+190);
				pg.text("isPlan: "+image.isPlan(), offsetX, offsetY+205);
				PVector cropCentre = image.getCropCentre();
				pg.text("crop centre: " + cropCentre.x+", "+cropCentre.y+", "+cropCentre.z, offsetX, offsetY+235);

			}
			pg.popStyle();
		}
	}

	// ===================  COORDINATES  ================================

	public void convertMouse() {

		float tempmx = (applet.mouseX - transX - posX) / scl;
		float tempmy = (sizeY - applet.mouseY - (sizeY - transY - posY)) / -scl;

		if (rotamt != 0) {
			float cosTheta = PApplet.cos(PApplet.radians(-rotamt));
			float sinTheta = PApplet.sin(PApplet.radians(-rotamt));
			mx = cosTheta * tempmx - sinTheta * tempmy;
			my = sinTheta * tempmx + cosTheta * tempmy;
		} else {
			mx = tempmx;
			my = tempmy;
		}

	}

	// ===================  VIEW MANIPULATION  ================================

	public void checkActive() {
		isActive = false;
		if (applet.mouseX > posX && applet.mouseX < endX && applet.mouseY > posY && applet.mouseY < endY) {
			isActive = true;
		}
	}

	public void resetView() {
		this.scl = Utils.DEFAULT_SCALE;
		resetRotation();
		resetTranslation();
		setToggle("lock", 0);
		setLockLock(false);
		updateScaleTextField(0);
	}

	public void resetRotation() {
		this.rotamt = 0;
	}

	public void resetCurrentImage() {
		this.image.resetImage();
	}


	public void resetTranslation() {
		transX = sizeX / 2;
		transY = sizeY / 2;
		internalTranslationReset();
	}


	public void resetMeasurePoints() {
		image.resetReferenceMeasurePoints();
		//image.resetTestPoints();
	}

	public void setCorrectScale() {
		float realscl = image.getCorrectScale();
		if (realscl!=0) {
			this.scl = realscl;
			resetTranslation();
		}
	}

	private void internalTranslationReset() {
		transXold = transX;
		transYold = transY;
	}

	public void reloadImageMeasurement() {
		if (image.isMeasured()) {
			scl = image.getCorrectScale();
			//setToggle("lock", 1);
		} 
	}

	public void reloadCorrectScale() {
		float correctScale = image.getCorrectScale();
		if (correctScale != 0) {
			this.scl = correctScale;
			setToggle("lock", 1);
		}
	}

	public void rescaleViews() {
		if (image != null)
			image.rescaleTestViews();
	}



	public void updateScaleTextField(float val) {
		setValueToTextField("scale", val);
	}

	// ===================  MOUSE ACTIONS  ===============================

	private void mouseZoom(MouseEvent event) {

		if (isActive && !isDrag && !isLock && !event.isControlDown() && !cropMode) {
			float e = event.getCount();
			float sclold = scl;
			scl = scl + e * 0.05f;
			scl = PApplet.constrain(scl, 0.1f, 10.0f);
			transX -= mx * scl - mx * sclold;
			transY -= my * scl - my * sclold;
		}
	}

	private void mouseRotate(MouseEvent event) {
		if (isActive && !isDrag && event.isControlDown() && !cropMode) {
			float e = event.getCount();
			if (event.isShiftDown()) {
				rotamt = Utils.roundToNumber(rotamt, 45);
				rotamt += e * 45;
			} else {
				rotamt += e;
			}	
			rotamt = rotamt % 360;
		}
	}

	private void mouseTranslate(MouseEvent event) {

		if (isActive) {
			int action = event.getAction();
			int button = event.getButton();
			if (button==Utils.MOUSEMIDDLE) {

				if (action==Utils.MOUSEPRESSED) {
					applet.cursor(PConstants.HAND);
					mxStart = applet.mouseX;
					myStart = applet.mouseY;
					internalTranslationReset();
					isDrag = true;
				}
				if (action==Utils.MOUSERELEASED) {
					applet.cursor(PConstants.ARROW);
					internalTranslationReset();
					isDrag = false;
				}

				if (action==Utils.MOUSEDRAG && isDrag) {
					transX = applet.mouseX - mxStart + transXold;
					transY = applet.mouseY - myStart + transYold;
				}
			}	
		}
	}

	private void mouseTranslateCropBox(MouseEvent event) {
		if (cropMode) {
			PVector vec = null;
			if (image != null) {
				vec = image.getCropCentre();
			}
			int action = event.getAction();
			int button = event.getButton();

			if (button==Utils.MOUSELEFT) {

				if (action==Utils.MOUSEPRESSED) {
					applet.cursor(PConstants.HAND);
					mxStart = applet.mouseX;
					myStart = applet.mouseY;
					transXold = vec.x;
					transYold = vec.y;
					isDrag = true;
				}

				if (action==Utils.MOUSERELEASED) {
					applet.cursor(PConstants.ARROW);
					transXold = vec.x;
					transYold = vec.y;
					isDrag = false;
				}

				if (action==Utils.MOUSEDRAG && isDrag) {
					float x = applet.mouseX - mxStart + transXold;
					float y = applet.mouseY - myStart + transYold;
					image.setCropCentre(new PVector(x,y, Utils.CROP_BOX_SIZE));
				}
			}
		}
	}

	private void mouseAddReferenceMeasurePoints(MouseEvent event) {
		if (isActive && referenceMeasureMode) {
			int action = event.getAction();
			int button = event.getButton();
			if (button ==Utils.MOUSELEFT && action==Utils.MOUSEPRESSED) {
				if (!image.hasReferenceMeasure()) {
					Measurement refMeasurement = new Measurement(Utils.REFERENCE);
					image.addMeasurement(refMeasurement);
				} 

				if (refMeasureAction.getMeasureClicks() == 0) {
					image.getReferenceMeasure().setP1(new PVector(mx, my));
					notifyListeners(new ViewEvent("POINT1_REF", ID));
					refMeasureAction.setMeasureClicks(refMeasureAction.getMeasureClicks()+1);
				} else if (refMeasureAction.getMeasureClicks() == 1) {
					image.getReferenceMeasure().setP2(new PVector(mx, my));
					float dist = image.getReferenceMeasure().getP1().dist(image.getReferenceMeasure().getP2());
					image.getReferenceMeasure().setPixelDistance(dist);
					notifyListeners(new ViewEvent("POINT2_REF", ID));
					referenceMeasureMode = false;
					setTypeMode(true);
					refMeasureAction.setMeasureClicks(0);
				}
			}
		}
	}

	private void mouseAddTestMeasurePoints(MouseEvent event) {
		if (isActive && testMeasureMode) {
			int action = event.getAction();
			int button = event.getButton();
			if (button ==Utils.MOUSELEFT && action==Utils.MOUSEPRESSED) {
				if (testMeasureAction.getMeasureClicks() == 0) {
					Measurement testMeasurement = new Measurement(Utils.TEST);
					testMeasurement.setP1(new PVector(mx, my));
					image.addMeasurement(testMeasurement);
					notifyListeners(new ViewEvent("POINT1_TEST", ID));
					testMeasureAction.setMeasureClicks(testMeasureAction.getMeasureClicks()+1);
				} else if (testMeasureAction.getMeasureClicks() == 1) {
					Measurement testMeasurement = image.getLastMeasurement();
					testMeasurement.setP2(new PVector(mx, my));
					float dist = testMeasurement.getP1().dist(testMeasurement.getP2());
					testMeasurement.setPixelDistance(dist);
					notifyListeners(new ViewEvent("POINT2_TEST", ID));
					testMeasureMode = false;
					setToggle("test_measure", 0);
					testMeasureAction.setMeasureClicks(0);
				}
			}
		}
	}

	public void mouseLockUnlock(MouseEvent event) {
		if (isActive) {
			int action = event.getAction();
			int button = event.getButton();
			if (button ==Utils.MOUSERIGHT && action==Utils.MOUSEPRESSED) {
				switchToggle("lock");
			}
		}
	}

	// ===================  MOUSE EVENTS  ================================


	public void mouseDragged(MouseEvent event) {
		mouseTranslate(event);
		mouseTranslateCropBox(event);
	}

	void mouseWheel(MouseEvent event) {
		mouseZoom(event);
		mouseRotate(event);
	}

	void mousePressed(MouseEvent event) {
		mouseTranslate(event);
		mouseTranslateCropBox(event);
		mouseAddReferenceMeasurePoints(event);
		mouseAddTestMeasurePoints(event);
		mouseLockUnlock(event);
	}

	void mouseReleased(MouseEvent event) {
		mouseTranslate(event);
		mouseTranslateCropBox(event);
	}

	// ===================  KEY ACTIONS  ================================


	public void setToggle(String name, int value) {
		Button toggle = (Button) cp5.getController(name+ID);
		if (value == 1) {
			toggle.setOn();
		} else if (value == 0) {
			toggle.setOff();
		}
	}

	public void setValueToTextField(String name, float value) {
		Textfield textField = (Textfield) cp5.getController(name+ID);
		textField.setValue((String.valueOf(value)));

	}

	public void switchToggle(String name) {
		Button toggle = (Button) cp5.getController(name+ID);
		boolean value = toggle.getBooleanValue();
		if (value) {
			toggle.setOff();
		} else  {
			toggle.setOn();
		}
	}

	public void activateButton(String name) {
		Button button = (Button) cp5.getController(name+ID);
		button.update();
	}


	public void setButtonLock(String name, int value) {
		Button toggle = (Button) cp5.getController(name+ID);
		if (value == 1) {
			toggle.lock();
		} else if (value == 0) {
			toggle.unlock();
		}
	}

	public void setLockLock(boolean val) {
		Button button = (Button) cp5.getController("lock"+ID);
		if (val) {
			button.lock();
		} else {
			button.unlock();
		}
	}

	public void copyPaste(KeyEvent event)  {
		if (event.isControlDown()) {
			Textfield tf = (Textfield) cp5.getController("scale"+ID);
			// c pressed
			if (event.getKeyCode()==67) {
				copyPasteValue = Float.valueOf(tf.getText());
			}

			// v pressed
			if (event.getKeyCode()==86) {
				tf.setText(String.valueOf(copyPasteValue));
				tf.submit();
			}
		}
	}


	// ===================  KEY EVENTS  ================================

	public void keyPressed(KeyEvent event) {

		copyPaste(event);

		if (isActive) {

			if (event.getKey()=='e') {
				setToggle("ref_measure", 1);
			} 

			if (event.getKey()=='t') {
				setToggle("test_measure", 1);
			} 

			if (event.getKey()=='R') {
				activateButton("reload_scale");
			} 

			if (applet.keyCode==PApplet.RIGHT) {
				activateButton("next");
			}

			if (applet.keyCode==PApplet.LEFT) {
				activateButton("previous");
			}

			if (applet.keyCode==34) {
				activateButton("next_unmeasured");
			}

			if (applet.keyCode==33) {
				activateButton("previous_unmeasured");
			}


			if (event.getKey()=='p') {
				if (image.isPlan()) {
					image.setPlan(false);
				} else {
					image.setPlan(true);
				}
			}

			if (event.getKey()=='r') {
				activateButton("reset");
			}

			// CTRL + S
			if (event.isControlDown() && event.getKeyCode()==83) {
				Button button = (Button) cp5.getController("save_metadata");
				saving = true;
				button.update();
			}

			if (event.getKey()=='q') {
				image.printAllMeasurements();
			}

			if (event.getKey()=='l') {
				switchToggle("lock");
			}

			if (event.getKey()=='+') {
				activateButton("next_unmeasured");
			}

			if (event.getKey()=='-') {
				activateButton("previous_unmeasured");
			}

			if (event.getKey()=='c') {
				if (cropMode) {
					setToggle("crop_mode", 0);
				} else {
					setToggle("crop_mode", 1);
				}
			}
		}
	}


	// ===================  EVENT LISTENERS   ================================


	public void addListerner(ViewListener toAdd) {
		listeners.add(toAdd);
	}

	public void notifyListeners(ViewEvent event) {
		for (ViewListener vl : listeners) {
			vl.viewEvent(event);
		}
	}

	// ===================  GETTERS AND SETTERS  ================================

	public void setImage(Image img) {
		if (img != null) {
			this.image = img;
			PImage pimage = applet.loadImage(Controller.getFullPath(img.getPath()));
			this.pimage = pimage;
			if (img.isMeasured()) {
				scl = image.getCorrectScale();
				updateScaleTextField(scl);
				setToggle("lock", 1);
			} else {
				img.loadProperties(pimage);	
				resetView();
			}
		}
	}

	public Image getImage() {
		return this.image;
	}

	public FrameCoords getFrameCoords() {
		FrameCoords fc = new FrameCoords(posX, posY, sizeX, sizeY);
		return fc;
	}

	public void setBackground(int c) {
		this.backgroundColor = c;
	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		this.ID = iD;
	}

	public boolean isActive() {
		return isActive;
	}

	public int getImageIndex() {
		return imageIndex;
	}

	public void setImageIndex(int imageIndex) {
		this.imageIndex = imageIndex;
	}

	public ControlP5 getcp5() {
		return cp5;
	}

	public boolean isReferenceMeasureMode() {
		return referenceMeasureMode;
	}

	public void setReferenceMeasureMode(boolean measureMode) {
		this.referenceMeasureMode = measureMode;
	}

	public boolean isTestMeasureMode() {
		return testMeasureMode;
	}

	public void setTestMeasureMode(boolean measureMode) {
		this.testMeasureMode = measureMode;
	}

	public boolean isTypeMode() {
		return typeMode;
	}

	public void setTypeMode(boolean typeMode) {
		this.typeMode = typeMode;
	}

	public boolean isLock() {
		return isLock;
	}

	public void setLock(boolean isLock) {
		this.isLock = isLock;
	}

	public float getViewScale() {
		return this.scl;
	}

	public void setNumImages(int numImages) {
		this.numImages = numImages;
	}

	public boolean isCropMode() {
		return cropMode;
	}

	public void setCropMode(boolean cropMode) {
		this.cropMode = cropMode;
	}


}
