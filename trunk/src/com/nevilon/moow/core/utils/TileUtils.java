package com.nevilon.moow.core.utils;

import junit.framework.Assert;

import org.junit.Test;

public class TileUtils {

	 public static final double TILE_SIZE = 256.0D;
	
	public static class Point {

		double x;
		double y;

		public Point(double x, double y) {
			this.x = x;
			this.y = y;
		}
		
		@Override
		public String toString(){
			return x+" : " + y;
		}

	}
	
	
	@Test
	public void testCoordinates(){
		for(int i=0;i<2000;i++){
			// получение долготы и широты по номеру тайла и величене зума
			Point p = getBoundingBox(10, i, 157,91,7);
			// получение сдвига для конечного тайла	для заданной широты и долготы
			System.out.println(getPixelXY(p.y, p.x, Math.abs(7-17)));
			
			Point xy = getTileNumber(p.x, p.y, Math.abs(7-17));
			if (xy.y !=  i){
				Assert.fail();
			}
	    }
	}
	
	public static Point getBoundingBox(double x, double y, double offsetx, double offsety, int zoom) {
        double lon = -180; // x
        double lonWidth = 360; // width 360

        //double lat = -90;  // y
        //double latHeight = 180; // height 180
        double lat = -1;
        double latHeight = 2;

        int tilesAtThisZoom = 1 << (17 - zoom);
        lonWidth = 360.0 / tilesAtThisZoom;
        lon = -180 + ((x+offsetx/256) * lonWidth);
        latHeight = -2.0 / tilesAtThisZoom;
        lat = 1 + ((y+offsety/256) * latHeight);

        // convert lat and latHeight to degrees in a transverse mercator projection
        // note that in fact the coordinates go from about -85 to +85 not -90 to 90!
        latHeight += lat;
        latHeight = (2 * Math.atan(Math.exp(Math.PI * latHeight))) - (Math.PI / 2);
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

        return new Point(lat, lon);
        //    return new Rectangle2D.Double(lon, lat, lonWidth, latHeight);
    }
	
	public static Point getTileNumber(final double lat, final double lon,
			final int zoom) {
		double xtile =  ((lon + 180) / 360 * (1 << zoom));
		double ytile =  ((1 - Math.log(Math
				.tan(lat * Math.PI / 180)
				+ 1 / Math.cos(lat * Math.PI / 180))
				/ Math.PI)
				/ 2 * (1 << zoom));
		return new Point(Math.rint(xtile), Math.rint(ytile)-1);
	}
	

	/**
     * Returns yoffset for lat in tile.
     * <p/>
     * public static int getY(double lat, Rectangle2D.Double llBox) {
     * return 256 - (int) ((Math.abs(llBox.y - lat)) * (256.0D / llBox.height));
     * }
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
              int y = (int) (radius / 2.0 * Math.log((1.0 + Math.sin(latitude)) / (1.0 - Math.sin(latitude))));
              y = (y - (int) falseNorthing) * -1;
              // Number of pixels to subtract for tiles skipped (offset)
        int tilesOffset = y / (int) TILE_SIZE;
        y = y - (int) (tilesOffset * TILE_SIZE);
        return y;
    }

    /**
     * Returns pixel offset in tile for given lon,lat,zoom.
     * see http://cfis.savagexi.com/articles/2006/05/03/google-maps-deconstructed
     */
    public static Point getPixelXY(double lon, double lat, int zoom) {
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
        int x = (int) (radius * longitude);
     
        // Correct for false easting
        x = (int) falseEasting + x;
     
        int tilesXOffset = x / (int) TILE_SIZE;
        x = x - (int) (tilesXOffset * TILE_SIZE);
     
        // Do y
        int y = (int) (radius / 2.0 * Math.log((1.0 + Math.sin(latitude)) / (1.0 - Math.sin(latitude))));
     
        // Correct for false northing
        y = (y - (int) falseNorthing) * -1;
     
        // Number of pixels to subtract for tiles skipped (offset)
        int tilesYOffset = y / (int) TILE_SIZE;
        y = y - (int) (tilesYOffset * TILE_SIZE);
     
        Point xy = new Point(x,y);
        return xy;
    }
	



}
