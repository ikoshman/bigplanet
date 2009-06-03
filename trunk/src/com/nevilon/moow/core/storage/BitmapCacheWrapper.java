package com.nevilon.moow.core.storage;

import com.nevilon.moow.core.RawTile;

import android.graphics.Bitmap;

/**
 * Кеш скалированнных и обычных тайлов
 * 
 * @author hudvin
 * 
 */
public class BitmapCacheWrapper {

	/*
	 * размер кеша (для каждого обоих типов)
	 */
	public final static int CACHE_SIZE = 30;
	
	private BitmapCache cache = new BitmapCache(BitmapCacheWrapper.CACHE_SIZE);

	private BitmapCache scaledCache = new BitmapCache(BitmapCacheWrapper.CACHE_SIZE);

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