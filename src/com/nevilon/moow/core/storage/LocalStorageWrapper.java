package com.nevilon.moow.core.storage;

import java.io.BufferedInputStream;

import com.nevilon.moow.core.Handler;
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
	public static Bitmap get(final RawTile tile, final int providerId) {
		BufferedInputStream outStream = localStorage.get(tile, providerId);
		Bitmap bmp = null;
		if (outStream != null) {
			bmp = BitmapFactory.decodeStream(outStream);
		}
		
		return bmp;
	}

	public void put(RawTile tile, byte[] data, int providerId) {
		localStorage.put(tile, data, providerId);
	}

	public static void get(final RawTile tile, final Handler handler, final int providerId) {
		new Thread() {

			public void run() {
				
				handler.handle(tile,get(tile, providerId),false);
			}

		}.start();
	}

}
