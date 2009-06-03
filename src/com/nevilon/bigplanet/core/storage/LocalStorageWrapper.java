package com.nevilon.bigplanet.core.storage;

import java.io.BufferedInputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.nevilon.bigplanet.core.Handler;
import com.nevilon.bigplanet.core.RawTile;

/**
 * Обертка над LocalStorage Производит декодирование тайла в синхронном и в
 * асинхронном режиме
 * 
 * @author hudvin
 * 
 */
public class LocalStorageWrapper {

	private static LocalStorage localStorage = LocalStorage.getInstance();

	/**
	 * Декодирует тайл
	 * 
	 * @param tile
	 * @return
	 */
	public static Bitmap get(final RawTile tile, boolean isTemp) {
		BufferedInputStream outStream = localStorage.get(tile,isTemp);
		Bitmap bmp = null;
		if (outStream != null) {
			bmp = BitmapFactory.decodeStream(outStream);
		}

		return bmp;
	}

	public static boolean isExists(RawTile tile) {
		return localStorage.isExists(tile);
	}

	public static void put(RawTile tile, byte[] data) {
		localStorage.put(tile, data,false);
	}

	public static void get(final RawTile tile,final boolean isTemp, final Handler handler) {
		new Thread() {

			public void run() {

				handler.handle(tile, get(tile, isTemp), false);
			}

		}.start();
	}

}
