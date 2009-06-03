package com.nevilon.bigplanet.core;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

import com.nevilon.bigplanet.core.storage.LocalStorageWrapper;

/**
 * Предназначен для выполнения скалирования
 * 
 * @author hudvin
 * 
 */
public class TileScaler implements Runnable {

	private RawTile tile;

	private Handler handler;

	public TileScaler(RawTile tile, Handler handler) {
		this.tile = tile;
		this.handler = handler;
	}

	public void run() {
		Bitmap bmp4scale = findTile(tile.x, tile.y, tile.z);
		// if (bmp4scale != null) {
		handler.handle(tile, bmp4scale, true);
		// }
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

			// получение отступа от начала координат на предыдущем уровне
			offsetParentX = (int) (offsetX / Math.pow(2, tmpZ - z));
			offsetParentY = (int) (offsetY / Math.pow(2, tmpZ - z));

			// получение координат тайла на предыдущем уровне
			parentTileX = offsetParentX / 256;
			parentTileY = offsetParentY / 256;

			// необходимо возвращать, во сколько раз увеличить!!!
			bitmap = LocalStorageWrapper.get(new RawTile(parentTileX,
					parentTileY, tmpZ, tile.s));
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
				if (offsetParentY >= 0 && offsetParentX >= 0) {
					bitmap.getPixels(pixels, 0, tileSize, offsetParentX,
							offsetParentY, tileSize, tileSize);
					bitmap = Bitmap.createBitmap(pixels, tileSize, tileSize,
							Config.RGB_565);
					pixels = null;
					return Bitmap.createScaledBitmap(bitmap, 256, 256, false);
				}
			}
		}
		return bitmap;
	}

}
