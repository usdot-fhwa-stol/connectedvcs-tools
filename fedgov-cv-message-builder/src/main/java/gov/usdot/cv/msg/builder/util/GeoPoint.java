/*
* Copyright (C) 2025 LEIDOS.
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License. You may obtain a copy of
* the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations under
* the License.
*/
package gov.usdot.cv.msg.builder.util;

import gov.usdot.cv.msg.builder.util.Vincenty.DistanceAndBearing;

public class GeoPoint {
	
	private double lat;
	private double lon;
	private double elevation; // New field for elevation in meters
	
	public GeoPoint(double lat, double lon) {
		super();
		this.lat = lat;
		this.lon = lon;
	}

	public GeoPoint(double lat, double lon, double elevation) {
        super();
        this.lat = lat;
        this.lon = lon;
        this.elevation = elevation;
    }

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

	public short getElevationOffsetInCentimeters(GeoPoint fromPoint) {
		// Check if either elevation is invalid
		// Note: from the J2735 standard, 6143.9 is the maximum elevation and -409.5 is the minimum elevation 
		if (this.elevation < -409.5 || this.elevation > 6143.9 || fromPoint.elevation < -409.5
				|| fromPoint.elevation > 6143.9) {
			return 0;
		}

		// Calculate the elevation offset in centimeters
		double elevationOffset = (this.elevation - fromPoint.elevation) * 100;

		return (short) Math.round(elevationOffset);
	}
	
	public short getLatOffsetInCentimeters(GeoPoint fromPoint) {
		// Calculate the distance on the Y-Axis(Latitude) by only changing the X-Axis(Longitude)
		DistanceAndBearing dab = Vincenty.getDistanceAndBearing(fromPoint.lat, fromPoint.lon,
																this.lat, fromPoint.lon);
		
		double latOffset = dab.getDistance();
		
		// Distance calculated by Vincenty provides no insight in directionality.
		// Use bearing to determine correct sign for offset.
		if(dab.getInitialBearing() > 90 && dab.getInitialBearing() < 270) {
			latOffset = -latOffset;
		}
		
		// Convert to CM
		latOffset *= 100;

		validateShortRange(fromPoint, latOffset, "latitude offset", "centimeters");
		
		return (short)Math.round(latOffset);
	}
	
	public short getLonOffsetInCentimeters(GeoPoint fromPoint) {
		// Calculate the distance on the X-Axis(Longitude) by only changing the Y-Axis(Latitude)
		DistanceAndBearing dab = Vincenty.getDistanceAndBearing(fromPoint.lat, fromPoint.lon,
																fromPoint.lat, this.lon);
		
		double lonOffset = dab.getDistance();
		
		// Distance calculated by Vincenty provides no insight in directionality.
		// Use bearing to determine correct sign for offset.
		if(dab.getInitialBearing() > 180 && dab.getInitialBearing() < 360) {
			lonOffset = -lonOffset;
		}
											
		// Convert to CM
		lonOffset *= 100;

		validateShortRange(fromPoint, lonOffset, "longitude offset", "centimeters");
		
		return (short)Math.round(lonOffset);
	}

	public short getDistanceInCentimeters(GeoPoint fromPoint) {
		DistanceAndBearing dab = Vincenty.getDistanceAndBearing(fromPoint.lat, fromPoint.lon,
																this.lat, this.lon);
		
		double dist = dab.getDistance();
		
		// Convert to CM
		dist *= 100;
		
		validateShortRange(fromPoint, dist, "distance", "centimeters");
		
		return (short)Math.round(dist);
	}
	
	public short getLatOffsetInMeters(GeoPoint fromPoint) {
		// Calculate the distance on the Y-Axis(Latitude) by only changing the X-Axis(Longitude)
		DistanceAndBearing dab = Vincenty.getDistanceAndBearing(fromPoint.lat, fromPoint.lon,
																this.lat, fromPoint.lon);
		
		double latOffset = dab.getDistance();
		
		// Distance calculated by Vincenty provides no insight in directionality.
		// Use bearing to determine correct sign for offset.
		if(dab.getInitialBearing() > 90 && dab.getInitialBearing() < 270) {
			latOffset = -latOffset;
		}

		validateShortRange(fromPoint, latOffset, "latitude offset", "meters");
		
		return (short)Math.round(latOffset);
	}
	
	public short getLonOffsetInMeters(GeoPoint fromPoint) {
		// Calculate the distance on the X-Axis(Longitude) by only changing the Y-Axis(Latitude)
		DistanceAndBearing dab = Vincenty.getDistanceAndBearing(fromPoint.lat, fromPoint.lon,
																fromPoint.lat, this.lon);
		
		double lonOffset = dab.getDistance();
		
		// Distance calculated by Vincenty provides no insight in directionality.
		// Use bearing to determine correct sign for offset.
		if(dab.getInitialBearing() > 180 && dab.getInitialBearing() < 360) {
			lonOffset = -lonOffset;
		}

		validateShortRange(fromPoint, lonOffset, "longitude offset", "meters");
		
		return (short)Math.round(lonOffset);
	}
	
	public short getDistanceInMeters(GeoPoint fromPoint) {
		DistanceAndBearing dab = Vincenty.getDistanceAndBearing(fromPoint.lat, fromPoint.lon,
																this.lat, this.lon);
		
		double dist = dab.getDistance();
		
		validateShortRange(fromPoint, dist, "distance", "meters");
		
		return (short)Math.round(dist);
	}
	
	private void validateShortRange(GeoPoint fromPoint, double value, String measurementType, String units) {
		if (value > Short.MAX_VALUE) {
			throw new IllegalArgumentException(
					String.format("The %s of %f %s between %s and %s cannot be greater than %d", 
										measurementType,
										value,
										units,
										fromPoint.toString(),
										this.toString(),
										Short.MAX_VALUE));
		}
		if (value < Short.MIN_VALUE) {
			throw new IllegalArgumentException(
					String.format("The %s of %f %s between %s and %s cannot be less than %d", 
										measurementType,
										value,
										units,
										fromPoint.toString(),
										this.toString(),
										Short.MIN_VALUE));
		}
	}

	@Override
	public String toString() {
		return "GeoPoint [lat=" + lat + ", lon=" + lon + "]";
	}
}
