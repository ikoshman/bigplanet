package com.nevilon.moow.core;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Bitmap.Config;

public class PhysicMap {

	private TileProvider tileProvider;

	private Bitmap[][] cells = new Bitmap[3][3];

	private RawTile defTile;
	
	private int zoom;
	
	public Point globalOffset = new Point();


	public PhysicMap(RawTile defTile) {
		this.defTile = defTile;
		this.zoom = defTile.z;
		tileProvider = new TileProvider(this);
		loadCells(defTile);
	}

	public RawTile getDefaultTile() {
		return this.defTile;
	}

	/**
	 * Callback method 
	 * @param bitmap
	 * @param tile
	 */
	public synchronized  void update(Bitmap bitmap, RawTile tile) {
		int dx = tile.x - defTile.x;
		int dy = tile.y - defTile.y;
		if (dx <= 2 && dy <= 2 && tile.z==defTile.z) {
			if (dx >= 0 && dy >= 0) {
				try {
					cells[dx][dy] = bitmap;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void move(int dx, int dy){	
		reload(defTile.x -dx,defTile.y - dy, defTile.z);
		
	}
	
	public Bitmap[][] getCells() {
		return cells;
	}

	public void zoom(int x, int y, int z) {
		reload(x-1,y-1,z);
	}

	
	/**
	 * Уменьшение уровня детализации
	 */
	public void zoomOut(){
		if((zoom)<16){
			int currentZoomX = getDefaultTile().x*256-globalOffset.x+160;
			int currentZoomY = getDefaultTile().y*256-globalOffset.y+240;
			int tileX = (currentZoomX/2)/256;
			int tileY = (currentZoomY/2)/256;
			zoom++;
			zoom(tileX, tileY, zoom);

		}
		
	}
	
	/**
	 * Увеличение центра
	 */
	public void zoomInCenter(){
		zoomIn(160, 240);
	}
	
	/**
	 * Увеличение уровня детализации
	 * @param offsetX
	 * @param offsetY
	 */
   public void zoomIn(int offsetX, int offsetY){
		if(zoom>0){
			//получение отступа он начала координат
			int currentZoomX = getDefaultTile().x*256-globalOffset.x+offsetX;
			int currentZoomY = getDefaultTile().y*256-globalOffset.y+offsetY;
			// получение координат углового тайла
			int tileX = (currentZoomX*2)/256;
			int tileY = (currentZoomY*2)/256;
			zoom--;
			zoom(tileX, tileY, zoom);
		}
	}

	
	private void reload(int x, int y, int z){
		System.out.println(y);
		int tileCount = (int) Math.pow(2, 17-defTile.z);
		/*if(y<0){return;}
		if(x<0){
			x = tileCount+x;
			System.out.println("offsetX " +globalOffset.x);
			globalOffset.x = 320+globalOffset.x-256;
		} else if(x>tileCount-1){
			x = Math.abs(x-tileCount);
			globalOffset.x = globalOffset.x -256;
		}
		*/
		defTile.x = x;
		defTile.y = y;
		defTile.z = z;
		loadCells(defTile);
	}
	
	
	/**
	 * Запрос на загрузку тайлов для данной группы ячеек (определяется по
	 * крайней левой верхней)
	 * 
	 * @param tile
	 */
	private synchronized void loadCells(RawTile tile) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				int x, y;
				x = (tile.x + i);
				y = (tile.y + j);

				/*int tileCount = (int) Math.pow(2, 17-defTile.z);
				
				if(x<0){
					x = tileCount+x;
				} else if(x>tileCount-1){
					x = Math.abs(tileCount-x);
				}
				*/
				Bitmap tmpBitmap = tileProvider.inMemoryCache.get(new RawTile(x,y,tile.z));
				if(tmpBitmap!=null){
					cells[i][j] = tmpBitmap;
				} else {
					cells[i][j] = null;
					tileProvider.getTile(new RawTile(x, y, tile.z));
				}
			}
		}
	}
	

}
