package com.nevilon.moow.core;

import java.util.Stack;

import android.graphics.Bitmap;


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
				Bitmap bmp = LocalStorageProvider.get(tile);
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
			//bitmap = localProvider.get(tile);
			localProvider.get(tile, new Handler() {

				@Override
				public void handle(Object object) {
					if (object!=null){
						physicMap.update((Bitmap) object, tile);
					} else {
						addToQueue(tile);
						new Thread(new TileScaler(tile, new Handler(){
							
							@Override
							public void handle(RawTile tile, Bitmap bitmap){
								returnTile(bitmap, tile, true);
							};
							
						})).start();
					}
					
				}

			});
		}
		
		return bitmap;
	}

	public void run() {
		while (true) {
				if (queue.size() > 0) {
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

	

}
