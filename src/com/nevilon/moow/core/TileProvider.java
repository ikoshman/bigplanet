package com.nevilon.moow.core;

import java.io.BufferedInputStream;
import java.util.Stack;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;
import android.util.Log;

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
				Log.i("LOADER", "try to load in any way");
				RawTile tile = queue.pop();
				BufferedInputStream outStream = localStorage.get(tile);
				if (outStream != null) {
					tmpBitmap = BitmapFactory.decodeStream(outStream);
					inMemoryCache.put(tile, tmpBitmap);
					returnTile(tmpBitmap, tile);
				} else {
					
					/*
					double scaledX = tile.getX()/2.0;
				 	double scaledY = tile.getY()/2.0;
				 	int tx = (int) Math.floor(scaledX);
				 	int ty = (int) Math.floor(scaledY);
				 	int ox, oy;
				 	if (tx<scaledX){
				 		ox = 1;
				 	} else {
				 		ox = 0;
				 	}
				 	if(ty<scaledY){
				 		oy = 1;
				 	} else {
				 		oy = 0;
				 	}
				 	Bitmap bmp4scale;
				 	RawTile tile4scale = new RawTile(tx, ty,tile.getZ()+1);
				 	bmp4scale = inMemoryCache.get(tile);
				 	if(bmp4scale == null){
				 		outStream = localStorage.get(tile4scale); 
				 		if (outStream!=null){
				 			bmp4scale = BitmapFactory.decodeStream(outStream);
				 		}
				 	}
				 	if (bmp4scale!=null){
				 		Bitmap scaledBitmap = Bitmap.createBitmap(128, 128, Config.ARGB_8888);
				 		for(int i = 0;i<128;i++){
				 			for(int j = 0; j<128; j++){
				 				scaledBitmap.setPixel(i, j, bmp4scale.getPixel(i+ox, j+oy));
				 			}
				 		}
				 		
				 		
				 		scaledBitmap = Bitmap.createScaledBitmap(scaledBitmap, 256, 256, false);
				 		
				 	 	returnTile(scaledBitmap, tile);
				 	
				 	}
				 	*/
					
					// return scaled tile
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
