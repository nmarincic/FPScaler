package com.nikolamarincic.fpscaler;


import controlP5.ControlP5;

public class ImageViewUI {
	private ControlP5 cp5;
	private ImageView imageView;
	public final static char[] tagNames = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'};
	public final static int NUM_TAG_BUTTONS = 5;


	public ImageViewUI(ImageView imageView) {
		this.imageView = imageView;
		this.cp5 = imageView.getcp5();
	}

	public void addWindowUI() {

		int topOffset = 5;

		// add buttons for each view;
		int offset = addNavigationButtons(0, topOffset, 50,20);
		offset = addResetButtons(offset, 50, 20, topOffset);
		offset = addLockToggles(offset, 50, 20, topOffset);
		offset = addReloadScaleButtons(offset, 85, 20,topOffset);
		offset = addReferenceMeasureToggles(offset, 85,20,topOffset);
		offset = addTestMeasureToggles(offset, 85,20,topOffset);
		offset = addZoomMeasureButton(offset, 85, 20, topOffset);
		offset = addScaleNudgeButtons(offset, 20, 20, topOffset);
		offset = addScaleTextField(offset+2, 85, 20, topOffset);
		offset = addMeasuredNavigationButtons(offset, topOffset, 100, 20);
		offset = addCropModeToggles(offset, 50, 20, topOffset);
		addTagButtons(ImageViewUI.NUM_TAG_BUTTONS, 20, 20, topOffset);

	}
	
	public int addCropModeToggles(int startX, int buttonWidth, int buttonHeight, int topOffset) {
		int offset = 3;
		FrameCoords fc = imageView.getFrameCoords();
		cp5.addButton("crop_mode" + imageView.getID())
		.setLabel("crop mode").setValue(0)
		.setPosition(startX + fc.posX, fc.posY - buttonHeight - topOffset)
		//.setColorBackground(Utils.GREEN_DARKEST)
		//.setColorActive(Utils.GREEN_MEDIUM)
		.setSwitch(true)
		.setSize(buttonWidth, buttonHeight);
		return startX + buttonWidth + offset;
	}
	
	public int addScaleTextField(int startX, int buttonWidth, int buttonHeight, int topOffset) {
		int offset = 3;
		FrameCoords fc = imageView.getFrameCoords();
		float x = startX + fc.posX;
		float y = fc.posY - buttonHeight - topOffset;
		cp5.addTextfield("scale"+imageView.getID())
		.setPosition(x,y)
		.setSize(buttonWidth, buttonHeight)
		.setFocus(false)
		.setAutoClear(false)
		.setColorBackground(Utils.GREEN_DARKEST)
		.setColorActive(Utils.GREEN_MEDIUM)
		.setLabel("");
		
		return startX + buttonWidth + offset;
	}

	public int addMeasuredNavigationButtons(int startX, int topOffset, int buttonWidth, int buttonHeight) {
		FrameCoords fc = imageView.getFrameCoords();
		int offset = 3;

		cp5.addButton("next_unmeasured" + imageView.getID())
		.setLabel("next unmeasured").setValue(0)
		.setPosition(startX + fc.posX, fc.posY - buttonHeight - topOffset)
		.setSize(buttonWidth, buttonHeight);

		cp5.addButton("previous_unmeasured" + imageView.getID())
		.setLabel("previous unmeasured")
		.setPosition(startX + fc.posX + buttonWidth + offset, fc.posY - buttonHeight - topOffset)
		.setSize(buttonWidth, buttonHeight);
		return startX + 2 * buttonWidth + 2 * offset;
	}
	
	
	public void addTagButtons(int noButtons, int buttonWidth, int buttonHeight, int topOffset) {
		FrameCoords fc = imageView.getFrameCoords();
		int offset = 5;
		int totalLength = fc.sizeX-(noButtons*(buttonWidth+offset)-offset);
		int startX = totalLength;
		for (int i=0; i<noButtons; i++) {
			cp5.addButton("tagLoad"+tagNames[i]+"_"+imageView.getID())
			.setLabel(""+tagNames[i])
			.setPosition(fc.posX + startX + i*(buttonWidth+offset), fc.posY-topOffset - buttonHeight)
			.setSize(buttonWidth, buttonHeight);
		}
		
		for (int i=0; i<noButtons; i++) {
			cp5.addButton("tagSet"+tagNames[i]+"_"+imageView.getID())
			.setLabel(""+tagNames[i])
			.setPosition(fc.posX + startX + i*(buttonWidth+offset), fc.posY-topOffset*2 - buttonHeight * 2)
			.setColorBackground(cp5.papplet.color(200, 0, 0))
			.setColorForeground(cp5.papplet.color(255, 0, 0))
			.setColorActive(cp5.papplet.color(150, 0, 0))
			.setSize(buttonWidth, buttonHeight);
		}
	}


