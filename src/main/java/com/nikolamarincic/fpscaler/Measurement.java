package com.nikolamarincic.fpscaler;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import processing.core.PVector;

@JsonPropertyOrder({"type", "distance_pixels", "distance_meters", "point_1", "point_2"})
public class Measurement {

	@JsonProperty("type")
	private String type;
	@JsonProperty("point_1")
	private PVector p1;
	@JsonProperty("point_2") 
	private PVector p2;
	@JsonProperty("distance_pixels")
	private float pixelDistance;
	@JsonProperty("distance_meters")
	private float meterDistance;

	
	public Measurement(String type) {
		this.type = type;
	}
	
	public Measurement() {
		
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public PVector getP1() {
		return p1;
	}

	public void setP1(PVector p1) {
		this.p1 = p1;
	}

	public PVector getP2() {
		return p2;
	}

	public void setP2(PVector p2) {
		this.p2 = p2;
	}

	public float getPixelDistance() {
		return pixelDistance;
	}

	public void setPixelDistance(float pixelDistance) {
		this.pixelDistance = pixelDistance;
	}

	public float getMeterDistance() {
		return meterDistance;
	}

	public void setMeterDistance(float meterDistance) {
		this.meterDistance = meterDistance;
	}
	
	
}

