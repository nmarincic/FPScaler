package com.nikolamarincic.fpscaler;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ObjectSerialization {

	public static ObjectMapper objectMapper = getDefaultObjectMapper();
	
	public static ObjectMapper getDefaultObjectMapper() {
		ObjectMapper defaultObjectMapper = new ObjectMapper();
		defaultObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		defaultObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		return defaultObjectMapper;
	}

	// Give string, get JsonNode 
	public static JsonNode parse(String src) throws IOException {
		return objectMapper.readTree(src);
	}

	// Give node and class type and get an object. Use when you don't know the type of the object
	public static <T> T fromJson(JsonNode node, Class<T> clazz) throws JsonProcessingException {
		return objectMapper.treeToValue(node, clazz);
	}

	public static JsonNode toJson(Object a) {
		return objectMapper.valueToTree(a);
	}

	public static String stringify(JsonNode node) throws JsonProcessingException {
		return generateString(node, false);
	}

	public static String prettyPrint(JsonNode node) throws JsonProcessingException {
		return generateString(node, true);	
	}

	private static String generateString(JsonNode node, boolean pretty) throws JsonProcessingException {
		ObjectWriter objectWriter = objectMapper.writer();
		if ( pretty) 
			objectWriter = objectWriter.with(SerializationFeature.INDENT_OUTPUT);
		return objectWriter.writeValueAsString(node);
	}
	
	// Give string and type reference, get object. Use when you know the type of the object
	public static <T> T fromJsonString(final TypeReference<T> type, final String jsonString) {
		T data = null;
		try {
			data = objectMapper.readValue(jsonString, type);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return data;
	}

	public static ObjectMapper getObjectMapper() {
		return objectMapper;
	}
	
	
}
