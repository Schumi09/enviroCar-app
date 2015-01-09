/* 
 * enviroCar 2013
 * Copyright (C) 2013  
 * Martin Dueren, Jakob Moellers, Gerald Pape, Christopher Stephan
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 * 
 */
package org.envirocar.app.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.envirocar.app.activity.preference.CarSelectionPreference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class holding all information for a car instance
 * 
 * @author matthes rieke
 *
 */
public class Car extends org.envirocar.obdig.model.Car implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1158542873383742748L;
	private static final Logger logger = LoggerFactory.getLogger(Car.class);

	public static final String TEMPORARY_SENSOR_ID = "%TMP_ID%";
	
	private Car(String fuelType, String manufacturer, String model, String id, int year, int engineDisplacement) {
		super(resolveFuelType(fuelType), manufacturer, model, id, year, engineDisplacement);
	}

	public Car(FuelType fuelType, String manufacturer, String model,
			String carId, int year, int engineDisplacement) {
		super(fuelType, manufacturer, model, carId, year, engineDisplacement);
	}

	public static Car fromJsonWithStrictEngineDisplacement(JSONObject jsonObject) throws JSONException {
		String manu = jsonObject.getString("manufacturer");
		String modl = jsonObject.getString("model");
		String foolType = jsonObject.getString("fuelType");
		int decon = jsonObject.getInt("constructionYear");
		String eyeD = jsonObject.getString("id");
		
		int engineDiss;
		try {
			engineDiss = jsonObject.getInt("engineDisplacement"); 
		} catch (JSONException e) {
			throw e;
		}
		
		return new Car(foolType, manu, modl, eyeD, decon, engineDiss);
	}
	
	public static Car fromJson(JSONObject jsonObject) throws JSONException {
		String manu = jsonObject.getString("manufacturer");
		String modl = jsonObject.getString("model");
		String foolType = jsonObject.getString("fuelType");
		int decon = jsonObject.getInt("constructionYear");
		String eyeD = jsonObject.getString("id");
		
		int engineDiss = jsonObject.optInt("engineDisplacement", 2000); 
		
		return new Car(foolType, manu, modl, eyeD, decon, engineDiss);
	}
	
	public static List<Car> fromJsonList(JSONObject json) throws JSONException {
		JSONArray cars = json.getJSONArray("sensors");
		
		List<Car> sensors = new ArrayList<Car>(cars.length());
		
		for (int i = 0; i < cars.length(); i++){
			String typeString;
			JSONObject properties;
			String carId;
			try {
				typeString = ((JSONObject) cars.get(i)).optString("type", "none");
				properties = ((JSONObject) cars.get(i)).getJSONObject("properties");
				carId = properties.getString("id");
			} catch (JSONException e) {
				logger.warn(e.getMessage(), e);
				continue;
			}
			if (typeString.equals(CarSelectionPreference.SENSOR_TYPE)) {
				try {
					sensors.add(Car.fromJsonWithStrictEngineDisplacement(properties));
				} catch (JSONException e) {
					logger.trace(String.format("Car '%s' not supported: %s", carId != null ? carId : "null", e.getMessage()));
				}
			}	
		}
		
		return sensors;
	}


	public static double ccmToLiter(int ccm) {
		float result = ccm / 1000.0f;
		return result;
	}
	
}
