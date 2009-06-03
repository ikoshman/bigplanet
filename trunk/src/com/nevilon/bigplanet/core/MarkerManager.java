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
	
	public void addMarker(Place place, int zoom,boolean isGPS){
		Marker marker = new Marker(place,isGPS);
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
		
		private boolean isGPS;
		
		public Marker(Place place, boolean isGPS){
			this.place = place;	
			this.isGPS = isGPS;
		}
		
		public Point getOffset(){
			return this.offset;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + (isGPS ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Marker other = (Marker) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (isGPS != other.isGPS)
				return false;
			return true;
		}

		private MarkerManager getOuterType() {
			return MarkerManager.this;
		}
		
	}
	
}
