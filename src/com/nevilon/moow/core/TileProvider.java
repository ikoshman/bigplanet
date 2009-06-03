package com.nevilon.moow.core;

import java.io.BufferedInputStream;
import java.util.Stack;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class TileProvider implements Runnable {

	private LocalStorage localStorage = new LocalStorage();

	private TileLoader tileLoader = new TileLoader(this);

	private BitmapCache inMemoryCache = new BitmapCache();

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
				RawTile tile = queue.pop();
				BufferedInputStream outStream = localStorage.get(tile);
				if (outStream != null) {
					tmpBitmap = BitmapFactory.decodeStream(outStream);
					inMemoryCache.put(tile, tmpBitmap);
					returnTile(tmpBitmap, tile);
				} else {
					tileLoader.load(tile);
				}

			}
		}
	}

	public void returnTile(Bitmap bitmap, RawTile tile) {
		physicMap.update(bitmap, tile);
	}

	public void putToStorage(RawTile tile, byte[] data) {
		localStorage.put(tile, data);
	}

	private void addToQueue(RawTile tile) {
		queue.push(tile);
	}

}
