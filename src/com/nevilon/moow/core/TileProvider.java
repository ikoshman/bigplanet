package com.nevilon.moow.core;

import java.util.Stack;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Log;

public class TileProvider implements Runnable {
	private TileLoader tileLoader;

	public BitmapCache scaledCache = new BitmapCache(30);

	private Stack<RawTile> queue = new Stack<RawTile>();

	private PhysicMap physicMap;

	private LocalStorageProvider localProvider = new LocalStorageProvider();

	private BitmapCacheProvider cacheProvider = new BitmapCacheProvider();

	public TileProvider(final PhysicMap physicMap) {
		tileLoader = new TileLoader(
				
				new Handler() {
					@Override
					public void handle(RawTile tile, byte[] data) {
						localProvider.put(tile, data);
						Bitmap bmp = localProvider.get(tile);
						cacheProvider.putToCache(tile, bmp);
						physicMap.update(bmp, tile);
					}

				}
		
		);
		this.physicMap = physicMap;
		new Thread(tileLoader).start();
		new Thread(this).start();
	}

	/**
	 * Загружает заданный тайл
	 * 
	 * @param tile
	 * @return
	 */
	public Bitmap getTile(final RawTile tile, boolean useCache) {
		Bitmap bitmap = null;
		if (useCache) {
			bitmap = cacheProvider.getTile(tile);
		}
		if (bitmap == null) {
			// асинхронная загрузка
			bitmap = localProvider.get(tile);
			localProvider.get(tile, new Handler(){

				@Override
				public void handle(Object object) {
					physicMap.update((Bitmap)object, tile);
					
				}

				
			});
		}
		if (bitmap == null) {
			new Thread(new TileScaler(tile)).start();
			addToQueue(tile);
		}
		return bitmap;
	}

	public void run() {
		while (true) {
			if (queue.size() > 0) {
				Log.i("LOADER", "try to load in any way");
				tileLoader.load(queue.pop());
	
			}
		}
	}

	public void returnTile(Bitmap bitmap, RawTile tile, boolean isScaled) {
		if (isScaled) {
			cacheProvider.putToScaledCache(tile, bitmap);
		} else {
			cacheProvider.putToCache(tile, bitmap);
		}
		physicMap.update(bitmap, tile);
	}

	public void putToStorage(RawTile tile, byte[] data) {
		localProvider.put(tile, data);
	}

	private void addToQueue(RawTile tile) {
		queue.push(tile);
	}

	/**
	 * Предназначен для выполнения скалирования
	 * 
	 * @author hudvin
	 * 
	 */
	private class TileScaler implements Runnable {

		private RawTile tile;

		public TileScaler(RawTile tile) {
			this.tile = tile;
		}

		public void run() {
			Bitmap bmp4scale = findTile(tile.x, tile.y, tile.z);
			if (bmp4scale != null) {
				returnTile(bmp4scale, tile, true);
			}
		}

		/**
		 * Возвращает размеры тайла при зуммировании
		 * 
		 * @param zoom
		 * @return
		 */
		private int getTileSize(int zoom) {
			return (int) (256 / Math.pow(2, zoom));
		}

		private Bitmap findTile(int x, int y, int z) {
			Bitmap bitmap = null;
			int offsetX;
			int offsetY;
			int offsetParentX;
			int offsetParentY;
			int parentTileX;
			int parentTileY;
			// получение отступа от начала координат на текущев уровне
			offsetX = x * 256; // отступ от начала координат по ox
			offsetY = y * 256; // отступ от начала координат по oy
			int tmpZ = z;
			while (bitmap == null && tmpZ <= 17) {
				tmpZ++;

				int scale = tmpZ - z;

				// получение отступа от начала координат на предыдущем уровне
				offsetParentX = (int) (offsetX / Math.pow(2, scale));
				offsetParentY = (int) (offsetY / Math.pow(2, scale));

				// получение координат тайла на предыдущем уровне
				parentTileX = offsetParentX / 256;
				parentTileY = offsetParentY / 256;

				offsetParentX = offsetParentX - parentTileX * 256;
				offsetParentY = offsetParentY - parentTileY * 256;

				// необходимо возвращать, во сколько раз увеличить!!!

				bitmap = localProvider.get(new RawTile(parentTileX,
						parentTileY, tmpZ));
				if (bitmap == null) {
				} else { // родительский тайл найден и загружен
					// получение отступа в родительском тайле
					offsetParentX = offsetParentX - parentTileX * 256;
					offsetParentY = offsetParentY - parentTileY * 256;

					// получение уровня скалирования
					// получение размера тайла в родительском тайле
					int tileSize = getTileSize(scale);

					// копирование области и скалирование
					int[] pixels = new int[tileSize * tileSize];
					if (offsetParentY >= 0 && offsetParentX >= 0) {
						bitmap.getPixels(pixels, 0, tileSize, offsetParentX,
								offsetParentY, tileSize, tileSize);
						bitmap = Bitmap.createBitmap(pixels, tileSize,
								tileSize, Config.RGB_565);
						pixels = null;
						return Bitmap.createScaledBitmap(bitmap, 256, 256,
								false);
					}
				}
			}
			return bitmap;
		}

	}

}
