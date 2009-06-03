package com.nevilon.moow.core;

import android.graphics.Bitmap;

import com.nevilon.moow.core.loader.TileLoader;
import com.nevilon.moow.core.providers.MapStrategy;
import com.nevilon.moow.core.providers.MapStrategyFactory;
import com.nevilon.moow.core.storage.LocalStorageWrapper;

public class TileResolver {
	private TileLoader tileLoader;

	private PhysicMap physicMap;

	private LocalStorageWrapper localProvider = new LocalStorageWrapper();

	//private BitmapCacheWrapper cacheProvider = new BitmapCacheWrapper();

	private Handler scaledHandler;

	private Handler localLoaderHandler;

	private MapStrategyFactory strategyFactory = MapStrategyFactory.getInstance(); 
	
	private int strategyId = MapStrategyFactory.GOOGLE_VECTOR;
	
	private MapStrategy mapStrategy = strategyFactory.getStrategy(strategyId);
	
	public int count = 0;

	public TileResolver(final PhysicMap physicMap) {
		this.physicMap = physicMap;
		tileLoader = new TileLoader(
		// обработчик загрузки тайла с сервера
				new Handler() {
					@Override
					public void handle(RawTile tile, byte[] data) {
						localProvider.put(tile, data, strategyId);
						Bitmap bmp = LocalStorageWrapper.get(tile, strategyId);
						//cacheProvider.putToCache(tile, bmp);
						updateMap(tile, bmp);
					}

				}, mapStrategy

		);
		// обработчик загрузки скалированых картинок
		this.scaledHandler = new Handler() {

			@Override
			public void handle(RawTile tile, Bitmap bitmap, boolean isScaled) {
				decCounter();
				if (isScaled) {
					//cacheProvider.putToScaledCache(tile, bitmap);
				}
				updateMap(tile, bitmap);
			}

		};
		// обработчик загрузки с дискового кеша
		this.localLoaderHandler = new Handler() {

			@Override
			public void handle(RawTile tile, Bitmap bitmap, boolean isScaled) {
				decCounter();
				if (bitmap != null) {
					//cacheProvider.putToCache(tile, bitmap);
					updateMap(tile, bitmap);
				} else {
					
					//bitmap = cacheProvider.getScaledTile(tile);
					
						incCounter();
						new Thread(new TileScaler(tile, scaledHandler, strategyId)).start();
					
					load(tile);
				}
			}

		};

		
		new Thread(tileLoader).start();
	}

	private void load(RawTile tile) {
		tileLoader.load(tile);
	}
	
	private void incCounter(){
		synchronized (this) {
			count++;
		}
	}
	
	
	private void decCounter(){
		synchronized (this) {
			count--;
		}
	}

	private synchronized void updateMap(RawTile tile, Bitmap bitmap) {
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
			//bitmap = cacheProvider.getTile(tile);
		}
		if (bitmap == null) {
			incCounter();
			// асинхронная загрузка
			LocalStorageWrapper.get(tile, localLoaderHandler, strategyId);
		}
		return bitmap;
	}

	public void changeMapSource(int sourceId) {
		mapStrategy = strategyFactory.getStrategy(sourceId);
		this.strategyId = sourceId;
		tileLoader.setMapStrategy(mapStrategy);
	}

	public int getMapSourceId() {
		return this.strategyId;
	}

}
