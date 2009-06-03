package com.nevilon.moow.core.utils;

import junit.framework.Assert;

import org.junit.Test;

public class TileUtils {

	public static final double TILE_SIZE = 256.0D;

	public static class GeoPoint {

		public double x;
		public double y;

		public GeoPoint(double x, double y) {
			this.x = x;
			this.y = y;
		}

		@Override
		
		public String toString() {
			return x + " : " + y;
		}

	}
	
	
	public static class GeoLocation{

		public double lat;
		public double lon;

		public GeoLocation(double lat, double lon) {
			this.lat = lat;
			this.lon = lon;
		}

		@Override
		public String toString() {
			return lat + " : " + lon;
		}
		
	}
	

	@Test
	public void getScaled(){
		/**
		 *  входные данные - координаты тайла
		 *  1. из координат тайла получить географические координаты
		 *  2. уменьшить зум, получить координаты тайла и отступ
		 *  3. по отсутупу получить квадрант в тайле
		 */
		// получение географических координат
		GeoLocation location = getBoundingBox(9, 7, 0, 0, 13);
		// получение координат тайла
		GeoPoint tile = getTileXY(location.lat, location.lon, 14);
		System.out.println(tile);
		
		
		// получение отступа в тайле
	 	GeoPoint offset =  getPixelXY(location.lon, location.lat, 14);
	 	
	 	System.out.println(offset);
	 	int xc = 0;
	 	int yc = 0;
	 	if (offset.x<=128){
	 		xc =0;
	 	} else{
	 		xc = 1;
	 	}
	 	if(offset.y<=128){
	 		yc = 0;
	 	} else {
	 		yc = 1;
	 	}
	 	
	 	System.out.println(xc + " % " + yc);
	 	
	}
	
	//@Test
	public void convertToGeoTest() {
		GeoPoint p;		
		GeoLocation location;

		location = getBoundingBox(0, 0,150 , 50, 16);
		
		// get tile number in different ways
		 p = getTileNumber(location.lat, location.lon, 15);
		System.out.println(p);
		System.out.println(getTileXY(location.lat, location.lon, 15));
	}

	// @Test
	public void testCoordinates() {
		for (int i = 0; i < 2000; i++) {
			// получение долготы и широты по номеру тайла и величене зума
			GeoLocation p = getBoundingBox(10, i, 157, 91, 7);
			// получение сдвига для конечного тайла для заданной широты и
			// долготы
			// System.out.println(getPixelXY(p.y, p.x, Math.abs(7-17)));
			GeoPoint xy = getTileNumber(p.lat, p.lon, 7);
			if (xy.y != i) {
				Assert.fail();
			}
		}
	}

	public static GeoPoint getTileXY(double lat, double lon, int zoom) {
		double nbTiles = Math.pow(2, 17 - (zoom) );
		double correctedLong = 180 + lon;
		double longTileSize = 360 / nbTiles;
		double tilex =  (correctedLong / longTileSize);

		double correctedLat = 0;
		if ((lat < -90) || (lat > 90)) {
			correctedLat = 90 - lat;
		} else {
			correctedLat = lat;
		}
		double phi = Math.PI * correctedLat / 180;
		double mercator = 0.5 * Math.log((1 + Math.sin(phi))
				/ (1 - Math.sin(phi)));
		double tiley =  (((1 - (mercator / Math.PI)) / 2) * nbTiles);
		return new GeoPoint(tilex, tiley-1);
	}


	public static GeoLocation getBoundingBox(double x, double y, double offsetx,
			double offsety, int zoom) {
		double lon = -180; // x
		double lonWidth = 360; // width 360

		// double lat = -90; // y
		// double latHeight = 180; // height 180
		double lat = -1;
		double latHeight = 2;

		int tilesAtThisZoom = 1 << (17 - zoom);
		lonWidth = 360.0 / tilesAtThisZoom;
		lon = -180 + ((x + offsetx / 256) * lonWidth);
		latHeight = -2.0 / tilesAtThisZoom;
		lat = 1 + ((y + offsety / 256) * latHeight);

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
		System.out.println(lat + " " + lon);
		return new GeoLocation(lat, lon);
		// return new Rectangle2D.Double(lon, lat, lonWidth, latHeight);
	}

	public static GeoPoint getTileNumber(final double lat, final double lon,
			int zoom) {
		zoom = Math.abs(zoom - 17);
		double xtile = ((lon + 180) / 360 * (1 << zoom));
		double ytile = ((1 - Math.log(Math.tan(lat * Math.PI / 180) + 1
				/ Math.cos(lat * Math.PI / 180))
				/ Math.PI) / 2 * (1 << zoom));
		return new GeoPoint(Math.rint(xtile), Math.rint(ytile) - 1);
	}

