package com.nevilon.moow.core;

import android.graphics.Bitmap;

/**
 * Кеш скалированнных и обычных тайлов
 * 
 * @author hudvin
 * 
 */
public class BitmapCacheProvider {

	public final static int CACHE_SIZE = 20;
	
	private BitmapCache cache = new BitmapCache(BitmapCacheProvider.CACHE_SIZE);

	private BitmapCache scaledCache = new BitmapCache(BitmapCacheProvider.CACHE_SIZE);

	/**
	 * Поиск в кеше скалированых тайлов
	 * 
	 * @param tile
	 * @return
	 */
	public Bitmap getScaledTile(RawTile tile) {
		return scaledCache.get(tile);
	}

	/**
	 * Помещает битмап в кеш скалированых тайлов
	 */
	public void putToScaledCache(RawTile tile, Bitmap bitmap) {
		scaledCache.put(tile, bitmap);
	}

	/**
	 * Поиск в кеше тайлов
	 * 
	 * @param tile
	 * @return
	 */
	public Bitmap getTile(RawTile tile) {
		return cache.get(tile);
	}

	/**
	 * Помещает битмап в кеш тайлов
	 */
	public void putToCache(RawTile tile, Bitmap bitmap) {
		cache.put(tile, bitmap);
	}

}
