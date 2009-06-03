package com.nevilon.moow.core;

import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class TileProvider implements Runnable {

	private LocalStorage localStorage = new LocalStorage();

	private TileLoader tileLoader = new TileLoader(this);

	private BitmapCache inMemoryCache = new BitmapCache();

	private LinkedList<RawTile> queue = new LinkedList<RawTile>();

	private PhysicMap physicMap;

	public TileProvider(PhysicMap physicMap) {
		this.physicMap = physicMap;
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
				RawTile tile = queue.poll();
				InputStream outStream = localStorage.get(tile);
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
		queue.add(tile);
	}

}
