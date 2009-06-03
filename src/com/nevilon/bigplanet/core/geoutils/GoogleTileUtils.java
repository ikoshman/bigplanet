package com.nevilon.bigplanet.core.geoutils;

/*
 * Originally written by Andrew Rowbottom. 
 * Released freely into the public domain, use it how you want, don't blame me.
 * No warranty for this code is taken in any way. 
 */


/**
 * A utility class to assist in encoding and decoding google tile references
 * 
 * For reasons of my own longitude is treated as being between -180 and +180 and
 * internally latitude is treated as being from -1 to +1 and then converted to a
 * mercator projection before return.
 * 
 * All rectangles are sorted so the width and height are +ve
 */
public class GoogleTileUtils {
	/**
	 * hidden constructor, this is a Utils class
	 */
	private GoogleTileUtils() {
		super();
	}

	

	
	/**
	 * returns a Rectangle2D with x = lon, y = lat, width=lonSpan,
	 * height=latSpan for an x,y,zoom as used by google.
	 */
	/*public static Point getLatLong(int x, int y, int zoom) {
		double lon = -180; // x
		double lonWidth = 360; // width 360

		// double lat = -90; // y
		// double latHeight = 180; // height 180
		double lat = -1;
		double latHeight = 2;

		int tilesAtThisZoom = 1 << (17 - zoom);
		lonWidth = 360.0 / tilesAtThisZoom;
		lon = -180 + (x * lonWidth);
		latHeight = -2.0 / tilesAtThisZoom;
		lat = 1 + (y * latHeight);

		// convert lat and latHeight to degrees in a transverse mercator
		// projection
		// note that in fact the coordinates go from about -85 to +85 not -90 to
		// 90!
		latHeight += lat;
		latHeight = (2 * Math.atan(Math.exp(Math.PI * latHeight)))
				- (Math.PI / 2);
		latHeight *= (180 / Math.PI);

		lat = (2 * Math.atan(Math.exp(Math.PI * lat))) - (Math.PI / 2);
		lat *= (180 / Math.PI);

		latHeight -= lat;

		if (lonWidth < 0) {
			lon = lon + lonWidth;
			lonWidth = -lonWidth;
		}

		if (latHeight < 0) {
			lat = lat + latHeight;
			latHeight = -latHeight;
		}

		Point point = new Point();
		point.x = (int) lat;
		point.y = (int) lon;

		return point;
	}
	public static boolean isValid(RawTile tile){
		int tileCount = (int) Math.pow(2, 17 - tile.z);
		return (tile.x<tileCount && tile.y<tileCount);
	}*/
	
}
