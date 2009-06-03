package com.nevilon.moow.core;

import android.graphics.Bitmap;

/**
 * Кеш bitmap
 * 
 * @author hudvin
 * 
 */
public class BitmapCache {

	private ExpiredHashMap cacheMap;

	public BitmapCache(int size) {
		cacheMap = new ExpiredHashMap(size);
	}

	public void put(RawTile tile, Bitmap bitmap) {
		cacheMap.put(tile, bitmap);
	}

	public Bitmap get(RawTile tile) {
		return cacheMap.get(tile);
	}

	public Bitmap get(int x, int y, int z) {
		return cacheMap.get(new RawTile(x, y, z));
	}

}
