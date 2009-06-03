package com.nevilon.moow.core;

import android.graphics.Bitmap;

import com.nevilon.moow.core.loader.TileLoader;
import com.nevilon.moow.core.providers.MapStrategy;
import com.nevilon.moow.core.providers.MapStrategyFactory;
import com.nevilon.moow.core.storage.BitmapCacheWrapper;
import com.nevilon.moow.core.storage.LocalStorageWrapper;

public class TileResolver {
	
	private TileLoader tileLoader;

	private PhysicMap physicMap;
	
	private BitmapCacheWrapper cacheProvider = new BitmapCacheWrapper();

	private Handler scaledHandler;

	private Handler localLoaderHandler;

	private int strategyId = MapStrategyFactory.GOOGLE_VECTOR;
	
	
	public TileResolver(final PhysicMap physicMap) {
		this.physicMap = physicMap;
		tileLoader = new TileLoader(
		// обработчик загрузки тайла с сервера
				new Handler() {
					@Override
					public void handle(RawTile tile, byte[] data) {
						LocalStorageWrapper.put(tile, data);
						if(tile.s == strategyId){
							Bitmap bmp = LocalStorageWrapper.get(tile);		
							cacheProvider.putToCache(tile, bmp);
							updateMap(tile, bmp);
						}
					}
				}
		);
		setMapSource(strategyId);
		new Thread(tileLoader).start();
		
		// обработчик загрузки скалированых картинок
		this.scaledHandler = new Handler() {

			@Override
			public synchronized void  handle(RawTile tile, Bitmap bitmap, boolean isScaled) {
				if(bitmap!=null && tile.s == strategyId){
					updateMap(tile, bitmap);
					if (isScaled) {
						cacheProvider.putToScaledCache(tile, bitmap);
					}	
				}
				
			}
		

		};
		// обработчик загрузки с дискового кеша
		this.localLoaderHandler = new Handler() {

			@Override
			public synchronized void handle(RawTile tile, Bitmap bitmap, boolean isScaled) {
				if(tile.s!=strategyId){return;}
				if (bitmap != null) {
					updateMap(tile, bitmap);
					cacheProvider.putToCache(tile, bitmap);
				} else {
					
					bitmap = cacheProvider.getScaledTile(tile);
					if(bitmap==null){
						new Thread(new TileScaler(tile, scaledHandler)).start();
						
					} else {
						updateMap(tile, bitmap);
						
					}
					load(tile);
				}
			}

		};

	}

	private void load(RawTile tile) {
		tileLoader.load(tile);
	}
	
	

	private void updateMap(RawTile tile, Bitmap bitmap) {
		physicMap.update(bitmap, tile);
	}

	/**
	 * Загружает заданный тайл
	 * 
	 * @param tile
	 * @return
	 */
	public void getTile(final RawTile tile, boolean useCache) {
		Bitmap bitmap = null;
		if (useCache) {
			bitmap = cacheProvider.getTile(tile);
			if(bitmap!=null){
				updateMap(tile, bitmap);
				return;
			}
		}
	//	if (bitmap == null) {
			// асинхронная загрузка
			LocalStorageWrapper.get(tile, localLoaderHandler);
		//} 
		//return bitmap;
	}

	public void setMapSource(int sourceId) {
		cacheProvider.gc();
		MapStrategy mapStrategy = MapStrategyFactory.getStrategy(sourceId);
		this.strategyId = sourceId;
		tileLoader.setMapStrategy(mapStrategy);
	}

	public int getMapSourceId() {
		return this.strategyId;
	}

	public void setUseNet(boolean useNet) {
		tileLoader.setUseNet(useNet);
		if(useNet){
			physicMap.reloadTiles();
		}
	}

	public synchronized void  clear() {
		cacheProvider.gc();
		tileLoader = null;
		cacheProvider = null;
		System.gc();
	
	}

}
