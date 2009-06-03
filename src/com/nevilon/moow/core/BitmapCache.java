package com.nevilon.moow.core;

import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;

public class BitmapCache {
	
	private Map<RawTile, Bitmap> cacheMap = new HashMap<RawTile, Bitmap>();

	public BitmapCache(){
	}
	
	public void put(RawTile tile, Bitmap bitmap){
		cacheMap.put(tile, bitmap);
	}
	
	public Bitmap get(RawTile tile){
		return cacheMap.get(tile);
	}
	
}
