package com.nevilon.moow.core.tools.savemap;

import java.util.Stack;

import android.os.Handler;

import com.nevilon.moow.core.Preferences;
import com.nevilon.moow.core.RawTile;
import com.nevilon.moow.core.loader.BaseLoader;
import com.nevilon.moow.core.providers.MapStrategy;
import com.nevilon.moow.core.storage.LocalStorageWrapper;

public class MapSaver {
	
	private RawTile[] tiles;
	
	private MapStrategy mapStrategy;
	
	private Handler handler;
	
	private int totalKB = 0;
	
	private int totalSuccessful  = 0;
	
	private int totalUnsuccessful = 0;
	
	private ThreadLoader threadLoader;
	
	public MapSaver(Stack<RawTile> tiles,MapStrategy mapStrategy, Handler handler){
		this.tiles = new RawTile[tiles.size()];
		tiles.toArray(this.tiles);
		this.handler = handler;
		this.mapStrategy = mapStrategy;
	}
	
	public void download(){
		threadLoader = new ThreadLoader(tiles);
		threadLoader.start();
	}
	
	
	public void stopDownload(){
		threadLoader.stopLoader();
	}
	
	public int getTotalKB() {
		return totalKB/(1024);
	}

	public int getTotalSuccessful() {
		return totalSuccessful;
	}

	public int getTotalUnsuccessful() {
		return totalUnsuccessful;
	}

	
	private void handle(RawTile tile, byte[] data, int meta){
		if(data == null && meta==1 ){
			totalSuccessful++;
		} else 
		if(data==null){
			totalUnsuccessful++;
		} else {
			totalSuccessful++;
			totalKB+=data.length;
			LocalStorageWrapper.put(tile, data, Integer.parseInt(Preferences.get(Preferences.MAP_SOURCE)));
		}
		handler.sendEmptyMessage(0);
	}
	
	
	
	 class ThreadLoader extends BaseLoader {

				
		public ThreadLoader(RawTile[] tiles) {
			super(tiles);
		}



		@Override
		protected MapStrategy getStrategy() {
			return MapSaver.this.mapStrategy;
		}



		@Override
		protected void handle(RawTile tile, byte[] data, int meta) {
			MapSaver.this.handle(tile, data, meta
					);
		}
		

	}

}