	public int addZoomMeasureButton(int startX, int buttonWidth, int buttonHeight, int topOffset) {
		int offset = 3;
		FrameCoords fc = imageView.getFrameCoords();
		cp5.addButton("zoom_measure" + imageView.getID())
		.setLabel("zoom measure").setValue(0)
		.setPosition(startX + fc.posX, fc.posY - buttonHeight - topOffset)
		.setColorBackground(Utils.GREEN_DARKEST)
		.setColorActive(Utils.GREEN_MEDIUM)
		.setSize(buttonWidth, buttonHeight);
		return startX + buttonWidth + offset;
	}

	public int addScaleNudgeButtons(int startX, int buttonWidth, int buttonHeight, int topOffset) {
		FrameCoords fc = imageView.getFrameCoords();
		int offset = 3;

		cp5.addButton("scale_up" + imageView.getID())
		.setLabel("+")
		.setPosition(startX + fc.posX, fc.posY - buttonHeight - topOffset)
		.setColorBackground(Utils.GREEN_DARKEST)
		.setColorActive(Utils.GREEN_MEDIUM)
		.setSize(buttonWidth, buttonHeight);

		cp5.addButton("scale_down" + imageView.getID())
		.setLabel("-")
		.setPosition(startX + fc.posX + buttonWidth + offset, fc.posY - buttonHeight - topOffset)
		.setColorBackground(Utils.GREEN_DARKEST)
		.setColorActive(Utils.GREEN_MEDIUM)
		.setSize(buttonWidth, buttonHeight);
		return startX + 2 * buttonWidth + 2 * offset;
	}


	public int addNavigationButtons(int startX, int topOffset, int buttonWidth, int buttonHeight) {
		FrameCoords fc = imageView.getFrameCoords();
		int offset = 3;

		cp5.addButton("next" + imageView.getID())
		.setLabel("next").setValue(0)
		.setPosition(startX + fc.posX, fc.posY - buttonHeight - topOffset)
		.setSize(buttonWidth, buttonHeight);

		cp5.addButton("previous" + imageView.getID())
		.setLabel("previous")
		.setPosition(startX + fc.posX + buttonWidth + offset, fc.posY - buttonHeight - topOffset)
		.setSize(buttonWidth, buttonHeight);
		return startX + 2 * buttonWidth + 2 * offset;
	}


	public int addResetButtons(int startX, int buttonWidth, int buttonHeight, int topOffset) {
		int offset = 3;
		FrameCoords fc = imageView.getFrameCoords();
		cp5.addButton("reset" + imageView.getID())
		.setLabel("reset").setValue(0)
		.setPosition(startX + fc.posX, fc.posY - buttonHeight - topOffset)
		.setSize(buttonWidth, buttonHeight);
		return startX + buttonWidth + offset;
	}
	public int addReferenceMeasureToggles(int startX, int buttonWidth, int buttonHeight, int topOffset) {
		int offset = 3;
		FrameCoords fc =imageView.getFrameCoords();
		float x = startX + fc.posX;
		float y = fc.posY - buttonHeight - topOffset;
		cp5.addButton("ref_measure" + imageView.getID())
		.setLabel("ref. measure")
		.setColorBackground(Utils.GREEN_DARKEST)
		.setColorActive(Utils.GREEN_MEDIUM)
		.setPosition(x,y)
		.setSwitch(true)
		.setSize(buttonWidth, buttonHeight);

		return startX + buttonWidth + offset;		
	}

	public int addTestMeasureToggles(int startX, int buttonWidth, int buttonHeight, int topOffset) {
		int offset = 3;
		FrameCoords fc =imageView.getFrameCoords();
		float x = startX + fc.posX;
		float y = fc.posY - buttonHeight - topOffset;
		cp5.addButton("test_measure" + imageView.getID())
		.setLabel("test measure")
		.setColorBackground(Utils.GREEN_DARKEST)
		.setColorActive(Utils.GREEN_MEDIUM)
		.setPosition(x,y)
		.setSwitch(true)
		.setSize(buttonWidth, buttonHeight);

		return startX + buttonWidth + offset;		
	}

	public int addLockToggles(int startX, int buttonWidth, int buttonHeight, int topOffset) {
		int offset = 3;
		FrameCoords fc = imageView.getFrameCoords();
		float x = startX + fc.posX;
		float y = fc.posY - buttonHeight - topOffset;
		cp5.addButton("lock" + imageView.getID())
		.setLabel("lock")
		.setPosition(x, y)
		.setSwitch(true)
		.setSize(buttonWidth, buttonHeight);

		return startX + buttonWidth + offset;
	}



	public int addReloadScaleButtons(int startX, int buttonWidth, int buttonHeight, int topOffset) {
		int offset = 3;
		FrameCoords fc = imageView.getFrameCoords();
		float x = startX + fc.posX;
		float y = fc.posY - buttonHeight - topOffset;
		cp5.addButton("reload_scale" + imageView.getID())
		.setLabel("correct scale")
		.setPosition(x, y)
		.setSize(buttonWidth, buttonHeight);

		return startX + buttonWidth + offset;

	}

}
