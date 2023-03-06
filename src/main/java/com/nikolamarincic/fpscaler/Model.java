package com.nikolamarincic.fpscaler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;


public class Model {

	private ArrayList<File> imagesPaths = new ArrayList<File>();
	@JsonProperty(value = "image_list")
	private ArrayList<Image> images = new ArrayList<Image>();
	private Path metadataPath;

	public Model() {

	}

	public void addImagesPaths(Path path) {
		File loadPath = path.toFile();

		if (loadPath.exists()) {
			ArrayList<File> imagesPaths = Utils.filterImages(Utils.listFilesForFolder(loadPath));
			this.metadataPath = Utils.getMetadataPath(path);
			File metadataFile = metadataPath.toFile();

			if (imagesPaths.size()!=0) {

				this.imagesPaths.clear();
				this.imagesPaths = imagesPaths;
				loadImages();

				if (metadataFile.exists()) {
					applyMeasurementsToImages(metadataFile);
				}
				sortImages();
			}
		}
	}

	private void sortImages() {
		Collections.sort(images);
	}
	
	public void applyMeasurementsToImages(File file) {
		ArrayList<Image> loadedImages = null;
		
		try (InputStream stream = new FileInputStream(file)) {
			TypeReference<ArrayList<Image>> tr = new TypeReference<ArrayList<Image>>() {};
			if (checkCompatibility(file, tr, ObjectSerialization.getObjectMapper())) {
				loadedImages = ObjectSerialization.getObjectMapper().readValue(stream, tr);
			}
		} catch (IOException e) { e.printStackTrace(); }

		
		if (loadedImages!=null) {
			
			for (Image currImage : images) {
				for (Image loadedImage : loadedImages) {
					if (currImage.getPath().contentEquals(loadedImage.getPath())) {
						currImage.setImage(loadedImage);
					}
				}
			}
		}
	}

	public boolean checkCompatibility(File file, TypeReference<ArrayList<Image>> typeRefrence, ObjectMapper mapper) {
		try (InputStream stream = new FileInputStream(file)) {
			mapper.readValue(stream, typeRefrence);
			return true;
		} catch (JsonParseException | MismatchedInputException e) {
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} 
	}

	void printImages() {
		for (Image img: images) {
			System.out.println(img.getPath());
		}
	}

	private void loadImages() {
		this.images.clear();

		for (File path: this.imagesPaths) {
			Image img = new Image(path.getName());
			this.images.add(img);
		}
	}


	public int getImageIndex(Image img) {
		return images.indexOf(img);
	}
	
	public String getImagePath(int index) {
		if (index < imagesPaths.size()) {
			return imagesPaths.get(index).getAbsolutePath();
		}
		return null;
	}

	public int getMaxIndex() {
		return imagesPaths.size();
	}

	public Image getImage(int index) {
		return images.get(index);
	}

	private void createFile(File file) {
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveMetadata() {
		File saveFile = metadataPath.toFile();
		createFile(saveFile);

		try {
			sortImages();
			JsonNode json = ObjectSerialization.toJson(images);
			ObjectSerialization.getObjectMapper().writeValue(saveFile, json);
			System.out.println("Metadata sucessfully saved at: "+saveFile.getPath().toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<Image> getImages() {
		return images;
	}

}
