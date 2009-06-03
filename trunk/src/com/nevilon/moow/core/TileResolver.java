package com.nevilon.moow.core;

import android.graphics.Bitmap;

import com.nevilon.moow.core.loader.TileLoader;
import com.nevilon.moow.core.providers.MapStrategy;
import com.nevilon.moow.core.providers.MapStrategyFactory;
import com.nevilon.moow.core.providers.MapStrategyFactory.MapSource;
import com.nevilon.moow.core.storage.BitmapCacheWrapper;
import com.nevilon.moow.core.storage.LocalStorageWrapper;

public class TileResolver {
	private TileLoader tileLoader;

	private PhysicMap physicMap;

	private LocalStorageWrapper localProvider = new LocalStorageWrapper();

	private BitmapCacheWrapper cacheProvider = new BitmapCacheWrapper();

	private Handler scaledHandler;

	private Handler localLoaderHandler;

	private MapStrategy mapStrategy = MapStrategyFactory.getInstance()
			.getStrategy(MapSource.GOOGLE_VECTOR);

	public int count = 0;

	public TileResolver(final PhysicMap physicMap) {
		this.physicMap = physicMap;
		tileLoader = new TileLoader(
		// обработчик загрузки тайла с сервера
				new Handler() {
					@Override
					public void handle(RawTile tile, byte[] data) {
						localProvider.put(tile, data, mapStrategy.getId());
						Bitmap bmp = LocalStorageWrapper.get(tile, mapStrategy
								.getId());
						cacheProvider.putToCache(tile, bmp);
						updateMap(tile, bmp);
					}

				}, mapStrategy

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
				count--;
				if (bitmap != null && !isScaled) {
					cacheProvider.putToCache(tile, bitmap);
					updateMap(tile, bitmap);
				} else {
					new Thread(new TileScaler(tile, scaledHandler, mapStrategy
							.getId())).start();
					load(tile);
				}
			}

		};

		new Thread(tileLoader).start();
	}

	private void load(RawTile tile) {
		tileLoader.load(tile);

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
			bitmap = cacheProvider.getTile(tile);
		}
		if (bitmap == null) {
			count++;
			// асинхронная загрузка
			LocalStorageWrapper.get(tile, localLoaderHandler, mapStrategy
					.getId());
		} else {
			System.out.println("found in cache");
		}
		return bitmap;
	}

}
