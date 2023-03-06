package com.nikolamarincic.fpscaler;

import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import processing.core.PImage;
import processing.core.PVector;

@JsonPropertyOrder({"filename","wid_original", "hei_original", "is_measured", "correct_scale", "wid_transformed", "hei_transformed", "measurements", "tag", "crop_centre"})
public class Image implements Comparable<Image> {

	
	@JsonProperty("wid_original")
	private int originalWidth;
	@JsonProperty("hei_original")
	private int originalHeight;
	@JsonProperty("correct_scale")
	private float correctScale;
	@JsonProperty("wid_transformed")
	private int finalWidth;
	@JsonProperty("hei_transformed")
	private int finalHeight;
	@JsonProperty("filename")
	private String path;
	@JsonProperty("is_measured")
	private boolean isMeasured;
//	@JsonProperty("tag")
	private char tag = ' ';
	private boolean isPlan = true;
	@JsonProperty("crop_centre")
	private PVector cropCentre = new PVector(0,0, Utils.CROP_BOX_SIZE);
	
	@JsonProperty(value = "measurement")
	private ArrayList<Measurement> measurements = new ArrayList<Measurement>();

	@JsonIgnore
	public void addMeasurement(Measurement measurement) {
		this.measurements.add(measurement);
	}
	
	@JsonIgnore
	public Measurement getReferenceMeasure() {
		for (Measurement m: measurements) {
			if (m.getType().contentEquals(Utils.REFERENCE)) {
				return m;
			}
		}
		return null;
	}
	
	@JsonIgnore
	public boolean hasReferenceMeasure() {
		for (Measurement m: measurements) {
			if (m.getType().contentEquals(Utils.REFERENCE)) {
				return true;
			}
		}
		return false;
	}
	
	@JsonIgnore
	public Measurement getLastMeasurement() {
		for (int i=measurements.size(); i>=0; i--) {
			Measurement m = measurements.get(i-1);
			if (m.getType()==Utils.TEST) {
				return m;
			}
		}
		return null;
	}
	
	public void printAllMeasurements() {
		for (Measurement m: measurements) {
			System.out.println("measurement: "+m.getType());
		}
	}
	
	public ArrayList<Measurement> getMeasurements() {
		return this.measurements;
	}
	
	public void setMeasurements(ArrayList<Measurement> measurements) {
		this.measurements = measurements;
	}
	
	@JsonIgnore
	public int numberOfMeasurements() {
		return measurements.size();
	}

	public Image() {

	}

	public Image(String path) {
		this.path = path;
	}

	public int compareTo(Image arg0) {
		return this.getPath().compareTo(arg0.getPath());
	}
	
	
	public void setCorrectScale(float scl) {
		this.correctScale = scl;
	}

	public void resetReferenceMeasurePoints() {
		if (hasReferenceMeasure()) {
		Measurement ref = getReferenceMeasure();
		ref.setP1(null);
		ref.setP2(null);
		}
	}
	

	public void resetTestPoints() {
		measurements.clear();
		
	}


	public int getOriginalWidth() {
		return this.originalWidth;
	}

	public int getOriginalHeight() {
		return this.originalHeight;
	}

	public void setOriginalWidth(int ow) {
		this.originalWidth = ow;
	}

	public void setOriginalHeight(int oh) {
		this.originalHeight = oh;
	}

	public void computeScaling() {
		Measurement refMeasurement = getReferenceMeasure();
		float pixelDistance = refMeasurement.getPixelDistance();
		float meterDistance = refMeasurement.getMeterDistance();
		
		if (pixelDistance!=0 && meterDistance!=0) {
			float targetPixels = meterDistance * Utils.PIXELS_PER_METER;
			this.correctScale = targetPixels / pixelDistance;
			this.finalWidth = Math.round(originalWidth * correctScale);
			this.finalHeight = Math.round(originalHeight * correctScale);
			this.isMeasured = true;
		}
	}
	
	

	public void rescaleTestViews() {
		for (Measurement measurement: measurements) {
				float distPixels = measurement.getPixelDistance();
				float distMeters = (distPixels * correctScale) / Utils.PIXELS_PER_METER;
				measurement.setMeterDistance(distMeters);
		}
	}
	

	public float getCorrectScale() {
		return this.correctScale; 
	}

	public int getFinalWidth() {
		return finalWidth;
	}

	public int getFinalHeight() {
		return finalHeight;
	}


	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void loadProperties(PImage pimg) {
		this.originalWidth = pimg.width;
		this.originalHeight = pimg.height;
	}


	public boolean isMeasured() {
		return isMeasured;
	}


	public void setMeasured(boolean isMeasured) {
		this.isMeasured = isMeasured;
	}
	
	

	
	public void setImage(Image img) {
		this.originalWidth = img.getOriginalWidth();
		this.originalHeight = img.getOriginalHeight();
		this.correctScale = img.getCorrectScale();
		this.finalHeight = img.getFinalHeight();
		this.finalWidth = img.getFinalWidth();
		this.path = img.getPath();
		this.isPlan = img.isPlan();
		this.isMeasured = img.isMeasured();
		this.measurements = img.getMeasurements();
		this.tag = img.getTag();
		this.cropCentre = img.getCropCentre();
	}

	public void resetImage() {
		this.correctScale = 0;
		this.finalHeight = 0;
		this.finalWidth = 0;
		this.isMeasured = false;
		this.measurements.clear();
		this.cropCentre = new PVector(0,0,Utils.CROP_BOX_SIZE);
		
	}

	public void setFinalWidth(int finalWidth) {
		this.finalWidth = finalWidth;
	}

	public void setFinalHeight(int finalHeight) {
		this.finalHeight = finalHeight;
	}

	public char getTag() {
		return tag;
	}

	public void setTag(char tag) {
		this.tag = tag;
	}

	public boolean isPlan() {
		return isPlan;
	}

	public void setPlan(boolean isPlan) {
		this.isPlan = isPlan;
	}

	public PVector getCropCentre() {
		return cropCentre;
	}

	public void setCropCentre(PVector cropCentre) {
		this.cropCentre = cropCentre;
	}





	
}