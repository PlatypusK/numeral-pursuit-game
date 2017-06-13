package com.games.numeral.pursuit.adfree;

import java.io.IOException;

import com.google.api.client.json.GenericJson;
import com.google.api.client.json.gson.GsonFactory;

public class GsonConverter {
	/**
	 * 
	 * @param json The json string
	 * @param clazz The class type to convert the json string into. Must be a subclass of GenericJson
	 * @return A GenericJson object that contains the data of a json string given as an argument or null if the conversion was unsuccessful.
	 * @throws IllegalArgumentException if the class type in the second argument is not a valid GenericJson class.
	 */
	public static final <T> T jsonStringToGenericJson(String json, Class<T> clazz){
		if(!GenericJson.class.isAssignableFrom(clazz)){
			throw new IllegalArgumentException("clazz must be the class object of a valid subtype of GenericJson");
		}
		GsonFactory factory= new GsonFactory();
		T result=null;
		try {
			result=factory.fromString(json,clazz);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return result;
	}
	
	/**
	 * 
	 * @param gsonObject An instance of a subclass of GenericJson
	 * @return A json String representation of gsonObject
	 */
	public static final String genericJsonToString(GenericJson gsonObject){
		GsonFactory factory= new GsonFactory();
		String gString=null;
		try {
			gString=factory.toString(gsonObject);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return gString;

	}
}
