package com.nevilon.moow.core;


import android.graphics.Bitmap;


public class TileProvider {
	private TileLoader tileLoader;

	public BitmapCache scaledCache = new BitmapCache(30);
	
	private PhysicMap physicMap;

	private LocalStorageProvider localProvider = new LocalStorageProvider();

	private BitmapCacheProvider cacheProvider = new BitmapCacheProvider();

	private Handler scaledHandler;

	private Handler localLoaderHandler;

	public TileProvider(final PhysicMap physicMap) {
		this.physicMap = physicMap;
		tileLoader = new TileLoader(
		// обработчик загрузки тайла с сервера
				new Handler() {
					@Override
					public void handle(RawTile tile, byte[] data) {
						localProvider.put(tile, data);
						Bitmap bmp = LocalStorageProvider.get(tile);
						cacheProvider.putToCache(tile, bmp);
						updateMap(tile, bmp);
					}

				}

		);
		// обработчик загрузки скалированых картинок
		this.scaledHandler = new Handler() {

			@Override
			public void handle(RawTile tile, Bitmap bitmap, boolean isScaled) {
				if (isScaled) {
					cacheProvider.putToScaledCache(tile, bitmap);
				}
				updateMap(tile, bitmap);
			}

		};
		// обработчик загрузки с дискового кеша
		this.localLoaderHandler = new Handler() {

			@Override
			public void handle(RawTile tile, Bitmap bitmap, boolean isScaled) {
				if (bitmap != null) {
					cacheProvider.putToCache(tile, bitmap);
					updateMap(tile, bitmap);
				} else {
					new Thread(new TileScaler(tile, scaledHandler)).start();
					load(tile);
				}
			}

						

		};
		
		new Thread(tileLoader).start();
	}

	private void load(RawTile tile) {
		tileLoader.load(tile);
		
	}
	
	private synchronized void updateMap(RawTile tile, Bitmap bitmap){
		physicMap.update(bitmap, tile);
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
			//LocalStorageProvider.get(tile, localLoaderHandler);
			
			
			bitmap = LocalStorageProvider.get(tile);
			if (bitmap == null) {
				new Thread(new TileScaler(tile, scaledHandler)).start();
				load(tile);
			} else {
				cacheProvider.putToCache(tile, bitmap);
			}
			
		}
		
		return bitmap;
	}


	
}
