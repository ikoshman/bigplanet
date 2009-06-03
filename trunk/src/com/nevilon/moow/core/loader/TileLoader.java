package com.nevilon.moow.core.loader;

import java.util.LinkedList;

import android.util.Log;

import com.nevilon.moow.core.Handler;
import com.nevilon.moow.core.RawTile;
import com.nevilon.moow.core.providers.MapStrategy;

/**
 * Загрузчик тайлов с сервера
 * 
 * @author hudvin
 * 
 */
public class TileLoader implements Runnable {

	private static final int MAX_THREADS = 5;

	private MapStrategy mapStrategy;

	private Handler handler;

	private int counter = 0;


	private LinkedList<RawTile> loadQueue = new LinkedList<RawTile>();

	/**
	 * Конструктор
	 * 
	 * @param handler
	 *            обработчик результата загрузки
	 */
	public TileLoader(Handler handler) {
		this.handler = handler;
	}
	
	public void setMapStrategy(MapStrategy mapStrategy){
		this.mapStrategy = mapStrategy;
	}
	

	/**
	 * Добавляет в очередь на загрузку
	 * 
	 * @param tile
	 */
	public synchronized void load(RawTile tile) {
		addToQueue(tile);
	}

	public synchronized void addToQueue(RawTile tile) {
		loadQueue.add(tile);
	}

	public synchronized RawTile getFromQueue() {
		return loadQueue.poll();
	}

	public synchronized void tileLoaded(RawTile tile, byte[] data) {
		if (data != null) {
			handler.handle(tile, data);
		}
		counter--;
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(200);
				if (counter < MAX_THREADS && loadQueue.size() > 0) {
					RawTile rt = getFromQueue();
					Log.i("LOADER", "Tile " + rt + " start loading");
					if (null != rt) {
						new ThreadLoader(rt).start();
						counter++;
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private class ThreadLoader extends BaseLoader {

	
		public ThreadLoader(RawTile tile) {
			super(tile);
		}

		
		@Override
		protected MapStrategy getStrategy() {
			return TileLoader.this.mapStrategy;
		}



		@Override
		protected void handle(RawTile tile, byte[] data) {
			TileLoader.this.tileLoaded(tile, data);
		}

	}

}
