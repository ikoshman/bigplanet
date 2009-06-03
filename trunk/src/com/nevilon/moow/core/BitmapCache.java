package com.nevilon.moow.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;
import android.util.Log;

public class BitmapCache {
	
	private ExpiredHashMap cacheMap = new ExpiredHashMap(50);

	
	public void put(RawTile tile, Bitmap bitmap){
		cacheMap.put(tile, bitmap);
	}
	
	public Bitmap get(RawTile tile){
		return cacheMap.get(tile);
	}
	
	
	private static class ExpiredHashMap{
		
		private int maxSize;
			
		private Map<ExpRawTile, Bitmap> expCacheMap = new HashMap<ExpRawTile, Bitmap>();
		
		public ExpiredHashMap(int maxSize){
			this.maxSize = maxSize;
		}
		
		public synchronized void put(RawTile tile, Bitmap bitmap){
			if (maxSize == expCacheMap.keySet().size()){
				clear();
			}
			expCacheMap.put(new ExpRawTile(tile,System.currentTimeMillis()), bitmap);
		}
		
		public Bitmap get(RawTile tile){
			return expCacheMap.get(tile);
		}
		
		/**
		 * Удаляет определенную часть самых старых элементов в кеше
		 */
		private void clear(){
			Iterator<ExpRawTile> it = expCacheMap.keySet().iterator();
			List<ExpRawTile> listToSort = new ArrayList<ExpRawTile>();
			while(it.hasNext()){
				listToSort.add(it.next());
			}
			Collections.sort(listToSort);
			for(int i=0;i<20;i++){
				expCacheMap.remove(listToSort.get(i));
			}
			Log.i("CACHE", "clean");
		}
		
		private class ExpRawTile extends RawTile implements Comparable<ExpRawTile>{

			private long addedOn =-1;
			
			public ExpRawTile(RawTile tile,long addedOn) {
				super(tile.getX(), tile.getY(), tile.getZ());
				this.addedOn = addedOn;
			}

			public int compareTo(ExpRawTile another) {
				return (int) (addedOn-another.addedOn);
			}
			
		}
		
	}
	
}
