package com.nevilon.moow.core;

import java.io.BufferedInputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;

/**
 * Обертка над LocalStorage Производит декодирование тайла в синхронном и в
 * асинхронном режиме
 * 
 * @author hudvin
 * 
 */
public class LocalStorageProvider {

	private static LocalStorage localStorage = LocalStorage.getInstance();

	/**
	 * Декодирует тайл
	 * 
	 * @param tile
	 * @return
	 */
	public static Bitmap get(final RawTile tile) {
		long start = System.currentTimeMillis();
		BufferedInputStream outStream = localStorage.get(tile);
		Bitmap bmp = null;
		if (outStream != null) {
			bmp = BitmapFactory.decodeStream(outStream);
		}
		
		//System.out.println("tile loaded in "+  (System.currentTimeMillis() - start));
		return bmp;
	}

	public void put(RawTile tile, byte[] data) {
		localStorage.put(tile, data);
	}

	public void get(final RawTile tile, final Handler handler) {
		new Thread() {

			public void run() {
				handler.handle(get(tile));
			}

		}.start();
	}

}
