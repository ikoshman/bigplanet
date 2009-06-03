package com.nevilon.moow.core.storage;

import android.graphics.Bitmap;

import com.nevilon.moow.core.RawTile;

/**
 * Кеш битмапов
 * 
 * @author hudvin
 * 
 */
public class BitmapCache {

	private ExpiredHashMap cacheMap;

	/**
	 * Конструктор
	 * 
	 * @param size
	 *            размер кеша
	 */
	public BitmapCache(int size) {
		cacheMap = new ExpiredHashMap(size);
	}

	public void gc(){
		cacheMap.clear();
	}
	
	/**
	 * Добавление битмапа в кеш
	 * 
	 * @param tile
	 *            тайл
	 * @param bitmap
	 *            битмап
	 */
	public void put(RawTile tile, Bitmap bitmap) {
		cacheMap.put(tile, bitmap);
	}

	/**
	 * Получение битмапа из кеша
	 * 
	 * @param tile
	 *            тайл
	 * @return битмап (или null если не найден)
	 */
	public Bitmap get(RawTile tile) {
		return cacheMap.get(tile);
	}

	/**
	 * Получение битмапа из кеша
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return битмап (или null если не найден)
	 */
	public Bitmap get(int x, int y, int z) {
		return cacheMap.get(new RawTile(x, y, z));
	}

}
