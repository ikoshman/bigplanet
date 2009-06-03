package com.nevilon.moow.core.storage;

import java.io.BufferedInputStream;

import com.nevilon.moow.core.RawTile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
	public static Bitmap get(final RawTile tile) {
		BufferedInputStream outStream = localStorage.get(tile);
		Bitmap bmp = null;
		if (outStream != null) {
			bmp = BitmapFactory.decodeStream(outStream);
		}
		
		return bmp;
	}

	public void put(RawTile tile, byte[] data) {
		localStorage.put(tile, data);
	}

	/*
	public static void get(final RawTile tile, final Handler handler) {
		new Thread() {

			public void run() {
				
				handler.handle(tile,get(tile),false);
			}

		}.start();
	}
	*/

}