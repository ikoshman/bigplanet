package com.nevilon.bigplanet.core;

import java.util.ArrayList;
import java.util.List;

import com.nevilon.bigplanet.core.geoutils.GeoUtils;
import com.nevilon.bigplanet.core.geoutils.Point;

public class MarkerManager {

	private List<Marker> markers = new ArrayList<Marker>();

	
	// вызывается при зуммировании, пересчет отступа и координат тайла всех маркеров
	public void updateCoordinates(int z){
		for(Marker marker: markers){
			Point tileXY = GeoUtils.toTileXY(marker.place.getLat(), marker.place.getLon(), z);
			Point offsetXY = GeoUtils.getPixelOffsetInTile(marker.place.getLat(), marker.place.getLon(), z);
			marker.offset = offsetXY;
			marker.tile.x = (int) tileXY.x;
			marker.tile.y = (int) tileXY.y;
			marker.tile.z = z;
			// пересчет координат тайла и отступа
		}
	}
	
	public void addMarker(Place place, int zoom){
		Marker marker = new Marker(place);
		updateParams(marker, zoom);
		markers.add(marker);	
	}
	
	public void updateParams(Marker marker, int zoom){
		Point tileXY =  GeoUtils.toTileXY(marker.place.getLat(), marker.place.getLon(), zoom);
		RawTile mTile = new RawTile((int)tileXY.x, (int)tileXY.y, zoom,-1);
		marker.tile = mTile;
		Point offset = GeoUtils.getPixelOffsetInTile(marker.place.getLat(), marker.place.getLon(), zoom);
		marker.offset = offset;
	}
	
	public void updateAll(int zoom){
		for(Marker marker : markers){
			updateParams(marker, zoom);
		}
	}
	
	public List<Marker> getMarkers(int x, int y, int z){
		List<Marker> result = new ArrayList<Marker>();
		for(Marker marker:markers){
			if(marker.tile.x ==x && marker.tile.y ==y && marker.tile.z ==z){
				result.add(marker);
			}
		}
		return result;
	}
	
	public  class Marker {
		
		private Place place;
		
		private RawTile tile;
		
		private Point offset;
		
		public Marker(Place place){
			this.place = place;	
		}
		
		public Point getOffset(){
			return this.offset;
		}
		
	}
	
}
