package com.nevilon.moow.core;


import java.io.InputStream;
import java.util.HashSet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class TileProvider {

	private LocalStorage localStorage = new LocalStorage();
	
	private TileLoader tileLoader = new TileLoader(this);
	
	private BitmapCache inMemoryCache = new BitmapCache();
	
	// список запросов на загрузку (отправленные, но незагруженные)
	private HashSet<RawTile> requestQueue = new HashSet<RawTile>();
	
	private PhysicMap physicMap;
	
	public TileProvider(PhysicMap physicMap){
		this.physicMap = physicMap;
	}
	
	public void getTile(RawTile tile, boolean useCache){
		// попытка загрузить из кеша
		Bitmap tmpBitmap;
		if(useCache){
			tmpBitmap = inMemoryCache.get(tile);
			if (tmpBitmap!=null){
				returnTile(tmpBitmap, tile);
			}
		}
		
		
		InputStream outStream = localStorage.get(tile);
		if(outStream!=null){
			tmpBitmap = BitmapFactory.decodeStream(outStream);
			inMemoryCache.put(tile, tmpBitmap);
			returnTile(tmpBitmap,tile);
		} else {
			//if(!requestQueue.contains(tile)){
				tileLoader.load(tile);
				requestQueue.add(tile);
			
		//	}
		}
	}
	
	public void returnTile(Bitmap bitmap, RawTile tile){
		requestQueue.remove(tile);
		physicMap.update(bitmap,tile);
	}
	
	public void putToStorage(RawTile tile,byte[]data){
		localStorage.put(tile,data);
	}
	
	
	
}
