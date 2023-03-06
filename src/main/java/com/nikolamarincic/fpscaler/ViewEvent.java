package com.nikolamarincic.fpscaler;



public class ViewEvent {

	private String message;
	private int viewID;

	
	public ViewEvent(String message, int viewID) {
		this.viewID = viewID;
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public void setViewID(int viewID) {
		this.viewID = viewID;
	}
	
	public int getViewID() {
		return this.viewID;
	}
}
