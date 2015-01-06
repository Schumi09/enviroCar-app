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

package org.envirocar.app.storage;

import java.text.ParseException;
import java.util.Iterator;

import org.envirocar.app.model.TrackId;
import org.envirocar.app.util.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Measurement class that contains all the measured values
 * 
 * @author jakob
 * 
 */

public class Measurement extends org.envirocar.obdig.storage.Measurement {

	public Measurement(double latitude, double longitude) {
		super(latitude, longitude);
	}

	private TrackId trackId;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Measurement [");
		sb.append("latitude=");
		sb.append(getLatitude());
		sb.append(", longitude=");
		sb.append(getLongitude());
		sb.append(", time=");
		sb.append(getTime());
		sb.append(", ");
		
		if (trackId != null) {
			sb.append(", trackId=");
			sb.append(trackId.getId());
			sb.append(", ");
		}
		
		synchronized (this) {
			for (PropertyKey key : propertyMap.keySet()) {
				sb.append(key.toString());
				sb.append("=");
				sb.append(propertyMap.get(key));
				sb.append(", ");
			}	
		}
		
		return sb.toString();
	}


	/**
	 * @return the track
	 */
	public TrackId getTrackId() {
		return trackId;
	}

	/**
	 * @param track
	 *            the track to set
	 */
	public void setTrackId(TrackId track) {
		this.trackId = track;
	}
	
	public synchronized boolean hasProperty(PropertyKey key) {
		return propertyMap.containsKey(key);
	}

	public synchronized void setProperty(PropertyKey key, Double value) {
		propertyMap.put(key, value);
	}


	public static Measurement fromJson(JSONObject measurementJsonObject) throws JSONException, ParseException {
		JSONArray coords = measurementJsonObject.getJSONObject("geometry").getJSONArray("coordinates");
		Measurement result = new Measurement(
				Float.valueOf(coords.getString(1)),
				Float.valueOf(coords.getString(0)));
		
		JSONObject properties = measurementJsonObject.getJSONObject("properties");
		result.setTime(Util.isoDateToLong((properties.getString("time"))));
		JSONObject phenomenons = properties.getJSONObject("phenomenons");
		Iterator<?> it = phenomenons.keys();
		String key;
		while (it.hasNext()) {
			key = (String) it.next();
			if (PropertyKeyValues.keySet().contains(key)) {
				Double value = ((JSONObject) phenomenons.get(key)).getDouble("value"); 
				result.setProperty(PropertyKeyValues.get(key), value);
			}
		}
		
		return result;
	}

	public Measurement carbonCopy() {
		Measurement result = new Measurement(this.getLatitude(), this.getLongitude());
		
		synchronized (this) {
			for (PropertyKey pk : this.propertyMap.keySet()) {
				result.propertyMap.put(pk, this.propertyMap.get(pk));
			}	
		}
		
		result.setTime(this.getTime());
		result.trackId = this.trackId;
		return result;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Measurement)) {
			return false;
		}
		
		Measurement m = (Measurement) o;
		if (m.getTime() != this.getTime()
				|| m.getLatitude() != this.getLatitude()
				|| m.getLongitude() != this.getLongitude()) {
			return false;
		}
		
		if (m.trackId == null && this.trackId != null
				|| m.trackId != null && this.trackId == null) {
			return false;
		}
		
		if (m.trackId != null && !m.trackId.equals(this.trackId)) {
			return false;
		}
		
		for (PropertyKey pk : this.propertyMap.keySet()) {
			if (m.getProperty(pk) != this.getProperty(pk)) {
				return false;
			}
		}
		
		return true;
	}
}
