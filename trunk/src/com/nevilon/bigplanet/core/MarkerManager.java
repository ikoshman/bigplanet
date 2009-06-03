package com.nevilon.bigplanet.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.nevilon.bigplanet.R;
import com.nevilon.bigplanet.core.db.GeoBookmark;
import com.nevilon.bigplanet.core.geoutils.GeoUtils;
import com.nevilon.bigplanet.core.geoutils.Point;

public class MarkerManager {

	public static final int MY_LOCATION_MARKER = 0;

	public static final int BOOKMARK_MARKER = 1;

	public static final int SEARCH_MARKER = 2;

	private HashMap<Integer, MarkerImage> images = new HashMap<Integer, MarkerImage>();

	private List<Marker> markers = new ArrayList<Marker>();

	private Resources resources;

	public MarkerManager(Resources resources) {
		this.resources = resources;
		images.put(MY_LOCATION_MARKER, new MarkerImage(
				decodeBitmap(R.drawable.current_postion), 0, 0));
		images.put(BOOKMARK_MARKER, new MarkerImage(
				decodeBitmap(R.drawable.bookmark_marker), 12, 32));
		images.put(SEARCH_MARKER, new MarkerImage(
				decodeBitmap(R.drawable.location_marker), 12, 32));
	}

	public void addMarker(Place place, int zoom, boolean isGPS, int type) {
		Marker marker = new Marker(place, images.get(type), isGPS, type);
		updateParams(marker, zoom);
		markers.add(marker);
	}

	public void addMarker(GeoBookmark bookmark, int type, int zoom) {
		Point offset = new Point();
		offset.set(bookmark.getOffsetX(), bookmark.getOffsetY());
		Marker marker = new Marker(bookmark.getTile(),offset,images.get(type),type);
		marker.tile = bookmark.getTile();
		updateParams(marker, zoom);
		markers.add(marker); 
	}

	public void updateParams(Marker marker, int zoom) {
		if (marker.type == BOOKMARK_MARKER) {
			int ox = (int) (marker.tile.x * 256 + marker.offset.x);
			int oy = (int) (marker.tile.y * 256 + marker.offset.y);
			// новый отступ
			ox = (int) (ox * Math.pow(2, zoom - marker.tile.z));
			oy = (int) (oy * Math.pow(2, zoom - marker.tile.z));
			int tx = ox / 256;
			int ty = oy / 256;
			ox = ox - tx * 256;
			oy = oy - ty * 256;
			marker.offset.x = ox;
			marker.offset.y = oy;
			marker.tile.x = tx;
			marker.tile.y = ty;
			System.out.println(marker.tile);

		} else {
			Point tileXY = GeoUtils.toTileXY(marker.place.getLat(),
					marker.place.getLon(), zoom);
			RawTile mTile = new RawTile((int) tileXY.x, (int) tileXY.y, zoom,
					-1);
			marker.tile = mTile;
			Point offset = GeoUtils.getPixelOffsetInTile(marker.place.getLat(),
					marker.place.getLon(), zoom);
			marker.offset = offset;
		}

	}

	public void updateAll(int zoom) {
		for (Marker marker : markers) {
			updateParams(marker, zoom);
		}
	}

	public List<Marker> getMarkers(int x, int y, int z) {
		List<Marker> result = new ArrayList<Marker>();
		for (Marker marker : markers) {
			if (marker.tile.x == x && marker.tile.y == y && marker.tile.z == z) {
				result.add(marker);
			}
		}
		return result;
	}

	private Bitmap decodeBitmap(int resourceId) {
		return BitmapFactory.decodeResource(resources, resourceId);
	}

	public static class MarkerImage {

		private Bitmap image;

		private int offsetX;

		private int offsetY;

		public MarkerImage(Bitmap image, int offsetX, int offsetY) {
			this.image = image;
			this.offsetX = offsetX;
			this.offsetY = offsetY;
		}

		public Bitmap getImage() {
			return this.image;
		}

		public int getOffsetX() {
			return this.offsetX;
		}

		public int getOffsetY() {
			return this.offsetY;
		}

	}

	public class Marker {

		private Place place;

		private RawTile tile;

		private Point offset;

		private boolean isGPS;

		private int type;

		private MarkerImage markerImage;

		public Marker(Place place, MarkerImage markerImage, boolean isGPS,
				int type) {
			this.type = type;
			this.place = place;
			this.isGPS = isGPS;
			this.markerImage = markerImage;
		}

		public Marker(RawTile tile,Point offset, MarkerImage markerImage, int type) {
			this.type = type;
			this.markerImage = markerImage;
			this.tile = tile;
			this.offset = offset;
		}

		public Point getOffset() {
			return this.offset;
		}

		public MarkerImage getMarkerImage() {
			return this.markerImage;
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