	/**
	 * Returns yoffset for lat in tile.
	 * <p/>
	 * public static int getY(double lat, Rectangle2D.Double llBox) { return 256
	 * - (int) ((Math.abs(llBox.y - lat)) * (256.0D / llBox.height)); }
	 */
	public static int getPixelX(double lon, int zoom) {
		double tiles = Math.pow(2, zoom);
		double circumference = TILE_SIZE * tiles;
		double falseEasting = circumference / 2.0D;
		double radius = circumference / (2 * Math.PI);
		double longitude = Math.toRadians(lon);
		int x = (int) (radius * longitude);
		x = (int) falseEasting + x;
		System.out.println("x2=" + x);

		int tilesOffset = x / (int) TILE_SIZE;
		x = x - (int) (tilesOffset * TILE_SIZE);
		return x;
	}

	// http://cfis.savagexi.com/articles/2006/05/03/google-maps-deconstructed
	public static int getPixelY(double lat, int zoom) {
		double tiles = Math.pow(2, zoom);
		double circumference = TILE_SIZE * tiles;
		double falseNorthing = circumference / 2.0D;
		double radius = circumference / (2 * Math.PI);
		double latitude = Math.toRadians(lat);
		int y = (int) (radius / 2.0 * Math.log((1.0 + Math.sin(latitude))
				/ (1.0 - Math.sin(latitude))));
		y = (y - (int) falseNorthing) * -1;
		// Number of pixels to subtract for tiles skipped (offset)
		int tilesOffset = y / (int) TILE_SIZE;
		y = y - (int) (tilesOffset * TILE_SIZE);
		return y;
	}

	/**
	 * Returns pixel offset in tile for given lon,lat,zoom. see
	 * http://cfis.savagexi.com/articles/2006/05/03/google-maps-deconstructed
	 */
	public static GeoPoint getPixelXY(double lon, double lat, int zoom) {
		// Number of tiles in each axis at zoom level
		double tiles = Math.pow(2, zoom);

		// Circumference in pixels
		double circumference = TILE_SIZE * tiles;
		double radius = circumference / (2 * Math.PI);

		// Use radians
		double longitude = Math.toRadians(lon);
		double latitude = Math.toRadians(lat);

		// To correct for origin in top left but calculated values from center
		double falseEasting = circumference / 2.0D;
		double falseNorthing = circumference / 2.0D;

		// Do x
		double x =  (radius * longitude);

		// Correct for false easting
		x =  falseEasting + x;

		double tilesXOffset = x /  TILE_SIZE;
		x = x -  (tilesXOffset * TILE_SIZE);

		// Do y
		double y =  (radius / 2.0 * Math.log((1.0 + Math.sin(latitude))
				/ (1.0 - Math.sin(latitude))));

		// Correct for false northing
		y = (y -  falseNorthing) * -1;

		// Number of pixels to subtract for tiles skipped (offset)
		double tilesYOffset = y /  TILE_SIZE;
		y = y - (tilesYOffset * TILE_SIZE);
		GeoPoint xy = new GeoPoint(x, y);
		return xy;
	}



	/**
	 * returns a Rectangle2D with x = lon, y = lat, width=lonSpan,
	 * height=latSpan for an x,y,zoom as used by google.
	 */
	public static GeoLocation getLatLong(int x, int y,int offsetx, int offsety, int zoom) {
		double lon = -180; // x
		double lonWidth = 360; // width 360

		double lat = -1;
		double latHeight = 2;

		int tilesAtThisZoom = 1 << (17 - zoom);
		lonWidth = 360.0 / tilesAtThisZoom;
		lon = -180 + ((x + offsetx/256) * lonWidth);
		latHeight = -2.0 / tilesAtThisZoom;
		lat = 1 + ((y+offsety/256) * latHeight);

		latHeight += lat;
		latHeight = (2 * Math.atan(Math.exp(Math.PI * latHeight)))
				- (Math.PI / 2);
		latHeight *= (180 / Math.PI);

		lat = (2 * Math.atan(Math.exp(Math.PI * lat))) - (Math.PI / 2);
		lat *= (180 / Math.PI);

		latHeight -= lat;

		if (lonWidth < 0) {
			lon = lon + lonWidth;
		}

		if (latHeight < 0) {
			lat = lat + latHeight;
		}

		return new GeoLocation(lat, lon);
	}

}
