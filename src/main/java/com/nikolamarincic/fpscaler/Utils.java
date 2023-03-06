package com.nikolamarincic.fpscaler;


import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.common.io.Files;

public class Utils {

	public final static int MOUSEMIDDLE = 3;
	public final static int MOUSELEFT = 37;
	public final static int MOUSERIGHT = 39;
	public final static int MOUSEPRESSED = 1;
	public final static int MOUSERELEASED = 2;
	public final static int KEYPRESSED = 1;
	public final static int KEYRELEASED = 2;
	public final static int MOUSEDRAG = 4;
	public final static int CONTROL = 17;
	public final static int SHIFT = 16;
	public final static int RED = 0xFFfe3c71;
	public final static int GREEN_DARKEST = 0xFF1E5631;
	public final static int GREEN_DARK = 0xFF4C9A2A;
	public final static int GREEN_MEDIUM = 0xFF76BA1B;
	public final static int GREEN_LIGHT = 0xFFA4DE02;
	public final static int BLUE = 0xFF0081fe;
	public final static int PINK = 0xFFefb6bf;
	public final static  int BLUISH = 0xFF2e9598;
	public final static int YELLOW = 0xFFf7db69;
	public final static int WHITE = 0xFFffffff;
	public final static float DEFAULT_SCALE = 1f;
	public final static float PIXELS_PER_METER = 50f;
	public final static int CROP_BOX_SIZE = 750;
	public final static String REFERENCE = "reference";
	public final static String TEST = "test";
	

	public static float roundToNumber(float val, float num) {
		float dist = Float.POSITIVE_INFINITY;
		float closest = 0;
		int fitsNum = (int) Math.ceil(360/num);
		float[] vals = new float[2*(fitsNum+1)];
		for (int i=0; i<=fitsNum; i++) {
			vals[i*2] = i*num;
			vals[(i*2)+1] = -i*num;
		}
		for (int i=0; i<vals.length; i++) {
			float tempDist = Math.abs(val-vals[i]);
			if (tempDist<dist) {
				dist = tempDist; 
				closest = vals[i];
			}
		}
		return closest;
	}

	public static ArrayList<File> listFilesForFolder(File folder) {
		ArrayList<File> images = new ArrayList<File>();
		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry);
			} else {
				images.add(fileEntry);
			}
		}
		return images;
	}

	public static ArrayList<File> filterImages(ArrayList<File> paths) {
		ArrayList<File> images = new ArrayList<File>();

		List<String> ext = Arrays.asList(new String[]{"jpg", "jpeg", "png", "gif", "tif"});
		for (File f: paths) {
			String extension = Files.getFileExtension(f.getName());
			if (ext.contains(extension)) {
				images.add(f);
			}
		}
		return images;
	}

	public static float convertInput(String input) {
		float measure;
		if (input.endsWith("cm")) {
			measure = Float.parseFloat(input.substring(0, input.length()-2));
			return measure*0.01f;
		} else if (input.endsWith("m")) {
			measure = Float.parseFloat(input.substring(0, input.length()-1));
			return measure;
		} else if (input.endsWith("f")) {
			measure = Float.parseFloat(input.substring(0, input.length()-1));
			return measure*0.3048f;
		} else if (isNumeric(input)) {
			return Float.parseFloat(input);
		}
		return 0;
	}

	public static boolean isNumeric(String strNum) {
		if (strNum == null) {
			return false;
		}
		try {
			Float.parseFloat(strNum);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static Path getMetadataPath(Path loadPath) {
		return loadPath.resolve("scale.json");
	}
	
	public static float textFieldToFloat(String strValue) {
		return Float.valueOf(strValue);
	}
	
}
