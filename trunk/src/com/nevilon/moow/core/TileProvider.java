package com.nevilon.moow.core;

import java.io.BufferedInputStream;
import java.util.Stack;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.util.Log;

public class TileProvider implements Runnable {

	private LocalStorage localStorage = new LocalStorage();

	private TileLoader tileLoader = new TileLoader(this);

	BitmapCache inMemoryCache = new BitmapCache();

	private Stack<RawTile> queue = new Stack<RawTile>();

	private PhysicMap physicMap;

	public TileProvider(PhysicMap physicMap) {
		this.physicMap = physicMap;
		new Thread(tileLoader).start();
		Thread th = new Thread(this);
		th.start();
	}

	void getTile(RawTile tile) {
		Bitmap tmpBitmap = inMemoryCache.get(tile);
		if (tmpBitmap != null) {
			returnTile(tmpBitmap, tile);
		} else {
			addToQueue(tile);
		}
	}

	public void run() {
		Bitmap tmpBitmap;
		while (true) {
			if (queue.size() > 0) {
				Log.i("LOADER", "try to load in any way");
				RawTile tile = queue.pop();
				BufferedInputStream outStream = localStorage.get(tile);
				if (outStream != null) {
					tmpBitmap = BitmapFactory.decodeStream(outStream);
					inMemoryCache.put(tile, tmpBitmap);
					returnTile(tmpBitmap, tile);
				} else {
					// скалирование существующего тайла с предыдущего уровня
					new Thread(new TileScaler(tile)).start();
					// запрос на загрузку тайла с сервера
					tileLoader.load(tile);
				}

			}
		}
	}

	public synchronized void returnTile(Bitmap bitmap, RawTile tile) {
		physicMap.update(bitmap, tile);
	}

	public void putToStorage(RawTile tile, byte[] data) {
		localStorage.put(tile, data);
	}

	private void addToQueue(RawTile tile) {
		queue.push(tile);
	}

	private class TileScaler implements Runnable {

		private RawTile tile;

		public TileScaler(RawTile tile) {
			this.tile = tile;
		}

		public void run() {
			Bitmap bmp4scale = findTile(tile.x, tile.y, tile.z);
			if (bmp4scale != null) {
				returnTile(bmp4scale, tile);
			}
		}

		private Bitmap loadTile(RawTile tile) {
			Bitmap bmp4scale = null;
			bmp4scale = TileProvider.this.inMemoryCache.get(tile);
			BufferedInputStream outStream;
			if (bmp4scale == null) {
				outStream = localStorage.get(tile);
				if (outStream != null) {
					bmp4scale = BitmapFactory.decodeStream(outStream);
				}
			}
			return bmp4scale;
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
			// получение отступа от начала координат на начальном уровне
			offsetX = x * 256; // отступ от начала координат по ox
			offsetY = y * 256; // отступ от начала координат по oy
			int tmpZ = z;
			while (bitmap == null && tmpZ <= 17) {
				tmpZ++;

				// получение отступа от начала координат на предыдущем уровне
				offsetParentX = (int) (offsetX / Math.pow(2, tmpZ - z));
				offsetParentY = (int) (offsetY / Math.pow(2, tmpZ - z));

				// получение координат тайла на предыдущем уровне
				parentTileX = offsetParentX / 256;
				parentTileY = offsetParentY / 256;

				// необходимо возвращать, во сколько раз увеличить!!!
				// pow(2,tmp-z)
				bitmap = loadTile(new RawTile(parentTileX, parentTileY, tmpZ));
				if (bitmap == null) {
				} else { // родительский тайл найден и загружен
					// получение отступа в родительском тайле
					offsetParentX = offsetParentX - parentTileX * 256;
					offsetParentY = offsetParentY - parentTileY * 256;

					// получение уровня скалирования
					int scale = tmpZ - z;
					// получение размера тайла в родительском тайле
					int tileSize = getTileSize(scale);

					// копирование области и скалирование
					int[] pixels = new int[tileSize * tileSize];
					if (offsetParentY > 0 && offsetParentX > 0) {
						bitmap.getPixels(pixels, 0, tileSize, offsetParentX,
								offsetParentY, tileSize, tileSize);
						bitmap = Bitmap.createBitmap(pixels, tileSize,
								tileSize, Config.RGB_565);
						return Bitmap.createScaledBitmap(bitmap, 256, 256,
								false);
					}
				}
			}
			return bitmap;
		}

	}

}
